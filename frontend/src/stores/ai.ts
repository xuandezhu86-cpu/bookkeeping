import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { AiConfig, AiMessage, AiToolCall, ConnectionStatus } from '../types/ai'
import { AI_TOOLS, buildSystemPrompt } from '../utils/ai-prompt'
import { sendChatRequest, resetClient } from '../api/ai-client'
import { statisticsApi } from '../api/statistics'
import { categoryApi } from '../api/categories'
import { budgetApi } from '../api/budgets'
import { recordApi } from '../api/records'
import type { CategoryVo } from '../types'

const CONFIG_KEY = 'ai-config'
const CONVERSATION_KEY = 'ai-conversation'

export const useAiStore = defineStore('ai', () => {
  // ── Config ──────────────────────────────────────────────
  const config = ref<AiConfig>(loadConfig())
  const isConfigured = computed(() => !!config.value.apiKey && !!config.value.endpoint)
  const connectionStatus = ref<ConnectionStatus>('untested')

  // ── Conversation ────────────────────────────────────────
  const messages = ref<AiMessage[]>(loadConversation())
  const isLoading = ref(false)
  const isOpen = ref(false)
  const error = ref<string | null>(null)

  let categoryCache: CategoryVo[] | null = null

  // ── Persistence ─────────────────────────────────────────
  function loadConfig(): AiConfig {
    try {
      const raw = localStorage.getItem(CONFIG_KEY)
      if (raw) return JSON.parse(raw)
    } catch { /* ignore */ }
    return {
      endpoint: 'https://api.openai.com/v1/chat/completions',
      apiKey: '',
      model: 'gpt-4o-mini',
    }
  }

  function saveConfig() {
    localStorage.setItem(CONFIG_KEY, JSON.stringify(config.value))
  }

  function setConfig(patch: Partial<AiConfig>) {
    Object.assign(config.value, patch)
    saveConfig()
    resetClient()
    connectionStatus.value = 'untested'
  }

  function loadConversation(): AiMessage[] {
    try {
      const raw = localStorage.getItem(CONVERSATION_KEY)
      if (raw) return JSON.parse(raw)
    } catch { /* ignore */ }
    return []
  }

  function saveConversation() {
    try {
      localStorage.setItem(CONVERSATION_KEY, JSON.stringify(messages.value))
    } catch { /* ignore */ }
  }

  // ── Tool execution ──────────────────────────────────────
  async function getCategoryTreeCached(): Promise<CategoryVo[]> {
    if (categoryCache) return categoryCache
    const res = await categoryApi.getTree()
    categoryCache = res.data || []
    return categoryCache!
  }

  function findCategoryId(tree: CategoryVo[], name: string): number | null {
    for (const parent of tree) {
      if (!parent.children) continue
      const hit = parent.children.find(
        (c) => c.name === name,
      )
      if (hit) return hit.id
    }
    return null
  }

  async function executeTool(tc: AiToolCall): Promise<string> {
    const args = JSON.parse(tc.function.arguments)

    switch (tc.function.name) {
      case 'getMonthlySpending': {
        const res = await statisticsApi.getMonthlyTotal(args.year, args.month)
        return JSON.stringify(res.data ?? 0)
      }

      case 'getCategoryBreakdown': {
        const start = `${args.year}-${args.month}-01`
        const end = `${args.year}-${args.month}-31`
        const res = await statisticsApi.getCategorySummary(start, end)
        return JSON.stringify(res.data ?? [])
      }

      case 'getCategoryTree': {
        const tree = await getCategoryTreeCached()
        return JSON.stringify(tree)
      }

      case 'createExpense': {
        const tree = await getCategoryTreeCached()
        const catId = findCategoryId(tree, args.categoryName)
        if (!catId) {
          const names = tree.flatMap((p) => p.children?.map((c) => c.name) ?? [])
          return JSON.stringify({
            error: `未找到分类"${args.categoryName}"，可用分类：${names.join('、')}`,
          })
        }
        await recordApi.create({
          categoryId: catId,
          amount: args.amount,
          recordDate: args.date,
          note: args.note ?? '',
        })
        return JSON.stringify({ success: true, message: `已记录：${args.categoryName} ¥${args.amount}` })
      }

      case 'getBudgetComparison': {
        const monthStr = `${args.year}-${args.month}`
        const [budgetRes, monthlyRes] = await Promise.all([
          budgetApi.getList(monthStr),
          statisticsApi.getMonthlyTotal(args.year, args.month),
        ])
        return JSON.stringify({
          budgets: budgetRes.data ?? [],
          totalSpent: monthlyRes.data ?? 0,
        })
      }

      case 'getDailyTrend': {
        const res = await statisticsApi.getDailyTrend(args.year, args.month)
        return JSON.stringify(res.data ?? [])
      }

      default:
        return JSON.stringify({ error: `未知工具：${tc.function.name}` })
    }
  }

  // ── Core: send message ──────────────────────────────────
  async function sendMessage(userContent: string) {
    if (!isConfigured.value) {
      error.value = '请先在设置中配置 AI 助手'
      return
    }

    isLoading.value = true
    error.value = null

    // Add user message
    messages.value.push({ role: 'user', content: userContent })
    saveConversation()

    const now = new Date()
    const dateStr = `${now.getFullYear()}年${now.getMonth() + 1}月${now.getDate()}日`

    const buildApiMessages = () => {
      const msgs: { role: string; content: string; tool_call_id?: string }[] = [
        { role: 'system', content: buildSystemPrompt(dateStr) },
      ]
      for (const m of messages.value) {
        const entry: { role: string; content: string; tool_call_id?: string } = {
          role: m.role,
          content: m.content ?? '',
        }
        if (m.tool_call_id) entry.tool_call_id = m.tool_call_id
        msgs.push(entry)
      }
      return msgs
    }

    try {
      // First request — AI may respond with tool calls
      const res1 = await sendChatRequest(config.value, {
        model: config.value.model,
        messages: buildApiMessages(),
        tools: AI_TOOLS,
        tool_choice: 'auto',
      })

      const msg1 = res1.choices[0].message

      if (msg1.tool_calls && msg1.tool_calls.length > 0) {
        // Store assistant message with tool calls
        messages.value.push({
          role: 'assistant',
          content: msg1.content ?? '',
          tool_calls: msg1.tool_calls,
        })

        // Execute each tool
        for (const tc of msg1.tool_calls) {
          const result = await executeTool(tc)
          messages.value.push({
            role: 'tool',
            tool_call_id: tc.id,
            content: result,
          } as AiMessage)
        }

        // Follow-up request with tool results
        const res2 = await sendChatRequest(config.value, {
          model: config.value.model,
          messages: buildApiMessages(),
        })

        const finalContent = res2.choices[0].message.content ?? ''
        messages.value.push({ role: 'assistant', content: finalContent })
      } else {
        // Direct text response
        messages.value.push({ role: 'assistant', content: msg1.content ?? '' })
      }

      saveConversation()
    } catch (e: any) {
      console.error('AI request failed:', e)
      error.value = e?.response?.data?.error?.message
        || e?.message
        || 'AI 响应失败，请检查网络和配置'

      // Also show as a visible assistant message so user sees it in the chat
      messages.value.push({
        role: 'assistant',
        content: `😅 ${error.value}`,
      })
      saveConversation()
    } finally {
      isLoading.value = false
    }
  }

  // ── UI actions ──────────────────────────────────────────
  function togglePanel() { isOpen.value = !isOpen.value }
  function closePanel() { isOpen.value = false }
  function openPanel() { isOpen.value = true }

  function clearConversation() {
    messages.value = []
    categoryCache = null
    saveConversation()
  }

  function ensureWelcome() {
    if (messages.value.length === 0) {
      messages.value.push({
        role: 'assistant',
        content:
          '你好！我是悠然记账的 AI 助手🤖\n\n'
          + '你可以这样问我：\n'
          + '• "这个月花了多少钱？"\n'
          + '• "哪类支出最多？"\n'
          + '• "帮我记一笔：中午吃饭花了25元"\n'
          + '• "这个月预算还剩多少？"\n\n'
          + '试试看吧！',
      })
      saveConversation()
    }
  }

  return {
    config, isConfigured, connectionStatus,
    messages, isLoading, isOpen, error,
    setConfig, saveConfig, sendMessage,
    togglePanel, closePanel, openPanel,
    clearConversation, ensureWelcome,
  }
})

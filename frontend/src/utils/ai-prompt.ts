import type { AiToolDefinition } from '../types/ai'

// ============================================================
// System Prompt
// ============================================================
export function buildSystemPrompt(currentDate: string): string {
  return `你是一个智能记账助手，集成在"悠然记账"桌面应用中。你的职责是帮助用户管理个人财务。

## 核心能力
1. **查询消费数据**：回答用户关于支出的各种问题
2. **快速记账**：通过自然语言创建消费记录
3. **预算建议**：基于消费数据提供理财建议

## 行为准则
- 始终使用中文（简体）回复
- 保持友好、热情的语气，像朋友一样交流
- 回答简洁明了，避免冗长
- 当无法理解用户意图时，主动询问澄清
- 对于金额相关的问题，始终使用"元"或"¥"作为单位
- 当前日期为 ${currentDate}

## 功能说明
当你需要查询数据或创建记录时，使用对应的工具函数。一次请求中你可以调用多个工具。
- 对于查询类问题，先调用工具获取数据，再根据数据给出回答
- 对于创建记录，先调用 getCategoryTree 获取分类信息（如果尚未获取），然后调用 createExpense
- 如果用户的问题不涉及记账或数据查询，你可以直接回答
- 当你需要更多信息才能执行操作时（如缺少分类信息），主动询问用户`
}

// ============================================================
// Tool Definitions (OpenAI function calling format)
// ============================================================
export const AI_TOOLS: AiToolDefinition[] = [
  {
    type: 'function',
    function: {
      name: 'getMonthlySpending',
      description: '获取指定月份的消费总额',
      parameters: {
        type: 'object',
        properties: {
          year: { type: 'string', description: '年份，如 2026' },
          month: { type: 'string', description: '月份，如 07' },
        },
        required: ['year', 'month'],
      },
    },
  },
  {
    type: 'function',
    function: {
      name: 'getCategoryBreakdown',
      description: '获取指定月份的各类别消费金额及占比',
      parameters: {
        type: 'object',
        properties: {
          year: { type: 'string', description: '年份，如 2026' },
          month: { type: 'string', description: '月份，如 07' },
        },
        required: ['year', 'month'],
      },
    },
  },
  {
    type: 'function',
    function: {
      name: 'getCategoryTree',
      description: '获取完整的分类树（含一级和二级分类），用于将分类名称映射为分类ID',
      parameters: {
        type: 'object',
        properties: {},
      },
    },
  },
  {
    type: 'function',
    function: {
      name: 'createExpense',
      description: '创建一笔新的消费记录。注意：二级分类名称必须从 getCategoryTree 返回的分类树中获取',
      parameters: {
        type: 'object',
        properties: {
          amount: { type: 'number', description: '消费金额（元）' },
          categoryName: { type: 'string', description: '二级分类名称，如 午餐、打车、电影演出' },
          date: { type: 'string', description: '消费日期，格式 YYYY-MM-DD' },
          note: { type: 'string', description: '备注说明（可选）' },
        },
        required: ['amount', 'categoryName', 'date'],
      },
    },
  },
  {
    type: 'function',
    function: {
      name: 'getBudgetComparison',
      description: '获取指定月份的预算与实际支出对比',
      parameters: {
        type: 'object',
        properties: {
          year: { type: 'string', description: '年份，如 2026' },
          month: { type: 'string', description: '月份，如 07' },
        },
        required: ['year', 'month'],
      },
    },
  },
  {
    type: 'function',
    function: {
      name: 'getDailyTrend',
      description: '获取指定月份的每日支出趋势',
      parameters: {
        type: 'object',
        properties: {
          year: { type: 'string', description: '年份，如 2026' },
          month: { type: 'string', description: '月份，如 07' },
        },
        required: ['year', 'month'],
      },
    },
  },
]

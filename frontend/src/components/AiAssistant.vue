<template>
  <!-- Floating trigger button -->
  <transition name="ai-scale">
    <div v-if="!aiStore.isOpen" class="ai-fab" @click="openPanel">
      <el-icon :size="24" color="#fff"><ChatDotRound /></el-icon>
    </div>
  </transition>

  <!-- Chat panel -->
  <transition name="ai-slide-up">
    <div v-if="aiStore.isOpen" class="ai-panel">
      <!-- Header -->
      <div class="ai-header">
        <div class="ai-header-left">
          <el-avatar :size="32" class="ai-header-avatar">
            <el-icon :size="18"><ChatDotRound /></el-icon>
          </el-avatar>
          <span class="ai-header-title">AI 记账助手</span>
        </div>
        <div class="ai-header-actions">
          <el-tooltip content="清空对话" placement="bottom">
            <el-icon :size="18" class="ai-header-btn" @click="aiStore.clearConversation()">
              <Delete />
            </el-icon>
          </el-tooltip>
          <el-tooltip content="关闭" placement="bottom">
            <el-icon :size="18" class="ai-header-btn" @click="aiStore.closePanel()">
              <Close />
            </el-icon>
          </el-tooltip>
        </div>
      </div>

      <!-- Messages -->
      <div class="ai-messages" ref="messagesRef">
        <div
          v-for="(msg, i) in aiStore.messages"
          :key="i"
          class="ai-msg"
          :class="{
            'ai-msg-user': msg.role === 'user',
            'ai-msg-assistant': msg.role === 'assistant',
          }"
        >
          <!-- AI avatar -->
          <div v-if="msg.role === 'assistant'" class="ai-msg-avatar">
            <el-avatar :size="32" class="ai-header-avatar">
              <el-icon :size="18"><ChatDotRound /></el-icon>
            </el-avatar>
          </div>

          <!-- Bubble -->
          <div v-if="msg.role !== 'tool'" class="ai-msg-bubble" v-html="renderContent(msg.content)" />

          <!-- User avatar -->
          <div v-if="msg.role === 'user'" class="ai-msg-avatar ai-msg-user-avatar">
            <el-avatar :size="32" icon="UserFilled" />
          </div>
        </div>

        <!-- Typing indicator -->
        <div v-if="aiStore.isLoading" class="ai-msg ai-msg-assistant">
          <div class="ai-msg-avatar">
            <el-avatar :size="32" class="ai-header-avatar">
              <el-icon :size="18"><ChatDotRound /></el-icon>
            </el-avatar>
          </div>
          <div class="ai-msg-bubble ai-typing">
            <span class="ai-dot">.</span>
            <span class="ai-dot">.</span>
            <span class="ai-dot">.</span>
          </div>
        </div>
      </div>

      <!-- Input area -->
      <div class="ai-input-area">
        <el-input
          v-model="inputText"
          type="textarea"
          :rows="2"
          placeholder="输入你的问题..."
          :disabled="aiStore.isLoading"
          resize="none"
          @keydown.enter.prevent="handleSend"
        />
        <el-button
          type="primary"
          :icon="Promotion"
          :loading="aiStore.isLoading"
          circle
          @click="handleSend"
          class="ai-send-btn"
        />
      </div>

      <!-- Unconfigured hint -->
      <div v-if="!aiStore.isConfigured && !aiStore.isLoading" class="ai-config-hint">
        <el-button text size="small" @click="goToSettings">
          <el-icon><Setting /></el-icon>
          点击配置 API Key
        </el-button>
      </div>
    </div>
  </transition>
</template>

<script setup lang="ts">
import { ref, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAiStore } from '../stores/ai'
import { ChatDotRound, Close, Delete, Promotion, Setting } from '@element-plus/icons-vue'

const aiStore = useAiStore()
const router = useRouter()
const inputText = ref('')
const messagesRef = ref<HTMLElement | null>(null)

watch(
  () => aiStore.messages.length,
  async () => {
    await nextTick()
    scrollToBottom()
  },
)

watch(
  () => aiStore.isOpen,
  (open) => {
    if (open) {
      aiStore.ensureWelcome()
      nextTick(() => scrollToBottom())
    }
  },
)

function scrollToBottom() {
  if (messagesRef.value) {
    messagesRef.value.scrollTop = messagesRef.value.scrollHeight
  }
}

function openPanel() {
  aiStore.openPanel()
}

async function handleSend() {
  const text = inputText.value.trim()
  if (!text || aiStore.isLoading) return
  inputText.value = ''
  await aiStore.sendMessage(text)
}

function goToSettings() {
  aiStore.closePanel()
  router.push('/settings')
}

function renderContent(content: string): string {
  if (!content) return ''
  return content
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
}
</script>

<style scoped>
/* ── Floating Action Button ─────────────────────────────── */
.ai-fab {
  position: fixed;
  bottom: 96px;
  right: 30px;
  width: 56px;
  height: 56px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.5);
  z-index: 9999;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
}

.ai-fab:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 24px rgba(102, 126, 234, 0.6);
}

/* ── Panel ───────────────────────────────────────────────── */
.ai-panel {
  position: fixed;
  bottom: 96px;
  right: 30px;
  width: 380px;
  height: 520px;
  background: var(--color-bg-white);
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  z-index: 9999;
}

/* ── Header ──────────────────────────────────────────────── */
.ai-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: #fff;
  flex-shrink: 0;
}

.ai-header-left {
  display: flex;
  align-items: center;
  gap: 10px;
}

.ai-header-avatar {
  background: rgba(255, 255, 255, 0.2) !important;
}

.ai-header-title {
  font-size: 15px;
  font-weight: 600;
}

.ai-header-actions {
  display: flex;
  gap: 4px;
}

.ai-header-btn {
  cursor: pointer;
  padding: 4px;
  border-radius: 6px;
  transition: background 0.2s;
}

.ai-header-btn:hover {
  background: rgba(255, 255, 255, 0.25);
}

/* ── Messages ────────────────────────────────────────────── */
.ai-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  background: var(--color-bg);
}

.ai-msg {
  display: flex;
  gap: 8px;
  max-width: 88%;
  align-items: flex-start;
}

.ai-msg-user {
  align-self: flex-end;
  flex-direction: row-reverse;
}

.ai-msg-assistant {
  align-self: flex-start;
}

.ai-msg-avatar {
  flex-shrink: 0;
  margin-top: 2px;
}

.ai-msg-user-avatar :deep(.el-avatar) {
  background: var(--color-primary) !important;
}

.ai-msg-bubble {
  padding: 10px 14px;
  border-radius: 12px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.ai-msg-user .ai-msg-bubble {
  background: var(--color-primary);
  color: #fff;
  border-bottom-right-radius: 4px;
}

.ai-msg-assistant .ai-msg-bubble {
  background: var(--color-bg-white);
  color: var(--color-text-regular);
  border: 1px solid var(--color-border-light);
  border-bottom-left-radius: 4px;
}

/* ── Typing indicator ────────────────────────────────────── */
.ai-typing {
  display: flex;
  gap: 3px;
  padding: 14px 18px;
}

.ai-dot {
  animation: ai-bounce 1.4s ease infinite;
  font-size: 22px;
  line-height: 0;
  color: var(--color-text-placeholder);
}

.ai-dot:nth-child(2) { animation-delay: 0.2s; }
.ai-dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes ai-bounce {
  0%, 80%, 100% { transform: translateY(0); }
  40% { transform: translateY(-6px); }
}

/* ── Input area ──────────────────────────────────────────── */
.ai-input-area {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  padding: 8px 12px 12px;
  border-top: 1px solid var(--color-border);
  background: var(--color-bg-white);
  flex-shrink: 0;
}

.ai-input-area :deep(.el-textarea__inner) {
  border-radius: 10px;
  resize: none;
  font-size: 14px;
  min-height: 40px !important;
}

.ai-send-btn {
  flex-shrink: 0;
  margin-bottom: 2px;
}

/* ── Config hint ─────────────────────────────────────────── */
.ai-config-hint {
  text-align: center;
  padding: 6px 0;
  border-top: 1px solid var(--color-border);
  background: var(--color-bg-page);
  flex-shrink: 0;
}

/* ── Transitions ─────────────────────────────────────────── */
.ai-slide-up-enter-active,
.ai-slide-up-leave-active {
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.ai-slide-up-enter-from,
.ai-slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}

.ai-scale-enter-active,
.ai-scale-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.ai-scale-enter-from,
.ai-scale-leave-to {
  opacity: 0;
  transform: scale(0.7);
}

/* ── Dark mode ────────────────────────────────────────────── */
html.dark .ai-panel {
  border: 1px solid var(--color-border);
}

html.dark .ai-msg-assistant .ai-msg-bubble {
  background: var(--color-bg-page);
  color: var(--color-text-primary);
  border-color: var(--color-border);
}
</style>

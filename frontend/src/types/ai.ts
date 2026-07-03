// ============================================================
// AI Assistant Type Definitions
// ============================================================

export interface AiMessage {
  role: 'system' | 'user' | 'assistant' | 'tool'
  content: string
  tool_call_id?: string
  tool_calls?: AiToolCall[]
}

export interface AiToolCall {
  id: string
  type: 'function'
  function: {
    name: string
    arguments: string
  }
}

export interface AiToolDefinition {
  type: 'function'
  function: {
    name: string
    description: string
    parameters: Record<string, any>
  }
}

export interface AiChatRequest {
  model: string
  messages: { role: string; content: string; tool_call_id?: string }[]
  tools?: AiToolDefinition[]
  tool_choice?: 'auto' | 'none' | { type: 'function'; function: { name: string } }
}

export interface AiChatResponse {
  id: string
  object: string
  created: number
  model: string
  choices: {
    index: number
    message: {
      role: string
      content: string | null
      tool_calls?: AiToolCall[]
    }
    finish_reason: string
  }[]
  usage?: {
    prompt_tokens: number
    completion_tokens: number
    total_tokens: number
  }
}

export interface AiConfig {
  endpoint: string
  apiKey: string
  model: string
}

export type ConnectionStatus = 'untested' | 'connected' | 'disconnected'

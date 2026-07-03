import axios, { type AxiosInstance } from 'axios'
import type { AiConfig, AiChatRequest, AiChatResponse } from '../types/ai'

let client: AxiosInstance | null = null

function getClient(config: AiConfig): AxiosInstance {
  if (client) return client
  client = axios.create({
    baseURL: config.endpoint,
    timeout: 60000,
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${config.apiKey}`,
    },
  })
  return client
}

export function resetClient(): void {
  client = null
}

export async function sendChatRequest(
  config: AiConfig,
  body: AiChatRequest,
): Promise<AiChatResponse> {
  const c = getClient(config)
  const response = await c.post<AiChatResponse>('', body)
  return response.data
}

export async function testConnection(config: AiConfig): Promise<boolean> {
  try {
    const c = getClient(config)
    await c.post('', {
      model: config.model,
      messages: [{ role: 'user', content: 'Hello' }],
      max_tokens: 5,
    })
    return true
  } catch {
    return false
  }
}

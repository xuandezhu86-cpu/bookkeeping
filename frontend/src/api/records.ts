import api from './index'

export const recordApi = {
  getList(params: {
    categoryId?: number
    startDate?: string
    endDate?: string
    page?: number
    size?: number
  }) {
    return api.get('/records', { params })
  },
  create(data: { categoryId: number; amount: number; recordDate: string; note?: string }) {
    return api.post('/records', data)
  },
  update(id: number, data: { categoryId: number; amount: number; recordDate: string; note?: string }) {
    return api.put(`/records/${id}`, data)
  },
  delete(id: number) {
    return api.delete(`/records/${id}`)
  },
  getDailySummary(date: string) {
    return api.get('/records/daily-summary', { params: { date } })
  },
}

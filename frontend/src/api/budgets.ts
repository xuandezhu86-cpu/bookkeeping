import api from './index'

export const budgetApi = {
  getList(month: string) {
    return api.get('/budgets', { params: { month } })
  },
  set(data: { categoryId?: number; month: string; amount: number }) {
    return api.post('/budgets', data)
  },
  delete(id: number) {
    return api.delete(`/budgets/${id}`)
  },
}

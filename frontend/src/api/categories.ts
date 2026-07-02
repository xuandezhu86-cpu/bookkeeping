import api from './index'

export const categoryApi = {
  getTree() {
    return api.get('/categories/tree')
  },
  getParents() {
    return api.get('/categories/parents')
  },
  getChildren(parentId: number) {
    return api.get(`/categories/${parentId}/children`)
  },
  create(data: { name: string; icon: string; parentId?: number }) {
    return api.post('/categories', data)
  },
  update(id: number, data: { name: string; icon: string }) {
    return api.put(`/categories/${id}`, data)
  },
  delete(id: number) {
    return api.delete(`/categories/${id}`)
  },
}

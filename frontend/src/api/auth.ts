import api from './index'

export const authApi = {
  login(data: { username: string; password: string }) {
    return api.post('/auth/login', data)
  },
  register(data: { username: string; password: string; nickname: string }) {
    return api.post('/auth/register', data)
  },
}

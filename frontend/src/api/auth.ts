import api from './index'

export const authApi = {
  login(data: { username: string; password: string }) {
    return api.post('/auth/login', data)
  },
  register(data: { username: string; password: string; nickname: string }) {
    return api.post('/auth/register', data)
  },
  getProfile() {
    return api.get('/auth/me')
  },
  updateProfile(data: { nickname?: string; email?: string }) {
    return api.put('/auth/profile', data)
  },
  uploadAvatar(file: File) {
    const formData = new FormData()
    formData.append('file', file)
    return api.post('/auth/avatar', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
  changePassword(data: { oldPassword: string; newPassword: string }) {
    return api.put('/auth/password', data)
  },
}

import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { authApi } from '../api/auth'
import type { User, LoginForm, RegisterForm } from '../types'

export const useAuthStore = defineStore('auth', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref<User | null>(null)

  const isLoggedIn = computed(() => !!token.value)

  // 从 localStorage 恢复用户信息
  const savedUser = localStorage.getItem('user')
  if (savedUser) {
    try { user.value = JSON.parse(savedUser) } catch { /* ignore */ }
  }

  // 持久化用户信息到 localStorage
  watch(user, (val) => {
    if (val) {
      localStorage.setItem('user', JSON.stringify(val))
    } else {
      localStorage.removeItem('user')
    }
  }, { deep: true })

  async function login(form: LoginForm) {
    const res = await authApi.login(form)
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', res.data.token)
  }

  async function register(form: RegisterForm) {
    const res = await authApi.register(form)
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', res.data.token)
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
  }

  async function fetchProfile() {
    const res = await authApi.getProfile()
    if (user.value) {
      user.value = { ...user.value, ...res.data }
    } else {
      user.value = res.data
    }
  }

  async function updateProfile(data: { nickname?: string; email?: string }) {
    const res = await authApi.updateProfile(data)
    if (user.value) {
      user.value = { ...user.value, ...res.data }
    } else {
      user.value = res.data
    }
  }

  async function uploadAvatar(file: File) {
    const res = await authApi.uploadAvatar(file)
    if (user.value) {
      user.value.avatar = res.data
    }
  }

  async function changePassword(data: { oldPassword: string; newPassword: string }) {
    await authApi.changePassword(data)
  }

  return {
    token, user, isLoggedIn,
    login, register, logout,
    fetchProfile, updateProfile, uploadAvatar, changePassword,
  }
})

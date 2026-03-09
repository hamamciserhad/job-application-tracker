import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import api from '../api/axios'

export interface User {
  id: number
  email: string
  name: string
}

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(localStorage.getItem('accessToken'))
  const refreshToken = ref<string | null>(localStorage.getItem('refreshToken'))
  const user = ref<User | null>(null)

  const isAuthenticated = computed(() => !!accessToken.value)

  const setTokens = (access: string, refresh: string) => {
    accessToken.value = access
    refreshToken.value = refresh
    localStorage.setItem('accessToken', access)
    localStorage.setItem('refreshToken', refresh)
  }

  const login = async (email: string, password: string) => {
    const { data } = await api.post('/api/auth/login', { email, password })
    setTokens(data.data.accessToken, data.data.refreshToken)
    user.value = data.data.user ?? null
  }

  const register = async (name: string, email: string, password: string) => {
    const { data } = await api.post('/api/auth/register', { name, email, password })
    setTokens(data.data.accessToken, data.data.refreshToken)
    user.value = data.data.user ?? null
  }

  const logout = () => {
    accessToken.value = null
    refreshToken.value = null
    user.value = null
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  }

  const fetchUser = async () => {
    try {
      const { data } = await api.get('/api/auth/me')
      user.value = data.data
    } catch {
      // silently ignore – user display is non-critical
    }
  }

  return {
    accessToken,
    refreshToken,
    user,
    isAuthenticated,
    login,
    register,
    logout,
    fetchUser
  }
})

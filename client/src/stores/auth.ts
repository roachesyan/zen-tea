import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfo } from '@/types'
import { getToken, setToken, setRefreshToken, clearTokens } from '@/utils/auth'
import { login as loginApi, register as registerApi, logout as logoutApi } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<UserInfo | null>(null)
  const token = ref<string | null>(getToken())

  const isLoggedIn = computed(() => !!token.value)

  async function login(username: string, password: string) {
    const { data: res } = await loginApi({ username, password })
    token.value = res.data.accessToken
    setToken(res.data.accessToken)
    setRefreshToken(res.data.refreshToken)
    user.value = res.data.user
  }

  async function register(username: string, password: string) {
    const { data: res } = await registerApi({ username, password })
    token.value = res.data.accessToken
    setToken(res.data.accessToken)
    setRefreshToken(res.data.refreshToken)
    user.value = res.data.user
  }

  async function logout() {
    try {
      await logoutApi()
    } finally {
      token.value = null
      user.value = null
      clearTokens()
    }
  }

  function setUser(userInfo: UserInfo) {
    user.value = userInfo
  }

  return { user, token, isLoggedIn, login, register, logout, setUser }
})

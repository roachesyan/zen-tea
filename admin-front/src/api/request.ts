import axios from 'axios'
import type { Result } from '@/types'
import { getToken, getRefreshToken, setToken, setRefreshToken, clearTokens } from '@/utils/auth'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
})

let isRefreshing = false
let pendingRequests: Array<(token: string) => void> = []

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const data = response.data as Result<unknown>
    if (data.code !== 200 && data.code !== 201) {
      return Promise.reject(new Error(data.message || '请求失败'))
    }
    return response
  },
  async (error) => {
    const originalRequest = error.config
    if (error.response?.status === 401 && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve) => {
          pendingRequests.push((token: string) => {
            originalRequest.headers.Authorization = `Bearer ${token}`
            resolve(request(originalRequest))
          })
        })
      }

      originalRequest._retry = true
      isRefreshing = true

      try {
        const refreshToken = getRefreshToken()
        if (!refreshToken) {
          throw new Error('No refresh token')
        }
        const { data } = await axios.post<Result<{ accessToken: string; refreshToken: string }>>('/api/auth/refresh', {
          refreshToken,
        })
        setToken(data.data.accessToken)
        setRefreshToken(data.data.refreshToken)
        pendingRequests.forEach((cb) => cb(data.data.accessToken))
        pendingRequests = []
        originalRequest.headers.Authorization = `Bearer ${data.data.accessToken}`
        return request(originalRequest)
      } catch {
        clearTokens()
        router.push('/login')
        return Promise.reject(error)
      } finally {
        isRefreshing = false
      }
    }

    const message = error.response?.data?.message || error.message || '请求失败'
    return Promise.reject(new Error(message))
  },
)

export default request

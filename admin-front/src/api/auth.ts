import request from './request'
import type { Result, LoginRequest, LoginResponse } from '@/types'

export function login(data: LoginRequest) {
  return request.post<Result<LoginResponse>>('/auth/login', data)
}

export function logout() {
  return request.post<Result<void>>('/auth/logout')
}

import request from './request'
import type { Result, LoginRequest, RegisterRequest, LoginResponse } from '@/types'

export function login(data: LoginRequest) {
  return request.post<Result<LoginResponse>>('/auth/login', data)
}

export function register(data: RegisterRequest) {
  return request.post<Result<LoginResponse>>('/auth/register', data)
}

export function logout() {
  return request.post<Result<void>>('/auth/logout')
}

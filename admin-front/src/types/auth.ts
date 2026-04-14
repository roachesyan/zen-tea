export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  accessToken: string
  refreshToken: string
  expiresIn: number
  user: UserInfo
}

export interface UserInfo {
  id: number
  username: string
  nickname: string
  phone: string
  avatarUrl: string
  role: string
}

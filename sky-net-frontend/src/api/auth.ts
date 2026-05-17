import http from './request'

export interface LoginParams {
  username: string
  password: string
}

export interface LoginResult {
  token: string
  tokenType: string
  expiresIn: number
  user: {
    id: number
    username: string
    nickname: string
    email: string
    avatar: string
  }
}

export const authApi = {
  login(data: LoginParams): Promise<LoginResult> {
    return http.post('/auth/login', data)
  }
}

/**
 * A02 统一请求层：baseURL 由 vite proxy 决定，不硬编码接口域名；
 * code=0 才 resolve，其余统一按业务码拦截；token 只保存服务端下发值。
 */
import type { ApiResponse } from './types'

// 非 H5 端直连后端；H5 端由条件编译覆盖为同源 /api（走 vite proxy）。
// 只声明一次变量，避免 vue-tsc 在条件编译注释下看到两个 const 而报 TS2451。
let BASE_URL = 'http://127.0.0.1:8016/api'
// #ifdef H5
BASE_URL = '/api'
// #endif

const TOKEN_KEY = 'campus_token'

export function getToken(): string {
  return uni.getStorageSync(TOKEN_KEY) || ''
}
export function setToken(token: string): void {
  if (token) uni.setStorageSync(TOKEN_KEY, token)
  else clearToken()
}
export function clearToken(): void { uni.removeStorageSync(TOKEN_KEY) }

export class BizError extends Error {
  constructor(public code: number, message: string) { super(message) }
}

export type Method = 'GET' | 'POST' | 'PUT' | 'DELETE'
export interface RequestOptions {
  url: string
  method?: Method
  data?: Record<string, unknown>
  params?: Record<string, string | number | undefined>
  auth?: boolean
}

function buildUrl(path: string, params?: RequestOptions['params']): string {
  let url = BASE_URL + path
  if (params) {
    const q = Object.entries(params)
      .filter(([, v]) => v !== undefined && v !== '')
      .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(String(v))}`)
      .join('&')
    if (q) url += (url.includes('?') ? '&' : '?') + q
  }
  return url
}

export function request<T>(options: RequestOptions): Promise<T> {
  const { url, method = 'GET', data, params, auth = false } = options
  const token = auth ? getToken() : ''
  return new Promise<T>((resolve, reject) => {
    uni.request({
      url: buildUrl(url, params),
      method,
      data,
      header: {
        'Content-Type': 'application/json',
        ...(token ? { Authorization: `Bearer ${token}` } : {})
      },
      success: (res) => {
        const body = res.data as ApiResponse<T>
        if (!body || typeof body.code !== 'number') {
          uni.showToast({ title: '响应格式异常', icon: 'none' })
          reject(new BizError(50000, '响应格式异常'))
          return
        }
        if (body.code !== 0) {
          if (body.code === 10001) { clearToken(); uni.navigateTo({ url: '/pages/auth/login' }) }
          else uni.showToast({ title: body.message, icon: 'none', duration: 2500 })
          reject(new BizError(body.code, body.message))
          return
        }
        resolve(body.data)
      },
      fail: (err) => {
        uni.showToast({ title: '网络异常，请检查后端 8016', icon: 'none' })
        reject(err)
      }
    })
  })
}

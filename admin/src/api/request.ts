/**
 * D02 请求层：axios 实例 + 统一响应 + code=0 判定 + 错误拦截。
 * 基础地址来自 VITE_API_BASE（默认同源 /api，由 vite proxy 转发到 8016），
 * 页面内不出现 axios、也不硬编码后端域名。
 */
import axios, { type AxiosRequestConfig, type AxiosError } from 'axios'
import type { ApiResponse } from './types'

const BASE_URL = import.meta.env.VITE_API_BASE || '/api'

export const httpInstance = axios.create({ baseURL: BASE_URL, timeout: 8000 })

export class BizError extends Error {
  constructor(public code: number, message: string) { super(message) }
}

/** D09 素材上传：FormData 直传 /api/upload，绕过 JSON 序列化但复用同一拆包出口。 */
export async function upload(file: File): Promise<{ url: string; name: string; size: number }> {
  const fd = new FormData()
  fd.append('file', file)
  const res = await httpInstance.post<ApiResponse<{ url: string; name: string; size: number }>>('/upload', fd)
  const body = res.data
  if (body.code !== 0) {
    if (body.code === 10001) {
      localStorage.removeItem('campus_token'); localStorage.removeItem('campus_user')
      if (location.pathname !== '/login') location.href = '/login'
    }
    throw new BizError(body.code, body.message)
  }
  return body.data
}

httpInstance.interceptors.request.use((config) => {
  const token = localStorage.getItem('campus_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

httpInstance.interceptors.response.use(
  (res) => res,
  (err: AxiosError) => Promise.reject(new BizError(50000, `网络异常：${err.message}`))
)

/** 唯一出口：拆包 code/message/data，非 0 一律转成 BizError。 */
export async function http<T>(config: AxiosRequestConfig): Promise<T> {
  const res = await httpInstance.request<ApiResponse<T>>(config)
  const body = res.data
  if (body.code !== 0) {
    if (body.code === 10001) {
      localStorage.removeItem('campus_token')
      localStorage.removeItem('campus_user')
      if (location.pathname !== '/login') location.href = '/login'
    }
    throw new BizError(body.code, body.message)
  }
  return body.data
}

import { BUSINESS_MESSAGES } from './bizCodes'
export function messageOf(code: number, fallback = '请求失败'): string {
  return BUSINESS_MESSAGES[code] || fallback
}

/**
 * 类型严格对齐后端真实响应字段。
 * 说明：JdbcTemplate 返回的是数据库列名（下划线），故 Activity 等接口保留下划线；
 * AuthController 主动构造的 user / dashboard 使用驼峰。详见 docs/api-contract.md。
 */
export interface ApiResponse<T = unknown> { code: number; message: string; data: T }
export interface PageData<T> { list: T[]; total: number; page: number; size: number }

export type ActivityStatus = 'DRAFT' | 'PUBLISHED' | 'ENDED'
export type AuditStatus = 'PENDING' | 'APPROVED' | 'REJECTED'
export type Role = 'STUDENT' | 'ORGANIZER' | 'ADMIN'

export interface Activity {
  id: number
  title: string
  category_id: number
  categoryName: string
  cover: string | null
  place: string
  start_time: string
  end_time: string
  signup_deadline: string
  capacity: number
  signed_up: number
  signedUp?: number
  status: ActivityStatus
  audit_status: AuditStatus
  organizer_id: number
  description: string
  checkin_code: string
}
export interface Category { id: number; name: string; sort: number }
export interface User {
  id: number; username: string; realName: string; role: Role
  studentNo: string | null; phone: string | null
}
export interface LoginResult { token: string; user: User }
export interface Signup {
  id: number; status: string; created_at: string
  activity_id?: number; activityId?: number; title?: string
  place?: string; start_time?: string; startTime?: string
  checkin_code?: string; checkinCode?: string
}
export interface Notice { id: number; user_id: number; title: string; content: string; created_at: string }
export interface Dashboard {
  operator: string; activityCount: number; publishedCount: number
  pendingAudit: number; signupCount: number; checkinCount: number; userCount: number
}
export interface UploadResult { url: string; name: string; size: number }
export interface OpLog { id: number; user_id: number; username: string; action: string; detail: string; created_at: string }

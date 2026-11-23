/** 与 app 端共享同一份后端契约；字段来自 docs/api-contract.md 的真实响应。 */
export interface ApiResponse<T = unknown> { code: number; message: string; data: T }
export interface PageData<T> { list: T[]; total: number; page: number; size: number }
export type Role = 'STUDENT' | 'ORGANIZER' | 'ADMIN'
export type ActivityStatus = 'DRAFT' | 'PUBLISHED' | 'ENDED'
export type AuditStatus = 'PENDING' | 'APPROVED' | 'REJECTED'

export interface Activity {
  id: number; title: string; category_id: number; categoryName: string
  cover: string | null; place: string; start_time: string; end_time: string
  signup_deadline: string; capacity: number; signed_up: number; signedUp?: number
  status: ActivityStatus; audit_status: AuditStatus; organizer_id: number
  description: string; checkin_code: string
}
export interface Category { id: number; name: string; sort: number }
export interface User { id: number; username: string; realName: string; role: Role
  studentNo: string | null; phone: string | null; status?: number }
export interface LoginResult { token: string; user: User }
export interface Dashboard { operator: string; activityCount: number; publishedCount: number
  pendingAudit: number; signupCount: number; checkinCount: number; userCount: number }
export interface Signup { id: number; activity_id: number; user_id: number; status: string
  created_at: string; title?: string; real_name?: string; student_no?: string }
export interface OpLog { id: number; user_id: number; username: string
  action: string; detail: string; created_at: string }
export interface UploadResult { url: string; name: string; size: number }

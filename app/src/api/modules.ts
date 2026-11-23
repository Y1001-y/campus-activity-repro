import { request } from './request'
import type {
  Activity, Category, PageData, LoginResult, User, Signup, Notice,
  Dashboard, UploadResult, OpLog
} from './types'

export const api = {
  login: (username: string, password: string) =>
    request<LoginResult>({ url: '/auth/login', method: 'POST', data: { username, password } }),
  me: () => request<User>({ url: '/auth/me', auth: true }),
  logout: () => request<void>({ url: '/auth/logout', method: 'POST', auth: true }),
  sendSms: (phone: string) =>
    request<{ phone: string; code: string; expiresIn: number; note: string }>(
      { url: '/auth/sms/send', method: 'POST', data: { phone } }),
  bind: (studentNo: string, phone: string, code: string) =>
    request<User>({ url: '/auth/bind', method: 'POST', data: { studentNo, phone, code } }),
  categories: () => request<Category[]>({ url: '/categories' }),
  activities: (params: Record<string, string | number | undefined>) =>
    request<PageData<Activity>>({ url: '/activities', params }),
  activity: (id: number) => request<Activity>({ url: `/activities/${id}` }),
  signup: (id: number) =>
    request<{ signupId: number; activityId: number; status: string }>(
      { url: `/activities/${id}/signup`, method: 'POST', auth: true }),
  mySignups: () => request<Signup[]>({ url: '/me/signups', auth: true }),
  cancelSignup: (id: number) => request<void>({ url: `/signups/${id}`, method: 'DELETE', auth: true }),
  checkin: (activityId: number, code: string) =>
    request<{ activityId: number; title: string; checkedInAt: string; message: string }>(
      { url: '/checkin', method: 'POST', data: { activityId, code }, auth: true }),
  notices: () => request<Notice[]>({ url: '/notices', auth: true }),
  dashboard: () => request<Dashboard>({ url: '/admin/dashboard', auth: true }),
  adminActivities: () => request<Activity[]>({ url: '/admin/activities', auth: true }),
  publishActivity: (data: Record<string, unknown>) =>
    request<{ id: number; checkinCode: string; auditStatus: string }>(
      { url: '/admin/activities', method: 'POST', data, auth: true }),
  auditActivity: (id: number, approve: boolean) =>
    request<{ id: number; auditStatus: string; status: string }>(
      { url: `/admin/activities/${id}/audit`, method: 'POST', data: { approve }, auth: true }),
  adminSignups: (activityId?: number) =>
    request<Signup[]>({ url: '/admin/signups', params: { activityId }, auth: true }),
  adminUsers: () => request<User[]>({ url: '/admin/users', auth: true }),
  adminLogs: () => request<OpLog[]>({ url: '/admin/logs', auth: true }),
  verifyCheckin: (activityId: number, userId: number) =>
    request<{ activityId: number; userId: number; verified: boolean }>(
      { url: '/admin/checkin/verify', method: 'POST', data: { activityId, userId }, auth: true }),
  uploadList: () => request<string[]>({ url: '/uploads' })
}

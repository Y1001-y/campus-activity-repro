import { http } from './request'
import type {
  Activity, Category, PageData, LoginResult, User, Dashboard, Signup, OpLog, UploadResult
} from './types'

export const api = {
  login: (username: string, password: string) =>
    http<LoginResult>({ url: '/auth/login', method: 'POST', data: { username, password } }),
  me: () => http<User>({ url: '/auth/me' }),
  logout: () => http<void>({ url: '/auth/logout', method: 'POST' }),
  categories: () => http<Category[]>({ url: '/categories' }),
  activities: (params: Record<string, unknown>) =>
    http<PageData<Activity>>({ url: '/activities', params }),
  activity: (id: number) => http<Activity>({ url: `/activities/${id}` }),
  dashboard: () => http<Dashboard>({ url: '/admin/dashboard' }),
  adminActivities: () => http<Activity[]>({ url: '/admin/activities' }),
  publishActivity: (data: Record<string, unknown>) =>
    http<{ id: number; checkinCode: string; auditStatus: string }>(
      { url: '/admin/activities', method: 'POST', data }),
  auditActivity: (id: number, approve: boolean) =>
    http<{ id: number; auditStatus: string; status: string }>(
      { url: `/admin/activities/${id}/audit`, method: 'POST', data: { approve } }),
  users: () => http<User[]>({ url: '/admin/users' }),
  signups: (activityId?: number) =>
    http<Signup[]>({ url: '/admin/signups', params: { activityId } }),
  logs: () => http<OpLog[]>({ url: '/admin/logs' }),
  verifyCheckin: (activityId: number, userId: number) =>
    http<{ activityId: number; userId: number; verified: boolean }>(
      { url: '/admin/checkin/verify', method: 'POST', data: { activityId, userId } }),
  uploads: () => http<string[]>({ url: '/uploads' })
}

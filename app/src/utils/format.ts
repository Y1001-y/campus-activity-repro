/** 统一字典与格式化，避免页面各自硬编码。 */
export const STATUS_TEXT: Record<string, string> = {
  DRAFT: '草稿', PUBLISHED: '已发布', ENDED: '已结束',
  PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回'
}
export const ROLE_TEXT: Record<string, string> = {
  STUDENT: '学生', ORGANIZER: '组织者', ADMIN: '管理员'
}
export function shortTime(v?: string): string { return v ? v.slice(5, 16) : '-' }
export function canManage(role?: string): boolean { return role === 'ADMIN' || role === 'ORGANIZER' }

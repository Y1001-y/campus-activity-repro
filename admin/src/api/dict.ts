/** D02 字典：与后端 ResultCode 一一对应，错误提示统一从这里取。 */
export const BUSINESS_MESSAGES: Record<number, string> = {
  10001: '未登录或登录已过期，请重新登录',
  10002: '没有操作权限，请联系管理员',
  10003: '请先绑定学号信息',
  10004: '学号不在名单内，请联系管理员',
  10005: '该学号已被其他账号绑定',
  10007: '账号或密码错误',
  20001: '活动不存在',
  20005: '活动暂未开放报名',
  30001: '名额已满',
  30002: '你已报名该活动',
  30003: '报名记录不存在',
  40001: '签到码无效或已过期，请刷新后重试',
  40002: '你未报名该活动，无法签到',
  40003: '你已签到，无需重复操作',
  40004: '当前不在签到时间内',
  50000: '系统繁忙，请稍后再试',
  50001: '参数错误'
}
export function messageOf(code: number, fallback = '请求失败'): string {
  return BUSINESS_MESSAGES[code] || fallback
}
export const STATUS_TEXT: Record<string, string> = {
  DRAFT: '草稿', PUBLISHED: '已发布', ENDED: '已结束',
  PENDING: '待审核', APPROVED: '已通过', REJECTED: '已驳回'
}
export const ROLE_TEXT: Record<string, string> = {
  STUDENT: '学生', ORGANIZER: '组织者', ADMIN: '管理员'
}

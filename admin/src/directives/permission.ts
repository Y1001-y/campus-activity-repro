import type { App, Directive } from 'vue'

function currentRole(): string {
  try { return JSON.parse(localStorage.getItem('campus_user') || '{}').role || '' } catch { return '' }
}

/** 按钮级权限：不满足角色的元素直接从 DOM 移除，与路由守卫、后端 10002 形成三道收口。 */
export const permission: Directive<HTMLElement, string[]> = {
  mounted(el, binding) {
    const roles = binding.value || []
    if (roles.length && !roles.includes(currentRole())) el.parentNode?.removeChild(el)
  }
}

export function registerPermission(app: App): void { app.directive('permission', permission) }

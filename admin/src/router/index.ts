import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

/** 路由分层：公开页 / 布局路由 + 业务子路由 / 403 / 404。守卫见文件末尾。 */
const children: RouteRecordRaw[] = [  { path: '', name: 'dashboard', component: () => import('../views/DashboardView.vue'), meta: { roles: [] } }
  { path: 'audit', name: 'audit', component: () => import('../views/AuditView.vue'), meta: { roles: ['ADMIN', 'ORGANIZER'] } }]

const routes: RouteRecordRaw[] = [
  { path: '/login', name: 'login', component: () => import('../views/LoginView.vue'), meta: { public: true } },
  { path: '/403', name: 'forbidden', component: () => import('../views/ForbiddenView.vue'), meta: { public: true } },
  children.length
    ? { path: '/', component: () => import('../layouts/AdminLayout.vue'), children }
    : { path: '/', redirect: '/login' },
  { path: '/:pathMatch(.*)*', name: 'notFound', component: () => import('../views/NotFoundView.vue'), meta: { public: true } }
]

const router = createRouter({ history: createWebHistory(), routes })

function currentRole(): string {
  try { return JSON.parse(localStorage.getItem('campus_user') || '{}').role || '' } catch { return '' }
}

/** 未登录拦截 + 角色守卫：低权限直接输入高权限 URL 会落到 /403。 */
router.beforeEach((to) => {
  if (to.meta.public) return true
  const token = localStorage.getItem('campus_token')
  if (!token) return { path: '/login', query: { redirect: to.fullPath } }
  const roles = to.meta.roles as string[] | undefined
  if (roles && roles.length && !roles.includes(currentRole())) return { path: '/403', query: { from: to.fullPath } }
  return true
})

export default router

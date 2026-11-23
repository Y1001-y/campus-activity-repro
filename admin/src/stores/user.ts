import { defineStore } from 'pinia'
import { api } from '../api/modules'
import type { User } from '../api/types'

/** Pinia 管理登录态；token 与用户同时写 localStorage，刷新后可恢复，登出必须清空。 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('campus_token') || '',
    user: ((): User | null => {
      try { return JSON.parse(localStorage.getItem('campus_user') || 'null') } catch { return null }
    })()
  }),
  getters: {
    loggedIn: (s) => Boolean(s.token),
    role: (s) => s.user?.role || '',
    isManager: (s) => s.user?.role === 'ADMIN' || s.user?.role === 'ORGANIZER'
  },
  actions: {
    setSession(token: string, user: User) {
      this.token = token; this.user = user
      localStorage.setItem('campus_token', token)
      localStorage.setItem('campus_user', JSON.stringify(user))
    },
    async refresh() {
      if (!this.token) return
      try {
        this.user = await api.me()
        localStorage.setItem('campus_user', JSON.stringify(this.user))
      } catch { this.clear() }
    },
    clear() {
      this.token = ''; this.user = null
      localStorage.removeItem('campus_token'); localStorage.removeItem('campus_user')
    }
  }
})

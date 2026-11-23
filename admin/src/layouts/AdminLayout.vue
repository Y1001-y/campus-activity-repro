<template>
  <div class="shell">
    <aside>
      <div class="brand">校园活动</div>
      <router-link v-for="m in menus" :key="m.path" :to="m.path" class="item">{{ m.label }}</router-link>
    </aside>
    <main>
      <div class="top">
        <span>{{ store.user?.realName || '未登录' }} · {{ store.role || '-' }}</span>
        <button class="btn gray" @click="logout">退出登录</button>
      </div>
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { api } from '../api/modules'
const store = useUserStore(), router = useRouter()
const all = [
  { path: '/', label: '工作台', roles: [] as string[] },
  { path: '/activities', label: '活动管理', roles: [] as string[] },
  { path: '/audit', label: '活动审核', roles: ['ADMIN', 'ORGANIZER'] },
  { path: '/users', label: '用户管理', roles: ['ADMIN'] },
  { path: '/logs', label: '操作日志', roles: ['ADMIN'] }
]
const menus = computed(() => all.filter((m) => !m.roles.length || m.roles.includes(store.role)))
async function logout() {
  try { await api.logout() } catch { /* 令牌失效也要清本地 */ }
  store.clear(); router.push('/login')
}
</script>

<style scoped>
.shell { display: flex; min-height: 100vh; }
aside { width: 210px; background: #1f2a44; color: #fff; padding: 20px 12px; }
.brand { font-size: 18px; font-weight: 700; padding: 8px 12px 20px; }
.item { display: block; padding: 12px; color: #c9d3e8; border-radius: 8px; margin-bottom: 4px; }
.item.router-link-exact-active { background: #3b6cf6; color: #fff; }
main { flex: 1; padding: 24px; }
.top { display: flex; justify-content: flex-end; align-items: center; gap: 12px; margin-bottom: 20px; font-size: 13px; color: #6b7280; }
</style>

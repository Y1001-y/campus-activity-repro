<template>
  <div class="login">
    <h1>校园活动管理后台</h1>
    <p class="hint">演示账号（任选一组）：</p>
    <p class="hint">管理员 admin / admin123</p>
    <p class="hint">组织者 organizer / org123</p>
    <p class="hint">学生 2024001001 / stu123</p>
    <input v-model="username" placeholder="账号" />
    <input v-model="password" type="password" placeholder="密码" @keyup.enter="submit" />
    <button class="btn" @click="submit">登录</button>
    <p v-if="err" class="err">{{ err }}</p>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { api } from '../api/modules'
const router = useRouter(), route = useRoute()
const username = ref(''), password = ref(''), err = ref('')
async function submit() {
  err.value = ''
  try {
    const r = await api.login(username.value, password.value)
    localStorage.setItem('campus_token', r.token)
    localStorage.setItem('campus_user', JSON.stringify(r.user))
    router.push((route.query.redirect as string) || '/')
  } catch (e) { err.value = e instanceof Error ? e.message : String(e) }
}
</script>

<style scoped>
.login { max-width: 380px; margin: 12vh auto; background: #fff; padding: 32px; border-radius: 16px; display: flex; flex-direction: column; gap: 14px; }
.login input { padding: 12px; border: 1px solid #dfe3ea; border-radius: 8px; font-size: 14px; }
.hint { font-size: 12px; } .err { color: #d03050; }
</style>

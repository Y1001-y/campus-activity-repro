<template>
  <div class="page">
    <PageHeader title="用户管理" subtitle="数据来自 /api/admin/users（仅 ADMIN 可见）" />
    <p v-if="err" class="err">{{ err }}</p>
    <DataTable :columns="cols" :rows="rows" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { api } from '../api/modules'
import type { User } from '../api/types'
import { ROLE_TEXT } from '../api/dict'
import PageHeader from '../components/PageHeader.vue'
import DataTable from '../components/DataTable.vue'
const list = ref<User[]>([]), err = ref('')
const cols = [{ key: 'id', label: 'ID' }, { key: 'username', label: '账号' },
  { key: 'realName', label: '姓名' }, { key: 'roleText', label: '角色' },
  { key: 'studentNo', label: '学号' }, { key: 'phone', label: '手机号' }]
const rows = computed(() => list.value.map((u) => ({
  id: u.id, username: u.username, realName: u.realName,
  roleText: ROLE_TEXT[u.role] || u.role, studentNo: u.studentNo || '-', phone: u.phone || '-'
})))
onMounted(async () => { try { list.value = await api.users() } catch (e) { err.value = String(e) } })
</script>

<style scoped>
.page { padding: 24px; }
.h1 { font-size: 22px; font-weight: 700; margin: 0 0 16px; }
.cards { display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 20px; }
.card { background: #fff; border-radius: 12px; padding: 20px; min-width: 150px; }
.grid { width: 100%; border-collapse: collapse; background: #fff; border-radius: 12px; overflow: hidden; }
.grid th, .grid td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #eef0f4; font-size: 14px; }
.btn { background: #3b6cf6; color: #fff; border: 0; border-radius: 8px; padding: 8px 16px; margin-right: 8px; }
.btn.gray { background: #e8ebf2; color: #333; }
.err { color: #d03050; } .hint { color: #6b7280; font-size: 13px; }
</style>

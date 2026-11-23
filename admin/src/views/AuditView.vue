<template>
  <div class="page">
    <PageHeader title="活动审核" subtitle="数据来自 /api/admin/activities">
      <template #actions><button class="btn" @click="load">刷新</button></template>
    </PageHeader>
    <p v-if="err" class="err">{{ err }}</p>
    <table class="grid">
      <thead><tr><th>ID</th><th>标题</th><th>状态</th><th>审核</th><th>操作</th></tr></thead>
      <tbody><tr v-for="a in list" :key="a.id">
        <td>{{ a.id }}</td><td>{{ a.title }}</td><td><StatusTag :status="a.status" /></td>
        <td><StatusTag :status="a.audit_status" /></td>
        <td><button class="btn" @click="current = a">查看详情</button></td>
      </tr></tbody>
    </table>
    <ActivityDrawer :activity="current" @close="current = null" @approve="audit(true)" @reject="audit(false)" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '../api/modules'
import type { Activity } from '../api/types'
import PageHeader from '../components/PageHeader.vue'
import StatusTag from '../components/StatusTag.vue'
import ActivityDrawer from '../components/ActivityDrawer.vue'
const list = ref<Activity[]>([]), current = ref<Activity | null>(null), err = ref('')
async function load() { try { list.value = await api.adminActivities() } catch (e) { err.value = String(e) } }
async function audit(approve: boolean) {
  if (!current.value) return
  try { await api.auditActivity(current.value.id, approve); current.value = null; await load() }
  catch (e) { err.value = String(e) }
}
onMounted(load)
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

<template>
  <div class="page">
    <PageHeader title="工作台" :subtitle="'真实数据来自 /api/admin/dashboard · 操作人 ' + (d?.operator || '-')" />
    <p v-if="err" class="err">{{ err }}</p>
    <div class="cards">
      <div v-for="c in cards" :key="c.label" class="card"><b>{{ c.value }}</b><div class="hint">{{ c.label }}</div></div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { api } from '../api/modules'
import type { Dashboard } from '../api/types'
import PageHeader from '../components/PageHeader.vue'
const d = ref<Dashboard | null>(null), err = ref('')
const cards = computed(() => d.value ? [
  { label: '活动总数', value: d.value.activityCount }, { label: '已发布', value: d.value.publishedCount },
  { label: '待审核', value: d.value.pendingAudit }, { label: '报名数', value: d.value.signupCount },
  { label: '签到数', value: d.value.checkinCount }, { label: '用户数', value: d.value.userCount }
] : [])
onMounted(async () => { try { d.value = await api.dashboard() } catch (e) { err.value = String(e) } })
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

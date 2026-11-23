<template>
  <div class="page">
    <PageHeader title="活动管理" subtitle="数据来自 /api/admin/activities">
      <template #actions><button class="btn" @click="load">刷新</button></template>
    </PageHeader>
    <p v-if="err" class="err">{{ err }}</p>
    <DataTable :columns="cols" :rows="rows" />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { api } from '../api/modules'
import type { Activity } from '../api/types'
import { STATUS_TEXT } from '../api/dict'
import PageHeader from '../components/PageHeader.vue'
import DataTable from '../components/DataTable.vue'
import StatusTag from '../components/StatusTag.vue'
const list = ref<Activity[]>([]), err = ref('')
const cols = [{ key: 'id', label: 'ID' }, { key: 'title', label: '标题' },
  { key: 'place', label: '地点' }, { key: 'start_time', label: '开始时间' },
  { key: 'statusText', label: '状态' }]
const rows = computed(() => list.value.map((a) => ({
  id: a.id, title: a.title, place: a.place, start_time: a.start_time,
  statusText: STATUS_TEXT[a.status] || a.status
})))
async function load() { try { list.value = await api.adminActivities() } catch (e) { err.value = String(e) } }
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

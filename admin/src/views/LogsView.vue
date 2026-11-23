<template>
  <div class="page">
    <PageHeader title="操作日志" subtitle="真实接口 /api/admin/logs（仅 ADMIN）" />
    <p v-if="err" class="err">{{ err }}</p>
    <table class="grid">
      <thead><tr><th>ID</th><th>时间</th><th>操作人</th><th>动作</th><th>详情</th></tr></thead>
      <tbody><tr v-for="r in list" :key="r.id">
        <td>{{ r.id }}</td><td>{{ r.created_at }}</td><td>{{ r.username }}</td>
        <td>{{ r.action }}</td><td>{{ r.detail }}</td>
      </tr></tbody>
    </table>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '../api/modules'
import type { OpLog } from '../api/types'
import PageHeader from '../components/PageHeader.vue'
const list = ref<OpLog[]>([]), err = ref('')
onMounted(async () => { try { list.value = await api.logs() } catch (e) { err.value = String(e) } })
</script>

<style scoped>
.page { padding: 24px; } .err { color: #d03050; }
.grid { width: 100%; border-collapse: collapse; background: #fff; border-radius: 12px; overflow: hidden; }
.grid th, .grid td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #eef0f4; font-size: 14px; }
</style>

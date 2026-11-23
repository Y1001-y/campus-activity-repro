<template>
  <div v-if="activity" class="mask" @click.self="$emit('close')">
    <div class="drawer">
      <h2>{{ activity.title }}</h2>
      <p class="hint">{{ activity.categoryName }} · {{ activity.place }}</p>
      <p>时间：{{ activity.start_time }} ~ {{ activity.end_time }}</p>
      <p>名额：{{ activity.signed_up }} / {{ activity.capacity }}</p>
      <p>签到码：{{ activity.checkin_code }}</p>
      <p>{{ activity.description }}</p>
      <button class="btn" @click="$emit('approve', activity.id)">通过</button>
      <button class="btn gray" @click="$emit('reject', activity.id)">驳回</button>
      <button class="btn gray" @click="$emit('close')">关闭</button>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Activity } from '../api/types'
defineProps<{ activity: Activity | null }>()
defineEmits<{ close: []; approve: [id: number]; reject: [id: number] }>()
</script>

<style scoped>
.mask { position: fixed; inset: 0; background: rgba(0,0,0,.35); display: flex; justify-content: flex-end; }
.drawer { width: 420px; background: #fff; padding: 28px; overflow: auto; }
.hint { color: #6b7280; }
</style>

<template>
  <view class="page">
    <text class="h1">消息通知</text>
    <text v-if="err" class="err">{{ err }}</text>
    <view v-for="n in list" :key="n.id" class="row">
      <text class="h1">{{ n.title }}</text><text class="meta">{{ n.content }}</text>
      <text class="hint">{{ n.created_at }}</text>
    </view>
    <text v-if="!list.length && !err" class="hint">暂无消息</text>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '../../api/modules'
import type { Notice } from '../../api/types'
const list = ref<Notice[]>([]), err = ref('')
onMounted(async () => { try { list.value = await api.notices() } catch (e) { err.value = String(e) } })
</script>

<style scoped>
.page { padding: 24rpx; display: flex; flex-direction: column; gap: 18rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; }
.row { background: #fff; border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; }
.h1 { font-size: 40rpx; font-weight: 700; }
.sub, .hint, .meta { font-size: 24rpx; color: #6b7280; }
.section { font-size: 30rpx; font-weight: 600; margin-top: 12rpx; }
.btn { background: #3b6cf6; color: #fff; border-radius: 12rpx; padding: 20rpx; text-align: center; }
.err { color: #d03050; font-size: 26rpx; }
</style>

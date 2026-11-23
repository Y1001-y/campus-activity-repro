<template>
  <view class="page">
    <text class="h1">我的报名</text>
    <text v-if="err" class="err">{{ err }}</text>
    <view v-for="s in list" :key="s.id" class="row">
      <text class="h1">{{ s.title }}</text>
      <text class="meta">{{ s.place }} · {{ s.start_time }}</text>
      <text class="hint">状态 {{ s.status }} · {{ s.created_at }}</text>
      <view class="btn" @tap="cancel(s.id)"><text>取消报名</text></view>
    </view>
    <text v-if="!list.length && !err" class="hint">暂无报名记录</text>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '../../api/modules'
import type { Signup } from '../../api/types'
const list = ref<Signup[]>([]), err = ref('')
async function load() { try { list.value = await api.mySignups() } catch (e) { err.value = String(e) } }
async function cancel(id: number) { try { await api.cancelSignup(id); await load() } catch (e) { err.value = String(e) } }
onMounted(load)
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

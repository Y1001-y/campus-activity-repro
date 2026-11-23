<template>
  <view class="page">
    <text class="h1">{{ a?.title || '活动详情' }}</text>
    <text v-if="err" class="err">{{ err }}</text>
    <view v-if="a" class="card">
      <text class="meta">{{ a.categoryName }} · {{ a.place }}</text>
      <text class="meta">时间：{{ a.start_time }} ~ {{ a.end_time }}</text>
      <text class="meta">报名截止：{{ a.signup_deadline }}</text>
      <text class="meta">名额：{{ a.signedUp ?? a.signed_up }} / {{ a.capacity }}</text>
      <text class="hint">{{ a.description }}</text>
    </view>
    <view class="btn" @tap="goSignup"><text>立即报名</text></view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { api } from '../../api/modules'
import type { Activity } from '../../api/types'
const a = ref<Activity | null>(null), err = ref(''), id = ref(0)
onLoad((q) => { id.value = Number(q?.id || 0) })
onMounted(async () => {
  try { a.value = await api.activity(id.value) } catch (e) { err.value = String(e) }
})
function goSignup() { uni.navigateTo({ url: '/pages/activity/signup?id=' + id.value }) }
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

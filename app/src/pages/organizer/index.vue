<template>
  <view class="page">
    <text class="h1">组织者工作台</text>
    <text class="hint">真实接口 GET /api/admin/dashboard（需 ORGANIZER/ADMIN 令牌）</text>
    <text v-if="err" class="err">{{ err }}</text>
    <view v-for="c in cards" :key="c.label" class="card">
      <text class="meta">{{ c.label }}</text><text class="h1">{{ c.value }}</text>
    </view>
    <view class="btn" @tap="go('/pages/activity/list')"><text>浏览活动</text></view>
    <view class="btn" @tap="go('/pages/activity/publish')"><text>发布活动（含封面上传）</text></view>
    <view class="btn" @tap="go('/pages/activity/signup-list')"><text>报名名单与签到核销</text></view>
  </view>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { api } from '../../api/modules'
import type { Dashboard } from '../../api/types'
const d = ref<Dashboard | null>(null), err = ref('')
const cards = computed(() => d.value ? [
  { label: '活动总数', value: d.value.activityCount },
  { label: '待审核', value: d.value.pendingAudit },
  { label: '报名数', value: d.value.signupCount },
  { label: '签到数', value: d.value.checkinCount }
] : [])
onMounted(async () => { try { d.value = await api.dashboard() } catch (e) { err.value = String(e) } })
function go(url: string) { uni.navigateTo({ url }) }
</script>

<style scoped>
.page { padding: 24rpx; display: flex; flex-direction: column; gap: 18rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; }
.row { background: #fff; border-radius: 16rpx; padding: 24rpx; }
.h1 { font-size: 36rpx; font-weight: 700; } .meta, .hint { font-size: 24rpx; color: #6b7280; }
.btn { background: #3b6cf6; color: #fff; border-radius: 12rpx; padding: 20rpx; text-align: center; }
.input { background: #fff; border-radius: 12rpx; padding: 18rpx; font-size: 26rpx; }
.err { color: #d03050; font-size: 26rpx; }
.ok { color: #18794e; font-size: 26rpx; }
</style>

<template>
  <view class="page">
    <text class="h1">报名名单与签到核销</text>
    <text class="hint">真实接口 GET /api/admin/signups · POST /api/admin/checkin/verify（需 ORGANIZER/ADMIN）</text>

    <view class="card">
      <text class="label">按活动筛选</text>
      <picker mode="selector" :range="activityNames" @change="onActivity">
        <view class="input picker">{{ activityNames[activityIndex] || '全部活动' }}</view>
      </picker>
    </view>

    <text v-if="err" class="err">{{ err }}</text>
    <text v-if="ok" class="ok">{{ ok }}</text>

    <view v-for="s in rows" :key="s.id" class="row">
      <text class="title">{{ s.title || ('活动 ' + (s.activity_id || s.activityId)) }}</text>
      <text class="meta">报名号 {{ s.id }} · 用户 {{ s.user_id }} · {{ s.real_name || '—' }} · {{ s.student_no || '未绑定学号' }} · {{ s.created_at }}</text>
      <view class="btn tiny" @tap="verify(s)"><text>核销签到</text></view>
    </view>
    <view v-if="!rows.length && !err" class="hint">暂无报名记录</view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '../../api/modules'
import { BizError } from '../../api/request'
import type { Signup, Activity } from '../../api/types'

const activities = ref<Activity[]>([])
const activityNames = ref<string[]>(['全部活动'])
const activityIndex = ref(0)
const rows = ref<Signup[]>([])
const err = ref(''), ok = ref('')

function onActivity(e: { detail: { value: number } }) {
  activityIndex.value = Number(e.detail.value)
  load()
}

async function load() {
  ok.value = ''; err.value = ''
  try {
    const picked = activities.value[activityIndex.value - 1]
    rows.value = await api.adminSignups(picked ? picked.id : undefined)
  } catch (e) { err.value = e instanceof BizError ? `${e.code}：${e.message}` : String(e) }
}

async function verify(s: Signup) {
  ok.value = ''; err.value = ''
  const activityId = s.activity_id || s.activityId
  if (!activityId || !s.user_id) { err.value = '记录缺少活动ID或用户ID'; return }
  try {
    const res = await api.verifyCheckin(activityId, s.user_id)
    ok.value = `核销成功：活动 ${res.activityId} 用户 ${res.userId} verified=${res.verified}`
  } catch (e) {
    err.value = e instanceof BizError
      ? `${e.code}：${e.message}${e.code === 40002 ? '（该用户尚未签到，请先在签到页用签到码 CK-2026-101 签到）' : ''}`
      : String(e)
  }
}

onMounted(async () => {
  try {
    activities.value = await api.adminActivities()
    activityNames.value = ['全部活动', ...activities.value.map((a) => `${a.id} ${a.title}`)]
  } catch (e) { err.value = String(e) }
  load()
})
</script>

<style scoped>
.page { padding: 24rpx; display: flex; flex-direction: column; gap: 16rpx; }
.card, .row { background: #fff; border-radius: 16rpx; padding: 22rpx; display: flex; flex-direction: column; gap: 8rpx; }
.h1 { font-size: 36rpx; font-weight: 700; }
.hint, .meta { font-size: 24rpx; color: #6b7280; }
.label { font-size: 26rpx; color: #374151; }
.input { background: #f8f9fc; border-radius: 12rpx; padding: 18rpx; font-size: 26rpx; }
.picker { color: #333; }
.title { font-size: 28rpx; font-weight: 600; }
.btn { background: #3b6cf6; color: #fff; border-radius: 10rpx; padding: 14rpx; text-align: center; }
.btn.tiny { font-size: 24rpx; }
.err { color: #d03050; font-size: 26rpx; }
.ok { color: #18794e; font-size: 26rpx; }
</style>

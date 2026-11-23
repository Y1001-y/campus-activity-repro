<template>
  <view class="card" @tap="open">
    <text class="h1">{{ activity.title }}</text>
    <text class="meta">{{ activity.categoryName }} · {{ activity.place }} · {{ activity.start_time }}</text>
    <text class="hint">已报名 {{ activity.signed_up }}/{{ activity.capacity }} · {{ statusText }}</text>
  </view>
</template>

<script setup lang="ts">
import { STATUS_TEXT } from '../../utils/format'
import type { Activity } from '../../api/types'
const props = defineProps<{ activity: Activity }>()
const statusText = STATUS_TEXT[props.activity.status] || props.activity.status
function open() { uni.navigateTo({ url: '/pages/activity/detail?id=' + props.activity.id }) }
</script>

<style scoped>
.card { display: flex; flex-direction: column; gap: 10rpx; background: #fff; border-radius: 16rpx; padding: 24rpx; }
.h1 { font-size: 30rpx; font-weight: 600; } .meta, .hint { font-size: 24rpx; color: #6b7280; }
</style>

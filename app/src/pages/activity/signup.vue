<template>
  <view class="page">
    <text class="h1">活动报名</text>
    <text class="sub">活动 ID：{{ id }}</text>
    <view class="btn" @tap="submit"><text>提交报名</text></view>
    <text v-if="msg" class="hint">{{ msg }}</text>
    <text v-if="err" class="err">{{ err }}</text>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { api } from '../../api/modules'
import { messageOf } from '../../api/errorText'
const id = ref(0), msg = ref(''), err = ref('')
onLoad((q) => { id.value = Number(q?.id || 0) })
async function submit() {
  msg.value = ''; err.value = ''
  try {
    const r = await api.signup(id.value)
    msg.value = '报名成功，记录号 ' + r.signupId
  } catch (e) { err.value = String(e) }
}
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

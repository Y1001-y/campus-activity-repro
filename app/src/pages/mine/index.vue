<template>
  <view class="page">
    <text class="h1">我的</text>
    <view class="card"><text class="meta">{{ user.realName || '未登录' }} · {{ user.role || '-' }}</text></view>
    <view class="btn" @tap="go('/pages/mine/signups')"><text>我的报名</text></view>
    <view class="btn" @tap="go('/pages/mine/messages')"><text>消息通知</text></view>
    <view class="btn" @tap="go('/pages/auth/login')"><text>去登录</text></view>
    <view class="btn" @tap="logout"><text>退出登录</text></view>
    <text v-if="msg" class="hint">{{ msg }}</text>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '../../api/modules'
import { clearToken } from '../../api/request'
const user = ref<Record<string, string>>({}), msg = ref('')
onMounted(() => { const u = uni.getStorageSync('campus_user'); if (u) user.value = u })
function go(url: string) { uni.navigateTo({ url }) }
async function logout() {
  try { await api.logout() } catch (e) { /* 令牌失效也照样清本地 */ }
  clearToken(); uni.removeStorageSync('campus_user'); user.value = {}; msg.value = '已退出登录'
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

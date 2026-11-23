<template>
  <view class="page">
    <text class="h1">绑定学号</text>
    <text class="sub">先用手机号取验证码，再绑定学号</text>
    <input class="input" v-model="phone" placeholder="手机号" />
    <view class="btn" @tap="send"><text>获取验证码</text></view>
    <input class="input" v-model="studentNo" placeholder="学号" />
    <input class="input" v-model="code" placeholder="验证码" />
    <view class="btn" @tap="bind"><text>提交绑定</text></view>
    <text v-if="msg" class="hint">{{ msg }}</text>
    <text v-if="err" class="err">{{ err }}</text>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { api } from '../../api/modules'
const phone = ref(''), studentNo = ref(''), code = ref(''), msg = ref(''), err = ref('')
async function send() {
  try { const r = await api.sendSms(phone.value); msg.value = '验证码：' + r.code }
  catch (e) { err.value = String(e) }
}
async function bind() {
  try { const u = await api.bind(studentNo.value, phone.value, code.value); msg.value = '已绑定：' + u.realName }
  catch (e) { err.value = String(e) }
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
.input { background: #fff; border-radius: 12rpx; padding: 18rpx; font-size: 26rpx; }
</style>

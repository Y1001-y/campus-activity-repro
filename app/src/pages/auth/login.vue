<template>
  <view class="page">
    <text class="h1">登录 / 学号绑定</text>
    <input class="input" v-model="username" placeholder="账号（如 2024001001）" />
    <input class="input" v-model="password" placeholder="密码（如 stu123）" type="password" />
    <view class="btn" @tap="login"><text>账号登录</text></view>
    <input class="input" v-model="phone" placeholder="手机号（验证码登录）" />
    <view class="btn" @tap="send"><text>获取验证码</text></view>
    <input class="input" v-model="studentNo" placeholder="学号" />
    <input class="input" v-model="code" placeholder="验证码" />
    <view class="btn" @tap="bind"><text>绑定学号</text></view>
    <text v-if="msg" class="hint">{{ msg }}</text>
    <text v-if="err" class="err">{{ err }}</text>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { api } from '../../api/modules'
import { setToken } from '../../api/request'
const username = ref(''), password = ref(''), phone = ref(''), studentNo = ref(''), code = ref('')
const msg = ref(''), err = ref('')
async function login() {
  err.value = ''
  try {
    const r = await api.login(username.value, password.value)
    setToken(r.token); uni.setStorageSync('campus_user', r.user)
    msg.value = '登录成功：' + r.user.realName + '（' + r.user.role + '）'
  } catch (e) { err.value = String(e) }
}
async function send() {
  err.value = ''
  try { const r = await api.sendSms(phone.value); msg.value = '本机验证码：' + r.code }
  catch (e) { err.value = String(e) }
}
async function bind() {
  err.value = ''
  try { const u = await api.bind(studentNo.value, phone.value, code.value); msg.value = '绑定成功：' + u.realName }
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

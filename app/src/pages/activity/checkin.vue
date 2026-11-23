<template>
  <view class="page">
    <text class="h1">扫码签到</text>
    <text class="hint">真实接口 POST /api/checkin；签到码见「我的报名」</text>
    <input class="input" v-model="activityId" placeholder="活动 ID（如 101）" />
    <input class="input" v-model="code" placeholder="签到码（如 CK-2026-101）" />
    <view class="btn" @tap="submit"><text>提交签到</text></view>
    <text v-if="ok" class="ok">{{ ok }}</text>
    <text v-if="err" class="err">{{ err }}</text>
    <view class="card"><text class="hint">错误分支：40001 签到码无效 / 40002 未报名 / 40003 重复签到 / 40004 不在签到时间</text></view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { api } from '../../api/modules'
import { messageOf } from '../../api/errorText'
const activityId = ref(''), code = ref(''), ok = ref(''), err = ref('')
async function submit() {
  ok.value = ''; err.value = ''
  try {
    const r = await api.checkin(Number(activityId.value), code.value)
    ok.value = r.message + ' @ ' + r.checkedInAt
  } catch (e) {
    const m = /^(\d+)/.exec(e instanceof Error ? e.message : String(e))
    err.value = m ? messageOf(Number(m[1]), String(e)) : String(e)
  }
}
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

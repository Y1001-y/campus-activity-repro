<template>
  <view class="page">
    <text class="h1">发布活动</text>
    <text class="hint">真实接口 POST /api/admin/activities · 封面 POST /api/upload（需 ORGANIZER/ADMIN）</text>

    <text v-if="err" class="err">{{ err }}</text>
    <text v-if="ok" class="ok">{{ ok }}</text>

    <view class="card">
      <text class="label">活动标题</text>
      <input class="input" v-model="form.title" placeholder="例如：前端工程化实践工作坊" />
      <text class="label">分类</text>
      <picker mode="selector" :range="categoryNames" @change="onCategory">
        <view class="input picker">{{ categoryNames[categoryIndex] || '请选择分类' }}</view>
      </picker>
      <text class="label">地点</text>
      <input class="input" v-model="form.place" placeholder="例如：实训楼 A302" />
      <text class="label">开始时间</text>
      <input class="input" v-model="form.startTime" placeholder="2026-11-28 14:00" />
      <text class="label">结束时间</text>
      <input class="input" v-model="form.endTime" placeholder="2026-11-28 17:00" />
      <text class="label">报名截止</text>
      <input class="input" v-model="form.signupDeadline" placeholder="2026-11-27 18:00" />
      <text class="label">容量</text>
      <input class="input" type="number" v-model="form.capacity" placeholder="30" />
      <text class="label">简介</text>
      <input class="input" v-model="form.description" placeholder="一句话介绍" />
    </view>

    <view class="card">
      <text class="label">封面素材（jpg/jpeg/png/webp，≤5MB）</text>
      <view class="btn ghost" @tap="chooseCover"><text>{{ cover ? '重新选择封面' : '选择封面图片' }}</text></view>
      <image v-if="coverUrl" class="cover" :src="coverUrl" mode="aspectFill" />
      <text v-if="cover" class="meta">已上传：{{ cover.name }}（{{ cover.size }} 字节）</text>
    </view>

    <view class="btn" @tap="submit"><text>提交发布</text></view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '../../api/modules'
import { upload, assetUrl, BizError } from '../../api/request'
import type { Category, UploadResult } from '../../api/types'

const form = ref({
  title: '', place: '', startTime: '', endTime: '', signupDeadline: '',
  capacity: '30', description: ''
})
const categories = ref<Category[]>([])
const categoryNames = ref<string[]>([])
const categoryIndex = ref(0)
const cover = ref<UploadResult | null>(null)
const coverUrl = ref('')
const err = ref(''), ok = ref('')

const ALLOWED = ['jpg', 'jpeg', 'png', 'webp']
const MAX = 5 * 1024 * 1024

function onCategory(e: { detail: { value: number } }) { categoryIndex.value = Number(e.detail.value) }

function chooseCover() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    success: async (res) => {
      const path = res.tempFilePaths[0]
      const ext = path.split('.').pop()?.toLowerCase() || ''
      if (!ALLOWED.includes(ext)) { err.value = '仅支持 jpg / jpeg / png / webp 格式'; return }
      const files = Array.isArray(res.tempFiles) ? res.tempFiles : [res.tempFiles]
      const size = (files[0] as { size?: number } | undefined)?.size || 0
      if (size > MAX) { err.value = '图片不得超过 5MB'; return }
      try {
        err.value = ''
        cover.value = await upload(path)
        coverUrl.value = assetUrl(cover.value.url)
      } catch (e) { err.value = e instanceof BizError ? `${e.code}：${e.message}` : String(e) }
    },
    fail: () => { err.value = '未选择图片' }
  })
}

async function submit() {
  ok.value = ''; err.value = ''
  const cat = categories.value[categoryIndex.value]
  if (!form.value.title.trim() || !cat || Number(form.value.capacity) <= 0) {
    err.value = '请填写标题、选择分类，容量需大于 0（本地校验）'; return
  }
  try {
    const res = await api.publishActivity({
      title: form.value.title.trim(),
      categoryId: cat.id,
      cover: cover.value ? cover.value.url : '',
      place: form.value.place,
      startTime: form.value.startTime,
      endTime: form.value.endTime,
      signupDeadline: form.value.signupDeadline,
      capacity: Number(form.value.capacity),
      description: form.value.description
    })
    ok.value = `发布成功：活动 ${res.id}，签到码 ${res.checkinCode}，审核状态 ${res.auditStatus}`
  } catch (e) {
    err.value = e instanceof BizError
      ? `${e.code}：${e.message}${e.code === 10002 ? '（请用 organizer / org123 登录）' : ''}`
      : String(e)
  }
}

onMounted(async () => {
  try {
    categories.value = await api.categories()
    categoryNames.value = categories.value.map((c) => c.name)
  } catch (e) { err.value = String(e) }
})
</script>

<style scoped>
.page { padding: 24rpx; display: flex; flex-direction: column; gap: 18rpx; }
.card { background: #fff; border-radius: 16rpx; padding: 24rpx; display: flex; flex-direction: column; gap: 10rpx; }
.h1 { font-size: 36rpx; font-weight: 700; }
.hint, .meta { font-size: 24rpx; color: #6b7280; }
.label { font-size: 26rpx; color: #374151; margin-top: 8rpx; }
.input { background: #f8f9fc; border-radius: 12rpx; padding: 18rpx; font-size: 26rpx; }
.picker { color: #333; }
.btn { background: #3b6cf6; color: #fff; border-radius: 12rpx; padding: 20rpx; text-align: center; }
.btn.ghost { background: #eaf0ff; color: #3b6cf6; }
.cover { width: 100%; height: 300rpx; border-radius: 12rpx; margin-top: 10rpx; }
.err { color: #d03050; font-size: 26rpx; }
.ok { color: #18794e; font-size: 26rpx; }
</style>

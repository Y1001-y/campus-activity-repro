<template>
  <view class="page">
    <text class="h1">校园活动</text>
    <text class="sub">数据来自真实后端 8016 /api</text>
    <view class="modules">
      <view class="module" @tap="go('/pages/activity/categories')"><text>活动分类</text></view>
      <view class="module" @tap="go('/pages/activity/hot')"><text>热门活动</text></view>
      <view class="module" @tap="go('/pages/activity/upcoming')"><text>即将开始</text></view>
    </view>
    <text class="section">热门活动</text>
    <text v-if="err" class="err">{{ err }}</text>
    <ActivityCard v-for="a in hot" :key="a.id" :activity="a" />
    <text class="section">全部分类</text>
    <view class="cats"><text v-for="c in cats" :key="c.id" class="cat">{{ c.name }}</text></view>
    <view class="btn" @tap="go('/pages/activity/list')"><text>查看全部活动</text></view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '../../api/modules'
import type { Activity, Category } from '../../api/types'
import ActivityCard from '../../components/ActivityCard.vue'
const hot = ref<Activity[]>([])
const cats = ref<Category[]>([])
const err = ref('')
onMounted(async () => {
  try {
    const [c, a] = await Promise.all([api.categories(), api.activities({ page: 1, size: 5 })])
    cats.value = c
    hot.value = a.list
  } catch (e) { err.value = '加载失败：' + String(e) }
})
function go(url: string) { uni.navigateTo({ url }) }
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
.modules { display: flex; gap: 16rpx; }
.module { flex: 1; background: #e8efff; color: #2b55c8; border-radius: 16rpx; padding: 28rpx 12rpx; text-align: center; font-size: 26rpx; }
.cats { display: flex; flex-wrap: wrap; gap: 12rpx; }
.cat { background: #fff; border-radius: 999rpx; padding: 10rpx 24rpx; font-size: 24rpx; }
</style>

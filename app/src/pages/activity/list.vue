<template>
  <view class="page">
    <text class="h1">活动列表</text>
    <input class="search" v-model="keyword" placeholder="搜索活动标题" @confirm="load" />
    <view class="btn" @tap="load"><text>搜索</text></view>
    <text v-if="err" class="err">{{ err }}</text>
    <ActivityCard v-for="a in list" :key="a.id" :activity="a" />
    <text class="hint">共 {{ total }} 条（真实分页）</text>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '../../api/modules'
import type { Activity } from '../../api/types'
import ActivityCard from '../../components/ActivityCard.vue'
const list = ref<Activity[]>([]), total = ref(0), keyword = ref(''), err = ref('')
async function load() {
  try {
    const r = await api.activities({ page: 1, size: 20, keyword: keyword.value })
    list.value = r.list; total.value = r.total
  } catch (e) { err.value = String(e) }
}
onMounted(load)
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
.search { background: #fff; border-radius: 12rpx; padding: 18rpx; font-size: 26rpx; }
</style>

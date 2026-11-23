<template>
<span class="tag" :class="status">{{ text }}</span>
</template>

<script setup lang="ts">
import { STATUS_TEXT } from '../api/dict'
const props = defineProps<{ status: string }>()
const text = STATUS_TEXT[props.status] || props.status
</script>

<style scoped>
.tag { display: inline-block; padding: 3px 10px; border-radius: 999px; font-size: 12px; background: #eef1f7; color: #4b5563; }
.tag.PUBLISHED, .tag.APPROVED { background: #e5f6ea; color: #18794e; }
.tag.PENDING { background: #fff4dd; color: #a15c00; }
.tag.REJECTED { background: #fdeaea; color: #b42318; }
</style>

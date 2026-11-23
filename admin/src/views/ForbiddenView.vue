<template>
<div class="page"><h1 class="h1">403 · 无权限</h1><p class="hint">当前账号不能访问该页面。</p><router-link to="/">返回工作台</router-link></div>
</template>

<script setup lang="ts">

</script>

<style scoped>
.page { padding: 24px; }
.h1 { font-size: 22px; font-weight: 700; margin: 0 0 16px; }
.cards { display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 20px; }
.card { background: #fff; border-radius: 12px; padding: 20px; min-width: 150px; }
.grid { width: 100%; border-collapse: collapse; background: #fff; border-radius: 12px; overflow: hidden; }
.grid th, .grid td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #eef0f4; font-size: 14px; }
.btn { background: #3b6cf6; color: #fff; border: 0; border-radius: 8px; padding: 8px 16px; margin-right: 8px; }
.btn.gray { background: #e8ebf2; color: #333; }
.err { color: #d03050; } .hint { color: #6b7280; font-size: 13px; }
</style>

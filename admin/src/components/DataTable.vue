<template>
  <table class="grid">
    <thead><tr><th v-for="c in columns" :key="c.key">{{ c.label }}</th></tr></thead>
    <tbody><tr v-for="(row, i) in rows" :key="i"><td v-for="c in columns" :key="c.key">{{ row[c.key] }}</td></tr></tbody>
  </table>
</template>

<script setup lang="ts">
defineProps<{ columns: { key: string; label: string }[]; rows: Record<string, unknown>[] }>()
</script>

<style scoped>
.grid { width: 100%; border-collapse: collapse; background: #fff; border-radius: 12px; overflow: hidden; }
.grid th, .grid td { padding: 12px 16px; text-align: left; border-bottom: 1px solid #eef0f4; font-size: 14px; }
</style>

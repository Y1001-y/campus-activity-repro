<template>
  <div class="ph">
    <div><h1>{{ title }}</h1><p v-if="subtitle">{{ subtitle }}</p></div>
    <div><slot name="actions" /></div>
  </div>
</template>

<script setup lang="ts">
defineProps<{ title: string; subtitle?: string }>()
</script>

<style scoped>
.ph { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.ph h1 { margin: 0; font-size: 22px; } .ph p { margin: 4px 0 0; color: #6b7280; font-size: 13px; }
</style>

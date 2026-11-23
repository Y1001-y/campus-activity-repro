<template>
  <div class="page">
    <PageHeader title="素材上传" subtitle="真实接口 POST /api/upload · GET /api/uploads">
      <template #actions><button class="btn" @click="load">刷新列表</button></template>
    </PageHeader>

    <div class="panel">
      <label class="label">选择图片（jpg/jpeg/png/webp，≤5MB）</label>
      <input type="file" accept=".jpg,.jpeg,.png,.webp" @change="onFile" />
      <p v-if="localErr" class="err">{{ localErr }}</p>
      <p v-if="ok" class="ok">{{ ok }}</p>
      <p class="hint">前端先校验格式与大小；后端 FileController 再次校验（双端一致）。</p>
    </div>

    <div class="panel">
      <h3>已上传素材（真实读取 uploads 目录）</h3>
      <p v-if="err" class="err">{{ err }}</p>
      <div class="thumbs">
        <div v-for="name in names" :key="name" class="thumb">
          <img :src="`/uploads/${name}`" :alt="name" />
          <span>{{ name }}</span>
        </div>
        <p v-if="!names.length && !err" class="hint">暂无素材，先上传一张图片。</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { api } from '../api/modules'
import { upload, BizError } from '../api/request'
import PageHeader from '../components/PageHeader.vue'

const names = ref<string[]>([])
const ok = ref(''), err = ref(''), localErr = ref('')
const ALLOWED = ['jpg', 'jpeg', 'png', 'webp']
const MAX = 5 * 1024 * 1024

async function onFile(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  localErr.value = ''; ok.value = ''
  if (!file) return
  const ext = file.name.split('.').pop()?.toLowerCase() || ''
  if (!ALLOWED.includes(ext)) { localErr.value = '仅支持 jpg / jpeg / png / webp 格式'; return }
  if (file.size > MAX) { localErr.value = '图片不得超过 5MB'; return }
  try {
    const res = await upload(file)
    ok.value = `上传成功：${res.name}（${res.size} 字节），访问地址 ${res.url}`
    await load()
  } catch (e) { localErr.value = e instanceof BizError ? `${e.code}：${e.message}` : String(e) }
}

async function load() {
  err.value = ''
  try { names.value = await api.uploads() } catch (e) { err.value = String(e) }
}
onMounted(load)
</script>

<style scoped>
.page { padding: 24px; display: flex; flex-direction: column; gap: 16px; }
.panel { background: #fff; border-radius: 12px; padding: 20px; }
.label { display: block; font-size: 14px; margin-bottom: 10px; color: #374151; }
.hint { color: #6b7280; font-size: 13px; }
.err { color: #d03050; font-size: 14px; }
.ok { color: #18794e; font-size: 14px; }
.btn { background: #3b6cf6; color: #fff; border: 0; border-radius: 8px; padding: 8px 16px; }
.thumbs { display: flex; flex-wrap: wrap; gap: 16px; }
.thumb { width: 140px; display: flex; flex-direction: column; gap: 6px; font-size: 12px; }
.thumb img { width: 140px; height: 100px; object-fit: cover; border-radius: 8px; background: #f0f2f6; }
</style>

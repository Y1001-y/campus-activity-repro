import { defineConfig } from 'vite'
import uniPlugin from '@dcloudio/vite-plugin-uni'

// CJS/ESM 互操作：ESM 加载时 default 是 { default, runDev, ... }，真正插件在 default.default
const uni = (typeof uniPlugin === 'function' ? uniPlugin : (uniPlugin as unknown as { default: typeof uniPlugin }).default)

export default defineConfig({
  plugins: [uni()],
  base: process.env.VITE_BASE || '/',
  server: {
    port: 5174,
    proxy: {
      '/api': { target: 'http://127.0.0.1:8016', changeOrigin: true },
      '/uploads': { target: 'http://127.0.0.1:8016', changeOrigin: true }
    }
  }
})

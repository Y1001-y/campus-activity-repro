import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

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

import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api/sri': 'http://localhost:8081', // ms-sri
      '/api/ant': 'http://localhost:8082', // ms-ant
    },
  },
})
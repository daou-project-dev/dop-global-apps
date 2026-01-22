import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import fs from 'fs'
import path from 'path'

// mkcert 인증서 경로
const certsDir = path.resolve(__dirname, '../../certs')
const keyPath = path.join(certsDir, 'localhost-key.pem')
const certPath = path.join(certsDir, 'localhost.pem')

// 인증서 존재 여부 확인
const httpsConfig = fs.existsSync(keyPath) && fs.existsSync(certPath)
  ? {
      key: fs.readFileSync(keyPath),
      cert: fs.readFileSync(certPath),
    }
  : undefined

// https://vite.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    https: httpsConfig,
    proxy: {
      '/api': {
        target: 'https://localhost:8443',
        changeOrigin: true,
        secure: false,
      },
    },
  },
})

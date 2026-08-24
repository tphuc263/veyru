import {defineConfig} from 'vitest/config'
import react from '@vitejs/plugin-react-swc'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
  },
  test: {
    exclude: ['e2e/**', 'node_modules/**'],
  },
})

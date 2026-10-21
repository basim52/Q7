import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
// Single JS bundle keeps the static deploy to 3 files (index.html, one CSS, one JS).
export default defineConfig({ plugins: [react()], build: { chunkSizeWarningLimit: 1500, rollupOptions: { output: { inlineDynamicImports: true } } } })

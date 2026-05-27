import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import tailwindcss from '@tailwindcss/vite'
import path from "path"
import { visualizer } from 'rollup-plugin-visualizer'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    tailwindcss(),
    visualizer({ open: true, filename: 'dist/stats.html', gzipSize: true }),
  ],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks(id) {
          if (id.includes('node_modules/react') || id.includes('node_modules/react-dom') || id.includes('node_modules/react-router')) {
            return 'vendor-react';
          }
          if (id.includes('node_modules/@tanstack/react-query') || id.includes('node_modules/@tanstack/react-virtual')) {
            return 'vendor-query';
          }
          if (id.includes('node_modules/radix-ui') || id.includes('node_modules/lucide-react') || id.includes('node_modules/sonner')) {
            return 'vendor-ui';
          }
        },
      },
    },
  },
})

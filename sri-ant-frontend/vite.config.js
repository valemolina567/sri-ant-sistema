// vite.config.js
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 3000,
    proxy: {
      // En dev, redirige /sri, /vehiculos, /ant al API Gateway en 8080
      '/sri':       { target: 'http://localhost:8080', changeOrigin: true },
      '/vehiculos': { target: 'http://localhost:8080', changeOrigin: true },
      '/ant':       { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
});

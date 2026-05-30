import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import { defineConfig } from 'vite';

export default defineConfig({
    plugins: [react(), tailwindcss()],
    server: {
        port: 8090,
        host: '127.0.0.1',
        strictPort: true,
        proxy: {
            // Forward le chiamate API al backend Hono in dev.
            '/api/v2': {
                target: 'http://127.0.0.1:8092',
                changeOrigin: true,
                secure: false
            }
        }
    },
    preview: {
        port: 8090,
        host: '127.0.0.1',
        strictPort: true
    },
    resolve: {
        alias: {
            '@': '/src'
        }
    },
    build: {
        outDir: 'dist',
        sourcemap: true,
        target: 'es2022'
    }
});

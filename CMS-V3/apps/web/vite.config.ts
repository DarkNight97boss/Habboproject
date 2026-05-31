import react from '@vitejs/plugin-react';
import tailwindcss from '@tailwindcss/vite';
import { defineConfig } from 'vite';

export default defineConfig({
    plugins: [react(), tailwindcss()],
    server: {
        port: 8093,
        host: '127.0.0.1',
        strictPort: true,
        proxy: {
            // Forward le chiamate API al backend Hono in dev.
            '/api/v2': {
                target: 'http://127.0.0.1:8092',
                changeOrigin: true,
                secure: false
            },
            // Nitro V3 standalone (PHP -S su :8091) — montato sotto /client/*
            // così è SAME-ORIGIN della home (cookie cms_v3_access viene inviato
            // a Nitro per la chiamata SSO senza issues SameSite/CORS).
            '/client': {
                target: 'http://127.0.0.1:8091',
                changeOrigin: true,
                secure: false,
                rewrite: path => path.replace(/^\/client/, '') || '/'
            },
            // Nitro chiede a `${api.url}/api/auth/*` — con api.url="" risolve
            // a /api/auth/* relative all'origin corrente :8093. Lo rinviamo
            // alla CMS-V3 API riscrivendo /api/auth/* → /api/v2/auth/*.
            '/api/auth': {
                target: 'http://127.0.0.1:8092',
                changeOrigin: true,
                secure: false,
                rewrite: path => path.replace(/^\/api\/auth/, '/api/v2/auth')
            },
            // Asset bundle Nitro + game data: ancora serviti dal vecchio CMS
            // PHP su :8080/react/* (non duplichiamo nel CMS-V3).
            '/react': {
                target: 'http://127.0.0.1:8080',
                changeOrigin: true,
                secure: false
            }
        }
    },
    preview: {
        port: 8093,
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

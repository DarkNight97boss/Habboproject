import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Route, Routes } from 'react-router';
import { AppShell } from './components/AppShell';
import { HomePage } from './routes/HomePage';
import './styles/global.css';

const queryClient = new QueryClient({
    defaultOptions: {
        queries: {
            staleTime: 30_000,
            retry: 1,
            refetchOnWindowFocus: false
        }
    }
});

const root = document.getElementById('root');
if(!root) throw new Error('root element missing');

createRoot(root).render(
    <StrictMode>
        <QueryClientProvider client={queryClient}>
            <BrowserRouter>
                <Routes>
                    <Route element={<AppShell />}>
                        <Route path="/" element={<HomePage />} />
                        <Route path="/community" element={<Placeholder name="Community" />} />
                        <Route path="/shop" element={<Placeholder name="Shop" />} />
                        <Route path="/world" element={<Placeholder name="Il Mondo di Habbo" />} />
                        <Route path="/collectibles" element={<Placeholder name="Collezionabili" />} />
                        <Route path="*" element={<NotFound />} />
                    </Route>
                </Routes>
            </BrowserRouter>
            {import.meta.env.DEV ? <ReactQueryDevtools initialIsOpen={false} /> : null}
        </QueryClientProvider>
    </StrictMode>
);

function Placeholder({ name }: { name: string }): JSX.Element
{
    return (
        <div className="container mx-auto px-4 py-12 text-center">
            <h1 className="text-white text-4xl font-bold uppercase mb-3">{name}</h1>
            <p className="text-habbo-page-text">Pagina in costruzione — Fase 2 del refactor CMS-V3.</p>
        </div>
    );
}

function NotFound(): JSX.Element
{
    return (
        <div className="container mx-auto px-4 py-12 text-center">
            <h1 className="text-white text-3xl font-bold uppercase mb-2">OH BOBBA! Pagina non trovata.</h1>
            <p className="text-habbo-page-text">
                Frank non ha trovato la pagina che stai cercando. Verifica l'URL o torna alla{' '}
                <a href="/" className="font-bold underline">Homepage</a>.
            </p>
        </div>
    );
}

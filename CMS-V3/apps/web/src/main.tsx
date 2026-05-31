import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { StrictMode, type ReactNode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router';
import { CommunityForumPage } from './routes/CommunityForumPage';
import { CommunityNewsPage } from './routes/CommunityNewsPage';
import { CommunityPhotosPage } from './routes/CommunityPhotosPage';
import { CommunityRoomsPage } from './routes/CommunityRoomsPage';
import { HomePage } from './routes/HomePage';
import { MessagingPage } from './routes/MessagingPage';
import { RegistrationPage } from './routes/RegistrationPage';

// NB: il CSS ufficiale habbo.it (app.a8ea7435.css) è caricato direttamente
// in index.html via <link>. Niente Tailwind/global.css qui — sarebbero
// override imprevedibili sopra la CSS ufficiale.

const queryClient = new QueryClient({
    defaultOptions: {
        queries: { staleTime: 30_000, retry: 1, refetchOnWindowFocus: false }
    }
});

const root = document.getElementById('root');
if(!root) throw new Error('root element missing');

createRoot(root).render(
    <StrictMode>
        <QueryClientProvider client={queryClient}>
            <BrowserRouter>
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/registration" element={<RegistrationPage />} />
                    <Route path="/messaging" element={<MessagingPage />} />
                    <Route path="/community" element={<Navigate to="/community/photos" replace />} />
                    <Route path="/community/photos" element={<CommunityPhotosPage />} />
                    <Route path="/community/rooms" element={<CommunityRoomsPage />} />
                    <Route path="/community/forum" element={<CommunityForumPage />} />
                    <Route path="/community/category" element={<Navigate to="/community/category/all" replace />} />
                    <Route path="/community/category/:category" element={<CommunityNewsPage />} />
                    <Route path="/shop" element={<Placeholder name="Shop" />} />
                    <Route path="/playing-habbo" element={<Placeholder name="Il Mondo di Habbo" />} />
                    <Route path="/habbo-nft" element={<Placeholder name="Collezionabili" />} />
                    <Route path="*" element={<NotFound />} />
                </Routes>
            </BrowserRouter>
            <ReactQueryDevtools initialIsOpen={false} />
        </QueryClientProvider>
    </StrictMode>
);

function Placeholder({ name }: { name: string }): ReactNode
{
    return (
        <div className="wrapper wrapper--content">
            <h1>{name}</h1>
            <p>Pagina in costruzione.</p>
        </div>
    );
}

function NotFound(): ReactNode
{
    return (
        <div className="wrapper wrapper--content">
            <h1>OH BOBBA! Pagina non trovata.</h1>
            <p>Frank non ha trovato la pagina che stai cercando. <a href="/">Homepage</a></p>
        </div>
    );
}

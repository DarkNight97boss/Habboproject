import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import { ReactQueryDevtools } from '@tanstack/react-query-devtools';
import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router';
import { CollectiblesPage } from './routes/CollectiblesPage';
import { CommunityArticlePage } from './routes/CommunityArticlePage';
import { CommunityForumPage } from './routes/CommunityForumPage';
import { CommunityNewsPage } from './routes/CommunityNewsPage';
import { CommunityPhotosPage } from './routes/CommunityPhotosPage';
import { CommunityRoomsPage } from './routes/CommunityRoomsPage';
import { HomePage } from './routes/HomePage';
import { MessagingPage } from './routes/MessagingPage';
import { NotFoundPage } from './routes/NotFoundPage';
import { PlayingHabboPage } from './routes/PlayingHabboPage';
import { RegistrationPage } from './routes/RegistrationPage';
import { ShopPage } from './routes/ShopPage';
import { StaticInfoPage } from './routes/StaticInfoPage';

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
                    <Route path="/community/article/:slug" element={<CommunityArticlePage />} />
                    <Route path="/shop" element={<ShopPage />} />
                    <Route path="/playing-habbo" element={<PlayingHabboPage />} />
                    <Route path="/playing-habbo/:slug" element={<StaticInfoPage />} />
                    <Route path="/help/:slug" element={<StaticInfoPage />} />
                    <Route path="/habbo-nft" element={<CollectiblesPage />} />
                    <Route path="*" element={<NotFoundPage />} />
                </Routes>
            </BrowserRouter>
            <ReactQueryDevtools initialIsOpen={false} />
        </QueryClientProvider>
    </StrictMode>
);


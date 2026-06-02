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
import { HelpPage } from './routes/HelpPage';
import { HomePage } from './routes/HomePage';
import { MessagingPage } from './routes/MessagingPage';
import { NotFoundPage } from './routes/NotFoundPage';
import { PhotoDetailPage } from './routes/PhotoDetailPage';
import { PlayingHabboPage } from './routes/PlayingHabboPage';
import { ProfilePage } from './routes/ProfilePage';
import { RegistrationPage } from './routes/RegistrationPage';
import { SettingsPage } from './routes/SettingsPage';
import { ShopPage } from './routes/ShopPage';
import { StaffPanelPage } from './routes/StaffPanelPage';
import { ShopPrepaidPage } from './routes/ShopPrepaidPage';
import { ShopPurchasesPage } from './routes/ShopPurchasesPage';
import { StaticInfoPage } from './routes/StaticInfoPage';
import { AsteriaLeaderboardPage } from './asteria/AsteriaLeaderboardPage';
import { AsteriaLoginPage } from './asteria/AsteriaLoginPage';
import { AsteriaMePage } from './asteria/AsteriaMePage';
import { AsteriaRegistrationPage } from './asteria/AsteriaRegistrationPage';
import { AsteriaShopPage } from './asteria/AsteriaShopPage';
import { AsteriaShopCancelPage, AsteriaShopSuccessPage } from './asteria/AsteriaShopResultPages';
import { AsteriaArticlesPage } from './asteria/AsteriaArticlesPage';
import { AsteriaArticlePage } from './asteria/AsteriaArticlePage';
import { AsteriaProfilePage } from './asteria/AsteriaProfilePage';
import { AsteriaSettingsPage } from './asteria/AsteriaSettingsPage';
import { AsteriaPhotosPage } from './asteria/AsteriaPhotosPage';
import { AsteriaRoomsPage } from './asteria/AsteriaRoomsPage';
import { AsteriaPhotoDetailPage } from './asteria/AsteriaPhotoDetailPage';
import { AsteriaPurchasesPage } from './asteria/AsteriaPurchasesPage';
import { AsteriaGuidePage, AsteriaHelpPage, AsteriaCollectiblesPage, AsteriaStaticInfoPage, AsteriaMessagingPage, AsteriaForumPage, AsteriaPrepaidPage } from './asteria/AsteriaContentPages';
import { hydrateSkin } from './lib/skin';
import { useSkin } from './hooks/useSkin';
import { type ReactNode } from 'react';
import './styles/skin-engine.css';

// Asteria: suite pagine completa (profilo, settings, community, shop, guida, info).
// NB: il CSS ufficiale habbo.it (app.a8ea7435.css) è caricato direttamente
// in index.html via <link>. Niente Tailwind/global.css qui — sarebbero
// override imprevedibili sopra la CSS ufficiale.
//
// ECCEZIONE: skin-engine.css (v2, consuma CSS vars scritte da applySkin a
// runtime). Hydratiamo lo skin cached PRIMA del render React per evitare FOUC.
hydrateSkin();

const queryClient = new QueryClient({
    defaultOptions: {
        queries: { staleTime: 30_000, retry: 1, refetchOnWindowFocus: false }
    }
});

const root = document.getElementById('root');
if(!root) throw new Error('root element missing');

/**
 * Watcher headless: chiama useSkin() (React Query) per fetchare lo skin
 * attivo server-side ogni 60s e applicarlo al DOM. Senza questo, il cached
 * resterebbe applicato all'infinito e non vedrebbe i cambi staff.
 */
function ThemeApplier(): ReactNode
{
    useSkin();
    return null;
}

/**
 * Switch skin-aware: se lo skin attivo è 'asteria-nebula' renderizza il
 * componente proprietario Asteria, altrimenti quello classic (habbo-style).
 * Usato in main routing per /login, /registration, /me, /shop.
 */
function SkinAware({ asteria, classic }: { asteria: ReactNode; classic: ReactNode }): ReactNode
{
    const skin = useSkin();
    return skin?.meta.slug === 'asteria-nebula' ? asteria : classic;
}

/** Redirect helper per pagine che non hanno controparte classic. */
function ToHomeRedirect(): ReactNode { return <Navigate to="/" replace />; }

createRoot(root).render(
    <StrictMode>
        <QueryClientProvider client={queryClient}>
            <ThemeApplier />
            <BrowserRouter>
                <Routes>
                    <Route path="/" element={<HomePage />} />
                    <Route path="/login" element={<SkinAware asteria={<AsteriaLoginPage />} classic={<ToHomeRedirect />} />} />
                    <Route path="/registration" element={<SkinAware asteria={<AsteriaRegistrationPage />} classic={<RegistrationPage />} />} />
                    <Route path="/me" element={<SkinAware asteria={<AsteriaMePage />} classic={<ToHomeRedirect />} />} />
                    <Route path="/leaderboard" element={<SkinAware asteria={<AsteriaLeaderboardPage />} classic={<ToHomeRedirect />} />} />
                    <Route path="/messaging" element={<SkinAware asteria={<AsteriaMessagingPage />} classic={<MessagingPage />} />} />
                    <Route path="/community" element={<Navigate to="/community/photos" replace />} />
                    <Route path="/community/photos" element={<SkinAware asteria={<AsteriaPhotosPage />} classic={<CommunityPhotosPage />} />} />
                    <Route path="/community/rooms" element={<SkinAware asteria={<AsteriaRoomsPage />} classic={<CommunityRoomsPage />} />} />
                    <Route path="/community/forum" element={<SkinAware asteria={<AsteriaForumPage />} classic={<CommunityForumPage />} />} />
                    <Route path="/community/category" element={<Navigate to="/community/category/all" replace />} />
                    <Route path="/community/category/:category" element={<SkinAware asteria={<AsteriaArticlesPage />} classic={<CommunityNewsPage />} />} />
                    <Route path="/community/article/:slug" element={<SkinAware asteria={<AsteriaArticlePage />} classic={<CommunityArticlePage />} />} />
                    <Route path="/shop" element={<SkinAware asteria={<AsteriaShopPage />} classic={<ShopPage />} />} />
                    <Route path="/shop/success" element={<AsteriaShopSuccessPage />} />
                    <Route path="/shop/cancel" element={<AsteriaShopCancelPage />} />
                    <Route path="/shop/prepagate" element={<SkinAware asteria={<AsteriaPrepaidPage />} classic={<ShopPrepaidPage />} />} />
                    <Route path="/shop/acquisti" element={<SkinAware asteria={<AsteriaPurchasesPage />} classic={<ShopPurchasesPage />} />} />
                    <Route path="/playing-habbo" element={<SkinAware asteria={<AsteriaGuidePage />} classic={<PlayingHabboPage />} />} />
                    <Route path="/playing-habbo/:slug" element={<SkinAware asteria={<AsteriaStaticInfoPage />} classic={<StaticInfoPage />} />} />
                    <Route path="/help/:slug" element={<SkinAware asteria={<AsteriaStaticInfoPage />} classic={<StaticInfoPage />} />} />
                    <Route path="/habbo-nft" element={<SkinAware asteria={<AsteriaCollectiblesPage />} classic={<CollectiblesPage />} />} />
                    <Route path="/profile/:username" element={<SkinAware asteria={<AsteriaProfilePage />} classic={<ProfilePage />} />} />
                    <Route path="/profile/:user/photo/:id" element={<SkinAware asteria={<AsteriaPhotoDetailPage />} classic={<PhotoDetailPage />} />} />
                    <Route path="/settings" element={<Navigate to="/settings/privacy" replace />} />
                    <Route path="/settings/:section" element={<SkinAware asteria={<AsteriaSettingsPage />} classic={<SettingsPage />} />} />
                    <Route path="/help" element={<SkinAware asteria={<AsteriaHelpPage />} classic={<HelpPage />} />} />
                    <Route path="/admin" element={<StaffPanelPage />} />
                    <Route path="/admin/:section" element={<StaffPanelPage />} />
                    <Route path="*" element={<NotFoundPage />} />
                </Routes>
            </BrowserRouter>
            <ReactQueryDevtools initialIsOpen={false} />
        </QueryClientProvider>
    </StrictMode>
);


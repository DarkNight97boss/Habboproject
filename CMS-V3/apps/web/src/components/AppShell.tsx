import type { ReactNode } from 'react';
import { Link, NavLink, Outlet } from 'react-router';

const navItems: { to: string; label: string; icon: string }[] = [
    { to: '/',             label: 'HOME',           icon: '🏠' },
    { to: '/community',    label: 'COMMUNITY',      icon: '🏛️' },
    { to: '/shop',         label: 'SHOP',           icon: '🛒' },
    { to: '/world',        label: 'IL MONDO DI HABBO', icon: 'ℹ️' },
    { to: '/collectibles', label: 'COLLEZIONABILI', icon: '🎫' }
];

export function AppShell(): ReactNode
{
    return (
        <div className="min-h-screen flex flex-col bg-habbo-page">
            {/* Header — logo centrato + UserChip a destra */}
            <header className="h-[60px] bg-habbo-header-bg flex items-center justify-center relative px-4">
                <Link to="/" aria-label="Home Habbo" className="flex items-center">
                    <HabboLogo />
                </Link>
                <div className="absolute right-4 top-1/2 -translate-y-1/2">
                    <UserChip username="Ospite" />
                </div>
            </header>

            {/* Nav bar bianca chiara con 5 link + GIOCA */}
            <nav className="h-[60px] bg-habbo-nav-top-bg border-b border-habbo-card-border flex items-center px-4 gap-8 justify-center relative">
                <ul className="flex items-center gap-8">
                    {navItems.map(item => (
                        <li key={item.to}>
                            <NavLink
                                to={item.to}
                                className={({ isActive }) =>
                                    `flex items-center gap-2 uppercase font-bold text-[15px] tracking-wide transition-colors ${
                                        isActive
                                            ? 'text-habbo-nav-top-accent'
                                            : 'text-habbo-nav-top-text hover:text-habbo-nav-top-accent'
                                    }`
                                }
                            >
                                <span aria-hidden>{item.icon}</span>
                                {item.label}
                            </NavLink>
                        </li>
                    ))}
                </ul>

                <Link
                    to="/client"
                    className="absolute right-4 top-1/2 -translate-y-1/2 btn-habbo btn-habbo-green flex items-center gap-2 text-[16px]"
                >
                    <span>GIOCA</span>
                    <span aria-hidden>▶</span>
                </Link>
            </nav>

            {/* Main content */}
            <main className="flex-1">
                <Outlet />
            </main>

            {/* Footer */}
            <footer className="bg-habbo-nav-sub-bg text-habbo-page-text text-xs py-4 px-4 text-center">
                © {new Date().getFullYear()} Habboproject — clone non ufficiale. Marchio Habbo® appartiene a Sulake Corporation Oy.
            </footer>
        </div>
    );
}

function HabboLogo(): ReactNode
{
    // Logo SVG semplice retro yellow/red — placeholder. Sostituibile con PNG ufficiale.
    return (
        <div className="font-bold text-[28px] leading-none tracking-tight">
            <span style={{
                color: '#FFB900',
                WebkitTextStroke: '2px #923E3A',
                textShadow: '2px 2px 0 #923E3A'
            }}>
                HABBO
            </span>
        </div>
    );
}

function UserChip({ username }: { username: string }): ReactNode
{
    return (
        <div className="flex items-center gap-2 bg-black/40 border border-habbo-card-border rounded-md px-3 py-1">
            <span className="text-white text-[13px]">{username}</span>
            <span className="text-white">▼</span>
            <div className="w-7 h-7 rounded-full bg-habbo-yellow flex items-center justify-center text-[10px] font-bold">
                {username.slice(0, 1).toUpperCase()}
            </div>
        </div>
    );
}

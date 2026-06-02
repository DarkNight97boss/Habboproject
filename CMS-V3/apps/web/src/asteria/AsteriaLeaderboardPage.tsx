import { type ReactNode, useState } from 'react';
import { avatarUrl } from '../hooks/useAuth';
import { AsteriaShell } from './AsteriaShell';
import './asteria.css';

/**
 * AsteriaLeaderboardPage — top utenti gaming-style.
 *
 * Mock data per ora (real data dal DB ms.users con sort BY credits DESC in
 * iter successivo). Layout:
 *  - Hero "TOP PLAYERS" gigante
 *  - Tabs filtro (Crediti / Diamanti / Online time / Stanze create)
 *  - Top 3 podium con avatar enormi + glow
 *  - Tabella restante 4-20
 */

interface LeaderUser {
    rank: number;
    username: string;
    look: string;
    motto: string;
    score: number;
    metric: 'credits' | 'diamonds' | 'rooms' | 'time';
    badge?: string;
    countryFlag?: string;
}

const MOCK_LEADERS: LeaderUser[] = [
    { rank: 1, username: 'asteria', look: 'hd-180-1.ch-210-66.lg-270-82.sh-290-91', motto: 'Founder · Sky bender', score: 142000, metric: 'credits', badge: 'STAFF' },
    { rank: 2, username: 'devadmin', look: 'hd-185-1.ch-225-78.lg-275-86.sh-295-1408', motto: 'DEV admin', score: 98750, metric: 'credits', badge: 'STAFF' },
    { rank: 3, username: 'lunaria', look: 'hd-180-2.ch-215-71.lg-270-82.sh-300-1408', motto: 'Lover of cosmic things', score: 67200, metric: 'credits' },
    { rank: 4, username: 'pixelio', look: 'hd-180-9.ch-220-91.lg-270-82.sh-290-91', motto: 'Pixel artist · since 2008', score: 55400, metric: 'credits', badge: 'HC1' },
    { rank: 5, username: 'martina', look: 'hd-600-1.ch-635-92.lg-700-92.sh-705-92', motto: 'Catwalk queen 👑', score: 48100, metric: 'credits' },
    { rank: 6, username: 'simoleo89', look: 'hd-180-2.ch-215-1408.lg-270-82.sh-300-1408', motto: 'Build · publish · vibe', score: 41700, metric: 'credits', badge: 'PRO' },
    { rank: 7, username: 'nightrider', look: 'hd-180-1.ch-210-66.lg-275-82.sh-305-1408', motto: 'Insomnia · Roller Disco regular', score: 38900, metric: 'credits' },
    { rank: 8, username: 'kira_aoi', look: 'hd-605-2.ch-665-1408.lg-720-92.sh-725-92', motto: 'Manga collector', score: 35100, metric: 'credits', badge: 'CREATOR' },
    { rank: 9, username: 'oracle_bot', look: 'hd-180-1.ch-3030-1408.lg-3290-110.sh-3115-110', motto: 'Bot Oracolo · Beep boop', score: 32000, metric: 'credits', badge: 'BOT' },
    { rank: 10, username: 'mareclaro', look: 'hd-180-7.ch-215-66.lg-270-82.sh-290-91', motto: 'Beach vibes only', score: 28500, metric: 'credits' }
];

const METRICS: { id: LeaderUser['metric']; label: string; icon: string; suffix: string }[] = [
    { id: 'credits',  label: 'Crediti',     icon: '◉', suffix: '' },
    { id: 'diamonds', label: 'Diamanti',    icon: '◆', suffix: '' },
    { id: 'rooms',    label: 'Stanze',      icon: '⌂', suffix: '' },
    { id: 'time',     label: 'Online time', icon: '⏱', suffix: 'h' }
];

export function AsteriaLeaderboardPage(): ReactNode
{
    const [metric, setMetric] = useState<LeaderUser['metric']>('credits');
    const leaders = MOCK_LEADERS;
    const top3 = leaders.slice(0, 3);
    const rest = leaders.slice(3);
    const metricInfo = METRICS.find(m => m.id === metric)!;

    return (
        <AsteriaShell activeNav="home">
            <section className="asteria-hero" style={{ paddingBottom: 40 }}>
                <div className="asteria-hero__eyebrow">Hall of fame · Aggiornato in tempo reale</div>
                <h1 className="asteria-hero__title" style={{ fontSize: 'clamp(48px, 10vw, 140px)' }}>
                    Top<br />
                    <span className="asteria-hero__title__outline">players.</span>
                </h1>
                <p className="asteria-hero__tagline">
                    I migliori della community Asteria. Crediti, diamanti, stanze, ore online — competi per il primo posto.
                </p>
            </section>

            <div className="asteria-section">
                <div className="asteria-shop__filters">
                    {METRICS.map(m => (
                        <button key={m.id} type="button"
                            className={`asteria-shop__filter ${metric === m.id ? 'is-active' : ''}`}
                            onClick={() => setMetric(m.id)}>
                            <span>{m.icon}</span> {m.label}
                        </button>
                    ))}
                </div>

                <div className="asteria-podium">
                    {/* Order: 2nd, 1st (più alta), 3rd */}
                    {[top3[1], top3[0], top3[2]].filter(Boolean).map((u) => (
                        <PodiumCard key={u!.username} user={u!} suffix={metricInfo.suffix} />
                    ))}
                </div>

                <div className="asteria-leaderboard">
                    {rest.map(u => (
                        <div key={u.username} className="asteria-leaderboard__row">
                            <div className="asteria-leaderboard__rank">#{u.rank}</div>
                            <img src={avatarUrl(u.look, { size: 's', headOnly: true })} alt="" className="asteria-leaderboard__avatar" />
                            <div className="asteria-leaderboard__info">
                                <div className="asteria-leaderboard__name">
                                    {u.username}
                                    {u.badge && <span className="asteria-leaderboard__badge">{u.badge}</span>}
                                </div>
                                <div className="asteria-leaderboard__motto">{u.motto}</div>
                            </div>
                            <div className="asteria-leaderboard__score">
                                <strong>{u.score.toLocaleString('it-IT')}</strong>
                                <span>{metricInfo.suffix || metricInfo.label.toLowerCase()}</span>
                            </div>
                        </div>
                    ))}
                </div>

                <p className="asteria-leaderboard__note">
                    Le classifiche si aggiornano ogni 10 minuti. Vuoi vederti qui? Connetti, gioca, contribuisci.
                </p>
            </div>
        </AsteriaShell>
    );
}

function PodiumCard({ user, suffix }: { user: LeaderUser; suffix: string }): ReactNode
{
    const medal = ['🥇', '🥈', '🥉'][user.rank - 1] || '';
    return (
        <div className={`asteria-podium__card asteria-podium__card--rank-${user.rank}`}>
            <div className="asteria-podium__medal">{medal}</div>
            <div className="asteria-podium__avatar">
                <img src={avatarUrl(user.look, { size: 'l' })} alt="" />
            </div>
            <div className="asteria-podium__name">{user.username}</div>
            <div className="asteria-podium__motto">{user.motto}</div>
            {user.badge && <div className="asteria-podium__badge">{user.badge}</div>}
            <div className="asteria-podium__score">
                <strong>{user.score.toLocaleString('it-IT')}</strong> {suffix}
            </div>
        </div>
    );
}

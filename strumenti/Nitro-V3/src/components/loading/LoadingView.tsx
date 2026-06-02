import { GetConfiguration } from '@nitrots/nitro-renderer';
import { CSSProperties, FC, useMemo } from 'react';

interface LoadingViewProps {
    isError?: boolean;
    message?: string;
    homeUrl?: string;
    progress?: number;
    currentTask?: string;
}

const resolveConfigUrl = (key: string): string =>
{
    try
    {
        const raw = GetConfiguration().getValue<string>(key, '');
        if(!raw) return '';
        return GetConfiguration().interpolate(raw) || raw;
    }
    catch
    {
        return '';
    }
};

const resolveConfigString = (key: string, fallback = ''): string =>
{
    try
    {
        const raw = GetConfiguration().getValue<string>(key, '');
        return raw || fallback;
    }
    catch
    {
        return fallback;
    }
};

// Starfield generato una volta sola a livello di modulo (client-only, niente SSR).
interface Star { id: number; left: string; top: string; size: number; opacity: number; delay: string }
const makeStars = (n: number, size: number, baseOpacity: number): Star[] =>
    Array.from({ length: n }, (_, id) => ({
        id,
        size,
        left: `${ (Math.random() * 120).toFixed(2) }%`,
        top: `${ (Math.random() * 100).toFixed(2) }%`,
        opacity: Number((baseOpacity + Math.random() * 0.4).toFixed(2)),
        delay: `${ (Math.random() * 3.5).toFixed(2) }s`
    }));
const STARS_FAR = makeStars(60, 1.5, 0.3);
const STARS_MID = makeStars(40, 2, 0.4);
const STARS_NEAR = makeStars(22, 3, 0.5);
const starStyle = (s: Star): CSSProperties => ({ left: s.left, top: s.top, width: s.size, height: s.size, opacity: s.opacity, animationDelay: s.delay });

// Tutto lo stile (keyframes + classi) è inline: il componente loading non ha
// un file SCSS dedicato e si auto-rimuove dopo il boot, quindi le classi
// globali `al-*` (Asteria Loading) non danno problemi di leakage.
const AL_STYLE = `
.al-stage{position:fixed;inset:0;overflow:hidden;z-index:2147483000;font-family:'Poppins','Segoe UI',system-ui,sans-serif;background:radial-gradient(ellipse 120% 90% at 50% 32%,#1c1146 0%,#0a0a22 52%,#03030c 100%)}
.al-conic{position:absolute;top:42%;left:50%;width:1100px;height:1100px;transform:translate(-50%,-50%);border-radius:50%;filter:blur(90px);opacity:.45;mix-blend-mode:screen;background:conic-gradient(from 0deg,#7c3aed,#22d3ee,#ec4899,#3b82f6,#7c3aed);animation:al-rotate 22s linear infinite}
@keyframes al-rotate{to{transform:translate(-50%,-50%) rotate(360deg)}}
.al-neb{position:absolute;border-radius:50%;filter:blur(70px);mix-blend-mode:screen}
.al-neb1{top:8%;left:12%;width:520px;height:520px;background:radial-gradient(closest-side,rgba(124,58,237,.5),transparent);animation:al-d1 18s ease-in-out infinite alternate}
.al-neb2{bottom:2%;right:8%;width:600px;height:600px;background:radial-gradient(closest-side,rgba(34,211,238,.38),transparent);animation:al-d2 21s ease-in-out infinite alternate}
@keyframes al-d1{to{transform:translate(60px,40px) scale(1.15)}}
@keyframes al-d2{to{transform:translate(-50px,-30px) scale(1.1)}}
.al-slayer{position:absolute;inset:-10%}
.al-l1{animation:al-pan 90s linear infinite}.al-l2{animation:al-pan 60s linear infinite}.al-l3{animation:al-pan 38s linear infinite}
@keyframes al-pan{to{transform:translateX(-40px)}}
.al-star{position:absolute;border-radius:50%;background:#fff;animation:al-tw 3.5s ease-in-out infinite}
@keyframes al-tw{0%,100%{opacity:.12}50%{opacity:1}}
.al-vignette{position:absolute;inset:0;pointer-events:none;background:radial-gradient(ellipse 75% 65% at 50% 45%,transparent 55%,rgba(0,0,0,.55) 100%)}
.al-brand{position:absolute;top:26px;left:32px;font-weight:800;letter-spacing:.26em;font-size:15px;z-index:5;background:linear-gradient(90deg,#a78bfa,#22d3ee);-webkit-background-clip:text;background-clip:text;color:transparent}
.al-brand small{display:block;font-size:8px;letter-spacing:.55em;color:#6b7690;font-weight:600;margin-top:3px}
.al-center{position:absolute;top:44%;left:50%;transform:translate(-50%,-50%);text-align:center;z-index:4;width:90%;opacity:0;animation:al-enter 1.1s cubic-bezier(.2,.8,.2,1) .15s forwards}
@keyframes al-enter{from{opacity:0;transform:translate(-50%,-44%) scale(.92)}to{opacity:1;transform:translate(-50%,-50%) scale(1)}}
.al-system{position:relative;width:230px;height:230px;margin:0 auto;transform-style:preserve-3d;perspective:700px}
.al-halo{position:absolute;inset:38px;border-radius:50%;filter:blur(6px);opacity:.9;background:conic-gradient(from 0deg,transparent 0deg,#22d3ee 60deg,#7c3aed 150deg,transparent 240deg);animation:al-rot2 3.6s linear infinite}
@keyframes al-rot2{to{transform:rotate(360deg)}}
.al-core{position:absolute;top:50%;left:50%;width:78px;height:78px;transform:translate(-50%,-50%);border-radius:50%;background:radial-gradient(circle at 34% 28%,#f5e9ff,#a855f7 45%,#6d28d9 75%,#4c1d95);box-shadow:0 0 50px 10px rgba(124,58,237,.75),0 0 120px 30px rgba(34,211,238,.25),inset -8px -8px 22px rgba(35,9,74,.7);animation:al-breathe 3.4s ease-in-out infinite}
@keyframes al-breathe{0%,100%{box-shadow:0 0 50px 10px rgba(124,58,237,.75),0 0 120px 30px rgba(34,211,238,.25),inset -8px -8px 22px rgba(35,9,74,.7)}50%{box-shadow:0 0 70px 18px rgba(168,85,247,1),0 0 160px 44px rgba(34,211,238,.45),inset -8px -8px 22px rgba(35,9,74,.7)}}
.al-ripple{position:absolute;top:50%;left:50%;width:78px;height:78px;transform:translate(-50%,-50%);border-radius:50%;border:2px solid rgba(124,58,237,.5);animation:al-ripple 3.4s ease-out infinite}
.al-ripple.al-d{animation-delay:1.7s}
@keyframes al-ripple{0%{opacity:.7;transform:translate(-50%,-50%) scale(1)}100%{opacity:0;transform:translate(-50%,-50%) scale(2.6)}}
.al-orbit{position:absolute;inset:0;border-radius:50%;border:1px solid rgba(167,139,250,.18)}
.al-o1{transform:rotateX(72deg)}
.al-o2{transform:rotateX(72deg) rotateY(60deg);inset:26px;border-color:rgba(34,211,238,.18)}
.al-o3{transform:rotateX(60deg) rotateY(-50deg);inset:14px;border-color:rgba(236,72,153,.16)}
.al-spin{position:absolute;inset:0;transform-style:preserve-3d}
.al-o1 .al-spin{animation:al-rot2 5s linear infinite}
.al-o2 .al-spin{animation:al-rot2 7s linear infinite reverse}
.al-o3 .al-spin{animation:al-rot2 9s linear infinite}
.al-dot{position:absolute;top:-5px;left:50%;width:11px;height:11px;margin-left:-5.5px;border-radius:50%}
.al-o1 .al-dot{background:#22d3ee;box-shadow:0 0 14px 4px rgba(34,211,238,.95)}
.al-o2 .al-dot{background:#a78bfa;box-shadow:0 0 14px 4px rgba(167,139,250,.95)}
.al-o3 .al-dot{background:#f472b6;box-shadow:0 0 14px 4px rgba(244,114,182,.9);width:8px;height:8px;margin-left:-4px}
.al-customlogo{display:block;max-width:80vw;max-height:38vh;width:auto;height:auto;margin:0 auto;user-select:none;pointer-events:none}
.al-wordmark{margin-top:30px;font-weight:800;font-size:48px;letter-spacing:.26em;line-height:1;background:linear-gradient(100deg,#c4b5fd 0%,#22d3ee 35%,#f472b6 60%,#c4b5fd 100%);background-size:220% auto;-webkit-background-clip:text;background-clip:text;color:transparent;filter:drop-shadow(0 4px 30px rgba(124,58,237,.5));animation:al-flow 5s linear infinite}
@keyframes al-flow{to{background-position:220% center}}
.al-uline{width:0;height:2px;margin:14px auto 0;border-radius:2px;background:linear-gradient(90deg,transparent,#22d3ee,#a78bfa,transparent);animation:al-grow 1.1s cubic-bezier(.2,.8,.2,1) .6s forwards,al-glowln 3s ease-in-out 1.7s infinite}
@keyframes al-grow{to{width:240px}}
@keyframes al-glowln{0%,100%{opacity:.6}50%{opacity:1}}
.al-tagline{margin-top:12px;font-size:11.5px;letter-spacing:.52em;color:#8b9bb4;font-weight:600}
.al-msg{margin-top:16px;font-size:15px;color:rgba(255,255,255,.85);font-weight:500;letter-spacing:.04em;white-space:pre-line;text-shadow:0 2px 6px rgba(0,0,0,.5)}
.al-progress{position:absolute;bottom:8.5vh;left:50%;transform:translateX(-50%);width:min(680px,84vw);z-index:4;opacity:0;animation:al-fadeup 1s ease .5s forwards}
@keyframes al-fadeup{from{opacity:0;transform:translate(-50%,16px)}to{opacity:1;transform:translate(-50%,0)}}
.al-track{position:relative;height:12px;border-radius:999px;overflow:hidden;background:rgba(255,255,255,.06);border:1px solid rgba(167,139,250,.22);box-shadow:inset 0 1px 4px rgba(0,0,0,.5),0 8px 30px rgba(0,0,0,.4)}
.al-fill{position:relative;height:100%;border-radius:999px;background:linear-gradient(90deg,#6d28d9,#7c3aed 40%,#22d3ee);background-size:200% auto;box-shadow:0 0 18px rgba(124,58,237,.8);animation:al-flow2 2.2s linear infinite;transition:width .4s ease}
@keyframes al-flow2{to{background-position:200% center}}
.al-fill::after{content:'';position:absolute;top:50%;right:-2px;width:18px;height:18px;transform:translateY(-50%);border-radius:50%;background:#e0f7ff;box-shadow:0 0 16px 5px rgba(34,211,238,.95),0 0 30px 10px rgba(124,58,237,.5)}
.al-meta{display:flex;justify-content:space-between;align-items:center;margin-top:14px}
.al-task{font-size:13px;letter-spacing:.05em;color:rgba(255,255,255,.8);font-weight:500}
.al-pct{font-size:13px;font-weight:700;letter-spacing:.06em;font-variant-numeric:tabular-nums;background:linear-gradient(90deg,#a78bfa,#22d3ee);-webkit-background-clip:text;background-clip:text;color:transparent}
.al-error{display:flex;flex-direction:column;align-items:center;gap:14px}
.al-error-msg{font-size:20px;color:#fff;text-align:center;white-space:pre-line;text-shadow:0 4px 4px rgba(0,0,0,.25);max-width:80vw}
.al-btn{margin-top:6px;padding:12px 26px;border-radius:12px;background:linear-gradient(90deg,#7c3aed,#22d3ee);color:#fff;font-size:15px;font-weight:700;text-decoration:none;cursor:pointer;box-shadow:0 8px 24px rgba(124,58,237,.5);transition:transform .15s ease,box-shadow .15s ease}
.al-btn:hover{transform:translateY(-2px);box-shadow:0 12px 30px rgba(124,58,237,.7)}
@media (max-width:640px){.al-wordmark{font-size:34px}.al-system{width:180px;height:180px}}
`;

export const LoadingView: FC<LoadingViewProps> = props =>
{
    const { isError = false, message = '', homeUrl = '', progress, currentTask = '' } = props;

    const customLogoUrl = useMemo(() => resolveConfigUrl('loading.logo.url'), []);
    const customBackground = useMemo(() => resolveConfigString('loading.background', ''), []);
    const progressBarColor = useMemo(() => resolveConfigString('loading.progress.color', ''), []);

    const clampedProgress = typeof progress === 'number' && Number.isFinite(progress)
        ? Math.max(0, Math.min(100, Math.round(progress)))
        : null;

    const fillStyle: CSSProperties = progressBarColor
        ? { width: `${ clampedProgress ?? 0 }%`, background: progressBarColor }
        : { width: `${ clampedProgress ?? 0 }%` };

    return (
        <div className="al-stage" style={ customBackground ? { background: customBackground } : undefined }>
            <style>{ AL_STYLE }</style>

            { !customBackground && (
                <>
                    <div className="al-conic" />
                    <div className="al-neb al-neb1" />
                    <div className="al-neb al-neb2" />
                </>
            ) }
            <div className="al-slayer al-l1">{ STARS_FAR.map(s => <span key={ s.id } className="al-star" style={ starStyle(s) } />) }</div>
            <div className="al-slayer al-l2">{ STARS_MID.map(s => <span key={ s.id } className="al-star" style={ starStyle(s) } />) }</div>
            <div className="al-slayer al-l3">{ STARS_NEAR.map(s => <span key={ s.id } className="al-star" style={ starStyle(s) } />) }</div>
            <div className="al-vignette" />
            <div className="al-brand">ASTERIA<small>HOTEL</small></div>

            { isError ? (
                <div className="al-center">
                    <div className="al-error">
                        <div className="al-error-msg">{ message || 'Errore' }</div>
                        { homeUrl && <a href={ homeUrl } className="al-btn">Torna all'hotel</a> }
                    </div>
                </div>
            ) : (
                <>
                    <div className="al-center">
                        { customLogoUrl
                            ? <img src={ customLogoUrl } alt="" draggable={ false } className="al-customlogo" />
                            : (
                                <div className="al-system">
                                    <div className="al-halo" />
                                    <div className="al-ripple" />
                                    <div className="al-ripple al-d" />
                                    <div className="al-core" />
                                    <div className="al-orbit al-o1"><div className="al-spin"><div className="al-dot" /></div></div>
                                    <div className="al-orbit al-o2"><div className="al-spin"><div className="al-dot" /></div></div>
                                    <div className="al-orbit al-o3"><div className="al-spin"><div className="al-dot" /></div></div>
                                </div>
                            )
                        }
                        <div className="al-wordmark">ASTERIA</div>
                        <div className="al-uline" />
                        <div className="al-tagline">HOTEL VIRTUALE ITALIANO</div>
                        { message ? <div className="al-msg">{ message }</div> : null }
                    </div>

                    { clampedProgress !== null && (
                        <div className="al-progress">
                            <div className="al-track"><div className="al-fill" style={ fillStyle } /></div>
                            <div className="al-meta">
                                <span className="al-task">{ currentTask }</span>
                                <span className="al-pct">{ clampedProgress }%</span>
                            </div>
                        </div>
                    ) }
                </>
            ) }
        </div>
    );
};

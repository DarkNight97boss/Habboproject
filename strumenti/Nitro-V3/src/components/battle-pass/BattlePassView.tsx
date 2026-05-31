import { FC, useEffect, useMemo, useRef, useState } from 'react';
import { FaCoins, FaCrown, FaCube, FaGem, FaLock, FaStar, FaTrophy } from 'react-icons/fa';
import { LocalizeText, SendMessageComposer } from '../../api';
import { GetCommunication } from '@nitrots/nitro-renderer';
const GetConnection = () => GetCommunication()?.connection ?? null;
import { Base, Button, Column, Flex, NitroCardContentView, NitroCardHeaderView, NitroCardView, Text } from '../../common';
import { useMessageEvent } from '../../hooks';
import { BattlePassBuyPremiumComposer, BattlePassClaimComposer, BattlePassClaimedEvent, BattlePassInfoEvent, BattlePassMessageConfiguration, BattlePassPremiumPurchasedEvent, BattlePassRequestInfoComposer, BattlePassTier } from './messages';

const KIND_CREDITS = 0;
const KIND_PIXELS = 1;
const KIND_DIAMONDS = 2;
const KIND_BADGE = 3;

const kindIcon = (kind: number) =>
{
    switch(kind)
    {
        case KIND_CREDITS: return <FaCoins />;
        case KIND_PIXELS: return <FaCube />;
        case KIND_DIAMONDS: return <FaGem />;
        case KIND_BADGE: return <FaTrophy />;
        default: return null;
    }
};

const kindLabel = (kind: number, amount: number, badge: string) =>
{
    switch(kind)
    {
        case KIND_CREDITS: return amount + 'c';
        case KIND_PIXELS: return amount + 'd';
        case KIND_DIAMONDS: return amount + ' 💎';
        case KIND_BADGE: return badge || LocalizeText('battlepass.badge');
        default: return String(amount);
    }
};

export const BattlePassView: FC<{}> = () =>
{
    const [ registered, setRegistered ] = useState(false);
    const [ isOpen, setIsOpen ] = useState(false);
    const [ openedOnce, setOpenedOnce ] = useState(false);
    const [ data, setData ] = useState<{
        seasonId: number; seasonName: string; xp: number; xpPerTier: number;
        currentTier: number; isPremium: boolean; premiumCost: number;
        seasonEnd: number; tiers: BattlePassTier[];
    } | null>(null);
    const [ flashMessage, setFlashMessage ] = useState<string>('');

    useEffect(() =>
    {
        const c = GetConnection();
        if(!c) return;
        c.registerMessages(new BattlePassMessageConfiguration());
        setRegistered(true);
    }, []);

    useMessageEvent<BattlePassInfoEvent>(BattlePassInfoEvent, event =>
    {
        const p = event.getParser();
        if(!p.seasonId) { setData(null); return; }
        setData({
            seasonId: p.seasonId, seasonName: p.seasonName, xp: p.xp,
            xpPerTier: p.xpPerTier, currentTier: p.currentTier, isPremium: p.isPremium,
            premiumCost: p.premiumCost, seasonEnd: p.seasonEnd, tiers: p.tiers
        });
        if(!openedOnce && p.currentTier >= 1)
        {
            setIsOpen(true);
            setOpenedOnce(true);
        }
    });

    useMessageEvent<BattlePassClaimedEvent>(BattlePassClaimedEvent, event =>
    {
        const p = event.getParser();
        if(p.success)
        {
            const label = kindLabel(p.kind, p.amount, p.badge);
            const key = p.premium ? 'battlepass.claim.success.premium' : 'battlepass.claim.success';
            setFlashMessage(LocalizeText(key, [ 'tier', 'reward' ], [ String(p.tier), label ]));
        }
        else if(p.message)
        {
            setFlashMessage(p.message);
        }
        setTimeout(() => setFlashMessage(''), 3500);
    });

    useMessageEvent<BattlePassPremiumPurchasedEvent>(BattlePassPremiumPurchasedEvent, event =>
    {
        const p = event.getParser();
        setFlashMessage(p.message || LocalizeText(p.success ? 'battlepass.premium.unlocked' : 'battlepass.premium.failed'));
        setTimeout(() => setFlashMessage(''), 3500);
    });

    useEffect(() =>
    {
        if(!registered) return;
        SendMessageComposer(new BattlePassRequestInfoComposer());
    }, [ registered ]);

    const xpProgressPct = useMemo(() =>
    {
        if(!data || data.xpPerTier <= 0) return 0;
        const withinTier = data.xp - data.currentTier * data.xpPerTier;
        return Math.max(0, Math.min(100, Math.floor((withinTier / data.xpPerTier) * 100)));
    }, [ data ]);

    const seasonEndsIn = useMemo(() =>
    {
        if(!data || !data.seasonEnd) return '';
        const seconds = data.seasonEnd - Math.floor(Date.now() / 1000);
        if(seconds <= 0) return LocalizeText('battlepass.season.ended');
        const days = Math.floor(seconds / 86400);
        if(days >= 1) return days + 'd';
        const hours = Math.floor(seconds / 3600);
        return hours + 'h';
    }, [ data ]);

    const claim = (tier: number, premium: boolean) =>
    {
        SendMessageComposer(new BattlePassClaimComposer(tier, premium));
    };

    const buyPremium = () =>
    {
        SendMessageComposer(new BattlePassBuyPremiumComposer());
    };

    // Floating mini-toggle (always visible when we know about a season) — draggable
    const TOGGLE_POS_KEY = 'nitro.battlepass.togglePos';
    const DRAG_THRESHOLD = 4; // px before a press becomes a drag (so click still works)
    const togglePosRef = useRef<{ left: number; top: number } | null>(null);
    const [ togglePos, setTogglePos ] = useState<{ left: number; top: number } | null>(() =>
    {
        try
        {
            const raw = window.localStorage.getItem(TOGGLE_POS_KEY);
            if(!raw) return null;
            const parsed = JSON.parse(raw);
            if(typeof parsed?.left === 'number' && typeof parsed?.top === 'number') return parsed;
        }
        catch {}
        return null;
    });
    const dragStateRef = useRef<{
        pointerId: number; startX: number; startY: number;
        originLeft: number; originTop: number; moved: boolean;
    } | null>(null);
    const toggleElRef = useRef<HTMLDivElement | null>(null);

    const clampPos = (left: number, top: number, w: number, h: number) =>
    {
        const maxLeft = Math.max(0, window.innerWidth - w);
        const maxTop = Math.max(0, window.innerHeight - h);
        return {
            left: Math.min(Math.max(0, left), maxLeft),
            top: Math.min(Math.max(0, top), maxTop)
        };
    };

    const onTogglePointerDown = (e: React.PointerEvent<HTMLDivElement>) =>
    {
        if(e.button !== undefined && e.button !== 0) return;
        const el = toggleElRef.current;
        if(!el) return;
        const rect = el.getBoundingClientRect();
        dragStateRef.current = {
            pointerId: e.pointerId,
            startX: e.clientX, startY: e.clientY,
            originLeft: rect.left, originTop: rect.top,
            moved: false
        };
        // seed ref with current rect so first move uses real on-screen position
        togglePosRef.current = { left: rect.left, top: rect.top };
        try { el.setPointerCapture(e.pointerId); } catch {}
    };

    const onTogglePointerMove = (e: React.PointerEvent<HTMLDivElement>) =>
    {
        const drag = dragStateRef.current;
        if(!drag || drag.pointerId !== e.pointerId) return;
        const dx = e.clientX - drag.startX;
        const dy = e.clientY - drag.startY;
        if(!drag.moved && Math.hypot(dx, dy) < DRAG_THRESHOLD) return;
        drag.moved = true;
        const el = toggleElRef.current;
        const w = el?.offsetWidth ?? 0;
        const h = el?.offsetHeight ?? 0;
        const next = clampPos(drag.originLeft + dx, drag.originTop + dy, w, h);
        togglePosRef.current = next;
        setTogglePos(next);
    };

    const onTogglePointerUp = (e: React.PointerEvent<HTMLDivElement>) =>
    {
        const drag = dragStateRef.current;
        if(!drag || drag.pointerId !== e.pointerId) return;
        const moved = drag.moved;
        dragStateRef.current = null;
        try { toggleElRef.current?.releasePointerCapture(e.pointerId); } catch {}
        if(moved)
        {
            const pos = togglePosRef.current;
            if(pos)
            {
                try { window.localStorage.setItem(TOGGLE_POS_KEY, JSON.stringify(pos)); } catch {}
            }
            // swallow the click that would follow a drag
            e.preventDefault();
            e.stopPropagation();
            return;
        }
        // treated as a click → open the panel
        setIsOpen(true);
    };

    // keep toggle on-screen when window resizes
    useEffect(() =>
    {
        const onResize = () =>
        {
            const pos = togglePosRef.current ?? togglePos;
            const el = toggleElRef.current;
            if(!pos || !el) return;
            const next = clampPos(pos.left, pos.top, el.offsetWidth, el.offsetHeight);
            if(next.left !== pos.left || next.top !== pos.top)
            {
                togglePosRef.current = next;
                setTogglePos(next);
                try { window.localStorage.setItem(TOGGLE_POS_KEY, JSON.stringify(next)); } catch {}
            }
        };
        window.addEventListener('resize', onResize);
        return () => window.removeEventListener('resize', onResize);
    }, [ togglePos ]);

    const togglePositionStyle: React.CSSProperties = togglePos
        ? { left: togglePos.left, top: togglePos.top }
        : { right: 16, bottom: 80 };

    const toggle = data && !isOpen && (
        <Base
            innerRef={ toggleElRef as any }
            position="fixed"
            onPointerDown={ onTogglePointerDown }
            onPointerMove={ onTogglePointerMove }
            onPointerUp={ onTogglePointerUp }
            onPointerCancel={ onTogglePointerUp }
            style={ {
                ...togglePositionStyle,
                zIndex: 50, cursor: dragStateRef.current?.moved ? 'grabbing' : 'grab',
                padding: '6px 10px', borderRadius: 10,
                background: 'linear-gradient(135deg,#6f42c1,#e83e8c)',
                color: '#fff', fontWeight: 'bold', boxShadow: '0 4px 12px rgba(0,0,0,.2)',
                touchAction: 'none', userSelect: 'none'
            } }>
            <Flex alignItems="center" gap={ 1 }>
                <FaStar />
                <span>BP T{ data.currentTier }</span>
            </Flex>
        </Base>
    );

    if(!data) return toggle as any;

    if(!isOpen) return toggle as any;

    return (
        <NitroCardView uniqueKey="battle-pass" className="nitro-battle-pass" theme="primary-slim">
            <NitroCardHeaderView headerText={ LocalizeText('battlepass.header', [ 'season' ], [ data.seasonName ]) } onCloseClick={ () => setIsOpen(false) } />
            <div className="border-b border-zinc-200 p-2">
                <Flex alignItems="center" gap={ 2 } fullWidth>
                    <Text bold>{ LocalizeText('battlepass.tier.progress', [ 'current', 'max' ], [ String(data.currentTier), String(data.tiers.length) ]) }</Text>
                    <Base grow style={ { height: 10, background: '#eee', borderRadius: 5, overflow: 'hidden' } }>
                        <div style={ { width: xpProgressPct + '%', height: '100%', background: 'linear-gradient(90deg,#28a745,#20c997)' } } />
                    </Base>
                    <Text small className="text-muted">{ LocalizeText('battlepass.xp', [ 'xp' ], [ String(data.xp) ]) }</Text>
                    <Text small className="text-muted">{ seasonEndsIn && LocalizeText('battlepass.season.ends_in', [ 'time' ], [ seasonEndsIn ]) }</Text>
                </Flex>
            </div>
            <NitroCardContentView className="text-black">
                <Column gap={ 2 }>
                    { !!flashMessage && <Text bold center className="text-success">{ flashMessage }</Text> }
                    <Base style={ { maxHeight: 360, overflowY: 'auto' } }>
                        <Column gap={ 1 }>
                            { data.tiers.map(t =>
                            {
                                const unlocked = t.tier <= data.currentTier;
                                const freeCanClaim = unlocked && !t.freeClaimed;
                                const premiumCanClaim = unlocked && data.isPremium && !t.premiumClaimed;
                                return (
                                    <Flex key={ t.tier } alignItems="center" gap={ 2 }
                                        style={ {
                                            padding: '6px 8px', borderRadius: 8,
                                            border: '1px solid ' + (unlocked ? '#28a745' : '#ddd'),
                                            background: unlocked ? '#f1fff5' : '#fafafa',
                                            opacity: unlocked ? 1 : 0.7
                                        } }>
                                        <Flex alignItems="center" justifyContent="center" style={ { width: 30, height: 30, borderRadius: 15, background: unlocked ? '#28a745' : '#aaa', color: '#fff', fontWeight: 'bold' } }>{ t.tier }</Flex>
                                        <Flex grow alignItems="center" gap={ 1 }>
                                            <Flex style={ { fontSize: 20 } } alignItems="center">{ kindIcon(t.freeKind) }</Flex>
                                            <Text small bold>{ kindLabel(t.freeKind, t.freeAmount, t.freeBadge) }</Text>
                                            <Text small className="text-muted">{ LocalizeText('battlepass.free') }</Text>
                                        </Flex>
                                        <Flex grow alignItems="center" gap={ 1 } style={ { opacity: data.isPremium ? 1 : 0.55 } }>
                                            <Flex style={ { fontSize: 20, color: '#d4af37' } } alignItems="center">
                                                { data.isPremium ? kindIcon(t.premiumKind) : <FaLock /> }
                                            </Flex>
                                            <Text small bold style={ { color: '#a07000' } }>{ kindLabel(t.premiumKind, t.premiumAmount, t.premiumBadge) }</Text>
                                            <Text small style={ { color: '#a07000' } }>{ LocalizeText('battlepass.premium') }</Text>
                                        </Flex>
                                        <Flex gap={ 1 }>
                                            <Button variant={ freeCanClaim ? 'success' : 'secondary' } disabled={ !freeCanClaim } onClick={ () => claim(t.tier, false) }>
                                                { t.freeClaimed ? '✓' : LocalizeText('battlepass.claim') }
                                            </Button>
                                            <Button variant={ premiumCanClaim ? 'warning' : 'secondary' } disabled={ !premiumCanClaim } onClick={ () => claim(t.tier, true) }>
                                                { t.premiumClaimed ? '✓' : (data.isPremium ? LocalizeText('battlepass.claim') : <FaLock />) }
                                            </Button>
                                        </Flex>
                                    </Flex>
                                );
                            }) }
                        </Column>
                    </Base>
                    { !data.isPremium && (
                        <Flex justifyContent="center" gap={ 1 } style={ { paddingTop: 4 } }>
                            <Button variant="warning" onClick={ buyPremium }>
                                <Flex alignItems="center" gap={ 1 }>
                                    <FaCrown />
                                    { LocalizeText('battlepass.unlock.premium', [ 'cost' ], [ String(data.premiumCost) ]) }
                                </Flex>
                            </Button>
                        </Flex>
                    ) }
                </Column>
            </NitroCardContentView>
        </NitroCardView>
    );
};

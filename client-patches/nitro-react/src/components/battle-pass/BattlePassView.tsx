import { FC, useEffect, useMemo, useState } from 'react';
import { FaCoins, FaCrown, FaCube, FaGem, FaLock, FaStar, FaTrophy } from 'react-icons/fa';
import { GetConnection, SendMessageComposer } from '../../api';
import { Base, Button, Column, Flex, NitroCardContentView, NitroCardHeaderView, NitroCardSubHeaderView, NitroCardView, Text } from '../../common';
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
        case KIND_BADGE: return badge || 'badge';
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
            setFlashMessage(`Tier ${p.tier} ${p.premium ? '(premium)' : ''}: +${label}`);
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
        setFlashMessage(p.message || (p.success ? 'Premium unlocked!' : 'Could not purchase.'));
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
        if(seconds <= 0) return 'ended';
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

    // Floating mini-toggle (always visible when we know about a season)
    const toggle = data && !isOpen && (
        <Base
            position="absolute"
            onClick={ () => setIsOpen(true) }
            style={ {
                right: 16, bottom: 80, zIndex: 50, cursor: 'pointer',
                padding: '6px 10px', borderRadius: 10,
                background: 'linear-gradient(135deg,#6f42c1,#e83e8c)',
                color: '#fff', fontWeight: 'bold', boxShadow: '0 4px 12px rgba(0,0,0,.2)'
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
            <NitroCardHeaderView headerText={ `Battle Pass · ${data.seasonName}` } onCloseClick={ () => setIsOpen(false) } />
            <NitroCardSubHeaderView>
                <Flex alignItems="center" gap={ 2 } fullWidth>
                    <Text bold>Tier { data.currentTier }/{ data.tiers.length }</Text>
                    <Base grow style={ { height: 10, background: '#eee', borderRadius: 5, overflow: 'hidden' } }>
                        <div style={ { width: xpProgressPct + '%', height: '100%', background: 'linear-gradient(90deg,#28a745,#20c997)' } } />
                    </Base>
                    <Text small className="text-muted">{ data.xp } XP</Text>
                    <Text small className="text-muted">{ seasonEndsIn && '· ends in ' + seasonEndsIn }</Text>
                </Flex>
            </NitroCardSubHeaderView>
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
                                            <Text small className="text-muted">FREE</Text>
                                        </Flex>
                                        <Flex grow alignItems="center" gap={ 1 } style={ { opacity: data.isPremium ? 1 : 0.55 } }>
                                            <Flex style={ { fontSize: 20, color: '#d4af37' } } alignItems="center">
                                                { data.isPremium ? kindIcon(t.premiumKind) : <FaLock /> }
                                            </Flex>
                                            <Text small bold style={ { color: '#a07000' } }>{ kindLabel(t.premiumKind, t.premiumAmount, t.premiumBadge) }</Text>
                                            <Text small style={ { color: '#a07000' } }>PREMIUM</Text>
                                        </Flex>
                                        <Flex gap={ 1 }>
                                            <Button variant={ freeCanClaim ? 'success' : 'secondary' } disabled={ !freeCanClaim } onClick={ () => claim(t.tier, false) }>
                                                { t.freeClaimed ? '✓' : 'Claim' }
                                            </Button>
                                            <Button variant={ premiumCanClaim ? 'warning' : 'secondary' } disabled={ !premiumCanClaim } onClick={ () => claim(t.tier, true) }>
                                                { t.premiumClaimed ? '✓' : (data.isPremium ? 'Claim' : <FaLock />) }
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
                                    Unlock Premium ({ data.premiumCost } 💎)
                                </Flex>
                            </Button>
                        </Flex>
                    ) }
                </Column>
            </NitroCardContentView>
        </NitroCardView>
    );
};

import { FC, useEffect, useMemo, useState } from 'react';
import { FaCoins, FaCube, FaFire, FaGem, FaTrophy } from 'react-icons/fa';
import { GetConnection, SendMessageComposer } from '../../api';
import { Button, Column, Flex, NitroCardContentView, NitroCardHeaderView, NitroCardSubHeaderView, NitroCardView, Text } from '../../common';
import { useMessageEvent } from '../../hooks';
import { DailyStreakClaimComposer, DailyStreakClaimedEvent, DailyStreakInfoEvent, DailyStreakMessageConfiguration, DailyStreakRequestInfoComposer, DailyStreakReward } from './messages';

const KIND_CREDITS = 0;
const KIND_PIXELS = 1;
const KIND_DIAMONDS = 2;
const KIND_BADGE = 3;

const rewardIcon = (kind: number) =>
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

const rewardLabel = (r: DailyStreakReward) =>
{
    switch(r.kind)
    {
        case KIND_CREDITS: return r.amount + 'c';
        case KIND_PIXELS: return r.amount + 'd';
        case KIND_DIAMONDS: return r.amount + ' 💎';
        case KIND_BADGE: return r.badgeCode;
        default: return String(r.amount);
    }
};

export const DailyStreakView: FC<{}> = props =>
{
    const [ registered, setRegistered ] = useState(false);
    const [ isVisible, setIsVisible ] = useState(false);
    const [ currentStreak, setCurrentStreak ] = useState(0);
    const [ bestStreak, setBestStreak ] = useState(0);
    const [ canClaim, setCanClaim ] = useState(false);
    const [ dayInCycle, setDayInCycle ] = useState(1);
    const [ cycle, setCycle ] = useState<DailyStreakReward[]>([]);
    const [ submitting, setSubmitting ] = useState(false);
    const [ message, setMessage ] = useState<string>('');

    // Register custom MFA-style message headers once.
    useEffect(() =>
    {
        const connection = GetConnection();
        if(!connection) return;
        connection.registerMessages(new DailyStreakMessageConfiguration());
        setRegistered(true);
    }, []);

    useMessageEvent<DailyStreakInfoEvent>(DailyStreakInfoEvent, event =>
    {
        const p = event.getParser();
        const isFreshLogin = !isVisible && !currentStreak && !bestStreak;
        setCurrentStreak(p.currentStreak);
        setBestStreak(p.bestStreak);
        setCanClaim(p.canClaimToday);
        setDayInCycle(p.dayInCycle);
        setCycle(p.cycle);
        // Auto-open the modal the first time we hear about a pending claim.
        if(p.canClaimToday) setIsVisible(true);
        // If we already claimed and this is the fresh-login push, keep it closed.
        if(!p.canClaimToday && isFreshLogin) setIsVisible(false);
        setSubmitting(false);
    });

    useMessageEvent<DailyStreakClaimedEvent>(DailyStreakClaimedEvent, event =>
    {
        const p = event.getParser();
        setSubmitting(false);
        if(p.success)
        {
            const label = p.kind === KIND_CREDITS ? p.amount + ' credits'
                : p.kind === KIND_PIXELS ? p.amount + ' duckets'
                : p.kind === KIND_DIAMONDS ? p.amount + ' diamonds'
                : p.badgeCode;
            const milestone = p.milestoneAwarded ? ` + badge ${p.milestoneBadge}!` : '';
            setMessage(`+${label}${milestone}`);
        }
        else
        {
            setMessage('Already claimed today.');
        }
    });

    // After registration, ask server for latest state (handles login race).
    useEffect(() =>
    {
        if(!registered) return;
        SendMessageComposer(new DailyStreakRequestInfoComposer());
    }, [ registered ]);

    const submit = () =>
    {
        if(submitting || !canClaim) return;
        setSubmitting(true);
        setMessage('');
        SendMessageComposer(new DailyStreakClaimComposer());
    };

    const grid = useMemo(() => cycle.slice().sort((a, b) => a.day - b.day), [ cycle ]);

    if(!isVisible) return null;

    return (
        <NitroCardView uniqueKey="daily-streak" className="nitro-daily-streak" theme="primary-slim">
            <NitroCardHeaderView headerText="Daily reward" onCloseClick={ () => setIsVisible(false) } />
            <NitroCardSubHeaderView>
                <Flex alignItems="center" gap={ 2 }>
                    <FaFire className="text-danger" />
                    <Text bold>Streak: { currentStreak } { currentStreak === 1 ? 'day' : 'days' }</Text>
                    <Text small className="text-muted">(best: { bestStreak })</Text>
                </Flex>
            </NitroCardSubHeaderView>
            <NitroCardContentView className="text-black">
                <Column gap={ 2 }>
                    <Flex gap={ 1 } justifyContent="center" style={ { flexWrap: 'wrap' } }>
                        { grid.map(r =>
                        {
                            const isToday = r.day === dayInCycle;
                            const isPast = !canClaim && r.day <= dayInCycle;
                            const wasPast = canClaim && r.day < dayInCycle;
                            const claimedLook = isPast || wasPast;
                            const todayClaimable = isToday && canClaim;
                            const style: any = {
                                width: 70, height: 78, borderRadius: 8, padding: 4,
                                border: '2px solid ' + (todayClaimable ? '#28a745' : (claimedLook ? '#999' : '#ddd')),
                                background: todayClaimable ? '#e9f9ee' : (claimedLook ? '#f1f1f1' : '#fff'),
                                opacity: claimedLook ? 0.6 : 1,
                                boxShadow: todayClaimable ? '0 0 0 3px rgba(40,167,69,.2)' : 'none'
                            };
                            return (
                                <Column key={ r.day } alignItems="center" justifyContent="center" gap={ 0 } style={ style }>
                                    <Text small bold>Day { r.day }</Text>
                                    <Flex justifyContent="center" alignItems="center" style={ { fontSize: 22, height: 26 } }>
                                        { rewardIcon(r.kind) }
                                    </Flex>
                                    <Text small>{ rewardLabel(r) }</Text>
                                </Column>
                            );
                        }) }
                    </Flex>
                    { !!message && <Text className={ canClaim ? '' : 'text-success' } bold center>{ message }</Text> }
                    <Flex justifyContent="end" gap={ 1 }>
                        { canClaim
                            ? <Button variant="success" disabled={ submitting } onClick={ submit }>
                                { submitting ? 'Claiming...' : 'Claim today' }
                              </Button>
                            : <Button variant="secondary" onClick={ () => setIsVisible(false) }>Close</Button>
                        }
                    </Flex>
                </Column>
            </NitroCardContentView>
        </NitroCardView>
    );
}

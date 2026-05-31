import { FC, useEffect, useState } from 'react';
import { FaPlus, FaTrash } from 'react-icons/fa';
import { LocalizeText, WiredFurniType } from '../../../../api';
import { Button, Slider, Text } from '../../../../common';
import { useWired } from '../../../../hooks';
import { NitroInput } from '../../../../layout';
import { WiredActionBaseView } from './WiredActionBaseView';
import { WiredSourcesSelector } from '../WiredSourcesSelector';

type RewardType = 'badge' | 'credits' | 'pixels' | 'diamonds' | 'points' | 'furni' | 'respect';

interface RewardEntry
{
    rewardType: RewardType;
    rewardValue: string;
    probability: number;
    pointsType: number;
}

const DEFAULT_PROBABILITY = 100;
const DEFAULT_POINTS_TYPE = 5;

const REWARD_TYPES: { value: RewardType, labelKey: string }[] = [
    { value: 'badge', labelKey: 'wired.reward.type.badge' },
    { value: 'credits', labelKey: 'wired.reward.type.credits' },
    { value: 'pixels', labelKey: 'wired.reward.type.pixels' },
    { value: 'diamonds', labelKey: 'wired.reward.type.diamonds' },
    { value: 'points', labelKey: 'wired.reward.type.points' },
    { value: 'furni', labelKey: 'wired.reward.type.furni' },
    { value: 'respect', labelKey: 'wired.reward.type.respect' }
];

const SELECTABLE_REWARD_TYPES = REWARD_TYPES.filter(entry => (entry.value !== 'respect'));

const createReward = (): RewardEntry =>
    ({
        rewardType: 'furni',
        rewardValue: '',
        probability: DEFAULT_PROBABILITY,
        pointsType: DEFAULT_POINTS_TYPE
    });

const getRewardValuePlaceholderKey = (rewardType: RewardType) =>
{
    switch(rewardType)
    {
        case 'badge':
            return 'wired.reward.value.placeholder.badge';
        case 'credits':
            return 'wired.reward.value.placeholder.credits';
        case 'pixels':
            return 'wired.reward.value.placeholder.pixels';
        case 'diamonds':
            return 'wired.reward.value.placeholder.diamonds';
        case 'points':
            return 'wired.reward.value.placeholder.points';
        case 'furni':
            return 'wired.reward.value.placeholder.furni';
        case 'respect':
            return 'wired.reward.value.placeholder.respect';
    }
};

const getExtraFieldLabelKey = (rewardType: RewardType) =>
{
    switch(rewardType)
    {
        case 'points':
            return 'wired.reward.extra.label.points';
        case 'badge':
            return 'wired.reward.extra.label.badge';
        default:
            return 'wired.reward.extra.label.info';
    }
};

const getExtraFieldPlaceholderKey = (rewardType: RewardType) =>
{
    switch(rewardType)
    {
        case 'points':
            return 'wired.reward.extra.placeholder.points';
        case 'badge':
            return 'wired.reward.extra.placeholder.badge';
        default:
            return '';
    }
};

const parseRewardEntry = (rawType: string, rawCode: string, rawProbability: string): RewardEntry =>
{
    const probability = Number(rawProbability);
    const parsedProbability = Number.isFinite(probability) ? probability : DEFAULT_PROBABILITY;

    if(rawType === '0')
    {
        return { rewardType: 'badge', rewardValue: rawCode, probability: parsedProbability, pointsType: DEFAULT_POINTS_TYPE };
    }

    const separatorIndex = rawCode.indexOf('#');

    if(separatorIndex === -1)
    {
        return { rewardType: 'furni', rewardValue: rawCode, probability: parsedProbability, pointsType: DEFAULT_POINTS_TYPE };
    }

    const rewardType = rawCode.slice(0, separatorIndex);
    const rewardValue = rawCode.slice(separatorIndex + 1);

    if(rewardType.startsWith('points'))
    {
        const pointsType = Number(rewardType.slice('points'.length));

        return {
            rewardType: 'points',
            rewardValue,
            probability: parsedProbability,
            pointsType: Number.isFinite(pointsType) ? pointsType : DEFAULT_POINTS_TYPE
        };
    }

    if(REWARD_TYPES.some(entry => (entry.value === rewardType)))
    {
        return { rewardType: rewardType as RewardType, rewardValue, probability: parsedProbability, pointsType: DEFAULT_POINTS_TYPE };
    }

    if(rewardType === 'cata')
    {
        return { rewardType: 'furni', rewardValue, probability: parsedProbability, pointsType: DEFAULT_POINTS_TYPE };
    }

    return { rewardType: 'furni', rewardValue: rawCode, probability: parsedProbability, pointsType: DEFAULT_POINTS_TYPE };
};

export const WiredActionGiveRewardView: FC<{}> = props =>
{
    const [ limitEnabled, setLimitEnabled ] = useState(false);
    const [ rewardTime, setRewardTime ] = useState(1);
    const [ uniqueRewards, setUniqueRewards ] = useState(false);
    const [ rewardsLimit, setRewardsLimit ] = useState(1);
    const [ limitationInterval, setLimitationInterval ] = useState(1);
    const [ rewards, setRewards ] = useState<RewardEntry[]>([]);
    const { trigger = null, setIntParams = null, setStringParam = null } = useWired();
    const [ userSource, setUserSource ] = useState<number>(() =>
    {
        if(trigger?.intData?.length > 4) return trigger.intData[4];
        return 0;
    });

    const addReward = () => setRewards(rewards => [ ...rewards, createReward() ]);
    const hasCustomCurrencyReward = rewards.some(reward => (reward.rewardType === 'points'));

    const removeReward = (index: number) =>
    {
        setRewards(prevValue =>
        {
            const newValues = Array.from(prevValue);

            newValues.splice(index, 1);

            return newValues;
        });
    };

    const updateReward = (index: number, updater: (reward: RewardEntry) => RewardEntry) =>
    {
        setRewards(prevValue => prevValue.map((reward, rewardIndex) => ((rewardIndex === index) ? updater(reward) : reward)));
    };

    const save = () =>
    {
        let stringRewards = [];

        for(const reward of rewards)
        {
            const rewardValue = reward.rewardValue.trim();

            if(!rewardValue) continue;

            const probability = Math.max(0, Number.isFinite(reward.probability) ? reward.probability : DEFAULT_PROBABILITY);
            const rewardCode = (() =>
            {
                if(reward.rewardType === 'badge') return rewardValue;
                if(reward.rewardType === 'points') return `points${ Math.max(0, reward.pointsType) }#${ rewardValue }`;

                return `${ reward.rewardType }#${ rewardValue }`;
            })();

            const rewardsString = [ reward.rewardType === 'badge' ? '0' : '1', rewardCode, (uniqueRewards ? DEFAULT_PROBABILITY : probability).toString() ];
            stringRewards.push(rewardsString.join(','));
        }

        if(stringRewards.length > 0)
        {
            setStringParam(stringRewards.join(';'));
            setIntParams([ rewardTime, uniqueRewards ? 1 : 0, rewardsLimit, limitationInterval, userSource ]);
        }
    };

    useEffect(() =>
    {
        const readRewards: RewardEntry[] = [];

        if(trigger.stringData.length > 0)
        {
            const splittedRewards = trigger.stringData.split(';');

            for(const rawReward of splittedRewards)
            {
                const reward = rawReward.split(',');

                if(reward.length !== 3) continue;

                readRewards.push(parseRewardEntry(reward[0], reward[1], reward[2]));
            }
        }

        if(readRewards.length === 0) readRewards.push(createReward());

        setRewardTime((trigger.intData.length > 0) ? trigger.intData[0] : 0);
        setUniqueRewards((trigger.intData.length > 1) ? (trigger.intData[1] === 1) : false);
        setRewardsLimit((trigger.intData.length > 2) ? trigger.intData[2] : 0);
        setLimitationInterval((trigger.intData.length > 3) ? trigger.intData[3] : 0);
        setLimitEnabled((trigger.intData.length > 3) ? trigger.intData[3] > 0 : false);
        setUserSource((trigger.intData.length > 4) ? trigger.intData[4] : 0);
        setRewards(readRewards);
    }, [ trigger ]);

    return (
        <WiredActionBaseView
            hasSpecialInput={ true }
            requiresFurni={ WiredFurniType.STUFF_SELECTION_OPTION_NONE }
            save={ save }
            footer={ <WiredSourcesSelector showUsers={ true } userSource={ userSource } onChangeUsers={ setUserSource } /> }>
            <div className="flex items-center gap-1">
                <input className="form-check-input" id="limitEnabled" type="checkbox" onChange={ event => setLimitEnabled(event.target.checked) } />
                <Text>{ LocalizeText('wiredfurni.params.prizelimit', [ 'amount' ], [ limitEnabled ? rewardsLimit.toString() : '' ]) }</Text>
            </div>
            { !limitEnabled &&
                <Text center small className="p-1 rounded bg-muted">
                    { LocalizeText('wired.reward.limit.not_set') }
                </Text> }
            { limitEnabled &&
                <Slider
                    max={ 1000 }
                    min={ 1 }
                    value={ rewardsLimit }
                    onChange={ event => setRewardsLimit(event) } /> }
            <hr className="m-0 bg-dark" />
            <div className="flex flex-col gap-1">
                <Text bold>{ LocalizeText('wired.reward.how_often') }</Text>
                <div className="flex gap-1">
                    <select className="w-full form-select form-select-sm" value={ rewardTime } onChange={ (e) => setRewardTime(Number(e.target.value)) }>
                        <option value="0">{ LocalizeText('wired.reward.frequency.once') }</option>
                        <option value="3">{ LocalizeText('wired.reward.frequency.every_minutes', [ 'value' ], [ limitationInterval.toString() ]) }</option>
                        <option value="2">{ LocalizeText('wired.reward.frequency.every_hours', [ 'value' ], [ limitationInterval.toString() ]) }</option>
                        <option value="1">{ LocalizeText('wired.reward.frequency.every_days', [ 'value' ], [ limitationInterval.toString() ]) }</option>
                    </select>
                    { (rewardTime > 0) && <NitroInput type="number" value={ limitationInterval } onChange={ event => setLimitationInterval(Number(event.target.value)) } /> }
                </div>
            </div>
            <hr className="m-0 bg-dark" />
            <div className="flex items-center gap-1">
                <input checked={ uniqueRewards } className="form-check-input" id="uniqueRewards" type="checkbox" onChange={ (e) => setUniqueRewards(e.target.checked) } />
                <Text>{ LocalizeText('wired.reward.unique') }</Text>
            </div>
            <Text center small className="p-1 rounded bg-muted">
                { LocalizeText('wired.reward.unique.help') }
            </Text>
            <hr className="m-0 bg-dark" />
            <div className="flex items-center justify-between">
                <Text bold>{ LocalizeText('wired.reward.rewards') }</Text>
                <Button variant="success" onClick={ addReward }>
                    <FaPlus className="fa-icon" />
                </Button>
            </div>
            <div className="flex flex-col gap-1">
                <div className="grid grid-cols-[1.2fr_1fr_110px_150px_42px] gap-1 px-1">
                    <Text small bold>{ LocalizeText('wired.reward.column.type') }</Text>
                    <Text small bold>{ LocalizeText('wired.reward.column.amount') }</Text>
                    <Text small bold>{ uniqueRewards ? LocalizeText('wired.reward.column.mode') : LocalizeText('wired.reward.column.chance') }</Text>
                    <Text small bold>{ hasCustomCurrencyReward ? LocalizeText('wired.reward.extra.label.points') : LocalizeText('wired.reward.column.extra_info') }</Text>
                    <Text small bold>{ LocalizeText('wired.reward.column.action') }</Text>
                </div>
                { rewards && rewards.map((reward, index) =>
                {
                    const rewardTypeOptions = (reward.rewardType === 'respect')
                        ? REWARD_TYPES
                        : SELECTABLE_REWARD_TYPES;

                    return (
                        <div key={ index } className="grid grid-cols-[1.2fr_1fr_110px_150px_42px] gap-1">
                            <select className="w-full form-select form-select-sm" value={ reward.rewardType } onChange={ event => updateReward(index, prevValue => ({ ...prevValue, rewardType: event.target.value as RewardType, rewardValue: '' })) }>
                                { rewardTypeOptions.map(entry => <option key={ entry.value } value={ entry.value }>{ LocalizeText(entry.labelKey) }</option>) }
                            </select>
                            <NitroInput
                                placeholder={ LocalizeText(getRewardValuePlaceholderKey(reward.rewardType)) }
                                type={ reward.rewardType === 'badge' ? 'text' : 'number' }
                                value={ reward.rewardValue }
                                onChange={ event => updateReward(index, prevValue => ({ ...prevValue, rewardValue: event.target.value })) } />
                            { uniqueRewards
                                ? <div className="flex items-center px-2 rounded bg-muted">
                                    <Text small>{ LocalizeText('wired.reward.unique.label') }</Text>
                                </div>
                                : <NitroInput
                                    min={ 0 }
                                    max={ 100 }
                                    placeholder={ LocalizeText('wired.reward.column.chance') }
                                    type="number"
                                    value={ reward.probability }
                                    onChange={ event => updateReward(index, prevValue => ({ ...prevValue, probability: Number(event.target.value) })) } /> }
                            { (reward.rewardType === 'points')
                                ?
                                <NitroInput
                                    min={ 0 }
                                    placeholder={ LocalizeText(getExtraFieldPlaceholderKey(reward.rewardType)) }
                                    type="number"
                                    value={ reward.pointsType }
                                    onChange={ event => updateReward(index, prevValue => ({ ...prevValue, pointsType: Number(event.target.value) })) } />
                                : <div className="flex items-center px-2 rounded bg-muted">
                                    <Text small>{ LocalizeText(getExtraFieldLabelKey(reward.rewardType)) }</Text>
                                </div> }
                            <div className="flex items-center justify-end">
                                { (index > 0) &&
                                    <Button variant="danger" onClick={ event => removeReward(index) }>
                                        <FaTrash className="fa-icon" />
                                    </Button> }
                            </div>
                        </div>
                    );
                }) }
            </div>
            <Text center small className="p-1 rounded bg-muted">
                { LocalizeText('wired.reward.extra_currency.help') }
            </Text>
        </WiredActionBaseView>
    );
};

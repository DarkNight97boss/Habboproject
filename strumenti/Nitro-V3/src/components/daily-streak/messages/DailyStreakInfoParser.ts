import { IMessageDataWrapper, IMessageParser } from '@nitrots/nitro-renderer';

// Incoming header 9510 (server -> client). See server DailyStreakInfoComposer.
export interface DailyStreakReward
{
    day: number;
    kind: number;       // 0=credits, 1=pixels, 2=diamonds, 3=badge
    amount: number;
    badgeCode: string;
}

export class DailyStreakInfoParser implements IMessageParser
{
    private _currentStreak: number = 0;
    private _bestStreak: number = 0;
    private _canClaimToday: boolean = false;
    private _dayInCycle: number = 1;
    private _cycle: DailyStreakReward[] = [];

    public flush(): boolean
    {
        this._currentStreak = 0;
        this._bestStreak = 0;
        this._canClaimToday = false;
        this._dayInCycle = 1;
        this._cycle = [];

        return true;
    }

    public parse(wrapper: IMessageDataWrapper): boolean
    {
        if(!wrapper) return false;

        this._currentStreak = wrapper.readInt();
        this._bestStreak = wrapper.readInt();
        this._canClaimToday = wrapper.readBoolean();
        this._dayInCycle = wrapper.readInt();

        const count = wrapper.readInt();
        const list: DailyStreakReward[] = [];

        for(let i = 0; i < count; i++)
        {
            const day = wrapper.readInt();
            const kind = wrapper.readInt();
            const amount = wrapper.readInt();
            const badgeCode = wrapper.readString();
            list.push({ day, kind, amount, badgeCode });
        }

        this._cycle = list;
        return true;
    }

    public get currentStreak(): number { return this._currentStreak; }
    public get bestStreak(): number { return this._bestStreak; }
    public get canClaimToday(): boolean { return this._canClaimToday; }
    public get dayInCycle(): number { return this._dayInCycle; }
    public get cycle(): DailyStreakReward[] { return this._cycle; }
}

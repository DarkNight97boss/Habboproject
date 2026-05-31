import { IMessageDataWrapper, IMessageParser } from '@nitrots/nitro-renderer';

// Incoming header 9511 (server -> client). See server DailyStreakClaimedComposer.
export class DailyStreakClaimedParser implements IMessageParser
{
    private _success: boolean = false;
    private _newStreak: number = 0;
    private _dayClaimed: number = 0;
    private _kind: number = 0;
    private _amount: number = 0;
    private _badgeCode: string = '';
    private _milestoneAwarded: boolean = false;
    private _milestoneBadge: string = '';

    public flush(): boolean
    {
        this._success = false;
        this._newStreak = 0;
        this._dayClaimed = 0;
        this._kind = 0;
        this._amount = 0;
        this._badgeCode = '';
        this._milestoneAwarded = false;
        this._milestoneBadge = '';

        return true;
    }

    public parse(wrapper: IMessageDataWrapper): boolean
    {
        if(!wrapper) return false;

        this._success = wrapper.readBoolean();
        this._newStreak = wrapper.readInt();
        this._dayClaimed = wrapper.readInt();
        this._kind = wrapper.readInt();
        this._amount = wrapper.readInt();
        this._badgeCode = wrapper.readString();
        this._milestoneAwarded = wrapper.readBoolean();
        this._milestoneBadge = wrapper.readString();

        return true;
    }

    public get success(): boolean { return this._success; }
    public get newStreak(): number { return this._newStreak; }
    public get dayClaimed(): number { return this._dayClaimed; }
    public get kind(): number { return this._kind; }
    public get amount(): number { return this._amount; }
    public get badgeCode(): string { return this._badgeCode; }
    public get milestoneAwarded(): boolean { return this._milestoneAwarded; }
    public get milestoneBadge(): string { return this._milestoneBadge; }
}

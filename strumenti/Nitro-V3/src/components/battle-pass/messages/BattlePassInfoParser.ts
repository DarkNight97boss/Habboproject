import { IMessageDataWrapper, IMessageParser } from '@nitrots/nitro-renderer';

export interface BattlePassTier
{
    tier: number;
    freeKind: number;
    freeAmount: number;
    freeBadge: string;
    premiumKind: number;
    premiumAmount: number;
    premiumBadge: string;
    freeClaimed: boolean;
    premiumClaimed: boolean;
}

// Incoming header 9530. See server BattlePassInfoComposer.
export class BattlePassInfoParser implements IMessageParser
{
    private _seasonId: number = 0;
    private _seasonName: string = '';
    private _seasonStart: number = 0;
    private _seasonEnd: number = 0;
    private _xp: number = 0;
    private _xpPerTier: number = 500;
    private _currentTier: number = 0;
    private _isPremium: boolean = false;
    private _premiumCost: number = 0;
    private _tiers: BattlePassTier[] = [];

    public flush(): boolean
    {
        this._seasonId = 0;
        this._seasonName = '';
        this._seasonStart = 0;
        this._seasonEnd = 0;
        this._xp = 0;
        this._xpPerTier = 500;
        this._currentTier = 0;
        this._isPremium = false;
        this._premiumCost = 0;
        this._tiers = [];

        return true;
    }

    public parse(wrapper: IMessageDataWrapper): boolean
    {
        if(!wrapper) return false;

        this._seasonId = wrapper.readInt();
        this._seasonName = wrapper.readString();
        this._seasonStart = wrapper.readInt();
        this._seasonEnd = wrapper.readInt();
        this._xp = wrapper.readInt();
        this._xpPerTier = wrapper.readInt();
        this._currentTier = wrapper.readInt();
        this._isPremium = wrapper.readBoolean();
        this._premiumCost = wrapper.readInt();

        const count = wrapper.readInt();
        const tiers: BattlePassTier[] = [];

        for(let i = 0; i < count; i++)
        {
            tiers.push({
                tier: wrapper.readInt(),
                freeKind: wrapper.readInt(),
                freeAmount: wrapper.readInt(),
                freeBadge: wrapper.readString(),
                premiumKind: wrapper.readInt(),
                premiumAmount: wrapper.readInt(),
                premiumBadge: wrapper.readString(),
                freeClaimed: wrapper.readBoolean(),
                premiumClaimed: wrapper.readBoolean()
            });
        }

        this._tiers = tiers;
        return true;
    }

    public get seasonId(): number { return this._seasonId; }
    public get seasonName(): string { return this._seasonName; }
    public get seasonStart(): number { return this._seasonStart; }
    public get seasonEnd(): number { return this._seasonEnd; }
    public get xp(): number { return this._xp; }
    public get xpPerTier(): number { return this._xpPerTier; }
    public get currentTier(): number { return this._currentTier; }
    public get isPremium(): boolean { return this._isPremium; }
    public get premiumCost(): number { return this._premiumCost; }
    public get tiers(): BattlePassTier[] { return this._tiers; }
}

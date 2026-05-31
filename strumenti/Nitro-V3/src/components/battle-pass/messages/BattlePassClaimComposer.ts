import { IMessageComposer } from '@nitrots/nitro-renderer';

// Outgoing header 9531. Wire: int tier, bool premium.
export class BattlePassClaimComposer implements IMessageComposer<ConstructorParameters<typeof BattlePassClaimComposer>>
{
    private _data: ConstructorParameters<typeof BattlePassClaimComposer>;

    constructor(tier: number, premium: boolean)
    {
        this._data = [ tier, premium ];
    }

    public getMessageArray() { return this._data; }
    public dispose(): void { return; }
}

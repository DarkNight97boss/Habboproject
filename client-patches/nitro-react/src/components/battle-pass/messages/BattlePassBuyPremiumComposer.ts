import { IMessageComposer } from '@nitrots/nitro-renderer';

// Outgoing header 9532. No payload.
export class BattlePassBuyPremiumComposer implements IMessageComposer<ConstructorParameters<typeof BattlePassBuyPremiumComposer>>
{
    private _data: ConstructorParameters<typeof BattlePassBuyPremiumComposer>;

    constructor()
    {
        this._data = [];
    }

    public getMessageArray() { return this._data; }
    public dispose(): void { return; }
}

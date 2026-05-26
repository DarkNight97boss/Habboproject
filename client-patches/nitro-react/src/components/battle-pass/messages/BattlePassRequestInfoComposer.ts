import { IMessageComposer } from '@nitrots/nitro-renderer';

// Outgoing header 9530.
export class BattlePassRequestInfoComposer implements IMessageComposer<ConstructorParameters<typeof BattlePassRequestInfoComposer>>
{
    private _data: ConstructorParameters<typeof BattlePassRequestInfoComposer>;

    constructor()
    {
        this._data = [];
    }

    public getMessageArray() { return this._data; }
    public dispose(): void { return; }
}

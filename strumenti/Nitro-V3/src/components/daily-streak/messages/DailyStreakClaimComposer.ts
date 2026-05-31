import { IMessageComposer } from '@nitrots/nitro-renderer';

// Outgoing header 9510 (client -> server). No payload.
export class DailyStreakClaimComposer implements IMessageComposer<ConstructorParameters<typeof DailyStreakClaimComposer>>
{
    private _data: ConstructorParameters<typeof DailyStreakClaimComposer>;

    constructor()
    {
        this._data = [];
    }

    public getMessageArray()
    {
        return this._data;
    }

    public dispose(): void
    {
        return;
    }
}

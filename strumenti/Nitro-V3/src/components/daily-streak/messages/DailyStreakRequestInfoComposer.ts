import { IMessageComposer } from '@nitrots/nitro-renderer';

// Outgoing header 9511 (client -> server). No payload.
export class DailyStreakRequestInfoComposer implements IMessageComposer<ConstructorParameters<typeof DailyStreakRequestInfoComposer>>
{
    private _data: ConstructorParameters<typeof DailyStreakRequestInfoComposer>;

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

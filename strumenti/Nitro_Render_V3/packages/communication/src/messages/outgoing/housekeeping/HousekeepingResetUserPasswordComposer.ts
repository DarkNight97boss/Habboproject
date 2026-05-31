import { IMessageComposer } from '@nitrots/api';

export class HousekeepingResetUserPasswordComposer implements IMessageComposer<ConstructorParameters<typeof HousekeepingResetUserPasswordComposer>>
{
    private _data: ConstructorParameters<typeof HousekeepingResetUserPasswordComposer>;

    constructor(userId: number)
    {
        this._data = [userId];
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

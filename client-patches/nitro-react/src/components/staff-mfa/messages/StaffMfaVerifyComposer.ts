import { IMessageComposer } from '@nitrots/nitro-renderer';

// Outgoing header 9501 (client -> server): submit a 6-digit authenticator code.
export class StaffMfaVerifyComposer implements IMessageComposer<ConstructorParameters<typeof StaffMfaVerifyComposer>>
{
    private _data: ConstructorParameters<typeof StaffMfaVerifyComposer>;

    constructor(code: string)
    {
        this._data = [ code ];
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

import { IMessageComposer } from '@nitrots/nitro-renderer';

// Outgoing header 9503 (client -> server): ask whether this session still needs
// MFA. Sent once when the modal mounts, to avoid the login-time race where the
// proactive popup can arrive before this handler is registered. No payload.
export class StaffMfaStatusRequestComposer implements IMessageComposer<ConstructorParameters<typeof StaffMfaStatusRequestComposer>>
{
    private _data: ConstructorParameters<typeof StaffMfaStatusRequestComposer>;

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

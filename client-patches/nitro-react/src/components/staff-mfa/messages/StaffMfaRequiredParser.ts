import { IMessageDataWrapper, IMessageParser } from '@nitrots/nitro-renderer';

// Incoming header 9500 (server -> client): show the staff MFA popup.
export class StaffMfaRequiredParser implements IMessageParser
{
    private _enrolled: boolean;
    private _secret: string;
    private _otpauthUri: string;

    public flush(): boolean
    {
        this._enrolled = false;
        this._secret = null;
        this._otpauthUri = null;

        return true;
    }

    public parse(wrapper: IMessageDataWrapper): boolean
    {
        if(!wrapper) return false;

        this._enrolled = wrapper.readBoolean();
        this._secret = wrapper.readString();
        this._otpauthUri = wrapper.readString();

        return true;
    }

    public get enrolled(): boolean
    {
        return this._enrolled;
    }

    public get secret(): string
    {
        return this._secret;
    }

    public get otpauthUri(): string
    {
        return this._otpauthUri;
    }
}

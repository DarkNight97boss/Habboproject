import { IMessageDataWrapper, IMessageParser } from '@nitrots/nitro-renderer';

// Incoming header 9532.
export class BattlePassPremiumPurchasedParser implements IMessageParser
{
    private _success: boolean = false;
    private _message: string = '';

    public flush(): boolean
    {
        this._success = false;
        this._message = '';
        return true;
    }

    public parse(wrapper: IMessageDataWrapper): boolean
    {
        if(!wrapper) return false;
        this._success = wrapper.readBoolean();
        this._message = wrapper.readString();
        return true;
    }

    public get success(): boolean { return this._success; }
    public get message(): string { return this._message; }
}

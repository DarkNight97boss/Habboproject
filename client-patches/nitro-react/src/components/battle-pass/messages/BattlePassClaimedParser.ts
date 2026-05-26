import { IMessageDataWrapper, IMessageParser } from '@nitrots/nitro-renderer';

// Incoming header 9531.
export class BattlePassClaimedParser implements IMessageParser
{
    private _success: boolean = false;
    private _tier: number = 0;
    private _premium: boolean = false;
    private _kind: number = 0;
    private _amount: number = 0;
    private _badge: string = '';
    private _message: string = '';

    public flush(): boolean
    {
        this._success = false;
        this._tier = 0;
        this._premium = false;
        this._kind = 0;
        this._amount = 0;
        this._badge = '';
        this._message = '';
        return true;
    }

    public parse(wrapper: IMessageDataWrapper): boolean
    {
        if(!wrapper) return false;
        this._success = wrapper.readBoolean();
        this._tier = wrapper.readInt();
        this._premium = wrapper.readBoolean();
        this._kind = wrapper.readInt();
        this._amount = wrapper.readInt();
        this._badge = wrapper.readString();
        this._message = wrapper.readString();
        return true;
    }

    public get success(): boolean { return this._success; }
    public get tier(): number { return this._tier; }
    public get premium(): boolean { return this._premium; }
    public get kind(): number { return this._kind; }
    public get amount(): number { return this._amount; }
    public get badge(): string { return this._badge; }
    public get message(): string { return this._message; }
}

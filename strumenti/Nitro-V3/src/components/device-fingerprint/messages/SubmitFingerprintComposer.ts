import { IMessageComposer } from '@nitrots/nitro-renderer';

// Outgoing header 9520 (client -> server). 6 strings:
//   userAgent, platform, screen ("WxH@cd"), timezone, language, canvasHash
export class SubmitFingerprintComposer implements IMessageComposer<ConstructorParameters<typeof SubmitFingerprintComposer>>
{
    private _data: ConstructorParameters<typeof SubmitFingerprintComposer>;

    constructor(userAgent: string, platform: string, screen: string, timezone: string, language: string, canvasHash: string)
    {
        this._data = [ userAgent, platform, screen, timezone, language, canvasHash ];
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

import { IMessageConfiguration } from '@nitrots/nitro-renderer';
import { SubmitFingerprintComposer } from './SubmitFingerprintComposer';

// Custom message header for the anti-multiaccount fingerprint submission.
// Outgoing.SubmitFingerprintEvent = 9520 (client -> server)
export class DeviceFingerprintMessageConfiguration implements IMessageConfiguration
{
    private _events: Map<number, Function>;
    private _composers: Map<number, Function>;

    constructor()
    {
        this._events = new Map();
        this._composers = new Map();
        this._composers.set(9520, SubmitFingerprintComposer);
    }

    public get events(): Map<number, Function> { return this._events; }
    public get composers(): Map<number, Function> { return this._composers; }
}

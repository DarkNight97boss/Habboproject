import { IMessageConfiguration } from '@nitrots/nitro-renderer';
import { StaffMfaRequiredEvent } from './StaffMfaRequiredEvent';
import { StaffMfaResultEvent } from './StaffMfaResultEvent';
import { StaffMfaStatusRequestComposer } from './StaffMfaStatusRequestComposer';
import { StaffMfaVerifyComposer } from './StaffMfaVerifyComposer';

// Custom message headers for staff MFA. These MUST match the emulator:
//   Outgoing.StaffMfaRequiredComposer       = 9500  (server -> client, event)
//   Incoming.StaffMfaVerifyEvent            = 9501  (client -> server, composer)
//   Outgoing.StaffMfaResultComposer         = 9502  (server -> client, event)
//   Incoming.StaffMfaStatusRequestEvent     = 9503  (client -> server, composer)
//
// Registered at runtime via GetConnection().registerMessages(...) so we don't
// have to fork @nitrots/nitro-renderer.
export class StaffMfaMessageConfiguration implements IMessageConfiguration
{
    private _events: Map<number, Function>;
    private _composers: Map<number, Function>;

    constructor()
    {
        this._events = new Map();
        this._composers = new Map();

        this._events.set(9500, StaffMfaRequiredEvent);
        this._events.set(9502, StaffMfaResultEvent);

        this._composers.set(9501, StaffMfaVerifyComposer);
        this._composers.set(9503, StaffMfaStatusRequestComposer);
    }

    public get events(): Map<number, Function>
    {
        return this._events;
    }

    public get composers(): Map<number, Function>
    {
        return this._composers;
    }
}

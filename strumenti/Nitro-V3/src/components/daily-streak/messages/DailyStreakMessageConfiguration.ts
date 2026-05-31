import { IMessageConfiguration } from '@nitrots/nitro-renderer';
import { DailyStreakClaimComposer } from './DailyStreakClaimComposer';
import { DailyStreakClaimedEvent } from './DailyStreakClaimedEvent';
import { DailyStreakInfoEvent } from './DailyStreakInfoEvent';
import { DailyStreakRequestInfoComposer } from './DailyStreakRequestInfoComposer';

// Custom message headers for daily streak. Must match the emulator:
//   Outgoing.DailyStreakInfoComposer       = 9510  (server -> client, event)
//   Incoming.DailyStreakClaimEvent         = 9510  (client -> server, composer)
//   Outgoing.DailyStreakClaimedComposer    = 9511  (server -> client, event)
//   Incoming.DailyStreakRequestInfoEvent   = 9511  (client -> server, composer)
//
// (Headers are namespaced by direction in nitro, so 9510 in/out are distinct.)
export class DailyStreakMessageConfiguration implements IMessageConfiguration
{
    private _events: Map<number, Function>;
    private _composers: Map<number, Function>;

    constructor()
    {
        this._events = new Map();
        this._composers = new Map();

        this._events.set(9510, DailyStreakInfoEvent);
        this._events.set(9511, DailyStreakClaimedEvent);

        this._composers.set(9510, DailyStreakClaimComposer);
        this._composers.set(9511, DailyStreakRequestInfoComposer);
    }

    public get events(): Map<number, Function> { return this._events; }
    public get composers(): Map<number, Function> { return this._composers; }
}

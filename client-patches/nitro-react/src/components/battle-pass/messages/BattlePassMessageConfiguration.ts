import { IMessageConfiguration } from '@nitrots/nitro-renderer';
import { BattlePassBuyPremiumComposer } from './BattlePassBuyPremiumComposer';
import { BattlePassClaimComposer } from './BattlePassClaimComposer';
import { BattlePassClaimedEvent } from './BattlePassClaimedEvent';
import { BattlePassInfoEvent } from './BattlePassInfoEvent';
import { BattlePassPremiumPurchasedEvent } from './BattlePassPremiumPurchasedEvent';
import { BattlePassRequestInfoComposer } from './BattlePassRequestInfoComposer';

// Match the emulator:
//   in  9530 BattlePassInfoComposer       (event)
//   in  9531 BattlePassClaimedComposer    (event)
//   in  9532 BattlePassPremiumPurchasedComposer (event)
//   out 9530 BattlePassRequestInfoEvent   (composer)
//   out 9531 BattlePassClaimEvent         (composer)
//   out 9532 BattlePassBuyPremiumEvent    (composer)
export class BattlePassMessageConfiguration implements IMessageConfiguration
{
    private _events: Map<number, Function>;
    private _composers: Map<number, Function>;

    constructor()
    {
        this._events = new Map();
        this._composers = new Map();

        this._events.set(9530, BattlePassInfoEvent);
        this._events.set(9531, BattlePassClaimedEvent);
        this._events.set(9532, BattlePassPremiumPurchasedEvent);

        this._composers.set(9530, BattlePassRequestInfoComposer);
        this._composers.set(9531, BattlePassClaimComposer);
        this._composers.set(9532, BattlePassBuyPremiumComposer);
    }

    public get events(): Map<number, Function> { return this._events; }
    public get composers(): Map<number, Function> { return this._composers; }
}

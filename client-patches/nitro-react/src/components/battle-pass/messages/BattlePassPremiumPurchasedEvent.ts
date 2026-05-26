import { IMessageEvent, MessageEvent } from '@nitrots/nitro-renderer';
import { BattlePassPremiumPurchasedParser } from './BattlePassPremiumPurchasedParser';

export class BattlePassPremiumPurchasedEvent extends MessageEvent implements IMessageEvent
{
    constructor(callBack: Function)
    {
        super(callBack, BattlePassPremiumPurchasedParser);
    }

    public getParser(): BattlePassPremiumPurchasedParser
    {
        return this.parser as BattlePassPremiumPurchasedParser;
    }
}

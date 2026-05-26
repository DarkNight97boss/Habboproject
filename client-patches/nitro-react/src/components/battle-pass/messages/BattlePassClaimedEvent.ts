import { IMessageEvent, MessageEvent } from '@nitrots/nitro-renderer';
import { BattlePassClaimedParser } from './BattlePassClaimedParser';

export class BattlePassClaimedEvent extends MessageEvent implements IMessageEvent
{
    constructor(callBack: Function)
    {
        super(callBack, BattlePassClaimedParser);
    }

    public getParser(): BattlePassClaimedParser
    {
        return this.parser as BattlePassClaimedParser;
    }
}

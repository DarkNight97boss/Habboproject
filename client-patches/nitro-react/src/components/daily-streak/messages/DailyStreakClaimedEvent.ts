import { IMessageEvent, MessageEvent } from '@nitrots/nitro-renderer';
import { DailyStreakClaimedParser } from './DailyStreakClaimedParser';

export class DailyStreakClaimedEvent extends MessageEvent implements IMessageEvent
{
    constructor(callBack: Function)
    {
        super(callBack, DailyStreakClaimedParser);
    }

    public getParser(): DailyStreakClaimedParser
    {
        return this.parser as DailyStreakClaimedParser;
    }
}

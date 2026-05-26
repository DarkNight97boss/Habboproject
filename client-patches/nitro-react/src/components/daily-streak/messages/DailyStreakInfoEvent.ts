import { IMessageEvent, MessageEvent } from '@nitrots/nitro-renderer';
import { DailyStreakInfoParser } from './DailyStreakInfoParser';

export class DailyStreakInfoEvent extends MessageEvent implements IMessageEvent
{
    constructor(callBack: Function)
    {
        super(callBack, DailyStreakInfoParser);
    }

    public getParser(): DailyStreakInfoParser
    {
        return this.parser as DailyStreakInfoParser;
    }
}

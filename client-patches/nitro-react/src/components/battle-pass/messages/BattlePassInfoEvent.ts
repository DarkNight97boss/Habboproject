import { IMessageEvent, MessageEvent } from '@nitrots/nitro-renderer';
import { BattlePassInfoParser } from './BattlePassInfoParser';

export class BattlePassInfoEvent extends MessageEvent implements IMessageEvent
{
    constructor(callBack: Function)
    {
        super(callBack, BattlePassInfoParser);
    }

    public getParser(): BattlePassInfoParser
    {
        return this.parser as BattlePassInfoParser;
    }
}

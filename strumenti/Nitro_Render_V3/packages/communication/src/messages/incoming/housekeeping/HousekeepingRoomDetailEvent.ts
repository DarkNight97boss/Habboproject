import { IMessageEvent } from '@nitrots/api';
import { MessageEvent } from '@nitrots/events';
import { HousekeepingRoomDetailParser } from '../../parser';

export class HousekeepingRoomDetailEvent extends MessageEvent implements IMessageEvent
{
    constructor(callBack: Function)
    {
        super(callBack, HousekeepingRoomDetailParser);
    }

    public getParser(): HousekeepingRoomDetailParser
    {
        return this.parser as HousekeepingRoomDetailParser;
    }
}

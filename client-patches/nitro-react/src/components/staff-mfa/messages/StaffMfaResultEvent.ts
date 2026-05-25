import { IMessageEvent, MessageEvent } from '@nitrots/nitro-renderer';
import { StaffMfaResultParser } from './StaffMfaResultParser';

export class StaffMfaResultEvent extends MessageEvent implements IMessageEvent
{
    constructor(callBack: Function)
    {
        super(callBack, StaffMfaResultParser);
    }

    public getParser(): StaffMfaResultParser
    {
        return this.parser as StaffMfaResultParser;
    }
}

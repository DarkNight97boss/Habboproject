import { IMessageEvent, MessageEvent } from '@nitrots/nitro-renderer';
import { StaffMfaRequiredParser } from './StaffMfaRequiredParser';

export class StaffMfaRequiredEvent extends MessageEvent implements IMessageEvent
{
    constructor(callBack: Function)
    {
        super(callBack, StaffMfaRequiredParser);
    }

    public getParser(): StaffMfaRequiredParser
    {
        return this.parser as StaffMfaRequiredParser;
    }
}

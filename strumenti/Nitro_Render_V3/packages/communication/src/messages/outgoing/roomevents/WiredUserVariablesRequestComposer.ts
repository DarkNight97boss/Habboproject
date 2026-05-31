import { IMessageComposer } from '@nitrots/api';

export class WiredUserVariablesRequestComposer implements IMessageComposer<[]>
{
    public getMessageArray(): []
    {
        return [];
    }

    public dispose(): void
    {
        return;
    }
}

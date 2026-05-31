import { IMessageComposer } from '@nitrots/api';

export class WiredRoomSettingsRequestComposer implements IMessageComposer<[]>
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

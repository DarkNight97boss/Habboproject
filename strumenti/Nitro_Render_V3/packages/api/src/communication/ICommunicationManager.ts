import { IConnection } from './IConnection';
import { IMessageEvent } from './IMessageEvent';

export interface ICommunicationManager
{
    init(): Promise<void>;
    registerMessageEvent(event: IMessageEvent): IMessageEvent;
    removeMessageEvent(event: IMessageEvent): void;
    subscribeMessage<T extends IMessageEvent>(eventCtor: new (callback: (event: T) => void) => T, handler: (event: T) => void): () => void;
    connection: IConnection;
}

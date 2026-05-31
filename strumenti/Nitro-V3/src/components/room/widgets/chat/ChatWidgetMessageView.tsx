import { GetRoomEngine, RoomChatSettings, RoomObjectCategory } from '@nitrots/nitro-renderer';
import { FC, useEffect, useMemo, useRef, useState } from 'react';
import { ChatBubbleMessage, formatStaffLinks } from '../../../../api';
import { UserIdentityView } from '../../../../common';
import { useOnClickChat, useRoomUserListSnapshot, useUserDataSnapshot } from '../../../../hooks';

// Soglia minima di security level per essere considerato staff (mod=5, admin=7).
// I link cliccabili nelle bolle sono abilitati per chiunque sia >= 5.
const STAFF_SECURITY_LEVEL = 5;

interface ChatWidgetMessageViewProps
{
    chat: ChatBubbleMessage;
    makeRoom: (chat: ChatBubbleMessage) => void;
    bubbleWidth?: number;
}

export const ChatWidgetMessageView: FC<ChatWidgetMessageViewProps> = ({
    chat = null,
    makeRoom = null,
    bubbleWidth = RoomChatSettings.CHAT_BUBBLE_WIDTH_NORMAL
}) =>
{
    const [ isVisible, setIsVisible ] = useState(false);
    const [ isReady, setIsReady ] = useState(false);
    const elementRef = useRef<HTMLDivElement>(null);
    const { onClickChat } = useOnClickChat();

    const getBubbleWidth = useMemo(() =>
    {
        switch(bubbleWidth)
        {
            case RoomChatSettings.CHAT_BUBBLE_WIDTH_NORMAL:
                return 'max-w-[350px]';
            case RoomChatSettings.CHAT_BUBBLE_WIDTH_THIN:
                return 'max-w-[240px]';
            case RoomChatSettings.CHAT_BUBBLE_WIDTH_WIDE:
                return 'max-w-[2000px]';
            default:
                return 'max-w-[350px]';
        }
    }, [ bubbleWidth ]);

    useEffect(() =>
    {
        const element = elementRef.current;
        if(!element) return;

        const previousWidth = chat.width;
        const previousHeight = chat.height;
        const { offsetWidth: width, offsetHeight: height } = element;

        chat.width = width;
        chat.height = height;
        chat.elementRef = element;

        let { left, top } = chat;

        if(!left && !top)
        {
            left = (chat.location.x - (width / 2));
            top = (element.parentElement.offsetHeight - height);

            chat.left = left;
            chat.top = top;
        }

        setIsReady(true);

        if(isVisible && ((previousWidth !== width) || (previousHeight !== height)) && makeRoom) makeRoom(chat);
    }, [ chat, chat.formattedText, chat.originalFormattedText, chat.showTranslation, chat.translatedFormattedText, isVisible, makeRoom ]);

    useEffect(() =>
    {
        return () =>
        {
            chat.elementRef = null;
        };
    }, [ chat ]);

    useEffect(() =>
    {
        if(!isReady || !chat || isVisible) return;

        if(makeRoom) makeRoom(chat);
        setIsVisible(true);
    }, [ chat, isReady, isVisible, makeRoom ]);

    const messageClassName = `message [overflow-wrap:anywhere] break-words${ chat.type === 1 ? ' italic text-[#595959]' : '' }${ chat.type === 2 ? ' font-bold' : '' }`;

    // Feature: link cliccabili nelle bolle dello staff (anti-phishing per
    // utenti normali). Usa due fonti di verita':
    //   1. se il messaggio e' del proprio utente, leggi securityLevel dallo
    //      UserData snapshot (sempre presente e affidabile)
    //   2. altrimenti cerca il sender nello snapshot lista utenti della
    //      stanza e controlla isModerator (set dal renderer su RoomUsers)
    const roomUsers = useRoomUserListSnapshot();
    const userData = useUserDataSnapshot();
    const senderIsStaff = useMemo(() =>
    {
        // chat.senderId e' il roomIndex (indice locale stanza), NON il webID.
        // Cerchiamo l'utente nello snapshot per roomIndex e poi:
        //   - se isModerator=true (set dal renderer su RoomUsers) → staff
        //   - se e' il proprio utente (webID === userData.userId) usa
        //     securityLevel come fallback affidabile
        const sender = roomUsers.find(u => u && u.roomIndex === chat.senderId);
        if(!sender) return false;
        if(sender.isModerator) return true;
        if(sender.webID === userData?.userId)
        {
            return (userData.securityLevel ?? 0) >= STAFF_SECURITY_LEVEL;
        }
        return false;
    }, [ roomUsers, chat.senderId, userData ]);

    const displayHtml = useMemo(
        () => senderIsStaff ? formatStaffLinks(chat.formattedText) : chat.formattedText,
        [ senderIsStaff, chat.formattedText ]
    );
    const originalDisplayHtml = useMemo(
        () => senderIsStaff ? formatStaffLinks(chat.originalFormattedText || chat.formattedText) : (chat.originalFormattedText || chat.formattedText),
        [ senderIsStaff, chat.originalFormattedText, chat.formattedText ]
    );
    const translatedDisplayHtml = useMemo(
        () => senderIsStaff ? formatStaffLinks(chat.translatedFormattedText || chat.formattedText) : (chat.translatedFormattedText || chat.formattedText),
        [ senderIsStaff, chat.translatedFormattedText, chat.formattedText ]
    );

    return (
        <div ref={ elementRef } className={ `bubble-container newbubblehe ${ isVisible ? 'visible' : 'invisible' } w-max absolute select-none pointer-events-auto` }
            onClick={ () => GetRoomEngine().selectRoomObject(chat.roomId, chat.senderId, RoomObjectCategory.UNIT) }>
            { chat.styleId === 0 && (
                <div className="absolute -top-px left-px w-[30px] h-[calc(100%-0.5px)] rounded-[7px] z-1" style={ { backgroundColor: chat.color } } />
            ) }
            <div className={ `chat-bubble bubble-${ chat.styleId } type-${ chat.type } ${ getBubbleWidth } relative z-1 wrap-break-word min-h-[26px] text-[14px]` }>
                <div className="user-container flex items-center justify-center h-full max-h-[24px] overflow-hidden">
                    { chat.imageUrl && chat.imageUrl.length > 0 && (
                        <div className="user-image absolute top-[-15px] left-[-9.25px] w-[45px] h-[65px] bg-no-repeat bg-center" style={ { backgroundImage: `url(${ chat.imageUrl })` } } />
                    ) }
                </div>
                <div className="chat-content py-[5px] px-[6px] ml-[27px] leading-none min-h-[25px]">
                    <UserIdentityView
                        className="mr-1 align-middle"
                        displayOrder={ chat.displayOrder }
                        iconClassName="inline-block w-auto h-auto align-[-1px]"
                        nameClassName="username font-bold"
                        nickIcon={ chat.nickIcon }
                        prefixClassName=""
                        prefixColor={ chat.prefixColor }
                        prefixEffect={ chat.prefixEffect }
                        prefixFont={ chat.prefixFont }
                        prefixIcon={ chat.prefixIcon }
                        prefixText={ chat.prefixText }
                        showColon={ true }
                        username={ chat.username } />
                    { !chat.showTranslation &&
                        <span className={ `${ messageClassName } align-middle` } dangerouslySetInnerHTML={ { __html: displayHtml } } onClick={ onClickChat } /> }
                    { chat.showTranslation &&
                        <div className="mt-[2px] flex flex-col gap-[2px]" onClick={ onClickChat }>
                            <div className="flex items-start gap-1 leading-[1.1]">
                                <span className="inline-block min-w-[52px] font-bold" style={ { opacity: 0.75 } }>original:</span>
                                <span className={ messageClassName } dangerouslySetInnerHTML={ { __html: originalDisplayHtml } } />
                            </div>
                            <div className="flex items-start gap-1 leading-[1.1]">
                                <span className="inline-block min-w-[52px] font-bold" style={ { opacity: 0.75 } }>translate:</span>
                                <span className={ messageClassName } dangerouslySetInnerHTML={ { __html: translatedDisplayHtml } } />
                            </div>
                        </div> }
                </div>
                <div className="pointer absolute left-[50%] translate-x-[-50%] w-[9px] h-[6px] bottom-[-5px]" />
            </div>
        </div>
    );
};

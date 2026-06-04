import { FC } from 'react';
import { useChatVoiceDictation } from '../../../../hooks';

interface ChatInputVoiceViewProps
{
    onTranscript: (text: string) => void;
}

/**
 * Bottone microfono accanto all'input chat: detta a voce un messaggio (il
 * parlato viene trascritto nel campo di testo). Visibile solo se il browser
 * supporta la Web Speech API. Logica in `useChatVoiceDictation`.
 */
export const ChatInputVoiceView: FC<ChatInputVoiceViewProps> = props =>
{
    const { onTranscript = null } = props;
    const { supported, listening, toggle } = useChatVoiceDictation(onTranscript);

    if(!supported) return null;

    return (
        <div
            className={ `cursor-pointer text-lg select-none px-1 ${ listening ? 'animate-pulse' : '' }` }
            title={ listening ? 'Sto ascoltando… clicca per fermare' : 'Detta a voce' }
            role="button"
            onClick={ toggle }
        >{ listening ? '🔴' : '🎤' }</div>
    );
};

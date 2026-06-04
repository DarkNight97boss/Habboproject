import { FC } from 'react';
import { useChatVoiceDictation } from '../../../../hooks';

interface ChatInputVoiceViewProps
{
    onTranscript: (text: string) => void;
}

/**
 * Bottone microfono accanto all'input chat: detta a voce un messaggio (il
 * parlato viene trascritto nel campo di testo). La dettatura resta attiva finche'
 * non si clicca di nuovo. Visibile solo se il browser supporta la Web Speech API.
 * Logica in `useChatVoiceDictation`.
 */
export const ChatInputVoiceView: FC<ChatInputVoiceViewProps> = props =>
{
    const { onTranscript = null } = props;
    const { supported, listening, toggle } = useChatVoiceDictation(onTranscript);

    if(!supported) return null;

    return (
        <div
            className={ `flex cursor-pointer select-none items-center justify-center rounded-full p-[5px] transition-colors ${ listening ? 'bg-[#e8484a]/15 text-[#e8484a]' : 'text-[#8a93a0] hover:bg-black/5 hover:text-[#495057]' }` }
            title={ listening ? 'Sto ascoltando… clicca per fermare' : 'Detta a voce' }
            role="button"
            aria-pressed={ listening }
            onClick={ toggle }
        >
            <svg viewBox="0 0 24 24" fill="currentColor" className={ `h-[18px] w-[18px] ${ listening ? 'animate-pulse' : '' }` } aria-hidden="true">
                <path d="M12 14.5a3.25 3.25 0 0 0 3.25-3.25v-5a3.25 3.25 0 0 0-6.5 0v5A3.25 3.25 0 0 0 12 14.5Z" />
                <path d="M18.5 11.25a.9.9 0 0 0-1.8 0 4.7 4.7 0 1 1-9.4 0 .9.9 0 1 0-1.8 0 6.5 6.5 0 0 0 5.6 6.44V20H9.4a.9.9 0 1 0 0 1.8h5.2a.9.9 0 1 0 0-1.8h-1.7v-2.31a6.5 6.5 0 0 0 5.6-6.44Z" />
            </svg>
        </div>
    );
};

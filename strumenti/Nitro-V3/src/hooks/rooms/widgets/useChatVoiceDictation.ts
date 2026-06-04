import { useCallback, useEffect, useRef, useState } from 'react';

/**
 * Dettatura vocale per l'input chat. Usa la Web Speech API del browser
 * (`SpeechRecognition` / `webkitSpeechRecognition`, disponibile su Chrome/Edge)
 * per trascrivere il parlato in testo, in italiano (`it-IT`).
 *
 * Ritorna `{ supported, listening, toggle }`; su ogni frase riconosciuta chiama
 * `onTranscript(testo)`. Self-contained: se l'API non c'e' `supported=false` e
 * il bottone microfono si nasconde. Nessuna dipendenza dal renderer.
 */

interface SpeechAlternativeLike { readonly transcript: string }
interface SpeechResultLike { readonly isFinal: boolean; readonly length: number; readonly [index: number]: SpeechAlternativeLike }
interface SpeechResultListLike { readonly length: number; readonly [index: number]: SpeechResultLike }
interface SpeechRecognitionEventLike { readonly resultIndex: number; readonly results: SpeechResultListLike }
interface SpeechRecognitionLike
{
    lang: string;
    continuous: boolean;
    interimResults: boolean;
    start(): void;
    stop(): void;
    abort(): void;
    onresult: ((event: SpeechRecognitionEventLike) => void) | null;
    onend: (() => void) | null;
    onerror: (() => void) | null;
}
type SpeechRecognitionCtor = new () => SpeechRecognitionLike;

const getRecognitionCtor = (): SpeechRecognitionCtor | null =>
{
    if(typeof window === 'undefined') return null;

    const w = window as unknown as { SpeechRecognition?: SpeechRecognitionCtor; webkitSpeechRecognition?: SpeechRecognitionCtor };

    return w.SpeechRecognition ?? w.webkitSpeechRecognition ?? null;
};

export const useChatVoiceDictation = (onTranscript: (text: string) => void) =>
{
    const [ supported ] = useState<boolean>(() => getRecognitionCtor() !== null);
    const [ listening, setListening ] = useState(false);
    const recognitionRef = useRef<SpeechRecognitionLike | null>(null);
    const onTranscriptRef = useRef(onTranscript);

    useEffect(() =>
    {
        onTranscriptRef.current = onTranscript;
    }, [ onTranscript ]);

    const stop = useCallback(() =>
    {
        const recognition = recognitionRef.current;

        if(recognition)
        {
            try { recognition.stop(); }
            catch { /* la sessione potrebbe essere gia' chiusa */ }
        }
    }, []);

    const start = useCallback(() =>
    {
        const RecognitionCtor = getRecognitionCtor();

        if(!RecognitionCtor) return;

        if(recognitionRef.current)
        {
            try { recognitionRef.current.abort(); }
            catch { /* noop */ }
        }

        const recognition = new RecognitionCtor();

        recognition.lang = 'it-IT';
        recognition.continuous = false;
        recognition.interimResults = false;

        recognition.onresult = (event) =>
        {
            let text = '';

            for(let i = event.resultIndex; i < event.results.length; i++)
            {
                const result = event.results[i];

                if(result && result.isFinal && result[0]) text += result[0].transcript;
            }

            text = text.trim();

            if(text) onTranscriptRef.current(text);
        };

        recognition.onend = () => setListening(false);
        recognition.onerror = () => setListening(false);

        recognitionRef.current = recognition;

        try
        {
            recognition.start();
            setListening(true);
        }
        catch
        {
            setListening(false);
        }
    }, []);

    const toggle = useCallback(() =>
    {
        if(listening) stop();
        else start();
    }, [ listening, start, stop ]);

    useEffect(() =>
    {
        return () =>
        {
            const recognition = recognitionRef.current;

            if(recognition)
            {
                try { recognition.abort(); }
                catch { /* noop */ }
            }
        };
    }, []);

    return { supported, listening, toggle };
};

import { useCallback, useEffect, useRef, useState } from 'react';

/**
 * Dettatura vocale CONTINUA per l'input chat. Usa la Web Speech API del browser
 * (`SpeechRecognition` / `webkitSpeechRecognition`, disponibile su Chrome/Edge)
 * per trascrivere il parlato in testo, in italiano (`it-IT`).
 *
 * Resta in ascolto finche' l'utente non ferma con `toggle`: se la sessione si
 * chiude da sola (timeout di silenzio del browser) viene riavviata
 * automaticamente, cosi' si puo' dettare a lungo senza ri-cliccare.
 *
 * Ritorna `{ supported, listening, toggle }`; su ogni frase finale chiama
 * `onTranscript(testo)`. Self-contained: se l'API non c'e' `supported=false` e
 * il bottone microfono si nasconde. Nessuna dipendenza dal renderer.
 */

interface SpeechAlternativeLike { readonly transcript: string }
interface SpeechResultLike { readonly isFinal: boolean; readonly length: number; readonly [index: number]: SpeechAlternativeLike }
interface SpeechResultListLike { readonly length: number; readonly [index: number]: SpeechResultLike }
interface SpeechRecognitionEventLike { readonly resultIndex: number; readonly results: SpeechResultListLike }
interface SpeechRecognitionErrorLike { readonly error?: string }
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
    onerror: ((event: SpeechRecognitionErrorLike) => void) | null;
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
    const manualStopRef = useRef(false);
    const onTranscriptRef = useRef(onTranscript);

    useEffect(() =>
    {
        onTranscriptRef.current = onTranscript;
    }, [ onTranscript ]);

    const stop = useCallback(() =>
    {
        manualStopRef.current = true;

        const recognition = recognitionRef.current;

        if(recognition)
        {
            try { recognition.stop(); }
            catch { /* la sessione potrebbe essere gia' chiusa */ }
        }

        setListening(false);
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

        manualStopRef.current = false;

        const recognition = new RecognitionCtor();

        recognition.lang = 'it-IT';
        recognition.continuous = true;
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

        recognition.onerror = (event) =>
        {
            // Errori fatali (permesso negato / servizio non disponibile): basta,
            // niente riavvio. Gli altri (no-speech, network, aborted) li gestisce
            // onend riavviando, cosi' l'ascolto resta continuo.
            if(event && (event.error === 'not-allowed' || event.error === 'service-not-allowed'))
            {
                manualStopRef.current = true;
                setListening(false);
            }
        };

        recognition.onend = () =>
        {
            // Riavvio automatico finche' l'utente non ha fermato manualmente.
            if(manualStopRef.current)
            {
                setListening(false);
                return;
            }

            try { recognition.start(); }
            catch { setListening(false); }
        };

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
            manualStopRef.current = true;

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

import { FC, useEffect, useRef } from 'react';
import { GetConnection, SendMessageComposer } from '../../api';
import { DeviceFingerprintMessageConfiguration, SubmitFingerprintComposer } from './messages';

/**
 * Anti-multiaccount fingerprint bridge.
 *
 * Mounted once. Computes a non-PII signature of the browser/device (canvas
 * render hash + UA + screen + timezone + language + platform) and submits it
 * to the server, which SHA-256s the canonical payload and stores it in
 * {@code user_fingerprints}. Detection only — never blocks the user, never
 * shows UI.
 *
 * No third-party calls: the canvas hash is computed with SubtleCrypto locally.
 */
const sha256Hex = async (input: string): Promise<string> =>
{
    try
    {
        if(typeof crypto === 'undefined' || !crypto.subtle) return '';
        const data = new TextEncoder().encode(input);
        const digest = await crypto.subtle.digest('SHA-256', data);
        const bytes = new Uint8Array(digest);
        let out = '';
        for(let i = 0; i < bytes.length; i++) out += bytes[i].toString(16).padStart(2, '0');
        return out;
    }
    catch
    {
        return '';
    }
};

const renderCanvasFingerprint = (): string =>
{
    try
    {
        if(typeof document === 'undefined') return '';
        const c = document.createElement('canvas');
        c.width = 280;
        c.height = 60;
        const ctx = c.getContext('2d');
        if(!ctx) return '';
        ctx.textBaseline = 'top';
        ctx.font = '14px "Arial"';
        ctx.fillStyle = '#f60';
        ctx.fillRect(0, 0, 280, 60);
        ctx.fillStyle = '#069';
        ctx.fillText('Habboproject fingerprint 0123!@#', 10, 20);
        ctx.fillStyle = 'rgba(102, 204, 0, 0.7)';
        ctx.fillText('Habboproject fingerprint 0123!@#', 12, 22);
        // Geometric primitive: adds GPU/anti-aliasing variation across devices.
        ctx.strokeStyle = '#333';
        ctx.beginPath();
        ctx.arc(140, 30, 18, 0, 2 * Math.PI);
        ctx.stroke();
        return c.toDataURL();
    }
    catch
    {
        return '';
    }
};

const screenString = (): string =>
{
    try
    {
        if(typeof screen === 'undefined') return '';
        return `${screen.width}x${screen.height}@${screen.colorDepth}`;
    }
    catch { return ''; }
};

const timezoneString = (): string =>
{
    try { return Intl.DateTimeFormat().resolvedOptions().timeZone || ''; }
    catch { return ''; }
};

export const DeviceFingerprintView: FC<{}> = () =>
{
    const sentRef = useRef(false);

    useEffect(() =>
    {
        if(sentRef.current) return;

        const connection = GetConnection();
        if(!connection) return;

        connection.registerMessages(new DeviceFingerprintMessageConfiguration());

        // Slight delay so the SSO auth has fully completed before we add to the
        // packet stream; also helps stagger the load when many tabs reconnect.
        const timer = window.setTimeout(async () =>
        {
            try
            {
                const ua = (typeof navigator !== 'undefined' && navigator.userAgent) || '';
                const platform = (typeof navigator !== 'undefined' && (navigator as any).platform) || '';
                const lang = (typeof navigator !== 'undefined' && navigator.language) || '';
                const scr = screenString();
                const tz = timezoneString();
                const canvasDataUrl = renderCanvasFingerprint();
                const canvasHash = canvasDataUrl ? await sha256Hex(canvasDataUrl) : '';

                SendMessageComposer(new SubmitFingerprintComposer(ua, platform, scr, tz, lang, canvasHash));
                sentRef.current = true;
            }
            catch
            {
                // Best-effort: a failure here doesn't impact gameplay.
            }
        }, 2500);

        return () => { window.clearTimeout(timer); };
    }, []);

    return null;
};

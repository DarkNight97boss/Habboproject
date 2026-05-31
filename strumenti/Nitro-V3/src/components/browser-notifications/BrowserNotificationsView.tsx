import { HabboBroadcastMessageEvent, NewFriendRequestEvent, RoomInviteEvent } from '@nitrots/nitro-renderer';
import { FC, useEffect, useRef } from 'react';
import { LocalizeText } from '../../api';
import { useMessageEvent } from '../../hooks';

/**
 * Bridges incoming server events to the browser's Web Notifications API, so
 * users get a desktop toast when the tab is not in focus.
 *
 * Pure client side; no server packets, no UI rendered. Ported from
 * nitro-react custom-features (commit 4de84ea).
 */
export const BrowserNotificationsView: FC<{}> = () =>
{
    const recentRef = useRef<Set<string>>(new Set());

    useEffect(() =>
    {
        if(typeof window === 'undefined' || !('Notification' in window)) return;
        if(Notification.permission === 'default')
        {
            try { Notification.requestPermission().catch(() => {}); } catch {}
        }
    }, []);

    const notify = (title: string, body: string, tag?: string) =>
    {
        try
        {
            if(typeof window === 'undefined' || !('Notification' in window)) return;
            if(Notification.permission !== 'granted') return;
            if(typeof document !== 'undefined' && document.visibilityState === 'visible' && document.hasFocus()) return;

            const t = tag || (title + '|' + body);
            const opts: NotificationOptions = {
                body,
                tag: t,
                icon: '/favicon-32x32.png',
                silent: false
            };
            const n = new Notification(title, opts);
            n.onclick = () =>
            {
                try { window.focus(); } catch {}
                try { n.close(); } catch {}
            };
            setTimeout(() => { try { n.close(); } catch {} }, 8000);

            recentRef.current.add(t);
            setTimeout(() => recentRef.current.delete(t), 2000);
        }
        catch {}
    };

    useMessageEvent<NewFriendRequestEvent>(NewFriendRequestEvent, event =>
    {
        const req = (event.getParser() as any)?.request;
        if(!req) return;
        const name = req.requesterName ?? req.name ?? LocalizeText('browser_notifications.someone');
        const id = req.requesterUserId ?? req.userId ?? 0;
        notify(LocalizeText('browser_notifications.friendreq.title'), LocalizeText('browser_notifications.friendreq.body', [ 'name' ], [ name ]), `friendreq:${id}`);
    });

    useMessageEvent<RoomInviteEvent>(RoomInviteEvent, event =>
    {
        const p: any = event.getParser();
        const text = String(p?.messageText || p?.message || '').trim() || LocalizeText('browser_notifications.roominvite.fallback');
        notify(LocalizeText('browser_notifications.roominvite.title'), text, `roominvite:${p?.senderId ?? ''}`);
    });

    useMessageEvent<HabboBroadcastMessageEvent>(HabboBroadcastMessageEvent, event =>
    {
        const p: any = event.getParser();
        const msg = String(p?.message || '').trim();
        if(!msg) return;
        const body = msg.length > 180 ? (msg.slice(0, 177) + '...') : msg;
        notify(LocalizeText('browser_notifications.broadcast.title'), body, 'broadcast');
    });

    return null;
};

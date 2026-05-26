import { HabboBroadcastMessageEvent, NewFriendRequestEvent, RoomInviteEvent } from '@nitrots/nitro-renderer';
import { FC, useEffect, useRef } from 'react';
import { useMessageEvent } from '../../hooks';

/**
 * Bridges incoming server events to the browser's Web Notifications API, so
 * users get a desktop toast when the tab is not in focus.
 *
 * Notifies on:
 *   - new friend request   (NewFriendRequestEvent  -> "Friend request from X")
 *   - room invite          (RoomInviteEvent        -> "Room invite: <message>")
 *   - hotel broadcast      (HabboBroadcastMessageEvent -> "Hotel announcement")
 *
 * Permission is requested once on mount (browser-policy permitting). When
 * permission is denied or the tab is focused, this component silently no-ops.
 * Pure client side; no server packets, no UI rendered.
 */
export const BrowserNotificationsView: FC<{}> = () =>
{
    // Tag set used to coalesce notifications: re-using a tag replaces the old one,
    // avoiding a stack of stale notifications for the same friend.
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
            // Don't pop while the user is already looking at the tab.
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
            // Auto-close after 8s so the OS tray doesn't fill up.
            setTimeout(() => { try { n.close(); } catch {} }, 8000);

            // Light de-dup memory (in case the server re-sends within 2s).
            recentRef.current.add(t);
            setTimeout(() => recentRef.current.delete(t), 2000);
        }
        catch
        {
            // Browser refused (e.g. user setting): swallow.
        }
    };

    useMessageEvent<NewFriendRequestEvent>(NewFriendRequestEvent, event =>
    {
        const req = event.getParser()?.request;
        if(!req) return;
        notify('New friend request', `${req.requesterName} wants to be your friend.`, `friendreq:${req.requesterUserId}`);
    });

    useMessageEvent<RoomInviteEvent>(RoomInviteEvent, event =>
    {
        const p = event.getParser();
        const text = (p?.messageText || '').trim() || 'You have been invited to a room.';
        notify('Room invite', text, `roominvite:${p?.senderId}`);
    });

    useMessageEvent<HabboBroadcastMessageEvent>(HabboBroadcastMessageEvent, event =>
    {
        const msg = (event.getParser()?.message || '').trim();
        if(!msg) return;
        // Trim very long broadcasts so the toast stays readable.
        const body = msg.length > 180 ? (msg.slice(0, 177) + '...') : msg;
        notify('Hotel announcement', body, 'broadcast');
    });

    return null;
};

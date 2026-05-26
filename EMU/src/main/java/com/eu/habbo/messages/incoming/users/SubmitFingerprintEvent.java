package com.eu.habbo.messages.incoming.users;

import com.eu.habbo.core.DeviceFingerprint;
import com.eu.habbo.messages.incoming.MessageHandler;

/**
 * Client -> server: device fingerprint bundle. Sent once per session, shortly
 * after the modal layer mounts.
 *
 * Wire layout:
 *   string userAgent
 *   string platform
 *   string screen        ("WxH@cd")
 *   string timezone
 *   string language
 *   string canvasHash    (32+ hex chars from a hidden canvas render)
 *
 * Heavily rate-limited: this is supposed to fire once per session.
 */
public class SubmitFingerprintEvent extends MessageHandler {

    @Override
    public int getRatelimit() {
        return 30000;
    }

    @Override
    public void handle() throws Exception {
        if (this.client == null || this.client.getHabbo() == null) return;
        if (!DeviceFingerprint.isEnabled()) return;

        String userAgent = this.packet.readString();
        String platform = this.packet.readString();
        String screen = this.packet.readString();
        String timezone = this.packet.readString();
        String language = this.packet.readString();
        String canvasHash = this.packet.readString();

        // Defensive caps; the server hash is computed over the full payload but
        // we never persist user-controlled blobs without bounds.
        if (userAgent.length() > 512) userAgent = userAgent.substring(0, 512);
        if (canvasHash.length() > 512) canvasHash = canvasHash.substring(0, 512);

        String fpHash = DeviceFingerprint.computeHash(userAgent, platform, screen, timezone, language, canvasHash);
        if (fpHash.isEmpty()) return;

        DeviceFingerprint.record(this.client, fpHash, userAgent);
    }
}

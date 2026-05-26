package com.eu.habbo.habbohotel.gameclients;

import com.eu.habbo.Emulator;
import com.eu.habbo.crypto.HabboEncryption;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.plugin.events.emulator.OutgoingPacketEvent;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

public class GameClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(GameClient.class);

    private final Channel channel;
    private final HabboEncryption encryption;

    private Habbo habbo;
    private boolean handshakeFinished;
    private String machineId = "";

    // Staff step-up MFA (Google Authenticator). When mfaRequired is set at login,
    // staff powers stay locked until the user verifies a code (mfaElevated = true).
    private boolean mfaRequired = false;
    private boolean mfaElevated = false;

    public final ConcurrentHashMap<Integer, Integer> incomingPacketCounter = new ConcurrentHashMap<>(25);
    public final ConcurrentHashMap<Class<? extends MessageHandler>, Long> messageTimestamps = new ConcurrentHashMap<>();
    public long lastPacketCounterCleared = Emulator.getIntUnixTimestamp();

    public GameClient(Channel channel) {
        this.channel = channel;
        this.encryption = Emulator.getCrypto().isEnabled()
                ? new HabboEncryption(
                    Emulator.getCrypto().getExponent(),
                    Emulator.getCrypto().getModulus(),
                    Emulator.getCrypto().getPrivateExponent())
                : null;
    }

    public Channel getChannel() {
        return this.channel;
    }

    public HabboEncryption getEncryption() {
        return encryption;
    }

    public Habbo getHabbo() {
        return this.habbo;
    }

    public void setHabbo(Habbo habbo) {
        this.habbo = habbo;
        if (habbo != null && habbo.getHabboInfo() != null) {
            Emulator.getGameServer().getGameClientManager().indexHabbo(habbo.getHabboInfo().getId(), this);
        }
    }

    public boolean isHandshakeFinished() {
        return handshakeFinished;
    }

    public void setHandshakeFinished(boolean handshakeFinished) {
        this.handshakeFinished = handshakeFinished;
    }

    public String getMachineId() {
        return this.machineId;
    }

    public void setMachineId(String machineId) {
        if (machineId == null) {
            throw new RuntimeException("Cannot set machineID to NULL");
        }

        this.machineId = machineId;
    }

    public boolean isMfaRequired() {
        return this.mfaRequired;
    }

    public void setMfaRequired(boolean mfaRequired) {
        this.mfaRequired = mfaRequired;
    }

    public boolean isMfaElevated() {
        return this.mfaElevated;
    }

    public void setMfaElevated(boolean mfaElevated) {
        this.mfaElevated = mfaElevated;
    }

    /** True when staff powers must be blocked: MFA was required but not yet verified this session. */
    public boolean isStaffMfaLocked() {
        return this.mfaRequired && !this.mfaElevated;
    }

    public void sendResponse(MessageComposer composer) {
        this.sendResponse(composer.compose());
    }

    public void sendResponse(ServerMessage response) {
        if (this.channel.isOpen()) {
            if (response == null || response.getHeader() <= 0) {
                return;
            }

            OutgoingPacketEvent event = new OutgoingPacketEvent(this.habbo, response.getComposer(), response);
            Emulator.getPluginManager().fireEvent(event);

            if (event.isCancelled()) {
                return;
            }

            if (event.hasCustomMessage()) {
                response = event.getCustomMessage();
            }

            // Backpressure: when the outbound buffer is above the high water mark
            // (set in Server.initializePipeline), `isWritable()` returns false. A
            // slow-loris-on-read client that never drains the socket would otherwise
            // pin unbounded memory in ChannelOutboundBuffer. Close the channel —
            // the user reconnects, the server stays alive.
            if (!this.channel.isWritable()) {
                this.channel.close();
                return;
            }

            this.channel.write(response, this.channel.voidPromise());
            this.channel.flush();
        }
    }

    public void sendResponses(ArrayList<ServerMessage> responses) {
        if (this.channel.isOpen()) {
            if (!this.channel.isWritable()) {
                this.channel.close();
                return;
            }
            for (ServerMessage response : responses) {
                if (response == null || response.getHeader() <= 0) {
                    return;
                }

                OutgoingPacketEvent event = new OutgoingPacketEvent(this.habbo, response.getComposer(), response);
                Emulator.getPluginManager().fireEvent(event);

                if (event.isCancelled()) {
                    continue;
                }

                if (event.hasCustomMessage()) {
                    response = event.getCustomMessage();
                }

                this.channel.write(response);
            }

            this.channel.flush();
        }
    }

    public void dispose() {
        try {
            this.channel.close();

            if (this.habbo != null) {
                // Drop per-user runtime guard state (anti-flood window etc) so we
                // don't leak it for the lifetime of the process.
                try {
                    if (this.habbo.getHabboInfo() != null) {
                        int uid = this.habbo.getHabboInfo().getId();
                        com.eu.habbo.core.ChatSpamGuard.onDisconnect(uid);
                        com.eu.habbo.core.UnknownPacketGuard.onDisconnect(uid);
                        com.eu.habbo.core.StaffCommandQuota.onDisconnect(uid);
                    }
                } catch (Exception ignored) {
                }

                if (this.habbo.isOnline()) {
                    this.habbo.getHabboInfo().setOnline(false);
                    this.habbo.disconnect();
                }

                this.habbo = null;
            }
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
        }
    }
}
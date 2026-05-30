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
    private long mfaElevatedAt = 0L; // ms epoch, used to expire the step-up MFA elevation after TTL

    public final ConcurrentHashMap<Integer, Integer> incomingPacketCounter = new ConcurrentHashMap<>(25);
    public final ConcurrentHashMap<Class<? extends MessageHandler>, Long> messageTimestamps = new ConcurrentHashMap<>();
    public long lastPacketCounterCleared = Emulator.getIntUnixTimestamp();

    /**
     * Conteggio cumulativo dei pacchetti ricevuti PRIMA dell'autenticazione (habbo == null).
     * Non viene azzerato dal reset della finestra rate-limit (1s) — e' un cap totale sulla
     * fase pre-login per limitare lo SSO spam / amplificazione DB. Touch solo sull'EventLoop
     * del channel (GameMessageRateLimit) -> int semplice, niente atomic necessario.
     */
    public int preAuthPacketCount = 0;

    /**
     * Conteggio drop consecutivi per backpressure outbound. Quando il client
     * non drena abbastanza in fretta (slow-loris OR temporaneo picco di
     * broadcast room), {@code Channel.isWritable()} ritorna false e noi
     * decidiamo di droppare il packet invece di chiudere subito. Solo dopo
     * un numero di drop consecutivi sopra soglia chiudiamo (il client e'
     * davvero non responsive). Reset a 0 al primo write riuscito.
     */
    private final java.util.concurrent.atomic.AtomicInteger consecutiveBackpressureDrops = new java.util.concurrent.atomic.AtomicInteger(0);

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
            throw new RuntimeException("Impossibile impostare machineID a NULL");
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
        if (!this.mfaElevated) return false;
        // TTL expiry: re-challenge staff MFA after staff.mfa.session.ttl.seconds (default 4h, 0 = no expiry).
        // Self-healing: if expired, demote here so the next privileged command re-prompts MFA.
        int ttlSec = Emulator.getConfig().getInt("staff.mfa.session.ttl.seconds", 14400);
        if (ttlSec > 0 && this.mfaElevatedAt > 0
                && (System.currentTimeMillis() - this.mfaElevatedAt) > (ttlSec * 1000L)) {
            this.mfaElevated = false;
            this.mfaElevatedAt = 0L;
            return false;
        }
        return true;
    }

    public void setMfaElevated(boolean mfaElevated) {
        this.mfaElevated = mfaElevated;
        this.mfaElevatedAt = mfaElevated ? System.currentTimeMillis() : 0L;
    }

    /** True when staff powers must be blocked: MFA was required but not yet verified this session. */
    public boolean isStaffMfaLocked() {
        return this.mfaRequired && !this.isMfaElevated();
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

            // Backpressure (POLICY tolerant): se l'outbound buffer e' sopra
            // l'high water mark, DROPPA il packet ma NON chiudere la connessione.
            // L'utente preferisce perdere qualche update di stato a essere
            // kickato. Se il client e' davvero un slow-loris/zombie, sara'
            // l'IdleTimeoutHandler a pulirlo via pong-timeout (180s), che e'
            // l'unico path di disconnect "fisiologico" rimasto.
            if (!this.channel.isWritable()) {
                // Solo counter incrementato (utile per metrics/audit), niente close.
                this.consecutiveBackpressureDrops.incrementAndGet();
                return;
            }
            this.consecutiveBackpressureDrops.set(0);

            this.channel.write(response, this.channel.voidPromise());
            this.channel.flush();
        }
    }

    public void sendResponses(ArrayList<ServerMessage> responses) {
        if (this.channel.isOpen()) {
            if (!this.channel.isWritable()) {
                // Stessa policy del sendResponse(): drop, niente close.
                this.consecutiveBackpressureDrops.incrementAndGet();
                return;
            }
            this.consecutiveBackpressureDrops.set(0);
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
            LOGGER.error("Eccezione intercettata", e);
        }
    }
}
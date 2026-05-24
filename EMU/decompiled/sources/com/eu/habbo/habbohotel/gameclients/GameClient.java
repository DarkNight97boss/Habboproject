package com.eu.habbo.habbohotel.gameclients;

import com.eu.habbo.Emulator;
import com.eu.habbo.crypto.HabboEncryption;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.MessageComposer;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/gameclients/GameClient.class */
public class GameClient {
    private static final Logger LOGGER = LoggerFactory.getLogger(GameClient.class);
    private final Channel channel;
    private final HabboEncryption encryption;
    private Habbo habbo;
    private boolean handshakeFinished;
    private String machineId = Emulator.PREVIEW;
    public final ConcurrentHashMap<Integer, Integer> incomingPacketCounter = new ConcurrentHashMap<>(25);
    public final ConcurrentHashMap<Class<? extends MessageHandler>, Long> messageTimestamps = new ConcurrentHashMap<>();
    public long lastPacketCounterCleared = Emulator.getIntUnixTimestamp();

    public GameClient(Channel channel) {
        this.channel = channel;
        this.encryption = Emulator.getCrypto().isEnabled() ? new HabboEncryption(Emulator.getCrypto().getExponent(), Emulator.getCrypto().getModulus(), Emulator.getCrypto().getPrivateExponent()) : null;
    }

    public Channel getChannel() {
        return this.channel;
    }

    public HabboEncryption getEncryption() {
        return this.encryption;
    }

    public Habbo getHabbo() {
        return this.habbo;
    }

    public void setHabbo(Habbo habbo) {
        this.habbo = habbo;
    }

    public boolean isHandshakeFinished() {
        return this.handshakeFinished;
    }

    public void setHandshakeFinished(boolean z) {
        this.handshakeFinished = z;
    }

    public String getMachineId() {
        return this.machineId;
    }

    public void setMachineId(String str) {
        if (str == null) {
            throw new RuntimeException("Cannot set machineID to NULL");
        }
        this.machineId = str;
    }

    public void sendResponse(MessageComposer messageComposer) {
        sendResponse(messageComposer.compose());
    }

    public void sendResponse(ServerMessage serverMessage) {
        if (!this.channel.isOpen() || serverMessage == null || serverMessage.getHeader() <= 0) {
            return;
        }
        this.channel.write(serverMessage, this.channel.voidPromise());
        this.channel.flush();
    }

    public void sendResponses(ArrayList<ServerMessage> arrayList) {
        if (this.channel.isOpen()) {
            for (ServerMessage serverMessage : arrayList) {
                if (serverMessage == null || serverMessage.getHeader() <= 0) {
                    return;
                } else {
                    this.channel.write(serverMessage);
                }
            }
            this.channel.flush();
        }
    }

    public void dispose() {
        try {
            this.channel.close();
            if (this.habbo != null) {
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

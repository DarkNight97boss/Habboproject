package com.eu.habbo.networking.rconserver;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.rcon.*;
import com.eu.habbo.networking.Server;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gnu.trove.map.hash.THashMap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RCONServer extends Server {

    private static final Logger LOGGER = LoggerFactory.getLogger(RCONServer.class);

    private final THashMap<String, Class<? extends RCONMessage>> messages;
    private final GsonBuilder gsonBuilder;
    List<String> allowedAdresses = new ArrayList<>();

    public RCONServer(String host, int port) throws Exception {
        super("RCON Server", host, port, 1, 2);

        this.messages = new THashMap<>();

        this.gsonBuilder = new GsonBuilder();
        this.gsonBuilder.registerTypeAdapter(RCONMessage.class, new RCONMessage.RCONMessageSerializer());

        this.addRCONMessage("alertuser", AlertUser.class);
        this.addRCONMessage("disconnect", DisconnectUser.class);
        this.addRCONMessage("forwarduser", ForwardUser.class);
        this.addRCONMessage("givebadge", GiveBadge.class);
        this.addRCONMessage("givecredits", GiveCredits.class);
        this.addRCONMessage("givepixels", GivePixels.class);
        this.addRCONMessage("givepoints", GivePoints.class);
        this.addRCONMessage("hotelalert", HotelAlert.class);
        this.addRCONMessage("sendgift", SendGift.class);
        this.addRCONMessage("sendroombundle", SendRoomBundle.class);
        this.addRCONMessage("setrank", SetRank.class);
        this.addRCONMessage("updatewordfilter", UpdateWordfilter.class);
        this.addRCONMessage("updatecatalog", UpdateCatalog.class);
        this.addRCONMessage("executecommand", ExecuteCommand.class);
        this.addRCONMessage("progressachievement", ProgressAchievement.class);
        this.addRCONMessage("updateuser", UpdateUser.class);
        this.addRCONMessage("friendrequest", FriendRequest.class);
        this.addRCONMessage("imagehotelalert", ImageHotelAlert.class);
        this.addRCONMessage("imagealertuser", ImageAlertUser.class);
        this.addRCONMessage("stalkuser", StalkUser.class);
        this.addRCONMessage("staffalert", StaffAlert.class);
        this.addRCONMessage("modticket", CreateModToolTicket.class);
        this.addRCONMessage("talkuser", TalkUser.class);
        this.addRCONMessage("changeroomowner", ChangeRoomOwner.class);
        this.addRCONMessage("muteuser", MuteUser.class);
        this.addRCONMessage("giverespect", GiveRespect.class);
        this.addRCONMessage("ignoreuser", IgnoreUser.class);
        this.addRCONMessage("setmotto", SetMotto.class);
        this.addRCONMessage("giveuserclothing", GiveUserClothing.class);
        this.addRCONMessage("modifysubscription", ModifyUserSubscription.class);
        this.addRCONMessage("changeusername", ChangeUsername.class);

        Collections.addAll(this.allowedAdresses, Emulator.getConfig().getValue("rcon.allowed", "127.0.0.1").split(";"));

        // SECURITY: shout loudly if RCON is allowed from a non-loopback IP — the protocol
        // exposes `executecommand` (full hotel RCE), so any wildcard / 0.0.0.0 / public IP
        // entry is almost certainly a misconfiguration. Refuse to start unless
        // `rcon.allow.public=true` is explicit.
        boolean publicOptIn = Emulator.getConfig().getBoolean("rcon.allow.public", false);
        for (String addr : this.allowedAdresses) {
            String trimmed = addr == null ? "" : addr.trim();
            if (trimmed.isEmpty()) continue;
            boolean isLoopback = trimmed.equals("127.0.0.1") || trimmed.equals("::1") || trimmed.equalsIgnoreCase("localhost");
            boolean isWildcard = trimmed.equals("0.0.0.0") || trimmed.equals("*") || trimmed.equals("::");
            if (isWildcard && !publicOptIn) {
                LOGGER.error("L'allow-list RCON contiene il wildcard {} — avvio rifiutato. Imposta rcon.allow.public=true SOLO se hai un motivo reale e un firewall davanti.", trimmed);
                throw new IllegalStateException("rcon.allowed contiene un wildcard senza rcon.allow.public=true");
            }
            if (!isLoopback && !isWildcard) {
                LOGGER.warn("L'allow-list RCON contiene un IP non-loopback {} — assicurati che il tuo firewall limiti la porta RCON ({}). executecommand = esecuzione di codice remoto.", trimmed, port);
            }
        }
    }

    @Override
    public void initializePipeline() {
        super.initializePipeline();

        this.serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) throws Exception {
                ch.pipeline().addLast(new RCONServerHandler());
            }
        });
    }


    public void addRCONMessage(String key, Class<? extends RCONMessage> clazz) {
        this.messages.put(key, clazz);
    }

    public String handle(ChannelHandlerContext ctx, String key, String body) throws Exception {
        Class<? extends RCONMessage> message = this.messages.get(key.replace("_", "").toLowerCase());

        String result;
        if (message != null) {
            try {
                RCONMessage rcon = message.getDeclaredConstructor().newInstance();
                Gson gson = this.gsonBuilder.create();
                rcon.handle(gson, gson.fromJson(body, rcon.type));
                LOGGER.info("Messaggio RCON gestito: {}", message.getSimpleName());
                result = gson.toJson(rcon, RCONMessage.class);

                if (Emulator.debugging) {
                    // Truncate to keep PII (usernames, motto, look) and credentials-bearing
                    // bodies (giveCredits/setRank) bounded if logs are ever shipped externally.
                    String safeBody = body == null ? "" : (body.length() > 256 ? body.substring(0, 256) + "...(truncated)" : body);
                    String safeResult = result == null ? "" : (result.length() > 256 ? result.substring(0, 256) + "...(truncated)" : result);
                    LOGGER.debug("Dati RCON {} Risultato RCON {}", safeBody, safeResult);
                }

                return result;
            } catch (Exception ex) {
                LOGGER.error("Gestione del RCONMessage fallita", ex);
            }
        } else {
            LOGGER.error("Impossibile trovare: {}", key);
        }

        throw new ArrayIndexOutOfBoundsException("Messaggio RCON non gestito");
    }

    public List<String> getCommands() {
        return new ArrayList<>(this.messages.keySet());
    }
}

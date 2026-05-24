package com.eu.habbo.networking.rconserver;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.rcon.AlertUser;
import com.eu.habbo.messages.rcon.ChangeRoomOwner;
import com.eu.habbo.messages.rcon.ChangeUsername;
import com.eu.habbo.messages.rcon.CreateModToolTicket;
import com.eu.habbo.messages.rcon.DisconnectUser;
import com.eu.habbo.messages.rcon.ExecuteCommand;
import com.eu.habbo.messages.rcon.ForwardUser;
import com.eu.habbo.messages.rcon.FriendRequest;
import com.eu.habbo.messages.rcon.GiveBadge;
import com.eu.habbo.messages.rcon.GiveCredits;
import com.eu.habbo.messages.rcon.GivePixels;
import com.eu.habbo.messages.rcon.GivePoints;
import com.eu.habbo.messages.rcon.GiveRespect;
import com.eu.habbo.messages.rcon.GiveUserClothing;
import com.eu.habbo.messages.rcon.HotelAlert;
import com.eu.habbo.messages.rcon.IgnoreUser;
import com.eu.habbo.messages.rcon.ImageAlertUser;
import com.eu.habbo.messages.rcon.ImageHotelAlert;
import com.eu.habbo.messages.rcon.ModifyUserSubscription;
import com.eu.habbo.messages.rcon.MuteUser;
import com.eu.habbo.messages.rcon.ProgressAchievement;
import com.eu.habbo.messages.rcon.RCONMessage;
import com.eu.habbo.messages.rcon.SendGift;
import com.eu.habbo.messages.rcon.SendRoomBundle;
import com.eu.habbo.messages.rcon.SetMotto;
import com.eu.habbo.messages.rcon.SetRank;
import com.eu.habbo.messages.rcon.StaffAlert;
import com.eu.habbo.messages.rcon.StalkUser;
import com.eu.habbo.messages.rcon.TalkUser;
import com.eu.habbo.messages.rcon.UpdateCatalog;
import com.eu.habbo.messages.rcon.UpdateUser;
import com.eu.habbo.messages.rcon.UpdateWordfilter;
import com.eu.habbo.networking.Server;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gnu.trove.map.hash.THashMap;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/rconserver/RCONServer.class */
public class RCONServer extends Server {
    private static final Logger LOGGER = LoggerFactory.getLogger(RCONServer.class);
    private final THashMap<String, Class<? extends RCONMessage>> messages;
    private final GsonBuilder gsonBuilder;
    List<String> allowedAdresses;

    public RCONServer(String str, int i) throws Exception {
        super("RCON Server", str, i, 1, 2);
        this.allowedAdresses = new ArrayList();
        this.messages = new THashMap<>();
        this.gsonBuilder = new GsonBuilder();
        this.gsonBuilder.registerTypeAdapter(RCONMessage.class, new RCONMessage.RCONMessageSerializer());
        addRCONMessage("alertuser", AlertUser.class);
        addRCONMessage("disconnect", DisconnectUser.class);
        addRCONMessage("forwarduser", ForwardUser.class);
        addRCONMessage("givebadge", GiveBadge.class);
        addRCONMessage("givecredits", GiveCredits.class);
        addRCONMessage("givepixels", GivePixels.class);
        addRCONMessage("givepoints", GivePoints.class);
        addRCONMessage("hotelalert", HotelAlert.class);
        addRCONMessage("sendgift", SendGift.class);
        addRCONMessage("sendroombundle", SendRoomBundle.class);
        addRCONMessage("setrank", SetRank.class);
        addRCONMessage("updatewordfilter", UpdateWordfilter.class);
        addRCONMessage("updatecatalog", UpdateCatalog.class);
        addRCONMessage("executecommand", ExecuteCommand.class);
        addRCONMessage("progressachievement", ProgressAchievement.class);
        addRCONMessage("updateuser", UpdateUser.class);
        addRCONMessage("friendrequest", FriendRequest.class);
        addRCONMessage("imagehotelalert", ImageHotelAlert.class);
        addRCONMessage("imagealertuser", ImageAlertUser.class);
        addRCONMessage("stalkuser", StalkUser.class);
        addRCONMessage("staffalert", StaffAlert.class);
        addRCONMessage("modticket", CreateModToolTicket.class);
        addRCONMessage("talkuser", TalkUser.class);
        addRCONMessage("changeroomowner", ChangeRoomOwner.class);
        addRCONMessage("muteuser", MuteUser.class);
        addRCONMessage("giverespect", GiveRespect.class);
        addRCONMessage("ignoreuser", IgnoreUser.class);
        addRCONMessage("setmotto", SetMotto.class);
        addRCONMessage("giveuserclothing", GiveUserClothing.class);
        addRCONMessage("modifysubscription", ModifyUserSubscription.class);
        addRCONMessage("changeusername", ChangeUsername.class);
        Collections.addAll(this.allowedAdresses, Emulator.getConfig().getValue("rcon.allowed", "127.0.0.1").split(";"));
    }

    @Override // com.eu.habbo.networking.Server
    public void initializePipeline() {
        super.initializePipeline();
        this.serverBootstrap.childHandler(new ChannelInitializer<SocketChannel>() { // from class: com.eu.habbo.networking.rconserver.RCONServer.1
            public void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new ChannelHandler[]{new RCONServerHandler()});
            }
        });
    }

    public void addRCONMessage(String str, Class<? extends RCONMessage> cls) {
        this.messages.put(str, cls);
    }

    public String handle(ChannelHandlerContext channelHandlerContext, String str, String str2) throws Exception {
        Class cls = (Class) this.messages.get(str.replace("_", Emulator.PREVIEW).toLowerCase());
        if (cls != null) {
            try {
                RCONMessage rCONMessage = (RCONMessage) cls.getDeclaredConstructor(new Class[0]).newInstance(new Object[0]);
                Gson gsonCreate = this.gsonBuilder.create();
                rCONMessage.handle(gsonCreate, gsonCreate.fromJson(str2, rCONMessage.type));
                LOGGER.info("Handled RCON Message: {}", cls.getSimpleName());
                String json = gsonCreate.toJson(rCONMessage, RCONMessage.class);
                if (Emulator.debugging) {
                    LOGGER.debug("RCON Data {} RCON Result {}", str2, json);
                }
                return json;
            } catch (Exception e) {
                LOGGER.error("Failed to handle RCONMessage", e);
            }
        } else {
            LOGGER.error("Couldn't find: {}", str);
        }
        throw new ArrayIndexOutOfBoundsException("Unhandled RCON Message");
    }

    public List<String> getCommands() {
        return new ArrayList(this.messages.keySet());
    }
}

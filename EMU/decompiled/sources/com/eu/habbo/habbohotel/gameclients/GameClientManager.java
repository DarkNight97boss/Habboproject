package com.eu.habbo.habbohotel.gameclients;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.networking.gameserver.GameServerAttributes;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/gameclients/GameClientManager.class */
public class GameClientManager {
    private final ConcurrentMap<ChannelId, GameClient> clients = new ConcurrentHashMap();

    public ConcurrentMap<ChannelId, GameClient> getSessions() {
        return this.clients;
    }

    public boolean addClient(final ChannelHandlerContext channelHandlerContext) {
        GameClient gameClient = new GameClient(channelHandlerContext.channel());
        channelHandlerContext.channel().closeFuture().addListener(new ChannelFutureListener() { // from class: com.eu.habbo.habbohotel.gameclients.GameClientManager.1
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                GameClientManager.this.disposeClient(channelHandlerContext.channel());
            }
        });
        channelHandlerContext.channel().attr(GameServerAttributes.CLIENT).set(gameClient);
        channelHandlerContext.fireChannelRegistered();
        return this.clients.putIfAbsent(channelHandlerContext.channel().id(), gameClient) == null;
    }

    public void disposeClient(GameClient gameClient) {
        disposeClient(gameClient.getChannel());
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void disposeClient(Channel channel) {
        GameClient gameClient = (GameClient) channel.attr(GameServerAttributes.CLIENT).get();
        if (gameClient != null) {
            gameClient.dispose();
        }
        channel.deregister();
        channel.attr(GameServerAttributes.CLIENT).set((Object) null);
        channel.closeFuture();
        channel.close();
        this.clients.remove(channel.id());
    }

    public boolean containsHabbo(Integer num) {
        if (this.clients.isEmpty()) {
            return false;
        }
        for (GameClient gameClient : this.clients.values()) {
            if (gameClient.getHabbo() != null && gameClient.getHabbo().getHabboInfo() != null && gameClient.getHabbo().getHabboInfo().getId() == num.intValue()) {
                return true;
            }
        }
        return false;
    }

    public Habbo getHabbo(int i) {
        for (GameClient gameClient : this.clients.values()) {
            if (gameClient.getHabbo() != null && gameClient.getHabbo().getHabboInfo().getId() == i) {
                return gameClient.getHabbo();
            }
        }
        return null;
    }

    public Habbo getHabbo(String str) {
        for (GameClient gameClient : this.clients.values()) {
            if (gameClient.getHabbo() != null && gameClient.getHabbo().getHabboInfo().getUsername().equalsIgnoreCase(str)) {
                return gameClient.getHabbo();
            }
        }
        return null;
    }

    public List<Habbo> getHabbosWithIP(String str) {
        ArrayList arrayList = new ArrayList();
        for (GameClient gameClient : this.clients.values()) {
            if (gameClient.getHabbo() != null && gameClient.getHabbo().getHabboInfo() != null && gameClient.getHabbo().getHabboInfo().getIpLogin().equalsIgnoreCase(str)) {
                arrayList.add(gameClient.getHabbo());
            }
        }
        return arrayList;
    }

    public List<Habbo> getHabbosWithMachineId(String str) {
        ArrayList arrayList = new ArrayList();
        for (GameClient gameClient : this.clients.values()) {
            if (gameClient.getHabbo() != null && gameClient.getHabbo().getHabboInfo() != null && gameClient.getMachineId().equalsIgnoreCase(str)) {
                arrayList.add(gameClient.getHabbo());
            }
        }
        return arrayList;
    }

    public void sendBroadcastResponse(MessageComposer messageComposer) {
        sendBroadcastResponse(messageComposer.compose());
    }

    public void sendBroadcastResponse(ServerMessage serverMessage) {
        Iterator<GameClient> it = this.clients.values().iterator();
        while (it.hasNext()) {
            it.next().sendResponse(serverMessage);
        }
    }

    public void sendBroadcastResponse(ServerMessage serverMessage, GameClient gameClient) {
        for (GameClient gameClient2 : this.clients.values()) {
            if (!gameClient2.equals(gameClient)) {
                gameClient2.sendResponse(serverMessage);
            }
        }
    }

    public void sendBroadcastResponse(ServerMessage serverMessage, String str, GameClient gameClient) {
        for (GameClient gameClient2 : this.clients.values()) {
            if (!gameClient2.equals(gameClient) && gameClient2.getHabbo() != null && gameClient2.getHabbo().hasPermission(str)) {
                gameClient2.sendResponse(serverMessage);
            }
        }
    }
}

package com.eu.habbo.habbohotel.gameclients;

import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.networking.gameserver.GameServerAttributes;
import io.netty.channel.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class GameClientManager {

    private final ConcurrentMap<ChannelId, GameClient> clients;
    private final ConcurrentMap<Integer, GameClient> habboIndex; // O(1) lookup by user id (perf under load)

    public GameClientManager() {
        this.clients = new ConcurrentHashMap<>();
        this.habboIndex = new ConcurrentHashMap<>();
    }


    public ConcurrentMap<ChannelId, GameClient> getSessions() {
        return this.clients;
    }

    /** Maintain the O(1) user-id -> client index (called from GameClient.setHabbo on login). */
    public void indexHabbo(int id, GameClient client) {
        this.habboIndex.put(id, client);
    }


    public boolean addClient(ChannelHandlerContext ctx) {
        GameClient client = new GameClient(ctx.channel());
        ctx.channel().closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                GameClientManager.this.disposeClient(ctx.channel());
            }
        });

        ctx.channel().attr(GameServerAttributes.CLIENT).set(client);
        ctx.fireChannelRegistered();

        return this.clients.putIfAbsent(ctx.channel().id(), client) == null;
    }


    public void disposeClient(GameClient client) {
        this.disposeClient(client.getChannel());
    }

    private void disposeClient(Channel channel) {
        GameClient client = channel.attr(GameServerAttributes.CLIENT).get();

        if (client != null) {
            Habbo habbo = client.getHabbo();
            if (habbo != null && habbo.getHabboInfo() != null) {
                this.habboIndex.remove(habbo.getHabboInfo().getId(), client); // only if still mapped to this client
            }
            client.dispose();
        }
        channel.deregister();
        channel.attr(GameServerAttributes.CLIENT).set(null);
        channel.closeFuture();
        channel.close();
        this.clients.remove(channel.id());
    }


    public boolean containsHabbo(Integer id) {
        GameClient client = this.habboIndex.get(id);
        return client != null && client.getHabbo() != null;
    }


    public Habbo getHabbo(int id) {
        GameClient client = this.habboIndex.get(id);
        if (client != null) {
            Habbo habbo = client.getHabbo();
            if (habbo != null && habbo.getHabboInfo() != null && habbo.getHabboInfo().getId() == id) {
                return habbo;
            }
        }
        return null;
    }


    public Habbo getHabbo(String username) {
        for (GameClient client : this.clients.values()) {
            if (client.getHabbo() == null)
                continue;

            if (client.getHabbo().getHabboInfo().getUsername().equalsIgnoreCase(username))
                return client.getHabbo();
        }

        return null;
    }


    public List<Habbo> getHabbosWithIP(String ip) {
        List<Habbo> habbos = new ArrayList<>();

        for (GameClient client : this.clients.values()) {
            if (client.getHabbo() != null && client.getHabbo().getHabboInfo() != null) {
                if (client.getHabbo().getHabboInfo().getIpLogin().equalsIgnoreCase(ip)) {
                    habbos.add(client.getHabbo());
                }
            }
        }

        return habbos;
    }


    public List<Habbo> getHabbosWithMachineId(String machineId) {
        List<Habbo> habbos = new ArrayList<>();

        for (GameClient client : this.clients.values()) {
            if (client.getHabbo() != null && client.getHabbo().getHabboInfo() != null && client.getMachineId().equalsIgnoreCase(machineId)) {
                habbos.add(client.getHabbo());
            }
        }

        return habbos;
    }


    public void sendBroadcastResponse(MessageComposer composer) {
        this.sendBroadcastResponse(composer.compose());
    }


    public void sendBroadcastResponse(ServerMessage message) {
        for (GameClient client : this.clients.values()) {
            client.sendResponse(message);
        }
    }


    public void sendBroadcastResponse(ServerMessage message, GameClient exclude) {
        for (GameClient client : this.clients.values()) {
            if (client.equals(exclude))
                continue;

            client.sendResponse(message);
        }
    }


    public void sendBroadcastResponse(ServerMessage message, String minPermission, GameClient exclude) {
        for (GameClient client : this.clients.values()) {
            if (client.equals(exclude))
                continue;

            if (client.getHabbo() != null) {
                if (client.getHabbo().hasPermission(minPermission)) {
                    client.sendResponse(message);
                }
            }
        }
    }
}
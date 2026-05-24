package com.eu.habbo.habbohotel.gameclients;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.networking.gameserver.GameServerAttributes;
import io.netty.channel.*;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

public class GameClientManager {

    private final ConcurrentMap<ChannelId, GameClient> clients;

    public GameClientManager() {
        this.clients = new ConcurrentHashMap<>();
    }


    public ConcurrentMap<ChannelId, GameClient> getSessions() {
        return this.clients;
    }


    public boolean addClient(ChannelHandlerContext ctx) {
        // Optional per-IP connection cap (0 = disabled). NOTE: uses the socket IP, so leave this at 0
        // behind a proxy/Cloudflare (the socket IP would be the proxy's) and cap per-IP at the proxy instead.
        int maxPerIp = Emulator.getConfig().getInt("networking.max.connections.per.ip", 0);
        if (maxPerIp > 0) {
            String ip = remoteIp(ctx.channel());
            if (ip != null) {
                int count = 0;
                for (GameClient existing : this.clients.values()) {
                    if (ip.equals(remoteIp(existing.getChannel()))) {
                        count++;
                    }
                }
                if (count >= maxPerIp) {
                    return false;
                }
            }
        }

        GameClient client = new GameClient(ctx.channel());
        ctx.channel().closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                GameClientManager.this.disposeClient(ctx.channel());
            }
        });

        ctx.channel().attr(GameServerAttributes.CLIENT).set(client);
        ctx.fireChannelRegistered();

        boolean added = this.clients.putIfAbsent(ctx.channel().id(), client) == null;

        // Pre-auth timeout: close connections that never authenticate (0 = disabled).
        int authTimeout = Emulator.getConfig().getInt("networking.auth.timeout.seconds", 30);
        if (added && authTimeout > 0) {
            ctx.channel().eventLoop().schedule(() -> {
                GameClient current = ctx.channel().attr(GameServerAttributes.CLIENT).get();
                if (ctx.channel().isOpen() && (current == null || current.getHabbo() == null)) {
                    ctx.channel().close();
                }
            }, authTimeout, TimeUnit.SECONDS);
        }

        return added;
    }

    private static String remoteIp(Channel channel) {
        if (channel != null && channel.remoteAddress() instanceof InetSocketAddress) {
            InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
            if (address.getAddress() != null) {
                return address.getAddress().getHostAddress();
            }
        }
        return null;
    }


    public void disposeClient(GameClient client) {
        this.disposeClient(client.getChannel());
    }

    private void disposeClient(Channel channel) {
        GameClient client = channel.attr(GameServerAttributes.CLIENT).get();

        if (client != null) {
            client.dispose();
        }
        channel.deregister();
        channel.attr(GameServerAttributes.CLIENT).set(null);
        channel.closeFuture();
        channel.close();
        this.clients.remove(channel.id());
    }


    public boolean containsHabbo(Integer id) {
        if (!this.clients.isEmpty()) {
            for (GameClient client : this.clients.values()) {
                if (client.getHabbo() != null) {
                    if (client.getHabbo().getHabboInfo() != null) {
                        if (client.getHabbo().getHabboInfo().getId() == id)
                            return true;
                    }
                }
            }
        }
        return false;
    }


    public Habbo getHabbo(int id) {
        for (GameClient client : this.clients.values()) {
            if (client.getHabbo() == null)
                continue;

            if (client.getHabbo().getHabboInfo().getId() == id)
                return client.getHabbo();
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
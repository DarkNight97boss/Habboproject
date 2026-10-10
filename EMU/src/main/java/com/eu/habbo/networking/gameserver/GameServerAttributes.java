package com.eu.habbo.networking.gameserver;

import com.eu.habbo.crypto.HabboRC4;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import io.netty.util.AttributeKey;

public class GameServerAttributes {

    public static final AttributeKey<GameClient> CLIENT = AttributeKey.valueOf("GameClient");
    public static final AttributeKey<HabboRC4> CRYPTO_CLIENT = AttributeKey.valueOf("CryptoClient");
    public static final AttributeKey<HabboRC4> CRYPTO_SERVER = AttributeKey.valueOf("CryptoServer");
    public static final AttributeKey<String> PROXY_REAL_IP = AttributeKey.valueOf("ProxyRealIp");
    /** IP reale del client dietro il WebSocket nativo (header ws.ip.header). Stesso nome usato dal plugin NitroWebsockets → compatibile con SecureLoginEvent. */
    public static final AttributeKey<String> WS_IP = AttributeKey.valueOf("WS_IP");

}

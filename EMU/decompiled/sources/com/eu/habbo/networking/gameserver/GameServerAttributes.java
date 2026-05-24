package com.eu.habbo.networking.gameserver;

import com.eu.habbo.crypto.HabboRC4;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import io.netty.util.AttributeKey;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/networking/gameserver/GameServerAttributes.class */
public class GameServerAttributes {
    public static final AttributeKey<GameClient> CLIENT = AttributeKey.valueOf("GameClient");
    public static final AttributeKey<HabboRC4> CRYPTO_CLIENT = AttributeKey.valueOf("CryptoClient");
    public static final AttributeKey<HabboRC4> CRYPTO_SERVER = AttributeKey.valueOf("CryptoServer");
}

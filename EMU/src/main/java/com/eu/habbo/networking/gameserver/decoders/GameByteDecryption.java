package com.eu.habbo.networking.gameserver.decoders;

import com.eu.habbo.crypto.HabboRC4;
import com.eu.habbo.networking.gameserver.GameServerAttributes;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;

public class GameByteDecryption extends ByteToMessageDecoder {
   public GameByteDecryption() {
      this.setSingleDecode(true);
   }

   protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
      ByteBuf data = in.readBytes(in.readableBytes());
      ((HabboRC4)ctx.channel().attr(GameServerAttributes.CRYPTO_CLIENT).get()).parse(data.array());
      out.add(data);
   }
}

package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

public class RoomAdErrorComposer extends MessageComposer {
   private final int errorCode;
   private final String unknownString;

   public RoomAdErrorComposer(int errorCode, String unknownString) {
      this.errorCode = errorCode;
      this.unknownString = unknownString;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(1759);
      this.response.appendInt(this.errorCode);
      this.response.appendString(this.unknownString);
      return this.response;
   }
}

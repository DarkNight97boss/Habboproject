package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

public class RoomOpenComposer extends MessageComposer {
   @Override
   protected ServerMessage composeInternal() {
      this.response.init(758);
      return this.response;
   }
}

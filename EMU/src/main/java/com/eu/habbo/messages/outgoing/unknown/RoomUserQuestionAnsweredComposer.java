package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import java.util.Map;
import java.util.Map.Entry;

public class RoomUserQuestionAnsweredComposer extends MessageComposer {
   private final int userId;
   private final String value;
   private final Map<String, Integer> unknownMap;

   public RoomUserQuestionAnsweredComposer(int userId, String value, Map<String, Integer> unknownMap) {
      this.userId = userId;
      this.value = value;
      this.unknownMap = unknownMap;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(-1);
      this.response.appendInt(this.userId);
      this.response.appendString(this.value);
      this.response.appendInt(this.unknownMap.size());

      for (Entry<String, Integer> entry : this.unknownMap.entrySet()) {
         this.response.appendString(entry.getKey());
         this.response.appendInt(entry.getValue());
      }

      return this.response;
   }
}

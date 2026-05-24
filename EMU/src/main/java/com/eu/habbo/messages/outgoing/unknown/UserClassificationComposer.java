package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import java.util.List;
import org.apache.commons.math3.util.Pair;

public class UserClassificationComposer extends MessageComposer {
   private final List<Pair<Integer, Pair<String, String>>> info;

   public UserClassificationComposer(List<Pair<Integer, Pair<String, String>>> info) {
      this.info = info;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(966);
      this.response.appendInt(this.info.size());

      for (Pair<Integer, Pair<String, String>> set : this.info) {
         this.response.appendInt((Integer)set.getKey());
         this.response.appendString((String)((Pair)set.getValue()).getKey());
         this.response.appendString((String)((Pair)set.getValue()).getValue());
      }

      return this.response;
   }
}

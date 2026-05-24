package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

public class UserPointsComposer extends MessageComposer {
   private final int currentAmount;
   private final int amountAdded;
   private final int type;

   public UserPointsComposer(int currentAmount, int amountAdded, int type) {
      this.currentAmount = currentAmount;
      this.amountAdded = amountAdded;
      this.type = type;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(2275);
      this.response.appendInt(this.currentAmount);
      this.response.appendInt(this.amountAdded);
      this.response.appendInt(this.type);
      return this.response;
   }
}

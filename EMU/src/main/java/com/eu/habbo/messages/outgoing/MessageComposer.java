package com.eu.habbo.messages.outgoing;

import com.eu.habbo.messages.ServerMessage;

public abstract class MessageComposer {
   private ServerMessage composed = null;
   protected final ServerMessage response = new ServerMessage();

   protected MessageComposer() {
   }

   protected abstract ServerMessage composeInternal();

   public ServerMessage compose() {
      if (this.composed == null) {
         this.composed = this.composeInternal();
      }

      return this.composed;
   }
}

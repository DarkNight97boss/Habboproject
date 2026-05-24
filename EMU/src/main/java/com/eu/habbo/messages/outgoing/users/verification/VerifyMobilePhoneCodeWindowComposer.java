package com.eu.habbo.messages.outgoing.users.verification;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

public class VerifyMobilePhoneCodeWindowComposer extends MessageComposer {
   private final int unknownInt1;
   private final int unknownInt2;

   public VerifyMobilePhoneCodeWindowComposer(int unknownInt1, int unknownInt2) {
      this.unknownInt1 = unknownInt1;
      this.unknownInt2 = unknownInt2;
   }

   @Override
   protected ServerMessage composeInternal() {
      this.response.init(800);
      this.response.appendInt(this.unknownInt1);
      this.response.appendInt(this.unknownInt2);
      return this.response;
   }
}

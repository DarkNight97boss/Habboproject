package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.google.gson.Gson;

public class UpdateWordfilter extends RCONMessage<UpdateWordfilter.WordFilterJSON> {
   public UpdateWordfilter() {
      super(UpdateWordfilter.WordFilterJSON.class);
   }

   public void handle(Gson gson, UpdateWordfilter.WordFilterJSON object) {
      Emulator.getGameEnvironment().getWordFilter().reload();
   }

   static class WordFilterJSON {
   }
}

package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogManager;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.messages.outgoing.generic.alerts.MessagesForYouComposer;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class AboutCommand extends Command {
   public static String credits = "Zabbo MS Arcturus 3.5.3 is an closed source project based on Morningstar Arcturus By TheGeneral & Krews \nThe Following people have all contributed to this emulator:\n TheGeneral\n Beny\n Justin \n Hxmada \n Alejandro\n Capheus\n Skeletor\n Harmonic\n Mike\n Remco\n zGrav \n Quadral \n Harmony\n Swirny\n ArpyAge\n Mikkel\n Rodolfo\n Rasmus\n Kitt Mustang\n Snaiker\n nttzx\n necmi\n Dome\n Jose Flores\n Cam\n Oliver\n Narzo\n Tenshie\n MartenM\n Ridge\n SenpaiDipper\n Snaiker\n Thijmen";

   public AboutCommand() {
      super(null, new String[]{"about", "info", "online", "server"});
   }

   @Override
   public boolean handle(GameClient gameClient, String[] params) {
      Emulator.getRuntime().gc();
      int seconds = Emulator.getIntUnixTimestamp() - Emulator.getTimeStarted();
      int day = (int)TimeUnit.SECONDS.toDays(seconds);
      long hours = TimeUnit.SECONDS.toHours(seconds) - day * 24;
      long minute = TimeUnit.SECONDS.toMinutes(seconds) - TimeUnit.SECONDS.toHours(seconds) * 60L;
      long second = TimeUnit.SECONDS.toSeconds(seconds) - TimeUnit.SECONDS.toMinutes(seconds) * 60L;
      String message = "<b>Arcturus Morningstar 3.5.3 </b>\r\n";
      if (Emulator.getConfig().getBoolean("info.shown", true)) {
         message = message
            + "<b>Hotel Statistics</b>\r- Online Users: "
            + Emulator.getGameEnvironment().getHabboManager().getOnlineCount()
            + "\r- Active Rooms: "
            + Emulator.getGameEnvironment().getRoomManager().getActiveRooms().size()
            + "\r- Shop:  "
            + Emulator.getGameEnvironment().getCatalogManager().catalogPages.size()
            + " pages and "
            + CatalogManager.catalogItemAmount
            + " items. \r- Furni: "
            + Emulator.getGameEnvironment().getItemManager().getItems().size()
            + " item definitions\r\n<b>Server Statistics</b>\r- Uptime: "
            + day
            + (day > 1 ? " days, " : " day, ")
            + hours
            + (hours > 1L ? " hours, " : " hour, ")
            + minute
            + (minute > 1L ? " minutes, " : " minute, ")
            + second
            + (second > 1L ? " seconds!" : " second!")
            + "\r- RAM Usage: "
            + (Emulator.getRuntime().totalMemory() - Emulator.getRuntime().freeMemory()) / 1048576L
            + "/"
            + Emulator.getRuntime().freeMemory() / 1048576L
            + "MB\r- CPU Cores: "
            + Emulator.getRuntime().availableProcessors()
            + "\r- Total Memory: "
            + Emulator.getRuntime().maxMemory() / 1048576L
            + "MB\r\n";
      }

      message = message + "\r<b>Thanks for using Zabbo MS Arcturus 3.5.3 Report issues on the forums. https://retrotools.xyz \r\r    - The General";
      gameClient.getHabbo().alert(message);
      gameClient.sendResponse(new MessagesForYouComposer(Collections.singletonList(credits)));
      return true;
   }
}

package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.CatalogManager;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.messages.outgoing.generic.alerts.MessagesForYouComposer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/AboutCommand.class */
public class AboutCommand extends Command {
    public static String credits = "Zabbo MS Arcturus 3.5.3 is an closed source project based on Morningstar Arcturus By TheGeneral & Krews \nThe Following people have all contributed to this emulator:\n TheGeneral\n Beny\n Justin \n Hxmada \n Alejandro\n Capheus\n Skeletor\n Harmonic\n Mike\n Remco\n zGrav \n Quadral \n Harmony\n Swirny\n ArpyAge\n Mikkel\n Rodolfo\n Rasmus\n Kitt Mustang\n Snaiker\n nttzx\n necmi\n Dome\n Jose Flores\n Cam\n Oliver\n Narzo\n Tenshie\n MartenM\n Ridge\n SenpaiDipper\n Snaiker\n Thijmen";

    public AboutCommand() {
        super(null, new String[]{"about", "info", "online", "server"});
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) {
        Emulator.getRuntime().gc();
        int intUnixTimestamp = Emulator.getIntUnixTimestamp() - Emulator.getTimeStarted();
        int days = (int) TimeUnit.SECONDS.toDays(intUnixTimestamp);
        long hours = TimeUnit.SECONDS.toHours(intUnixTimestamp) - ((long) (days * 24));
        long minutes = TimeUnit.SECONDS.toMinutes(intUnixTimestamp) - (TimeUnit.SECONDS.toHours(intUnixTimestamp) * 60);
        long seconds = TimeUnit.SECONDS.toSeconds(intUnixTimestamp) - (TimeUnit.SECONDS.toMinutes(intUnixTimestamp) * 60);
        String str = "<b>Arcturus Morningstar 3.5.3 </b>\r\n";
        if (Emulator.getConfig().getBoolean("info.shown", true)) {
            str = str + "<b>Hotel Statistics</b>\r- Online Users: " + Emulator.getGameEnvironment().getHabboManager().getOnlineCount() + "\r- Active Rooms: " + Emulator.getGameEnvironment().getRoomManager().getActiveRooms().size() + "\r- Shop:  " + Emulator.getGameEnvironment().getCatalogManager().catalogPages.size() + " pages and " + CatalogManager.catalogItemAmount + " items. \r- Furni: " + Emulator.getGameEnvironment().getItemManager().getItems().size() + " item definitions\r\n<b>Server Statistics</b>\r- Uptime: " + days + (days > 1 ? " days, " : " day, ") + hours + (hours > 1 ? " hours, " : " hour, ") + minutes + (minutes > 1 ? " minutes, " : " minute, ") + seconds + (seconds > 1 ? " seconds!" : " second!") + "\r- RAM Usage: " + ((Emulator.getRuntime().totalMemory() - Emulator.getRuntime().freeMemory()) / 1048576) + "/" + (Emulator.getRuntime().freeMemory() / 1048576) + "MB\r- CPU Cores: " + Emulator.getRuntime().availableProcessors() + "\r- Total Memory: " + (Emulator.getRuntime().maxMemory() / 1048576) + "MB\r\n";
        }
        gameClient.getHabbo().alert(str + "\r<b>Thanks for using Zabbo MS Arcturus 3.5.3 Report issues on the forums. https://retrotools.xyz \r\r    - The General");
        gameClient.sendResponse(new MessagesForYouComposer((List<String>) Collections.singletonList(credits)));
        return true;
    }
}

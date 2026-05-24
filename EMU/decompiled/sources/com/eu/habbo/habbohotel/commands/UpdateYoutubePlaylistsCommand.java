package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/UpdateYoutubePlaylistsCommand.class */
public class UpdateYoutubePlaylistsCommand extends Command {
    public UpdateYoutubePlaylistsCommand() {
        super("cmd_update_youtube_playlists", Emulator.getTexts().getValue("commands.keys.cmd_update_youtube_playlists").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        Emulator.getGameEnvironment().getItemManager().getYoutubeManager().load();
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_update_youtube_playlists"), RoomChatMessageBubbles.ALERT);
        return true;
    }
}

package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.YoutubeManager;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/AddYoutubePlaylistCommand.class */
public class AddYoutubePlaylistCommand extends Command {
    private static final Logger LOGGER = LoggerFactory.getLogger(AddYoutubePlaylistCommand.class);

    public AddYoutubePlaylistCommand() {
        super("cmd_add_youtube_playlist", Emulator.getTexts().getValue("commands.keys.cmd_add_youtube_playlist").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length < 3) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_add_youtube_playlist.usage"));
            return true;
        }
        try {
            int i = Integer.parseInt(strArr[1]);
            if (Emulator.getGameEnvironment().getItemManager().getItem(i) == null) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_add_youtube_playlist.no_base_item"));
                return true;
            }
            YoutubeManager.YoutubePlaylist playlistDataById = Emulator.getGameEnvironment().getItemManager().getYoutubeManager().getPlaylistDataById(strArr[2]);
            if (playlistDataById == null) {
                gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_add_youtube_playlist.failed_playlist"));
                return true;
            }
            Emulator.getGameEnvironment().getItemManager().getYoutubeManager().addPlaylistToItem(Integer.parseInt(strArr[1]), playlistDataById);
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO `youtube_playlists` (`item_id`, `playlist_id`) VALUES (?, ?)");
                    try {
                        preparedStatementPrepareStatement.setInt(1, i);
                        preparedStatementPrepareStatement.setString(2, strArr[2]);
                        preparedStatementPrepareStatement.execute();
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                    } catch (Throwable th) {
                        if (preparedStatementPrepareStatement != null) {
                            try {
                                preparedStatementPrepareStatement.close();
                            } catch (Throwable th2) {
                                th.addSuppressed(th2);
                            }
                        }
                        throw th;
                    }
                } finally {
                }
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_add_youtube_playlist"));
            return true;
        } catch (NumberFormatException e2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_add_youtube_playlist.no_base_item"));
            return true;
        }
    }
}

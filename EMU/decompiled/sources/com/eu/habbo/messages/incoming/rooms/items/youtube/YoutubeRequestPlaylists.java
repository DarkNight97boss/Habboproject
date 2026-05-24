package com.eu.habbo.messages.incoming.rooms.items.youtube;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.YoutubeManager;
import com.eu.habbo.habbohotel.items.interactions.InteractionYoutubeTV;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.Outgoing;
import com.eu.habbo.messages.outgoing.handshake.ConnectionErrorComposer;
import com.eu.habbo.messages.outgoing.rooms.items.youtube.YoutubeDisplayListComposer;
import java.util.ArrayList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/youtube/YoutubeRequestPlaylists.class */
public class YoutubeRequestPlaylists extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(YoutubeRequestPlaylists.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        if (this.client.getHabbo().getHabboInfo().getCurrentRoom() != null) {
            HabboItem habboItem = this.client.getHabbo().getHabboInfo().getCurrentRoom().getHabboItem(iIntValue);
            if (habboItem instanceof InteractionYoutubeTV) {
                InteractionYoutubeTV interactionYoutubeTV = (InteractionYoutubeTV) habboItem;
                ArrayList<YoutubeManager.YoutubePlaylist> playlistsForItemId = Emulator.getGameEnvironment().getItemManager().getYoutubeManager().getPlaylistsForItemId(habboItem.getBaseItem().getId());
                if (playlistsForItemId != null) {
                    this.client.sendResponse(new YoutubeDisplayListComposer(iIntValue, playlistsForItemId, interactionYoutubeTV.currentPlaylist));
                } else {
                    LOGGER.error("No YouTube playlists set for base item #" + habboItem.getBaseItem().getId());
                    this.client.sendResponse(new ConnectionErrorComposer(Outgoing.CraftableProductsComposer));
                }
            }
        }
    }
}

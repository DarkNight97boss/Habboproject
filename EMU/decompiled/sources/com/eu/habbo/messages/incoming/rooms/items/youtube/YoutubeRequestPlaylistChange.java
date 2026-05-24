package com.eu.habbo.messages.incoming.rooms.items.youtube;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.YoutubeManager;
import com.eu.habbo.habbohotel.items.interactions.InteractionYoutubeTV;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.Outgoing;
import com.eu.habbo.messages.outgoing.rooms.items.youtube.YoutubeVideoComposer;
import com.eu.habbo.threading.runnables.YoutubeAdvanceVideo;
import java.util.Optional;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/youtube/YoutubeRequestPlaylistChange.class */
public class YoutubeRequestPlaylistChange extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Room currentRoom;
        HabboItem habboItem;
        YoutubeManager.YoutubeVideo youtubeVideo;
        int iIntValue = this.packet.readInt().intValue();
        String string = this.packet.readString();
        Habbo habbo = this.client.getHabbo();
        if (habbo == null || (currentRoom = habbo.getHabboInfo().getCurrentRoom()) == null) {
            return;
        }
        if ((currentRoom.isOwner(habbo) || habbo.hasPermission(Permission.ACC_ANYROOMOWNER)) && (habboItem = this.client.getHabbo().getHabboInfo().getCurrentRoom().getHabboItem(iIntValue)) != null && (habboItem instanceof InteractionYoutubeTV)) {
            Optional optionalFindAny = Emulator.getGameEnvironment().getItemManager().getYoutubeManager().getPlaylistsForItemId(habboItem.getBaseItem().getId()).stream().filter(youtubePlaylist -> {
                return youtubePlaylist.getId().equals(string);
            }).findAny();
            if (!optionalFindAny.isPresent() || (youtubeVideo = ((YoutubeManager.YoutubePlaylist) optionalFindAny.get()).getVideos().get(0)) == null) {
                return;
            }
            ((InteractionYoutubeTV) habboItem).currentVideo = youtubeVideo;
            ((InteractionYoutubeTV) habboItem).currentPlaylist = (YoutubeManager.YoutubePlaylist) optionalFindAny.get();
            ((InteractionYoutubeTV) habboItem).cancelAdvancement();
            currentRoom.updateItem(habboItem);
            currentRoom.sendComposer(new YoutubeVideoComposer(iIntValue, youtubeVideo, true, 0).compose());
            ((InteractionYoutubeTV) habboItem).autoAdvance = Emulator.getThreading().run(new YoutubeAdvanceVideo((InteractionYoutubeTV) habboItem), youtubeVideo.getDuration() * Outgoing.CraftableProductsComposer);
            habboItem.needsUpdate(true);
        }
    }
}

package com.eu.habbo.messages.incoming.rooms.items.youtube;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionYoutubeTV;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.Outgoing;
import com.eu.habbo.messages.outgoing.rooms.items.youtube.YoutubeStateChangeComposer;
import com.eu.habbo.messages.outgoing.rooms.items.youtube.YoutubeVideoComposer;
import com.eu.habbo.threading.runnables.YoutubeAdvanceVideo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/youtube/YoutubeRequestStateChange.class */
public class YoutubeRequestStateChange extends MessageHandler {

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/rooms/items/youtube/YoutubeRequestStateChange$YoutubeState.class */
    public enum YoutubeState {
        PREVIOUS(0),
        NEXT(1),
        PAUSE(2),
        RESUME(3);

        private int state;

        YoutubeState(int i) {
            this.state = i;
        }

        public int getState() {
            return this.state;
        }

        public static YoutubeState getByState(int i) {
            switch (i) {
                case 0:
                    return PREVIOUS;
                case 1:
                    return NEXT;
                case 2:
                    return PAUSE;
                case 3:
                    return RESUME;
                default:
                    return null;
            }
        }
    }

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        Habbo habbo;
        Room currentRoom;
        int iIntValue = this.packet.readInt().intValue();
        YoutubeState byState = YoutubeState.getByState(this.packet.readInt().intValue());
        if (byState == null || (habbo = this.client.getHabbo()) == null || (currentRoom = habbo.getHabboInfo().getCurrentRoom()) == null) {
            return;
        }
        if (currentRoom.isOwner(habbo) || habbo.hasPermission(Permission.ACC_ANYROOMOWNER)) {
            HabboItem habboItem = this.client.getHabbo().getHabboInfo().getCurrentRoom().getHabboItem(iIntValue);
            if (habboItem instanceof InteractionYoutubeTV) {
                InteractionYoutubeTV interactionYoutubeTV = (InteractionYoutubeTV) habboItem;
                if (interactionYoutubeTV.currentPlaylist == null || interactionYoutubeTV.currentPlaylist.getVideos().isEmpty()) {
                    return;
                }
                switch (byState) {
                    case PAUSE:
                        interactionYoutubeTV.playing = false;
                        interactionYoutubeTV.offset += Emulator.getIntUnixTimestamp() - interactionYoutubeTV.startedWatchingAt;
                        if (interactionYoutubeTV.autoAdvance != null) {
                            interactionYoutubeTV.autoAdvance.cancel(true);
                        }
                        currentRoom.sendComposer(new YoutubeStateChangeComposer(interactionYoutubeTV.getId(), 2).compose());
                        break;
                    case RESUME:
                        interactionYoutubeTV.playing = true;
                        interactionYoutubeTV.startedWatchingAt = Emulator.getIntUnixTimestamp();
                        interactionYoutubeTV.autoAdvance = Emulator.getThreading().run(new YoutubeAdvanceVideo(interactionYoutubeTV), (interactionYoutubeTV.currentVideo.getDuration() - interactionYoutubeTV.offset) * Outgoing.CraftableProductsComposer);
                        currentRoom.sendComposer(new YoutubeStateChangeComposer(interactionYoutubeTV.getId(), 1).compose());
                        break;
                    case PREVIOUS:
                        int iIndexOf = interactionYoutubeTV.currentPlaylist.getVideos().indexOf(interactionYoutubeTV.currentVideo) - 1;
                        if (iIndexOf < 0) {
                            iIndexOf = interactionYoutubeTV.currentPlaylist.getVideos().size() - 1;
                        }
                        interactionYoutubeTV.currentVideo = interactionYoutubeTV.currentPlaylist.getVideos().get(iIndexOf);
                        break;
                    case NEXT:
                        int iIndexOf2 = interactionYoutubeTV.currentPlaylist.getVideos().indexOf(interactionYoutubeTV.currentVideo) + 1;
                        if (iIndexOf2 >= interactionYoutubeTV.currentPlaylist.getVideos().size()) {
                            iIndexOf2 = 0;
                        }
                        interactionYoutubeTV.currentVideo = interactionYoutubeTV.currentPlaylist.getVideos().get(iIndexOf2);
                        break;
                }
                if (byState == YoutubeState.PREVIOUS || byState == YoutubeState.NEXT) {
                    currentRoom.sendComposer(new YoutubeVideoComposer(interactionYoutubeTV.getId(), interactionYoutubeTV.currentVideo, true, 0).compose());
                    interactionYoutubeTV.cancelAdvancement();
                    interactionYoutubeTV.autoAdvance = Emulator.getThreading().run(new YoutubeAdvanceVideo(interactionYoutubeTV), interactionYoutubeTV.currentVideo.getDuration() * Outgoing.CraftableProductsComposer);
                    interactionYoutubeTV.startedWatchingAt = Emulator.getIntUnixTimestamp();
                    interactionYoutubeTV.offset = 0;
                    interactionYoutubeTV.playing = true;
                    currentRoom.updateItem(interactionYoutubeTV);
                }
            }
        }
    }
}

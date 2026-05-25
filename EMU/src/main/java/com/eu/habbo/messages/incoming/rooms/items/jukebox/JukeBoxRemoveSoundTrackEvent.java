package com.eu.habbo.messages.incoming.rooms.items.jukebox;

import com.eu.habbo.habbohotel.items.interactions.InteractionMusicDisc;
import com.eu.habbo.messages.incoming.MessageHandler;

public class JukeBoxRemoveSoundTrackEvent extends MessageHandler {
    @Override
    public void handle() throws Exception {
        int index = this.packet.readInt();

        com.eu.habbo.habbohotel.rooms.Room room = this.client.getHabbo().getHabboInfo().getCurrentRoom();
        if (room == null || !room.hasRights(this.client.getHabbo())) return; // only users with rights can edit the jukebox

        java.util.List<InteractionMusicDisc> songs = room.getTraxManager().getSongs();
        if (index < 0 || index >= songs.size()) return; // bounds-check the client index

        InteractionMusicDisc musicDisc = songs.get(index);

        if (musicDisc != null) {
            room.getTraxManager().removeSong(musicDisc.getId());
        }
    }
}

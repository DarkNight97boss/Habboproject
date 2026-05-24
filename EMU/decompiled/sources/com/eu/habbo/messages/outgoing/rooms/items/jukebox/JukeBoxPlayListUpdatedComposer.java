package com.eu.habbo.messages.outgoing.rooms.items.jukebox;

import com.eu.habbo.habbohotel.items.SoundTrack;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/jukebox/JukeBoxPlayListUpdatedComposer.class */
public class JukeBoxPlayListUpdatedComposer extends MessageComposer {
    private final THashSet<SoundTrack> tracks;

    public JukeBoxPlayListUpdatedComposer(THashSet<SoundTrack> tHashSet) {
        this.tracks = tHashSet;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.JukeBoxPlayListUpdatedComposer);
        int length = 0;
        TObjectHashIterator it = this.tracks.iterator();
        while (it.hasNext()) {
            length += ((SoundTrack) it.next()).getLength();
        }
        this.response.appendInt(Integer.valueOf(length * Outgoing.CraftableProductsComposer));
        this.response.appendInt(Integer.valueOf(this.tracks.size()));
        TObjectHashIterator it2 = this.tracks.iterator();
        while (it2.hasNext()) {
            SoundTrack soundTrack = (SoundTrack) it2.next();
            this.response.appendInt(Integer.valueOf(soundTrack.getId()));
            this.response.appendInt(Integer.valueOf(soundTrack.getLength() * Outgoing.CraftableProductsComposer));
            this.response.appendString(soundTrack.getCode());
            this.response.appendString(soundTrack.getAuthor());
        }
        return this.response;
    }
}

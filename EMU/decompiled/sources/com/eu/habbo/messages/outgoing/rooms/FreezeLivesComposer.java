package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.habbohotel.games.freeze.FreezeGamePlayer;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/FreezeLivesComposer.class */
public class FreezeLivesComposer extends MessageComposer {
    private final FreezeGamePlayer gamePlayer;

    public FreezeLivesComposer(FreezeGamePlayer freezeGamePlayer) {
        this.gamePlayer = freezeGamePlayer;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.FreezeLivesComposer);
        this.response.appendInt(Integer.valueOf(this.gamePlayer.getHabbo().getRoomUnit().getId()));
        this.response.appendInt(Integer.valueOf(this.gamePlayer.getLives()));
        return this.response;
    }
}

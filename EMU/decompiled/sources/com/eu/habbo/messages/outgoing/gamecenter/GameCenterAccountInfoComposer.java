package com.eu.habbo.messages.outgoing.gamecenter;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/gamecenter/GameCenterAccountInfoComposer.class */
public class GameCenterAccountInfoComposer extends MessageComposer {
    private final int gameId;
    private final int gamesLeft;

    public GameCenterAccountInfoComposer(int i, int i2) {
        this.gameId = i;
        this.gamesLeft = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GameCenterAccountInfoComposer);
        this.response.appendInt(Integer.valueOf(this.gameId));
        this.response.appendInt(Integer.valueOf(this.gamesLeft));
        this.response.appendInt((Integer) 1);
        return this.response;
    }
}

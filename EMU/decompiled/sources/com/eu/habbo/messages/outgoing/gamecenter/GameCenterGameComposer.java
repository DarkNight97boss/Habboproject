package com.eu.habbo.messages.outgoing.gamecenter;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/gamecenter/GameCenterGameComposer.class */
public class GameCenterGameComposer extends MessageComposer {
    public static final int OK = 0;
    public static final int ERROR = 1;
    public final int gameId;
    public final int status;

    public GameCenterGameComposer(int i, int i2) {
        this.gameId = i;
        this.status = i2;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GameCenterGameComposer);
        this.response.appendInt(Integer.valueOf(this.gameId));
        this.response.appendInt(Integer.valueOf(this.status));
        return this.response;
    }
}

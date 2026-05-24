package com.eu.habbo.messages.outgoing.friends;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/friends/FriendNotificationComposer.class */
public class FriendNotificationComposer extends MessageComposer {
    public static final int INSTANT_MESSAGE = -1;
    public static final int ROOM_EVENT = 0;
    public static final int ACHIEVEMENT_COMPLETED = 1;
    public static final int QUEST_COMPLETED = 2;
    public static final int IS_PLAYING_GAME = 3;
    public static final int FINISHED_GAME = 4;
    public static final int INVITE_TO_PLAY_GAME = 5;
    private final int userId;
    private final int type;
    private final String data;

    public FriendNotificationComposer(int i, int i2, String str) {
        this.userId = i;
        this.type = i2;
        this.data = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(3082);
        this.response.appendString(this.userId + Emulator.PREVIEW);
        this.response.appendInt(Integer.valueOf(this.type));
        this.response.appendString(this.data);
        return this.response;
    }
}

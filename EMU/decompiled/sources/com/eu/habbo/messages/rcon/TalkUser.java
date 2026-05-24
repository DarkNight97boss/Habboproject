package com.eu.habbo.messages.rcon;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.google.gson.Gson;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/TalkUser.class */
public class TalkUser extends RCONMessage<JSON> {

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/rcon/TalkUser$JSON.class */
    static class JSON {
        public String type;
        public int user_id;
        public int bubble_id = -1;
        public String message;

        JSON() {
        }
    }

    public TalkUser() {
        super(JSON.class);
    }

    @Override // com.eu.habbo.messages.rcon.RCONMessage
    public void handle(Gson gson, JSON json) {
        Habbo habbo = Emulator.getGameEnvironment().getHabboManager().getHabbo(json.user_id);
        if (habbo == null) {
            this.status = 2;
            this.message = "offline";
        }
        json.type = json.type.toLowerCase();
        switch (json.type) {
            case "talk":
                habbo.talk(json.message, json.bubble_id != -1 ? RoomChatMessageBubbles.getBubble(json.bubble_id) : habbo.getHabboStats().chatColor);
                break;
            case "whisper":
                habbo.whisper(json.message, json.bubble_id != -1 ? RoomChatMessageBubbles.getBubble(json.bubble_id) : habbo.getHabboStats().chatColor);
                break;
            case "shout":
                habbo.shout(json.message, json.bubble_id != -1 ? RoomChatMessageBubbles.getBubble(json.bubble_id) : habbo.getHabboStats().chatColor);
                break;
            default:
                this.status = 1;
                this.message = "Talk type: " + json.type + " not found! Use talk, whisper or shout!";
                break;
        }
    }
}

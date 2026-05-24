package com.eu.habbo.plugin.events.bots;

import com.eu.habbo.habbohotel.bots.Bot;
import java.util.ArrayList;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/bots/BotSavedChatEvent.class */
public class BotSavedChatEvent extends BotEvent {
    public boolean autoChat;
    public boolean randomChat;
    public int chatDelay;
    public ArrayList<String> chat;

    public BotSavedChatEvent(Bot bot, boolean z, boolean z2, int i, ArrayList<String> arrayList) {
        super(bot);
        this.autoChat = z;
        this.randomChat = z2;
        this.chatDelay = i;
        this.chat = arrayList;
    }
}

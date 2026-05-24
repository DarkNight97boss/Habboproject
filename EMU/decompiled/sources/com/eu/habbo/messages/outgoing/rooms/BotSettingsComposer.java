package com.eu.habbo.messages.outgoing.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.bots.Bot;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/BotSettingsComposer.class */
public class BotSettingsComposer extends MessageComposer {
    private final Bot bot;
    private final int settingId;

    public BotSettingsComposer(Bot bot, int i) {
        this.bot = bot;
        this.settingId = i;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.BotSettingsComposer);
        this.response.appendInt(Integer.valueOf(-this.bot.getId()));
        this.response.appendInt(Integer.valueOf(this.settingId));
        switch (this.settingId) {
            case 1:
                this.response.appendString(Emulator.PREVIEW);
                break;
            case 2:
                StringBuilder sb = new StringBuilder();
                if (this.bot.hasChat()) {
                    Iterator<String> it = this.bot.getChatLines().iterator();
                    while (it.hasNext()) {
                        sb.append(it.next()).append("\r");
                    }
                } else {
                    sb.append(Bot.NO_CHAT_SET);
                }
                sb.append(";#;").append(this.bot.isChatAuto() ? "true" : "false");
                sb.append(";#;").append(this.bot.getChatDelay());
                sb.append(";#;").append(this.bot.isChatRandom() ? "true" : "false");
                this.response.appendString(sb.toString());
                break;
            case 3:
                this.response.appendString(Emulator.PREVIEW);
                break;
            case 4:
                this.response.appendString(Emulator.PREVIEW);
                break;
            case 5:
                this.response.appendString(this.bot.getName());
                break;
            case 6:
                this.response.appendString(Emulator.PREVIEW);
                break;
            case 9:
                this.response.appendString(this.bot.getMotto());
                break;
        }
        return this.response;
    }
}

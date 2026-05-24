package com.eu.habbo.messages.outgoing.gamecenter;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/gamecenter/GameCenterGameListComposer.class */
public class GameCenterGameListComposer extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.GameCenterGameListComposer);
        this.response.appendInt((Integer) 2);
        this.response.appendInt((Integer) 0);
        this.response.appendString("snowwar");
        this.response.appendString("93d4f3");
        this.response.appendString(Emulator.PREVIEW);
        this.response.appendString(Emulator.getConfig().getValue("images.gamecenter.snowwar"));
        this.response.appendString(Emulator.PREVIEW);
        this.response.appendInt((Integer) 3);
        this.response.appendString("basejump");
        this.response.appendString("68bbd2");
        this.response.appendString(Emulator.PREVIEW);
        this.response.appendString(Emulator.getConfig().getValue("images.gamecenter.basejump"));
        this.response.appendString(Emulator.PREVIEW);
        this.response.appendInt((Integer) 4);
        this.response.appendString("slotcar");
        this.response.appendString("4a95df");
        this.response.appendString(Emulator.PREVIEW);
        this.response.appendString("http://habboo-a.akamaihd.net/gamecenter/Sulake/slotcar/20130214010101/");
        this.response.appendString(Emulator.PREVIEW);
        return this.response;
    }
}

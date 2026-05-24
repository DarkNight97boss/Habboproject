package com.eu.habbo.messages.outgoing.trading;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/trading/TradeStartFailComposer.class */
public class TradeStartFailComposer extends MessageComposer {
    public static final int HOTEL_TRADING_NOT_ALLOWED = 1;
    public static final int YOU_TRADING_OFF = 2;
    public static final int TARGET_TRADING_NOT_ALLOWED = 4;
    public static final int ROOM_TRADING_NOT_ALLOWED = 6;
    public static final int YOU_ALREADY_TRADING = 7;
    public static final int TARGET_ALREADY_TRADING = 8;
    private final String username;
    private final int code;

    public TradeStartFailComposer(int i) {
        this.code = i;
        this.username = Emulator.PREVIEW;
    }

    public TradeStartFailComposer(int i, String str) {
        this.code = i;
        this.username = str;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.TradeStartFailComposer);
        this.response.appendInt(Integer.valueOf(this.code));
        this.response.appendString(this.username);
        return this.response;
    }
}

package com.eu.habbo.messages.outgoing.unknown;

import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/ModToolSanctionDataComposer.class */
public class ModToolSanctionDataComposer extends MessageComposer {
    private final int unknownInt1;
    private final int accountId;
    private final CFHSanction sanction;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/unknown/ModToolSanctionDataComposer$CFHSanction.class */
    public static class CFHSanction implements ISerialize {
        private final String name;
        private final int length;
        private final int unknownInt1;
        private final boolean avatarOnly;
        private final String tradelockInfo;
        private final String machineBanInfo;

        public CFHSanction(String str, int i, int i2, boolean z, String str2, String str3) {
            this.name = str;
            this.length = i;
            this.unknownInt1 = i2;
            this.avatarOnly = z;
            this.tradelockInfo = str2;
            this.machineBanInfo = str3;
        }

        @Override // com.eu.habbo.messages.ISerialize
        public void serialize(ServerMessage serverMessage) {
            serverMessage.appendString(this.name);
            serverMessage.appendInt(Integer.valueOf(this.length));
            serverMessage.appendInt(Integer.valueOf(this.unknownInt1));
            serverMessage.appendBoolean(Boolean.valueOf(this.avatarOnly));
            serverMessage.appendString(this.tradelockInfo);
            serverMessage.appendString(this.machineBanInfo);
        }
    }

    public ModToolSanctionDataComposer(int i, int i2, CFHSanction cFHSanction) {
        this.unknownInt1 = i;
        this.accountId = i2;
        this.sanction = cFHSanction;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ModToolSanctionDataComposer);
        this.response.appendInt(Integer.valueOf(this.unknownInt1));
        this.response.appendInt(Integer.valueOf(this.accountId));
        this.sanction.serialize(this.response);
        return this.response;
    }
}

package com.eu.habbo.messages.outgoing.generic.alerts;

import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.map.hash.THashMap;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/generic/alerts/BubbleAlertComposer.class */
public class BubbleAlertComposer extends MessageComposer {
    private final String errorKey;
    private final THashMap<String, String> keys;

    public BubbleAlertComposer(String str, THashMap<String, String> tHashMap) {
        this.errorKey = str;
        this.keys = tHashMap;
    }

    public BubbleAlertComposer(String str, String str2) {
        this.errorKey = str;
        this.keys = new THashMap<>();
        this.keys.put("message", str2);
    }

    public BubbleAlertComposer(String str) {
        this.errorKey = str;
        this.keys = new THashMap<>();
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.BubbleAlertComposer);
        this.response.appendString(this.errorKey);
        this.response.appendInt(Integer.valueOf(this.keys.size()));
        for (Map.Entry entry : this.keys.entrySet()) {
            this.response.appendString((String) entry.getKey());
            this.response.appendString((String) entry.getValue());
        }
        return this.response;
    }
}

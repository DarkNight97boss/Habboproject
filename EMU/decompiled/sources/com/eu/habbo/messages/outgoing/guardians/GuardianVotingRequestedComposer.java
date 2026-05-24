package com.eu.habbo.messages.outgoing.guardians;

import com.eu.habbo.habbohotel.guides.GuardianTicket;
import com.eu.habbo.habbohotel.modtool.ModToolChatLog;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.map.hash.TIntIntHashMap;
import java.util.Calendar;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/guardians/GuardianVotingRequestedComposer.class */
public class GuardianVotingRequestedComposer extends MessageComposer {
    private final GuardianTicket ticket;

    public GuardianVotingRequestedComposer(GuardianTicket guardianTicket) {
        this.ticket = guardianTicket;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        TIntIntHashMap tIntIntHashMap = new TIntIntHashMap();
        tIntIntHashMap.put(this.ticket.getReported().getHabboInfo().getId(), 0);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(this.ticket.getDate());
        StringBuilder sb = new StringBuilder(calendar.get(1) + " ");
        sb.append(calendar.get(2)).append(" ");
        sb.append(calendar.get(5)).append(" ");
        sb.append(calendar.get(12)).append(" ");
        sb.append(calendar.get(13)).append(";");
        sb.append("\r");
        for (ModToolChatLog modToolChatLog : this.ticket.getChatLogs()) {
            if (!tIntIntHashMap.containsKey(modToolChatLog.habboId)) {
                tIntIntHashMap.put(modToolChatLog.habboId, tIntIntHashMap.size());
            }
            sb.append("unused;").append(tIntIntHashMap.get(modToolChatLog.habboId)).append(";").append(modToolChatLog.message).append("\r");
        }
        this.response.init(Outgoing.GuardianVotingRequestedComposer);
        this.response.appendInt(Integer.valueOf(this.ticket.getTimeLeft()));
        this.response.appendString(sb.toString());
        return this.response;
    }
}

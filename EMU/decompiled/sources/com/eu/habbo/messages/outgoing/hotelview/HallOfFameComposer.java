package com.eu.habbo.messages.outgoing.hotelview;

import com.eu.habbo.habbohotel.hotelview.HallOfFame;
import com.eu.habbo.habbohotel.hotelview.HallOfFameWinner;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import java.util.ArrayList;
import java.util.Collections;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/hotelview/HallOfFameComposer.class */
public class HallOfFameComposer extends MessageComposer {
    private final HallOfFame hallOfFame;

    public HallOfFameComposer(HallOfFame hallOfFame) {
        this.hallOfFame = hallOfFame;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(3005);
        this.response.appendString(this.hallOfFame.getCompetitionName());
        this.response.appendInt(Integer.valueOf(this.hallOfFame.getWinners().size()));
        int i = 1;
        ArrayList<HallOfFameWinner> arrayList = new ArrayList(this.hallOfFame.getWinners().values());
        Collections.sort(arrayList);
        for (HallOfFameWinner hallOfFameWinner : arrayList) {
            this.response.appendInt(Integer.valueOf(hallOfFameWinner.getId()));
            this.response.appendString(hallOfFameWinner.getUsername());
            this.response.appendString(hallOfFameWinner.getLook());
            this.response.appendInt(Integer.valueOf(i));
            this.response.appendInt(Integer.valueOf(hallOfFameWinner.getPoints()));
            i++;
        }
        return this.response;
    }
}

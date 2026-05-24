package com.eu.habbo.messages.outgoing.rooms.items;

import com.eu.habbo.habbohotel.rooms.RoomMoodlightData;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.map.TIntObjectMap;
import java.util.Iterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/items/MoodLightDataComposer.class */
public class MoodLightDataComposer extends MessageComposer {
    private final TIntObjectMap<RoomMoodlightData> moodLightData;

    public MoodLightDataComposer(TIntObjectMap<RoomMoodlightData> tIntObjectMap) {
        this.moodLightData = tIntObjectMap;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.MoodLightDataComposer);
        this.response.appendInt((Integer) 3);
        int i = 1;
        Iterator it = this.moodLightData.valueCollection().iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            RoomMoodlightData roomMoodlightData = (RoomMoodlightData) it.next();
            if (roomMoodlightData.isEnabled()) {
                this.response.appendInt(Integer.valueOf(roomMoodlightData.getId()));
                i = -1;
                break;
            }
            i++;
        }
        if (i != -1) {
            this.response.appendInt((Integer) 1);
        }
        int i2 = 1;
        for (RoomMoodlightData roomMoodlightData2 : this.moodLightData.valueCollection()) {
            this.response.appendInt(Integer.valueOf(roomMoodlightData2.getId()));
            this.response.appendInt(Integer.valueOf(roomMoodlightData2.isBackgroundOnly() ? 2 : 1));
            this.response.appendString(roomMoodlightData2.getColor());
            this.response.appendInt(Integer.valueOf(roomMoodlightData2.getIntensity()));
            i2++;
        }
        while (i2 <= 3) {
            this.response.appendInt(Integer.valueOf(i2));
            this.response.appendInt((Integer) 1);
            this.response.appendString("#000000");
            this.response.appendInt((Integer) 255);
            i2++;
        }
        return this.response;
    }
}

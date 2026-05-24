package com.eu.habbo.messages.outgoing.navigator;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/navigator/PrivateRoomsComposer.class */
public class PrivateRoomsComposer extends MessageComposer {
    private static final Logger LOGGER = LoggerFactory.getLogger(PrivateRoomsComposer.class);
    private final List<Room> rooms;

    public PrivateRoomsComposer(List<Room> list) {
        this.rooms = list;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        try {
            this.response.init(52);
            this.response.appendInt((Integer) 2);
            this.response.appendString(Emulator.PREVIEW);
            this.response.appendInt(Integer.valueOf(this.rooms.size()));
            Iterator<Room> it = this.rooms.iterator();
            while (it.hasNext()) {
                it.next().serialize(this.response);
            }
            this.response.appendBoolean(true);
            this.response.appendInt((Integer) 0);
            this.response.appendString("A");
            this.response.appendString("B");
            this.response.appendInt((Integer) 1);
            this.response.appendString("C");
            this.response.appendString("D");
            this.response.appendInt((Integer) 1);
            this.response.appendInt((Integer) 1);
            this.response.appendInt((Integer) 1);
            this.response.appendString("E");
            return this.response;
        } catch (Exception e) {
            LOGGER.error("Caught exception", e);
            return null;
        }
    }
}

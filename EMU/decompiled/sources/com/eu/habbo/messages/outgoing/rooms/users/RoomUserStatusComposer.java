package com.eu.habbo.messages.outgoing.rooms.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUnitStatus;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.util.Collection;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/rooms/users/RoomUserStatusComposer.class */
public class RoomUserStatusComposer extends MessageComposer {
    private Collection<Habbo> habbos;
    private THashSet<RoomUnit> roomUnits;
    private double overrideZ;

    public RoomUserStatusComposer(RoomUnit roomUnit) {
        this.overrideZ = -1.0d;
        this.roomUnits = new THashSet<>();
        this.roomUnits.add(roomUnit);
    }

    public RoomUserStatusComposer(RoomUnit roomUnit, double d) {
        this(roomUnit);
        this.overrideZ = d;
    }

    public RoomUserStatusComposer(THashSet<RoomUnit> tHashSet, boolean z) {
        this.overrideZ = -1.0d;
        this.roomUnits = tHashSet;
    }

    public RoomUserStatusComposer(Collection<Habbo> collection) {
        this.overrideZ = -1.0d;
        this.habbos = collection;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.RoomUserStatusComposer);
        if (this.roomUnits != null) {
            this.response.appendInt(Integer.valueOf(this.roomUnits.size()));
            TObjectHashIterator it = this.roomUnits.iterator();
            while (it.hasNext()) {
                RoomUnit roomUnit = (RoomUnit) it.next();
                this.response.appendInt(Integer.valueOf(roomUnit.getId()));
                this.response.appendInt(Short.valueOf(roomUnit.getPreviousLocation().x));
                this.response.appendInt(Short.valueOf(roomUnit.getPreviousLocation().y));
                this.response.appendString((this.overrideZ != -1.0d ? this.overrideZ : roomUnit.getPreviousLocationZ()) + Emulator.PREVIEW);
                this.response.appendInt(Integer.valueOf(roomUnit.getHeadRotation().getValue()));
                this.response.appendInt(Integer.valueOf(roomUnit.getBodyRotation().getValue()));
                StringBuilder sb = new StringBuilder("/");
                for (Map.Entry<RoomUnitStatus, String> entry : roomUnit.getStatusMap().entrySet()) {
                    sb.append(entry.getKey()).append(" ").append(entry.getValue()).append("/");
                }
                this.response.appendString(sb.toString());
                roomUnit.setPreviousLocation(roomUnit.getCurrentLocation());
            }
        } else {
            synchronized (this.habbos) {
                this.response.appendInt(Integer.valueOf(this.habbos.size()));
                for (Habbo habbo : this.habbos) {
                    this.response.appendInt(Integer.valueOf(habbo.getRoomUnit().getId()));
                    this.response.appendInt(Short.valueOf(habbo.getRoomUnit().getPreviousLocation().x));
                    this.response.appendInt(Short.valueOf(habbo.getRoomUnit().getPreviousLocation().y));
                    this.response.appendString(habbo.getRoomUnit().getPreviousLocationZ() + Emulator.PREVIEW);
                    this.response.appendInt(Integer.valueOf(habbo.getRoomUnit().getHeadRotation().getValue()));
                    this.response.appendInt(Integer.valueOf(habbo.getRoomUnit().getBodyRotation().getValue()));
                    StringBuilder sb2 = new StringBuilder("/");
                    for (Map.Entry<RoomUnitStatus, String> entry2 : habbo.getRoomUnit().getStatusMap().entrySet()) {
                        sb2.append(entry2.getKey()).append(" ").append(entry2.getValue()).append("/");
                    }
                    this.response.appendString(sb2.toString());
                    habbo.getRoomUnit().setPreviousLocation(habbo.getRoomUnit().getCurrentLocation());
                }
            }
        }
        return this.response;
    }
}

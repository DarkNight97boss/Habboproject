package com.eu.habbo.messages.incoming.rooms;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.messages.incoming.MessageHandler;

public class RequestRoomLoadEvent extends MessageHandler {

    @Override
    public int getRatelimit() {
        // Hard floor at the dispatcher level — kills the password brute-force
        // entirely (1 attempt / second).
        return 1000;
    }

    @Override
    public void handle() throws Exception {
        int roomId = this.packet.readInt();
        String password = this.packet.readString();

        // BUG FIX: `roomEnterTimestamp` is stored in unix SECONDS
        // (Emulator.getIntUnixTimestamp() inside RoomManager.enterRoom), but
        // was being compared to System.currentTimeMillis(). Effect: the gate
        // was inert and an attacker could pump thousands of password attempts
        // per second on any PASSWORD-protected room. Use the same unit on
        // both sides.
        long nowSec = Emulator.getIntUnixTimestamp();
        if (this.client.getHabbo().getHabboInfo().getLoadingRoom() == 0
                && this.client.getHabbo().getHabboStats().roomEnterTimestamp + 1 <= nowSec) {
            // Stamp the attempt up-front (failed or successful) so the gate
            // throttles bursts of wrong passwords as well as valid loads.
            this.client.getHabbo().getHabboStats().roomEnterTimestamp = (int) nowSec;

            Room room = this.client.getHabbo().getHabboInfo().getCurrentRoom();
            if (room != null) {
                Emulator.getGameEnvironment().getRoomManager().logExit(this.client.getHabbo());

                room.removeHabbo(this.client.getHabbo(), true);

                this.client.getHabbo().getHabboInfo().setCurrentRoom(null);
            }

            if (this.client.getHabbo().getRoomUnit() != null && this.client.getHabbo().getRoomUnit().isTeleporting) {
                this.client.getHabbo().getRoomUnit().isTeleporting = false;
            }

            Emulator.getGameEnvironment().getRoomManager().enterRoom(this.client.getHabbo(), roomId, password);
        }
    }
}

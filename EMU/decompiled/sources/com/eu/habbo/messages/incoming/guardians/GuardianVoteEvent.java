package com.eu.habbo.messages.incoming.guardians;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.guides.GuardianTicket;
import com.eu.habbo.habbohotel.guides.GuardianVoteType;
import com.eu.habbo.messages.incoming.MessageHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/guardians/GuardianVoteEvent.class */
public class GuardianVoteEvent extends MessageHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuardianVoteEvent.class);

    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        GuardianTicket ticketForGuardian = Emulator.getGameEnvironment().getGuideManager().getTicketForGuardian(this.client.getHabbo());
        if (ticketForGuardian != null) {
            GuardianVoteType guardianVoteType = GuardianVoteType.NOT_VOTED;
            if (iIntValue == 0) {
                guardianVoteType = GuardianVoteType.ACCEPTABLY;
            } else if (iIntValue == 1) {
                guardianVoteType = GuardianVoteType.BADLY;
            } else if (iIntValue == 2) {
                guardianVoteType = GuardianVoteType.AWFULLY;
            } else {
                LOGGER.error("Uknown vote type: " + iIntValue);
            }
            ticketForGuardian.vote(this.client.getHabbo(), guardianVoteType);
        }
    }
}

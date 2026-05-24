package com.eu.habbo.messages.incoming.gamecenter;

import com.eu.habbo.messages.incoming.MessageHandler;
import com.eu.habbo.messages.outgoing.gamecenter.GameCenterAchievementsConfigurationComposer;
import com.eu.habbo.messages.outgoing.gamecenter.basejump.BaseJumpJoinQueueComposer;
import com.eu.habbo.messages.outgoing.gamecenter.basejump.BaseJumpLoadGameComposer;
import com.eu.habbo.messages.outgoing.gamecenter.basejump.BaseJumpLoadGameURLComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/incoming/gamecenter/GameCenterJoinGameEvent.class */
public class GameCenterJoinGameEvent extends MessageHandler {
    @Override // com.eu.habbo.messages.incoming.MessageHandler
    public void handle() throws Exception {
        int iIntValue = this.packet.readInt().intValue();
        if (iIntValue == 3) {
            this.client.sendResponse(new GameCenterAchievementsConfigurationComposer());
            this.client.sendResponse(new BaseJumpLoadGameURLComposer());
            this.client.sendResponse(new BaseJumpLoadGameComposer(this.client, 3));
        } else if (iIntValue == 4) {
            this.client.sendResponse(new BaseJumpJoinQueueComposer(4));
            this.client.sendResponse(new BaseJumpLoadGameURLComposer());
        }
    }
}

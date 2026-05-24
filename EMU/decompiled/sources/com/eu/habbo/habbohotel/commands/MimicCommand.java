package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.rooms.RoomChatMessageBubbles;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.habbohotel.users.HabboGender;
import com.eu.habbo.habbohotel.users.clothingvalidation.ClothingValidationManager;
import com.eu.habbo.messages.outgoing.rooms.users.RoomUserDataComposer;
import com.eu.habbo.messages.outgoing.users.UserDataComposer;
import com.eu.habbo.util.figure.FigureUtil;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/MimicCommand.class */
public class MimicCommand extends Command {
    public MimicCommand() {
        super("cmd_mimic", Emulator.getTexts().getValue("commands.keys.cmd_mimic").split(";"));
    }

    @Override // com.eu.habbo.habbohotel.commands.Command
    public boolean handle(GameClient gameClient, String[] strArr) throws Exception {
        if (strArr.length != 2) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_mimic.not_found").replace("%user%", Emulator.PREVIEW), RoomChatMessageBubbles.ALERT);
            return true;
        }
        Habbo habbo = gameClient.getHabbo().getHabboInfo().getCurrentRoom().getHabbo(strArr[1]);
        if (habbo == null) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_mimic.not_found").replace("%user%", Emulator.PREVIEW), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (habbo == gameClient.getHabbo()) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_mimic.not_self"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (habbo.hasPermission(Permission.ACC_NOT_MIMICED) && !gameClient.getHabbo().hasPermission(Permission.ACC_NOT_MIMICED)) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_mimic.blocked").replace("%user%", strArr[1]).replace("%gender_name%", habbo.getHabboInfo().getGender().equals(HabboGender.M) ? Emulator.getTexts().getValue("gender.him") : Emulator.getTexts().getValue("gender.her")), RoomChatMessageBubbles.ALERT);
            return true;
        }
        if (!habbo.hasPermission("acc_mimic_unredeemed") && FigureUtil.hasBlacklistedClothing(habbo.getHabboInfo().getLook(), gameClient.getHabbo().getForbiddenClothing())) {
            gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.error.cmd_mimic.forbidden_clothing"), RoomChatMessageBubbles.ALERT);
            return true;
        }
        gameClient.getHabbo().getHabboInfo().setLook(ClothingValidationManager.VALIDATE_ON_MIMIC ? ClothingValidationManager.validateLook(gameClient.getHabbo(), habbo.getHabboInfo().getLook(), habbo.getHabboInfo().getGender().name()) : habbo.getHabboInfo().getLook());
        gameClient.getHabbo().getHabboInfo().setGender(habbo.getHabboInfo().getGender());
        gameClient.sendResponse(new UserDataComposer(gameClient.getHabbo()));
        gameClient.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new RoomUserDataComposer(gameClient.getHabbo()).compose());
        gameClient.getHabbo().whisper(Emulator.getTexts().getValue("commands.succes.cmd_mimic.copied").replace("%user%", strArr[1]).replace("%gender_name%", habbo.getHabboInfo().getGender().equals(HabboGender.M) ? Emulator.getTexts().getValue("gender.him") : Emulator.getTexts().getValue("gender.her")), RoomChatMessageBubbles.ALERT);
        return true;
    }
}

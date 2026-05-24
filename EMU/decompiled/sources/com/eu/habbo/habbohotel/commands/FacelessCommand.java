package com.eu.habbo.habbohotel.commands;

import com.eu.habbo.Emulator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/commands/FacelessCommand.class */
public class FacelessCommand extends Command {
    public FacelessCommand() {
        super("cmd_faceless", Emulator.getTexts().getValue("commands.keys.cmd_faceless").split(";"));
    }

    /* JADX WARN: Code restructure failed: missing block: B:10:0x0052, code lost:
    
        if (r0[1].equals("99999") != false) goto L14;
     */
    /* JADX WARN: Code restructure failed: missing block: B:11:0x0055, code lost:
    
        r0[1] = "99999";
        r6.getHabbo().getHabboInfo().setLook(r6.getHabbo().getHabboInfo().getLook().replace(r0, "hd-" + r0[1] + "-" + r0[2]));
        r6.sendResponse(new com.eu.habbo.messages.outgoing.users.UpdateUserLookComposer(r6.getHabbo()));
        r6.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new com.eu.habbo.messages.outgoing.rooms.users.RoomUserDataComposer(r6.getHabbo()).compose());
     */
    /* JADX WARN: Code restructure failed: missing block: B:12:0x00c5, code lost:
    
        return true;
     */
    /* JADX WARN: Code restructure failed: missing block: B:9:0x0040, code lost:
    
        r0 = r0.split("-");
     */
    @Override // com.eu.habbo.habbohotel.commands.Command
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct add '--show-bad-code' argument
    */
    public boolean handle(com.eu.habbo.habbohotel.gameclients.GameClient r6, java.lang.String[] r7) throws java.lang.Exception {
        /*
            Method dump skipped, instruction units count: 210
            To view this dump add '--comments-level debug' option
        */
        throw new UnsupportedOperationException("Method not decompiled: com.eu.habbo.habbohotel.commands.FacelessCommand.handle(com.eu.habbo.habbohotel.gameclients.GameClient, java.lang.String[]):boolean");
    }
}

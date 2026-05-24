package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.ModToolCategory;
import com.eu.habbo.habbohotel.modtool.ModToolIssue;
import com.eu.habbo.habbohotel.modtool.ModToolTicketState;
import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.procedure.TObjectProcedure;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/modtool/ModToolComposer.class */
public class ModToolComposer extends MessageComposer implements TObjectProcedure<ModToolCategory> {
    private final Habbo habbo;

    public ModToolComposer(Habbo habbo) {
        this.habbo = habbo;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.ModToolComposer);
        if (this.habbo.hasPermission(Permission.ACC_MODTOOL_TICKET_Q)) {
            THashSet tHashSet = new THashSet();
            for (ModToolIssue modToolIssue : Emulator.getGameEnvironment().getModToolManager().getTickets().values()) {
                if (modToolIssue.state != ModToolTicketState.CLOSED) {
                    tHashSet.add(modToolIssue);
                }
            }
            int size = tHashSet.size();
            if (size > 100) {
                size = 100;
            }
            this.response.appendInt(Integer.valueOf(size));
            TObjectHashIterator it = tHashSet.iterator();
            for (int i = 0; i < size; i++) {
                ((ModToolIssue) it.next()).serialize(this.response);
            }
        } else {
            this.response.appendInt((Integer) 0);
        }
        synchronized (Emulator.getGameEnvironment().getModToolManager().getPresets()) {
            this.response.appendInt(Integer.valueOf(((THashSet) Emulator.getGameEnvironment().getModToolManager().getPresets().get("user")).size()));
            TObjectHashIterator it2 = ((THashSet) Emulator.getGameEnvironment().getModToolManager().getPresets().get("user")).iterator();
            while (it2.hasNext()) {
                this.response.appendString((String) it2.next());
            }
        }
        this.response.appendInt(Integer.valueOf(Emulator.getGameEnvironment().getModToolManager().getCategory().size()));
        Emulator.getGameEnvironment().getModToolManager().getCategory().forEachValue(this);
        this.response.appendBoolean(Boolean.valueOf(this.habbo.hasPermission(Permission.ACC_MODTOOL_TICKET_Q)));
        this.response.appendBoolean(Boolean.valueOf(this.habbo.hasPermission(Permission.ACC_MODTOOL_USER_LOGS)));
        this.response.appendBoolean(Boolean.valueOf(this.habbo.hasPermission(Permission.ACC_MODTOOL_USER_ALERT)));
        this.response.appendBoolean(Boolean.valueOf(this.habbo.hasPermission(Permission.ACC_MODTOOL_USER_KICK)));
        this.response.appendBoolean(Boolean.valueOf(this.habbo.hasPermission(Permission.ACC_MODTOOL_USER_BAN)));
        this.response.appendBoolean(Boolean.valueOf(this.habbo.hasPermission(Permission.ACC_MODTOOL_ROOM_INFO)));
        this.response.appendBoolean(Boolean.valueOf(this.habbo.hasPermission(Permission.ACC_MODTOOL_ROOM_LOGS)));
        synchronized (Emulator.getGameEnvironment().getModToolManager().getPresets()) {
            this.response.appendInt(Integer.valueOf(((THashSet) Emulator.getGameEnvironment().getModToolManager().getPresets().get("room")).size()));
            TObjectHashIterator it3 = ((THashSet) Emulator.getGameEnvironment().getModToolManager().getPresets().get("room")).iterator();
            while (it3.hasNext()) {
                this.response.appendString((String) it3.next());
            }
        }
        return this.response;
    }

    public boolean execute(ModToolCategory modToolCategory) {
        this.response.appendString(modToolCategory.getName());
        return true;
    }
}

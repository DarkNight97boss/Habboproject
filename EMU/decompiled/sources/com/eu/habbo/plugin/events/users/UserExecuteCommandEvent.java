package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.commands.Command;
import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserExecuteCommandEvent.class */
public class UserExecuteCommandEvent extends UserEvent {
    public final Command command;
    public final String[] params;
    private boolean success;

    public UserExecuteCommandEvent(Habbo habbo, Command command, String[] strArr) {
        super(habbo);
        this.command = command;
        this.params = strArr;
        this.success = true;
    }

    public void setSuccess(boolean z) {
        this.success = z;
    }

    public boolean isSuccess() {
        return this.success;
    }
}

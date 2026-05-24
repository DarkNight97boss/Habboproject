package com.eu.habbo.plugin.events.users;

import com.eu.habbo.habbohotel.users.Habbo;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/plugin/events/users/UserGetIPAddressEvent.class */
public class UserGetIPAddressEvent extends UserEvent {
    public final String oldIp;
    private String updatedIp;
    private boolean changedIP;

    public UserGetIPAddressEvent(Habbo habbo, String str) {
        super(habbo);
        this.changedIP = false;
        this.oldIp = str;
    }

    public void setUpdatedIp(String str) {
        this.updatedIp = str;
        this.changedIP = true;
    }

    public boolean hasChangedIP() {
        return this.changedIP;
    }

    public String getUpdatedIp() {
        return this.updatedIp;
    }
}

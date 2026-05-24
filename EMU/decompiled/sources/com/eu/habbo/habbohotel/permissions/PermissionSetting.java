package com.eu.habbo.habbohotel.permissions;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/permissions/PermissionSetting.class */
public enum PermissionSetting {
    DISALLOWED,
    ALLOWED,
    ROOM_OWNER;

    public static PermissionSetting fromString(String str) {
        switch (str) {
            case "1":
                return ALLOWED;
            case "2":
                return ROOM_OWNER;
            default:
                return DISALLOWED;
        }
    }
}

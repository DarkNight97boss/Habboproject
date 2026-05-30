package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.habbohotel.permissions.Permission;
import com.eu.habbo.habbohotel.permissions.PermissionSetting;
import com.eu.habbo.habbohotel.permissions.Rank;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

public class UserPermissionsComposer extends MessageComposer {
    private final int clubLevel;

    private final Habbo habbo;

    public UserPermissionsComposer(Habbo habbo) {
        this.clubLevel = habbo.getHabboStats().hasActiveClub() ? 2 : 0;
        this.habbo = habbo;
    }

    @Override
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UserPermissionsComposer);
        this.response.appendInt(this.clubLevel);
        this.response.appendInt(this.habbo.getHabboInfo().getRank().getLevel());
        this.response.appendBoolean(this.habbo.hasPermission(Permission.ACC_AMBASSADOR));

        // Optional trailing block (Nitro_Render_V3 fork): rank metadata + resolved
        // permission map (key -> ALLOWED|ROOM_OWNER), backward-compatible because
        // the renderer parser guards the read with `bytesAvailable`. Older clients
        // (legacy nitro-react) ignore it; modern clients use it to render
        // `useHasPermission(key)` instead of falling back to securityLevel-based
        // checks. Block layout matches UserPermissionsParser.ts:
        //   int    rankId
        //   string rankName
        //   string rankBadge
        //   string rankPrefix
        //   string rankPrefixColor
        //   int    count
        //   { string key, int value (1=ALLOWED, 2=ROOM_OWNER) } x count
        try {
            Rank rank = this.habbo.getHabboInfo().getRank();
            if (rank != null) {
                this.response.appendInt(rank.getId());
                this.response.appendString(rank.getName() == null ? "" : rank.getName());
                this.response.appendString(rank.getBadge() == null ? "" : rank.getBadge());
                this.response.appendString(rank.getPrefix() == null ? "" : rank.getPrefix());
                this.response.appendString(rank.getPrefixColor() == null ? "" : rank.getPrefixColor());

                int count = 0;
                for (Permission p : rank.getPermissions().values()) {
                    if (p == null || p.setting == PermissionSetting.DISALLOWED) continue;
                    count++;
                }
                this.response.appendInt(count);

                for (Permission p : rank.getPermissions().values()) {
                    if (p == null || p.setting == PermissionSetting.DISALLOWED) continue;
                    this.response.appendString(p.key);
                    this.response.appendInt(p.setting == PermissionSetting.ROOM_OWNER ? 2 : 1);
                }
            }
        } catch (Throwable ignored) {
            // Defensive: any wire-time failure should not block the basic 3-field response
            // that legacy clients depend on. The trailing block is best-effort.
        }

        return this.response;
    }

    public int getClubLevel() {
        return clubLevel;
    }

    public Habbo getHabbo() {
        return habbo;
    }
}

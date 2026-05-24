package com.eu.habbo.habbohotel.guilds;

import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/guilds/GuildMember.class */
public class GuildMember implements Comparable {
    private int userId;
    private String username;
    private String look;
    private int joinDate;
    private GuildRank rank;

    public GuildMember(ResultSet resultSet) throws SQLException {
        this.userId = resultSet.getInt("user_id");
        this.username = resultSet.getString("username");
        this.look = resultSet.getString("look");
        this.joinDate = resultSet.getInt("member_since");
        this.rank = GuildRank.getRank(resultSet.getInt("level_id"));
    }

    public GuildMember(int i, String str, String str2, int i2, int i3) {
        this.userId = i;
        this.username = str;
        this.look = str2;
        this.joinDate = i2;
        this.rank = GuildRank.values()[i3];
    }

    public int getUserId() {
        return this.userId;
    }

    public String getUsername() {
        return this.username;
    }

    public String getLook() {
        return this.look;
    }

    public void setLook(String str) {
        this.look = str;
    }

    public int getJoinDate() {
        return this.joinDate;
    }

    public void setJoinDate(int i) {
        this.joinDate = i;
    }

    public GuildRank getRank() {
        return this.rank;
    }

    public void setRank(GuildRank guildRank) {
        this.rank = guildRank;
    }

    @Override // java.lang.Comparable
    public int compareTo(Object obj) {
        return 0;
    }

    public GuildMembershipStatus getMembershipStatus() {
        return this.rank == GuildRank.DELETED ? GuildMembershipStatus.NOT_MEMBER : (this.rank == GuildRank.OWNER || this.rank == GuildRank.ADMIN || this.rank == GuildRank.MEMBER) ? GuildMembershipStatus.MEMBER : this.rank == GuildRank.REQUESTED ? GuildMembershipStatus.PENDING : GuildMembershipStatus.NOT_MEMBER;
    }
}

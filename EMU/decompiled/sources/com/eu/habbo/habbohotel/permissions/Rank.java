package com.eu.habbo.habbohotel.permissions;

import gnu.trove.map.hash.THashMap;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/permissions/Rank.class */
public class Rank {
    private final int id;
    private final int level;
    private String name;
    private String badge;
    private int roomEffect;
    private boolean logCommands;
    private String prefix;
    private String prefixColor;
    private boolean hasPrefix;
    private final THashMap<String, Permission> permissions = new THashMap<>();
    private final THashMap<String, String> variables = new THashMap<>();
    private int diamondsTimerAmount = 1;
    private int creditsTimerAmount = 1;
    private int pixelsTimerAmount = 1;
    private int gotwTimerAmount = 1;

    public Rank(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.level = resultSet.getInt("level");
        load(resultSet);
    }

    public void load(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        this.name = resultSet.getString("rank_name");
        this.badge = resultSet.getString("badge");
        this.roomEffect = resultSet.getInt("room_effect");
        this.logCommands = resultSet.getString("log_commands").equals("1");
        this.prefix = resultSet.getString("prefix");
        this.prefixColor = resultSet.getString("prefix_color");
        this.diamondsTimerAmount = resultSet.getInt("auto_points_amount");
        this.creditsTimerAmount = resultSet.getInt("auto_credits_amount");
        this.pixelsTimerAmount = resultSet.getInt("auto_pixels_amount");
        this.gotwTimerAmount = resultSet.getInt("auto_gotw_amount");
        this.hasPrefix = !this.prefix.isEmpty();
        for (int i = 1; i < metaData.getColumnCount() + 1; i++) {
            String columnName = metaData.getColumnName(i);
            if (columnName.startsWith("cmd_") || columnName.startsWith("acc_")) {
                this.permissions.put(metaData.getColumnName(i), new Permission(columnName, PermissionSetting.fromString(resultSet.getString(i))));
            } else {
                this.variables.put(metaData.getColumnName(i), resultSet.getString(i));
            }
        }
    }

    public boolean hasPermission(String str, boolean z) {
        if (!this.permissions.containsKey(str)) {
            return false;
        }
        Permission permission = (Permission) this.permissions.get(str);
        return permission.setting == PermissionSetting.ALLOWED || (permission.setting == PermissionSetting.ROOM_OWNER && z);
    }

    public int getId() {
        return this.id;
    }

    public int getLevel() {
        return this.level;
    }

    public String getName() {
        return this.name;
    }

    public String getBadge() {
        return this.badge;
    }

    public THashMap<String, Permission> getPermissions() {
        return this.permissions;
    }

    public THashMap<String, String> getVariables() {
        return this.variables;
    }

    public int getRoomEffect() {
        return this.roomEffect;
    }

    public boolean isLogCommands() {
        return this.logCommands;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public String getPrefixColor() {
        return this.prefixColor;
    }

    public boolean hasPrefix() {
        return this.hasPrefix;
    }

    public int getDiamondsTimerAmount() {
        return this.diamondsTimerAmount;
    }

    public int getCreditsTimerAmount() {
        return this.creditsTimerAmount;
    }

    public int getPixelsTimerAmount() {
        return this.pixelsTimerAmount;
    }

    public int getGotwTimerAmount() {
        return this.gotwTimerAmount;
    }
}

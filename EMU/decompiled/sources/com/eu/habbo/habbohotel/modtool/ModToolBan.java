package com.eu.habbo.habbohotel.modtool;

import com.eu.habbo.Emulator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/modtool/ModToolBan.class */
public class ModToolBan implements Runnable {
    private static final Logger LOGGER = LoggerFactory.getLogger(ModToolBan.class);
    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public int userId;
    public String ip;
    public String machineId;
    public int staffId;
    public int expireDate;
    public int timestamp;
    public String reason;
    public ModToolBanType type;
    public int cfhTopic;
    private boolean needsInsert;

    public ModToolBan(ResultSet resultSet) throws SQLException {
        this.userId = resultSet.getInt("user_id");
        this.ip = resultSet.getString("ip");
        this.machineId = resultSet.getString("machine_id");
        this.staffId = resultSet.getInt("user_staff_id");
        this.timestamp = resultSet.getInt("timestamp");
        this.expireDate = resultSet.getInt("ban_expire");
        this.reason = resultSet.getString("ban_reason");
        this.type = ModToolBanType.fromString(resultSet.getString("type"));
        this.cfhTopic = resultSet.getInt("cfh_topic");
        this.needsInsert = false;
    }

    public ModToolBan(int i, String str, String str2, int i2, int i3, String str3, ModToolBanType modToolBanType, int i4) {
        this.userId = i;
        this.staffId = i2;
        this.timestamp = Emulator.getIntUnixTimestamp();
        this.expireDate = i3;
        this.reason = str3;
        this.ip = str;
        this.machineId = str2;
        this.type = modToolBanType;
        this.cfhTopic = i4;
        this.needsInsert = true;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.needsInsert) {
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("INSERT INTO bans (user_id, ip, machine_id, user_staff_id, timestamp, ban_expire, ban_reason, type, cfh_topic) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
                    try {
                        preparedStatementPrepareStatement.setInt(1, this.userId);
                        preparedStatementPrepareStatement.setString(2, this.ip);
                        preparedStatementPrepareStatement.setString(3, this.machineId);
                        preparedStatementPrepareStatement.setInt(4, this.staffId);
                        preparedStatementPrepareStatement.setInt(5, Emulator.getIntUnixTimestamp());
                        preparedStatementPrepareStatement.setInt(6, this.expireDate);
                        preparedStatementPrepareStatement.setString(7, this.reason);
                        preparedStatementPrepareStatement.setString(8, this.type.getType());
                        preparedStatementPrepareStatement.setInt(9, this.cfhTopic);
                        preparedStatementPrepareStatement.execute();
                        if (preparedStatementPrepareStatement != null) {
                            preparedStatementPrepareStatement.close();
                        }
                        if (connection != null) {
                            connection.close();
                        }
                    } catch (Throwable th) {
                        if (preparedStatementPrepareStatement != null) {
                            try {
                                preparedStatementPrepareStatement.close();
                            } catch (Throwable th2) {
                                th.addSuppressed(th2);
                            }
                        }
                        throw th;
                    }
                } finally {
                }
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
        }
    }

    public String listInfo() {
        return "Banned User Id: " + this.userId + "\rType: " + this.type.getType() + "\rReason: <i>" + this.reason + "</i>\rModerator Id: " + this.staffId + "\rDate: " + dateFormat.format(Long.valueOf(((long) this.timestamp) * 1000)) + "\rExpire Date: " + dateFormat.format(Long.valueOf(((long) this.expireDate) * 1000)) + "\rIP: " + this.ip + "\rMachineID: " + this.machineId + "\rTopic: " + this.cfhTopic;
    }
}

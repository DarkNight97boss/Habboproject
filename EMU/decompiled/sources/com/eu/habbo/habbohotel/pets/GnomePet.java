package com.eu.habbo.habbohotel.pets;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/GnomePet.class */
public class GnomePet extends Pet implements IPetLook {
    private static final Logger LOGGER = LoggerFactory.getLogger(GnomePet.class);
    private final String gnomeData;

    public GnomePet(ResultSet resultSet) throws SQLException {
        super(resultSet);
        this.gnomeData = resultSet.getString("gnome_data");
    }

    public GnomePet(int i, int i2, String str, String str2, int i3, String str3) {
        super(i, i2, str, str2, i3);
        this.gnomeData = str3;
    }

    @Override // com.eu.habbo.habbohotel.pets.Pet, com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        serverMessage.appendInt(Integer.valueOf(getId()));
        serverMessage.appendString(getName());
        serverMessage.appendInt(Integer.valueOf(this.petData.getType()));
        serverMessage.appendInt(Integer.valueOf(this.race));
        serverMessage.appendString(this.color);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt((Integer) 0);
        serverMessage.appendInt((Integer) 0);
    }

    @Override // com.eu.habbo.habbohotel.pets.Pet, java.lang.Runnable
    public void run() {
        if (this.needsUpdate) {
            super.run();
            try {
                Connection connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    PreparedStatement preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users_pets SET gnome_data = ? WHERE id = ? LIMIT 1");
                    try {
                        preparedStatementPrepareStatement.setString(1, this.gnomeData);
                        preparedStatementPrepareStatement.setInt(2, this.id);
                        preparedStatementPrepareStatement.executeUpdate();
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

    @Override // com.eu.habbo.habbohotel.pets.IPetLook
    public String getLook() {
        return getPetData().getType() + " 0 FFFFFF " + this.gnomeData;
    }

    @Override // com.eu.habbo.habbohotel.pets.Pet
    public void scratched(Habbo habbo) {
        super.scratched(habbo);
        if (getPetData().getType() == 26) {
            if (habbo != null) {
                AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("GnomeRespectGiver"));
            }
            AchievementManager.progressAchievement(Emulator.getGameEnvironment().getHabboManager().getHabbo(getUserId()), Emulator.getGameEnvironment().getAchievementManager().getAchievement("GnomeRespectReceiver"));
        } else if (getPetData().getType() == 27) {
            if (habbo != null) {
                AchievementManager.progressAchievement(habbo, Emulator.getGameEnvironment().getAchievementManager().getAchievement("LeprechaunRespectGiver"));
            }
            AchievementManager.progressAchievement(Emulator.getGameEnvironment().getHabboManager().getHabbo(getUserId()), Emulator.getGameEnvironment().getAchievementManager().getAchievement("LeprechaunRespectReceiver"));
        }
    }

    @Override // com.eu.habbo.habbohotel.pets.Pet
    protected void levelUp() {
        super.levelUp();
        if (getPetData().getType() == 26) {
            AchievementManager.progressAchievement(Emulator.getGameEnvironment().getHabboManager().getHabbo(getUserId()), Emulator.getGameEnvironment().getAchievementManager().getAchievement("GnomeLevelUp"));
        } else if (getPetData().getType() == 27) {
            AchievementManager.progressAchievement(Emulator.getGameEnvironment().getHabboManager().getHabbo(getUserId()), Emulator.getGameEnvironment().getAchievementManager().getAchievement("LeprechaunLevelUp"));
        }
    }
}

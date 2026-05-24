package com.eu.habbo.habbohotel.pets;

import com.eu.habbo.Emulator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/HorsePet.class */
public class HorsePet extends RideablePet {
    private static final Logger LOGGER = LoggerFactory.getLogger(HorsePet.class);
    private int hairColor;
    private int hairStyle;

    public HorsePet(ResultSet resultSet) throws SQLException {
        super(resultSet);
        this.hairColor = resultSet.getInt("hair_color");
        this.hairStyle = resultSet.getInt("hair_style");
        hasSaddle(resultSet.getString("saddle").equalsIgnoreCase("1"));
        setAnyoneCanRide(resultSet.getString("ride").equalsIgnoreCase("1"));
        setSaddleItemId(resultSet.getInt("saddle_item_id"));
    }

    public HorsePet(int i, int i2, String str, String str2, int i3) {
        super(i, i2, str, str2, i3);
        this.hairColor = 0;
        this.hairStyle = -1;
        hasSaddle(false);
        setAnyoneCanRide(false);
    }

    @Override // com.eu.habbo.habbohotel.pets.Pet, java.lang.Runnable
    public void run() {
        Connection connection;
        PreparedStatement preparedStatementPrepareStatement;
        if (this.needsUpdate) {
            try {
                connection = Emulator.getDatabase().getDataSource().getConnection();
                try {
                    preparedStatementPrepareStatement = connection.prepareStatement("UPDATE users_pets SET hair_style = ?, hair_color = ?, saddle = ?, ride = ?, saddle_item_id = ? WHERE id = ?");
                } finally {
                }
            } catch (SQLException e) {
                LOGGER.error("Caught SQL exception", e);
            }
            try {
                preparedStatementPrepareStatement.setInt(1, this.hairStyle);
                preparedStatementPrepareStatement.setInt(2, this.hairColor);
                preparedStatementPrepareStatement.setString(3, hasSaddle() ? "1" : "0");
                preparedStatementPrepareStatement.setString(4, anyoneCanRide() ? "1" : "0");
                preparedStatementPrepareStatement.setInt(5, getSaddleItemId());
                preparedStatementPrepareStatement.setInt(6, super.getId());
                preparedStatementPrepareStatement.execute();
                if (preparedStatementPrepareStatement != null) {
                    preparedStatementPrepareStatement.close();
                }
                if (connection != null) {
                    connection.close();
                }
                super.run();
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
        }
    }

    public int getHairColor() {
        return this.hairColor;
    }

    public void setHairColor(int i) {
        this.hairColor = i;
    }

    public int getHairStyle() {
        return this.hairStyle;
    }

    public void setHairStyle(int i) {
        this.hairStyle = i;
    }
}

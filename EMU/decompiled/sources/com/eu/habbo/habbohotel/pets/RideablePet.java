package com.eu.habbo.habbohotel.pets;

import com.eu.habbo.habbohotel.users.Habbo;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/pets/RideablePet.class */
public class RideablePet extends Pet {
    private Habbo rider;
    private boolean hasSaddle;
    private boolean anyoneCanRide;
    private int saddleItemId;

    public RideablePet(ResultSet resultSet) throws SQLException {
        super(resultSet);
        this.rider = null;
    }

    public RideablePet(int i, int i2, String str, String str2, int i3) {
        super(i, i2, str, str2, i3);
        this.rider = null;
    }

    public boolean hasSaddle() {
        return this.hasSaddle;
    }

    public void hasSaddle(boolean z) {
        this.hasSaddle = z;
    }

    public boolean anyoneCanRide() {
        return this.anyoneCanRide;
    }

    public void setAnyoneCanRide(boolean z) {
        this.anyoneCanRide = z;
    }

    public Habbo getRider() {
        return this.rider;
    }

    public void setRider(Habbo habbo) {
        this.rider = habbo;
    }

    public int getSaddleItemId() {
        return this.saddleItemId;
    }

    public void setSaddleItemId(int i) {
        this.saddleItemId = i;
    }
}

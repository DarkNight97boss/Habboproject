package com.eu.habbo.habbohotel.items.interactions;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.gameclients.GameClient;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.habbohotel.rooms.*;
import com.eu.habbo.threading.runnables.KickBallAction;

import java.sql.ResultSet;
import java.sql.SQLException;


public abstract class InteractionPushable extends InteractionDefault {


    private KickBallAction currentThread;

    /**
     * Timestamp dell'ultimo kick (ms). Usato per applicare un cooldown a
     * livello "esperienza utente": dopo OGNI calcio della palla (anche se
     * il KickBallAction termina in pochi ms perche' totalSteps=1), nuove
     * interazioni vengono ignorate per un breve periodo.
     *
     * Perche' serve: la sola guard `currentThread.dead == false` non basta.
     * Un kick con totalSteps=1 termina in <10ms; quando l'avatar arriva sulla
     * palla ~500ms dopo (perche' continuava il path), currentThread.dead e'
     * gia' true, la guard non scatta, e parte un secondo kick.
     */
    private volatile long lastKickedAtMs = 0L;
    /** Durata del cooldown in ms tra un kick e l'altro. */
    private static final long KICK_COOLDOWN_MS = 1000L;

    private boolean inKickCooldown() {
        return (System.currentTimeMillis() - this.lastKickedAtMs) < KICK_COOLDOWN_MS;
    }

    public InteractionPushable(ResultSet set, Item baseItem) throws SQLException {
        super(set, baseItem);
        this.setExtradata("0");
    }

    public InteractionPushable(int id, int userId, Item item, String extradata, int limitedStack, int limitedSells) {
        super(id, userId, item, extradata, limitedStack, limitedSells);
        this.setExtradata("0");
    }

    @Override
    public boolean canWalkOn(RoomUnit roomUnit, Room room, Object[] objects) {
        return true;
    }

    @Override
    public boolean isWalkable() {
        return true;
    }

    @Override
    public void onWalkOff(RoomUnit roomUnit, final Room room, Object[] objects) throws Exception {
        super.onWalkOff(roomUnit, room, objects);

        // Cooldown anti-doppio-kick (vedi commento sul campo lastKickedAtMs).
        if (this.inKickCooldown()) return;

        int velocity = this.getWalkOffVelocity(roomUnit, room);
        RoomUserRotation direction = this.getWalkOffDirection(roomUnit, room);
        this.onKick(room, roomUnit, velocity, direction);

        if (velocity > 0) {
            if (this.currentThread != null)
                this.currentThread.dead = true;

            this.currentThread = new KickBallAction(this, room, roomUnit, direction, velocity, false);
            this.lastKickedAtMs = System.currentTimeMillis();
            Emulator.getThreading().run(this.currentThread, 0);
        }
    }

    @Override
    public void onClick(GameClient client, Room room, Object[] objects) throws Exception {
        super.onClick(client, room, objects);

        if (client == null) return;
        // Cooldown anti-doppio-kick.
        if (this.inKickCooldown()) return;
        if (RoomLayout.tilesAdjecent(client.getHabbo().getRoomUnit().getCurrentLocation(), room.getLayout().getTile(this.getX(), this.getY()))) {
            int velocity = this.getTackleVelocity(client.getHabbo().getRoomUnit(), room);
            RoomUserRotation direction = this.getWalkOnDirection(client.getHabbo().getRoomUnit(), room);
            this.onTackle(room, client.getHabbo().getRoomUnit(), velocity, direction);

            if (velocity > 0) {
                if (this.currentThread != null)
                    this.currentThread.dead = true;

                this.currentThread = new KickBallAction(this, room, client.getHabbo().getRoomUnit(), direction, velocity, false);
                this.lastKickedAtMs = System.currentTimeMillis();
                Emulator.getThreading().run(this.currentThread, 0);
            }
        }
    }

    @Override
    public void onWalkOn(RoomUnit roomUnit, final Room room, Object[] objects) throws Exception {
        super.onWalkOn(roomUnit, room, objects);

        // BUGFIX (palla doppio-kick): cooldown temporale di KICK_COOLDOWN_MS
        // dopo OGNI kick. La sola guard `currentThread.dead == false` non
        // bastava perche' un kick con totalSteps=1 termina in pochi ms; quando
        // l'avatar raggiungeva la palla ~500ms dopo, currentThread.dead era
        // gia' true e partiva un secondo kick. Risultato: palla a 2 caselle.
        // Con il cooldown, anche dopo il completamento del thread, la palla
        // resta "intoccabile" per 1 secondo. Niente piu' doppio kick.
        if (this.inKickCooldown()) return;

        int velocity;
        boolean isDrag = false;
        RoomUserRotation direction;

        if (this.getX() == roomUnit.getGoal().x && this.getY() == roomUnit.getGoal().y) //User clicked on the tile the ball is on, they want to kick it
        {
            velocity = this.getWalkOnVelocity(roomUnit, room);
            direction = this.getWalkOnDirection(roomUnit, room);
            this.onKick(room, roomUnit, velocity, direction);
        } else //User is walking past the ball, they want to drag it with them
        {
            velocity = this.getDragVelocity(roomUnit, room);
            direction = this.getDragDirection(roomUnit, room);
            this.onDrag(room, roomUnit, velocity, direction);
            isDrag = true;
        }

        if (velocity > 0) {
            this.currentThread = new KickBallAction(this, room, roomUnit, direction, velocity, isDrag);
            this.lastKickedAtMs = System.currentTimeMillis();
            Emulator.getThreading().run(this.currentThread, 0);
        }
    }


    public abstract int getWalkOnVelocity(RoomUnit roomUnit, Room room);


    public abstract RoomUserRotation getWalkOnDirection(RoomUnit roomUnit, Room room);


    public abstract int getWalkOffVelocity(RoomUnit roomUnit, Room room);


    public abstract RoomUserRotation getWalkOffDirection(RoomUnit roomUnit, Room room);


    public abstract int getDragVelocity(RoomUnit roomUnit, Room room);


    public abstract RoomUserRotation getDragDirection(RoomUnit roomUnit, Room room);


    public abstract int getTackleVelocity(RoomUnit roomUnit, Room room);


    public abstract RoomUserRotation getTackleDirection(RoomUnit roomUnit, Room room);


    public abstract int getNextRollDelay(int currentStep, int totalSteps); //The length in milliseconds when the ball should next roll


    public abstract RoomUserRotation getBounceDirection(Room room, RoomUserRotation currentDirection); //Returns the new direction to move the ball when the ball cannot move


    public abstract boolean validMove(Room room, RoomTile from, RoomTile to); //Checks if the next move is valid


    public abstract void onDrag(Room room, RoomUnit roomUnit, int velocity, RoomUserRotation direction);


    public abstract void onKick(Room room, RoomUnit roomUnit, int velocity, RoomUserRotation direction);


    public abstract void onTackle(Room room, RoomUnit roomUnit, int velocity, RoomUserRotation direction);


    public abstract void onMove(Room room, RoomTile from, RoomTile to, RoomUserRotation direction, RoomUnit kicker, int nextRoll, int currentStep, int totalSteps);


    public abstract void onBounce(Room room, RoomUserRotation oldDirection, RoomUserRotation newDirection, RoomUnit kicker);


    public abstract void onStop(Room room, RoomUnit kicker, int currentStep, int totalSteps);


    public abstract boolean canStillMove(Room room, RoomTile from, RoomTile to, RoomUserRotation direction, RoomUnit kicker, int nextRoll, int currentStep, int totalSteps);

}
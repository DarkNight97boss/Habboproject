package com.eu.habbo.threading.runnables;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionPushable;
import com.eu.habbo.habbohotel.rooms.Room;
import com.eu.habbo.habbohotel.rooms.RoomTile;
import com.eu.habbo.habbohotel.rooms.RoomUnit;
import com.eu.habbo.habbohotel.rooms.RoomUserRotation;
import com.eu.habbo.messages.outgoing.rooms.items.FloorItemOnRollerComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/threading/runnables/KickBallAction.class */
public class KickBallAction implements Runnable {
    private final InteractionPushable ball;
    private final Room room;
    private final RoomUnit kicker;
    private final int totalSteps;
    private RoomUserRotation currentDirection;
    public final boolean isDrag;
    public boolean dead = false;
    private int currentStep = 0;

    public KickBallAction(InteractionPushable interactionPushable, Room room, RoomUnit roomUnit, RoomUserRotation roomUserRotation, int i, boolean z) {
        this.ball = interactionPushable;
        this.room = room;
        this.kicker = roomUnit;
        this.currentDirection = roomUserRotation;
        this.totalSteps = i;
        this.isDrag = z;
    }

    @Override // java.lang.Runnable
    public void run() {
        if (this.dead || !this.room.isLoaded()) {
            return;
        }
        if (this.currentStep >= this.totalSteps) {
            this.ball.onStop(this.room, this.kicker, this.currentStep, this.totalSteps);
            this.dead = true;
            return;
        }
        RoomTile tileInFront = this.room.getLayout().getTileInFront(this.room.getLayout().getTile(this.ball.getX(), this.ball.getY()), this.currentDirection.getValue());
        if (tileInFront == null || !this.ball.validMove(this.room, this.room.getLayout().getTile(this.ball.getX(), this.ball.getY()), tileInFront)) {
            RoomUserRotation roomUserRotation = this.currentDirection;
            if (!this.isDrag) {
                this.currentDirection = this.ball.getBounceDirection(this.room, this.currentDirection);
            }
            if (this.currentDirection != roomUserRotation) {
                this.ball.onBounce(this.room, roomUserRotation, this.currentDirection, this.kicker);
            } else {
                this.currentStep = this.totalSteps;
            }
            run();
            return;
        }
        this.currentStep++;
        int nextRollDelay = this.ball.getNextRollDelay(this.currentStep, this.totalSteps);
        if (!this.ball.canStillMove(this.room, this.room.getLayout().getTile(this.ball.getX(), this.ball.getY()), tileInFront, this.currentDirection, this.kicker, nextRollDelay, this.currentStep, this.totalSteps)) {
            this.currentStep = this.totalSteps;
            run();
        } else {
            this.ball.onMove(this.room, this.room.getLayout().getTile(this.ball.getX(), this.ball.getY()), tileInFront, this.currentDirection, this.kicker, nextRollDelay, this.currentStep, this.totalSteps);
            this.room.sendComposer(new FloorItemOnRollerComposer(this.ball, null, tileInFront, tileInFront.getStackHeight() - this.ball.getZ(), this.room).compose());
            Emulator.getThreading().run(this, nextRollDelay);
        }
    }
}

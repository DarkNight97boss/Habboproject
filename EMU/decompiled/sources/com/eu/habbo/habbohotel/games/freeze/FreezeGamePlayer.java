package com.eu.habbo.habbohotel.games.freeze;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.achievements.AchievementManager;
import com.eu.habbo.habbohotel.games.GamePlayer;
import com.eu.habbo.habbohotel.games.GameTeamColors;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.outgoing.rooms.FreezeLivesComposer;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/games/freeze/FreezeGamePlayer.class */
public class FreezeGamePlayer extends GamePlayer {
    public boolean nextDiagonal;
    public boolean nextHorizontal;
    public boolean tempMassiveExplosion;
    public boolean dead;
    private int lives;
    private int snowBalls;
    private int explosionBoost;
    private int protectionTime;
    private int frozenTime;

    public FreezeGamePlayer(Habbo habbo, GameTeamColors gameTeamColors) {
        super(habbo, gameTeamColors);
    }

    @Override // com.eu.habbo.habbohotel.games.GamePlayer
    public void reset() {
        this.lives = 3;
        this.snowBalls = 1;
        this.explosionBoost = 0;
        this.protectionTime = 0;
        this.frozenTime = 0;
        this.nextDiagonal = false;
        this.nextHorizontal = true;
        this.tempMassiveExplosion = false;
        this.dead = false;
        super.reset();
    }

    @Override // com.eu.habbo.habbohotel.games.GamePlayer
    public void addScore(int i) {
        super.addScore(i);
        if (i > 0) {
            AchievementManager.progressAchievement(getHabbo(), Emulator.getGameEnvironment().getAchievementManager().getAchievement("FreezePlayer"), i);
        }
    }

    public void addLife() {
        if (this.lives < FreezeGame.MAX_LIVES) {
            this.lives++;
            super.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new FreezeLivesComposer(this).compose());
        }
    }

    public void takeLife() {
        this.lives--;
        if (this.lives != 0) {
            super.getHabbo().getHabboInfo().getCurrentRoom().sendComposer(new FreezeLivesComposer(this).compose());
            return;
        }
        this.dead = true;
        FreezeGame freezeGame = (FreezeGame) super.getHabbo().getHabboInfo().getCurrentRoom().getGame(FreezeGame.class);
        if (freezeGame != null) {
            freezeGame.playerDies(this);
        }
    }

    public int getLives() {
        return this.lives;
    }

    public boolean canPickupLife() {
        return this.lives < 3;
    }

    public void addSnowball() {
        if (this.snowBalls < FreezeGame.MAX_SNOWBALLS) {
            this.snowBalls++;
        }
    }

    public void addSnowball(int i) {
        this.snowBalls += i;
        if (this.snowBalls < 1) {
            this.snowBalls = 1;
        }
    }

    public void takeSnowball() {
        if (this.snowBalls > 0) {
            this.snowBalls--;
        }
    }

    public boolean canThrowSnowball() {
        return this.snowBalls > 0 && !isFrozen();
    }

    public void freeze() {
        if (this.protectionTime > 0 || this.frozenTime > 0) {
            return;
        }
        takeLife();
        this.frozenTime = FreezeGame.FREEZE_TIME;
        addSnowball(-FreezeGame.FREEZE_LOOSE_SNOWBALL);
        addExplosion(-FreezeGame.FREEZE_LOOSE_BOOST);
        super.getHabbo().getRoomUnit().setCanWalk(false);
        updateEffect();
    }

    public void unfreeze() {
        super.getHabbo().getRoomUnit().setCanWalk(true);
        this.frozenTime = 0;
        addProtection();
    }

    public boolean isFrozen() {
        return this.frozenTime > 0;
    }

    public boolean canGetFrozen() {
        return (isFrozen() || isProtected()) ? false : true;
    }

    public void addProtection() {
        updateEffect();
        if (!isProtected() || FreezeGame.POWERUP_STACK) {
            this.protectionTime += FreezeGame.POWER_UP_PROTECT_TIME;
        }
    }

    public boolean isProtected() {
        return this.protectionTime > 0;
    }

    public int getExplosionBoost() {
        if (!this.tempMassiveExplosion) {
            return this.explosionBoost;
        }
        this.tempMassiveExplosion = false;
        return 5;
    }

    public void increaseExplosion() {
        if (this.explosionBoost < 5) {
            this.explosionBoost++;
        }
    }

    public void addExplosion(int i) {
        this.explosionBoost += i;
        if (this.explosionBoost < 0) {
            this.explosionBoost = 0;
        }
        if (this.explosionBoost > 5) {
            this.explosionBoost = 5;
        }
    }

    public void cycle() {
        boolean z = false;
        if (isProtected()) {
            this.protectionTime--;
            if (!isProtected()) {
                z = true;
            }
        }
        if (this.frozenTime > 0) {
            this.frozenTime--;
            if (this.frozenTime <= 0) {
                super.getHabbo().getRoomUnit().setCanWalk(true);
                z = true;
            }
        }
        if (z) {
            updateEffect();
        }
    }

    public int correctEffectId() {
        if (this.dead) {
            return 0;
        }
        if (isFrozen()) {
            return 12;
        }
        int i = 39 + super.getTeamColor().type;
        if (isProtected()) {
            i += 9;
        }
        return i;
    }

    public void updateEffect() {
        if (this.dead) {
            return;
        }
        super.getHabbo().getHabboInfo().getCurrentRoom().giveEffect(super.getHabbo(), correctEffectId(), -1);
    }
}

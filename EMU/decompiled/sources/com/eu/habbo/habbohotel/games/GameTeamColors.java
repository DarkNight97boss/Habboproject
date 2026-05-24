package com.eu.habbo.habbohotel.games;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/games/GameTeamColors.class */
public enum GameTeamColors {
    NONE(0),
    RED(1),
    GREEN(2),
    BLUE(3),
    YELLOW(4),
    ONE(5),
    TWO(6),
    THREE(7),
    FOUR(8),
    FIVE(9),
    SIX(10),
    SEVEN(11),
    EIGHT(12),
    NINE(13),
    TEN(14);

    public final int type;

    GameTeamColors(int i) {
        this.type = i;
    }

    public static GameTeamColors fromType(int i) {
        for (GameTeamColors gameTeamColors : values()) {
            if (gameTeamColors.type == i) {
                return gameTeamColors;
            }
        }
        return NONE;
    }
}

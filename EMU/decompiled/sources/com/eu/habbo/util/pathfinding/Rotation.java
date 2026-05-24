package com.eu.habbo.util.pathfinding;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/util/pathfinding/Rotation.class */
public class Rotation {
    public static int Calculate(int i, int i2, int i3, int i4) {
        int i5 = 0;
        if (i > i3 && i2 > i4) {
            i5 = 7;
        } else if (i < i3 && i2 < i4) {
            i5 = 3;
        } else if (i > i3 && i2 < i4) {
            i5 = 5;
        } else if (i < i3 && i2 > i4) {
            i5 = 1;
        } else if (i > i3) {
            i5 = 6;
        } else if (i < i3) {
            i5 = 2;
        } else if (i2 < i4) {
            i5 = 4;
        } else if (i2 > i4) {
            i5 = 0;
        }
        return i5;
    }
}

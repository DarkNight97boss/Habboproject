package com.eu.habbo.habbohotel.rooms;

import com.eu.habbo.Emulator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/rooms/RoomChatMessageBubbles.class */
public enum RoomChatMessageBubbles {
    NORMAL(0, Emulator.PREVIEW, true, true),
    ALERT(1, Emulator.PREVIEW, true, true),
    BOT(2, Emulator.PREVIEW, true, true),
    RED(3, Emulator.PREVIEW, true, true),
    BLUE(4, Emulator.PREVIEW, true, true),
    YELLOW(5, Emulator.PREVIEW, true, true),
    GREEN(6, Emulator.PREVIEW, true, true),
    BLACK(7, Emulator.PREVIEW, true, true),
    FORTUNE_TELLER(8, Emulator.PREVIEW, false, false),
    ZOMBIE_ARM(9, Emulator.PREVIEW, true, false),
    SKELETON(10, Emulator.PREVIEW, true, false),
    LIGHT_BLUE(11, Emulator.PREVIEW, true, true),
    PINK(12, Emulator.PREVIEW, true, true),
    PURPLE(13, Emulator.PREVIEW, true, true),
    DARK_YEWLLOW(14, Emulator.PREVIEW, true, true),
    DARK_BLUE(15, Emulator.PREVIEW, true, true),
    HEARTS(16, Emulator.PREVIEW, true, true),
    ROSES(17, Emulator.PREVIEW, true, true),
    UNUSED(18, Emulator.PREVIEW, true, true),
    PIG(19, Emulator.PREVIEW, true, true),
    DOG(20, Emulator.PREVIEW, true, true),
    BLAZE_IT(21, Emulator.PREVIEW, true, true),
    DRAGON(22, Emulator.PREVIEW, true, true),
    STAFF(23, Emulator.PREVIEW, false, true),
    BATS(24, Emulator.PREVIEW, true, false),
    MESSENGER(25, Emulator.PREVIEW, true, false),
    STEAMPUNK(26, Emulator.PREVIEW, true, false),
    THUNDER(27, Emulator.PREVIEW, true, true),
    PARROT(28, Emulator.PREVIEW, false, false),
    PIRATE(29, Emulator.PREVIEW, false, false),
    BOT_GUIDE(30, Emulator.PREVIEW, true, true),
    BOT_RENTABLE(31, Emulator.PREVIEW, true, true),
    SCARY_THING(32, Emulator.PREVIEW, true, false),
    FRANK(33, Emulator.PREVIEW, true, false),
    WIRED(34, Emulator.PREVIEW, false, true),
    GOAT(35, Emulator.PREVIEW, true, false),
    SANTA(36, Emulator.PREVIEW, true, false),
    AMBASSADOR(37, "acc_ambassador", false, true),
    RADIO(38, Emulator.PREVIEW, true, false),
    UNKNOWN_39(39, Emulator.PREVIEW, true, false),
    UNKNOWN_40(40, Emulator.PREVIEW, true, false),
    UNKNOWN_41(41, Emulator.PREVIEW, true, false),
    UNKNOWN_42(42, Emulator.PREVIEW, true, false),
    UNKNOWN_43(43, Emulator.PREVIEW, true, false),
    UNKNOWN_44(44, Emulator.PREVIEW, true, false),
    UNKNOWN_45(45, Emulator.PREVIEW, true, false),
    UNKNOWN_46(46, Emulator.PREVIEW, true, false),
    UNKNOWN_47(47, Emulator.PREVIEW, true, false),
    UNKNOWN_48(48, Emulator.PREVIEW, true, false),
    UNKNOWN_49(49, Emulator.PREVIEW, true, false),
    UNKNOWN_50(50, Emulator.PREVIEW, true, false),
    UNKNOWN_51(51, Emulator.PREVIEW, true, false),
    UNKNOWN_52(52, Emulator.PREVIEW, true, false),
    UNKNOWN_53(53, Emulator.PREVIEW, true, false),
    UNKNOWN_54(54, Emulator.PREVIEW, true, false),
    UNKNOWN_55(55, Emulator.PREVIEW, true, false),
    UNKNOWN_56(56, Emulator.PREVIEW, true, false),
    UNKNOWN_57(57, Emulator.PREVIEW, true, false),
    UNKNOWN_58(58, Emulator.PREVIEW, true, false),
    UNKNOWN_59(59, Emulator.PREVIEW, true, false),
    UNKNOWN_60(60, Emulator.PREVIEW, true, false),
    UNKNOWN_61(61, Emulator.PREVIEW, true, false),
    UNKNOWN_62(62, Emulator.PREVIEW, true, false),
    UNKNOWN_63(63, Emulator.PREVIEW, true, false),
    UNKNOWN_64(64, Emulator.PREVIEW, true, false),
    UNKNOWN_65(65, Emulator.PREVIEW, true, false),
    UNKNOWN_66(66, Emulator.PREVIEW, true, false),
    UNKNOWN_67(67, Emulator.PREVIEW, true, false),
    UNKNOWN_68(68, Emulator.PREVIEW, true, false),
    UNKNOWN_69(69, Emulator.PREVIEW, true, false),
    UNKNOWN_70(70, Emulator.PREVIEW, true, false),
    UNKNOWN_71(71, Emulator.PREVIEW, true, false),
    UNKNOWN_72(72, Emulator.PREVIEW, true, false),
    UNKNOWN_73(73, Emulator.PREVIEW, true, false);

    private final int type;
    private final String permission;
    private final boolean overridable;
    private final boolean triggersTalkingFurniture;

    RoomChatMessageBubbles(int i, String str, boolean z, boolean z2) {
        this.type = i;
        this.permission = str;
        this.overridable = z;
        this.triggersTalkingFurniture = z2;
    }

    public static RoomChatMessageBubbles getBubble(int i) {
        try {
            return values()[i];
        } catch (Exception e) {
            return NORMAL;
        }
    }

    public int getType() {
        return this.type;
    }

    public String getPermission() {
        return this.permission;
    }

    public boolean isOverridable() {
        return this.overridable;
    }

    public boolean triggersTalkingFurniture() {
        return this.triggersTalkingFurniture;
    }
}

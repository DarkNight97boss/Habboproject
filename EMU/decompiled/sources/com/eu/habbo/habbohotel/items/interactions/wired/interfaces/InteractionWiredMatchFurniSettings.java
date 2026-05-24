package com.eu.habbo.habbohotel.items.interactions.wired.interfaces;

import com.eu.habbo.habbohotel.wired.WiredMatchFurniSetting;
import gnu.trove.set.hash.THashSet;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/interactions/wired/interfaces/InteractionWiredMatchFurniSettings.class */
public interface InteractionWiredMatchFurniSettings {
    THashSet<WiredMatchFurniSetting> getMatchFurniSettings();

    boolean shouldMatchState();

    boolean shouldMatchRotation();

    boolean shouldMatchPosition();
}

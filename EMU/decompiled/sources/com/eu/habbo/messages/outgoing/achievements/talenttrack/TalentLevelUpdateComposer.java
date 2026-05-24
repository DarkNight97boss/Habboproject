package com.eu.habbo.messages.outgoing.achievements.talenttrack;

import com.eu.habbo.habbohotel.achievements.TalentTrackLevel;
import com.eu.habbo.habbohotel.achievements.TalentTrackType;
import com.eu.habbo.habbohotel.items.Item;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/achievements/talenttrack/TalentLevelUpdateComposer.class */
public class TalentLevelUpdateComposer extends MessageComposer {
    private final TalentTrackType talentTrackType;
    private final TalentTrackLevel talentTrackLevel;

    public TalentLevelUpdateComposer(TalentTrackType talentTrackType, TalentTrackLevel talentTrackLevel) {
        this.talentTrackType = talentTrackType;
        this.talentTrackLevel = talentTrackLevel;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.TalentLevelUpdateComposer);
        this.response.appendString(this.talentTrackType.name());
        this.response.appendInt(Integer.valueOf(this.talentTrackLevel.level));
        if (this.talentTrackLevel.perks != null) {
            this.response.appendInt(Integer.valueOf(this.talentTrackLevel.perks.length));
            for (String str : this.talentTrackLevel.perks) {
                this.response.appendString(str);
            }
        } else {
            this.response.appendInt((Integer) 0);
        }
        this.response.appendInt(Integer.valueOf(this.talentTrackLevel.items.size()));
        TObjectHashIterator it = this.talentTrackLevel.items.iterator();
        while (it.hasNext()) {
            Item item = (Item) it.next();
            this.response.appendString(item.getName());
            this.response.appendInt(Integer.valueOf(item.getSpriteId()));
        }
        return this.response;
    }
}

package com.eu.habbo.messages.outgoing.quests;

import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import java.util.Iterator;
import java.util.List;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/quests/QuestsComposer.class */
public class QuestsComposer extends MessageComposer {
    private final List<Quest> quests;
    private final boolean unknownBoolean;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/quests/QuestsComposer$Quest.class */
    public static class Quest implements ISerialize {
        private final String campaignCode;
        private final int completedQuestsInCampaign;
        private final int questCountInCampaign;
        private final int activityPointType;
        private final int id;
        private final boolean accepted;
        private final String type;
        private final String imageVersion;
        private final int rewardCurrencyAmount;
        private final String localizationCode;
        private final int completedSteps;
        private final int totalSteps;
        private final int sortOrder;
        private final String catalogPageName;
        private final String chainCode;
        private final boolean easy;

        public Quest(String str, int i, int i2, int i3, int i4, boolean z, String str2, String str3, int i5, String str4, int i6, int i7, int i8, String str5, String str6, boolean z2) {
            this.campaignCode = str;
            this.completedQuestsInCampaign = i;
            this.questCountInCampaign = i2;
            this.activityPointType = i3;
            this.id = i4;
            this.accepted = z;
            this.type = str2;
            this.imageVersion = str3;
            this.rewardCurrencyAmount = i5;
            this.localizationCode = str4;
            this.completedSteps = i6;
            this.totalSteps = i7;
            this.sortOrder = i8;
            this.catalogPageName = str5;
            this.chainCode = str6;
            this.easy = z2;
        }

        @Override // com.eu.habbo.messages.ISerialize
        public void serialize(ServerMessage serverMessage) {
            serverMessage.appendString(this.campaignCode);
            serverMessage.appendInt(Integer.valueOf(this.completedQuestsInCampaign));
            serverMessage.appendInt(Integer.valueOf(this.questCountInCampaign));
            serverMessage.appendInt(Integer.valueOf(this.activityPointType));
            serverMessage.appendInt(Integer.valueOf(this.id));
            serverMessage.appendBoolean(Boolean.valueOf(this.accepted));
            serverMessage.appendString(this.type);
            serverMessage.appendString(this.imageVersion);
            serverMessage.appendInt(Integer.valueOf(this.rewardCurrencyAmount));
            serverMessage.appendString(this.localizationCode);
            serverMessage.appendInt(Integer.valueOf(this.completedSteps));
            serverMessage.appendInt(Integer.valueOf(this.totalSteps));
            serverMessage.appendInt(Integer.valueOf(this.sortOrder));
            serverMessage.appendString(this.catalogPageName);
            serverMessage.appendString(this.chainCode);
            serverMessage.appendBoolean(Boolean.valueOf(this.easy));
        }
    }

    public QuestsComposer(List<Quest> list, boolean z) {
        this.quests = list;
        this.unknownBoolean = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.QuestsComposer);
        this.response.appendInt(Integer.valueOf(this.quests.size()));
        Iterator<Quest> it = this.quests.iterator();
        while (it.hasNext()) {
            this.response.append(it.next());
        }
        this.response.appendBoolean(Boolean.valueOf(this.unknownBoolean));
        return this.response;
    }
}

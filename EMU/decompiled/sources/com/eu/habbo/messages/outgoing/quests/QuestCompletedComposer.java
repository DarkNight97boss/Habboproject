package com.eu.habbo.messages.outgoing.quests;

import com.eu.habbo.Emulator;
import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/quests/QuestCompletedComposer.class */
public class QuestCompletedComposer extends MessageComposer {
    private final UnknownClass unknownClass;
    private final boolean unknowbOolean;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/quests/QuestCompletedComposer$UnknownClass.class */
    public static class UnknownClass implements ISerialize {
        private final int activityPointsType;
        private final boolean accepted;
        private final int id;
        private final String type;
        private final int sortOrder;
        private final boolean easy;

        public UnknownClass(int i, boolean z, int i2, String str, int i3, boolean z2) {
            this.activityPointsType = i;
            this.accepted = z;
            this.id = i2;
            this.type = str;
            this.sortOrder = i3;
            this.easy = z2;
        }

        @Override // com.eu.habbo.messages.ISerialize
        public void serialize(ServerMessage serverMessage) {
            serverMessage.appendString(Emulator.PREVIEW);
            serverMessage.appendInt((Integer) 0);
            serverMessage.appendInt((Integer) 0);
            serverMessage.appendInt(Integer.valueOf(this.activityPointsType));
            serverMessage.appendInt(Integer.valueOf(this.id));
            serverMessage.appendBoolean(Boolean.valueOf(this.accepted));
            serverMessage.appendString(this.type);
            serverMessage.appendString(Emulator.PREVIEW);
            serverMessage.appendInt((Integer) 0);
            serverMessage.appendString(Emulator.PREVIEW);
            serverMessage.appendInt((Integer) 0);
            serverMessage.appendInt((Integer) 0);
            serverMessage.appendInt(Integer.valueOf(this.sortOrder));
            serverMessage.appendString(Emulator.PREVIEW);
            serverMessage.appendString(Emulator.PREVIEW);
            serverMessage.appendBoolean(Boolean.valueOf(this.easy));
        }
    }

    public QuestCompletedComposer(UnknownClass unknownClass, boolean z) {
        this.unknownClass = unknownClass;
        this.unknowbOolean = z;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.QuestCompletedComposer);
        return this.response;
    }
}

package com.eu.habbo.messages.outgoing.modtool;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.modtool.CfhCategory;
import com.eu.habbo.habbohotel.modtool.CfhTopic;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.procedure.TObjectProcedure;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/modtool/CfhTopicsMessageComposer.class */
public class CfhTopicsMessageComposer extends MessageComposer {
    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.CfhTopicsMessageComposer);
        this.response.appendInt(Integer.valueOf(Emulator.getGameEnvironment().getModToolManager().getCfhCategories().valueCollection().size()));
        Emulator.getGameEnvironment().getModToolManager().getCfhCategories().forEachValue(new TObjectProcedure<CfhCategory>() { // from class: com.eu.habbo.messages.outgoing.modtool.CfhTopicsMessageComposer.1
            public boolean execute(CfhCategory cfhCategory) {
                CfhTopicsMessageComposer.this.response.appendString(cfhCategory.getName());
                CfhTopicsMessageComposer.this.response.appendInt(Integer.valueOf(cfhCategory.getTopics().valueCollection().size()));
                cfhCategory.getTopics().forEachValue(new TObjectProcedure<CfhTopic>() { // from class: com.eu.habbo.messages.outgoing.modtool.CfhTopicsMessageComposer.1.1
                    public boolean execute(CfhTopic cfhTopic) {
                        CfhTopicsMessageComposer.this.response.appendString(cfhTopic.name);
                        CfhTopicsMessageComposer.this.response.appendInt(Integer.valueOf(cfhTopic.id));
                        CfhTopicsMessageComposer.this.response.appendString(cfhTopic.action.toString());
                        return true;
                    }
                });
                return true;
            }
        });
        return this.response;
    }
}

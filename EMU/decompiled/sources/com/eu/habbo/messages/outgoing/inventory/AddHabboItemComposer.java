package com.eu.habbo.messages.outgoing.inventory;

import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.iterator.hash.TObjectHashIterator;
import gnu.trove.set.hash.THashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/inventory/AddHabboItemComposer.class */
public class AddHabboItemComposer extends MessageComposer {
    private THashSet<HabboItem> itemsList;
    private HabboItem item;
    private int[] ids;
    private AddHabboItemCategory category;
    private Map<AddHabboItemCategory, List<Integer>> entries;

    /* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/inventory/AddHabboItemComposer$AddHabboItemCategory.class */
    public enum AddHabboItemCategory {
        OWNED_FURNI(1),
        RENTED_FURNI(2),
        PET(3),
        BADGE(4),
        BOT(5),
        GAME(6);

        public final int number;

        AddHabboItemCategory(int i) {
            this.number = i;
        }
    }

    public AddHabboItemComposer(THashSet<HabboItem> tHashSet) {
        this.itemsList = tHashSet;
        this.category = AddHabboItemCategory.OWNED_FURNI;
    }

    public AddHabboItemComposer(HabboItem habboItem) {
        this.item = habboItem;
        this.category = AddHabboItemCategory.OWNED_FURNI;
    }

    public AddHabboItemComposer(int[] iArr, AddHabboItemCategory addHabboItemCategory) {
        this.ids = iArr;
        this.category = addHabboItemCategory;
    }

    public AddHabboItemComposer(int i, AddHabboItemCategory addHabboItemCategory) {
        this.ids = new int[]{i};
        this.category = addHabboItemCategory;
    }

    public AddHabboItemComposer(Map<AddHabboItemCategory, List<Integer>> map) {
        this.entries = map;
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.AddHabboItemComposer);
        if (this.ids != null) {
            this.response.appendInt((Integer) 1);
            this.response.appendInt(Integer.valueOf(this.category.number));
            this.response.appendInt(Integer.valueOf(this.ids.length));
            for (int i : this.ids) {
                this.response.appendInt(Integer.valueOf(i));
            }
        } else if (this.entries != null) {
            this.response.appendInt(Integer.valueOf(this.entries.size()));
            for (Map.Entry<AddHabboItemCategory, List<Integer>> entry : this.entries.entrySet()) {
                this.response.appendInt(Integer.valueOf(entry.getKey().number));
                this.response.appendInt(Integer.valueOf(entry.getValue().size()));
                Iterator<Integer> it = entry.getValue().iterator();
                while (it.hasNext()) {
                    this.response.appendInt(Integer.valueOf(it.next().intValue()));
                }
            }
        } else if (this.item == null) {
            this.response.appendInt((Integer) 1);
            this.response.appendInt((Integer) 1);
            this.response.appendInt(Integer.valueOf(this.itemsList.size()));
            TObjectHashIterator it2 = this.itemsList.iterator();
            while (it2.hasNext()) {
                this.response.appendInt(Integer.valueOf(((HabboItem) it2.next()).getId()));
            }
        } else {
            this.response.appendInt((Integer) 1);
            this.response.appendInt((Integer) 1);
            this.response.appendInt((Integer) 1);
            this.response.appendInt(Integer.valueOf(this.item.getId()));
        }
        return this.response;
    }
}

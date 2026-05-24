package com.eu.habbo.messages.outgoing.users;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.catalog.ClothItem;
import com.eu.habbo.habbohotel.users.Habbo;
import com.eu.habbo.messages.ServerMessage;
import com.eu.habbo.messages.outgoing.MessageComposer;
import com.eu.habbo.messages.outgoing.Outgoing;
import gnu.trove.procedure.TIntProcedure;
import java.util.ArrayList;
import java.util.Objects;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/messages/outgoing/users/UserClothesComposer.class */
public class UserClothesComposer extends MessageComposer {
    private final ArrayList<Integer> idList = new ArrayList<>();
    private final ArrayList<String> nameList = new ArrayList<>();

    public UserClothesComposer(Habbo habbo) {
        habbo.getInventory().getWardrobeComponent().getClothing().forEach(new TIntProcedure() { // from class: com.eu.habbo.messages.outgoing.users.UserClothesComposer.1
            public boolean execute(int i) {
                ClothItem clothItem = (ClothItem) Emulator.getGameEnvironment().getCatalogManager().clothing.get(Integer.valueOf(i));
                if (clothItem == null) {
                    return true;
                }
                for (int i2 : clothItem.setId) {
                    UserClothesComposer.this.idList.add(Integer.valueOf(i2));
                }
                UserClothesComposer.this.nameList.add(clothItem.name);
                return true;
            }
        });
    }

    @Override // com.eu.habbo.messages.outgoing.MessageComposer
    protected ServerMessage composeInternal() {
        this.response.init(Outgoing.UserClothesComposer);
        this.response.appendInt(Integer.valueOf(this.idList.size()));
        ArrayList<Integer> arrayList = this.idList;
        ServerMessage serverMessage = this.response;
        Objects.requireNonNull(serverMessage);
        arrayList.forEach(serverMessage::appendInt);
        this.response.appendInt(Integer.valueOf(this.nameList.size()));
        ArrayList<String> arrayList2 = this.nameList;
        ServerMessage serverMessage2 = this.response;
        Objects.requireNonNull(serverMessage2);
        arrayList2.forEach(serverMessage2::appendString);
        return this.response;
    }
}

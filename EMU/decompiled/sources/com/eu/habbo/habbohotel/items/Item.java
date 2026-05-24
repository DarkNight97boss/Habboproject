package com.eu.habbo.habbohotel.items;

import com.eu.habbo.Emulator;
import com.eu.habbo.habbohotel.items.interactions.InteractionMultiHeight;
import com.eu.habbo.habbohotel.users.HabboItem;
import com.eu.habbo.messages.ISerialize;
import com.eu.habbo.messages.ServerMessage;
import gnu.trove.list.array.TIntArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;

/* JADX INFO: loaded from: Habbo-3.5.3.jar:com/eu/habbo/habbohotel/items/Item.class */
public class Item implements ISerialize {
    private int id;
    private int spriteId;
    private String name;
    private String fullName;
    private FurnitureType type;
    private short width;
    private short length;
    private double height;
    private boolean allowStack;
    private boolean allowWalk;
    private boolean allowSit;
    private boolean allowLay;
    private boolean allowRecyle;
    private boolean allowTrade;
    private boolean allowMarketplace;
    private boolean allowGift;
    private boolean allowInventoryStack;
    private short stateCount;
    private short effectM;
    private short effectF;
    private TIntArrayList vendingItems;
    private double[] multiHeights;
    private String customParams;
    private String clothingOnWalk;
    private ItemInteraction interactionType;
    private int rotations;

    public Item(ResultSet resultSet) throws SQLException {
        load(resultSet);
    }

    public static boolean isPet(Item item) {
        return item.getName().toLowerCase().startsWith("a0 pet");
    }

    public static double getCurrentHeight(HabboItem habboItem) {
        if ((habboItem instanceof InteractionMultiHeight) && habboItem.getBaseItem().getMultiHeights().length > 0) {
            if (habboItem.getExtradata().isEmpty()) {
                habboItem.setExtradata("0");
            }
            try {
                return habboItem.getBaseItem().getMultiHeights()[habboItem.getExtradata().isEmpty() ? 0 : Integer.valueOf(habboItem.getExtradata()).intValue() % habboItem.getBaseItem().getMultiHeights().length];
            } catch (NumberFormatException e) {
            }
        }
        return habboItem.getBaseItem().getHeight();
    }

    public void update(ResultSet resultSet) throws SQLException {
        load(resultSet);
    }

    private void load(ResultSet resultSet) throws SQLException {
        this.id = resultSet.getInt("id");
        this.spriteId = resultSet.getInt("sprite_id");
        this.name = resultSet.getString("item_name");
        this.fullName = resultSet.getString("public_name");
        this.type = FurnitureType.fromString(resultSet.getString("type"));
        this.width = resultSet.getShort("width");
        this.length = resultSet.getShort("length");
        this.height = resultSet.getDouble("stack_height");
        if (this.height == 0.0d) {
            this.height = 1.0E-6d;
        }
        this.allowStack = resultSet.getBoolean("allow_stack");
        this.allowWalk = resultSet.getBoolean("allow_walk");
        this.allowSit = resultSet.getBoolean("allow_sit");
        this.allowLay = resultSet.getBoolean("allow_lay");
        this.allowRecyle = resultSet.getBoolean("allow_recycle");
        this.allowTrade = resultSet.getBoolean("allow_trade");
        this.allowMarketplace = resultSet.getBoolean("allow_marketplace_sell");
        this.allowGift = resultSet.getBoolean("allow_gift");
        this.allowInventoryStack = resultSet.getBoolean("allow_inventory_stack");
        this.interactionType = Emulator.getGameEnvironment().getItemManager().getItemInteraction(resultSet.getString("interaction_type").toLowerCase());
        this.stateCount = resultSet.getShort("interaction_modes_count");
        this.effectM = resultSet.getShort("effect_id_male");
        this.effectF = resultSet.getShort("effect_id_female");
        this.customParams = resultSet.getString("customparams");
        this.clothingOnWalk = resultSet.getString("clothing_on_walk");
        if (!resultSet.getString("vending_ids").isEmpty()) {
            this.vendingItems = new TIntArrayList();
            for (String str : resultSet.getString("vending_ids").replace(";", ",").split(",")) {
                this.vendingItems.add(Integer.valueOf(str.replace(" ", Emulator.PREVIEW)).intValue());
            }
        }
        if (resultSet.getString("multiheight").contains(";")) {
            String[] strArrSplit = resultSet.getString("multiheight").split(";");
            this.multiHeights = new double[strArrSplit.length];
            for (int i = 0; i < strArrSplit.length; i++) {
                this.multiHeights[i] = Double.parseDouble(strArrSplit[i]);
            }
        } else {
            this.multiHeights = new double[0];
        }
        this.rotations = 4;
        try {
            this.rotations = resultSet.getInt("rotations");
        } catch (SQLException e) {
        }
    }

    public int getId() {
        return this.id;
    }

    public int getSpriteId() {
        return this.spriteId;
    }

    public String getName() {
        return this.name;
    }

    public String getFullName() {
        return this.fullName;
    }

    public FurnitureType getType() {
        return this.type;
    }

    public int getWidth() {
        return this.width;
    }

    public int getLength() {
        return this.length;
    }

    public double getHeight() {
        return this.height;
    }

    public boolean allowStack() {
        return this.allowStack;
    }

    public boolean allowWalk() {
        return this.allowWalk;
    }

    public boolean allowSit() {
        return this.allowSit;
    }

    public boolean allowLay() {
        return this.allowLay;
    }

    public boolean allowRecyle() {
        return this.allowRecyle;
    }

    public boolean allowTrade() {
        return this.allowTrade;
    }

    public boolean allowMarketplace() {
        return this.allowMarketplace;
    }

    public boolean allowGift() {
        return this.allowGift;
    }

    public boolean allowInventoryStack() {
        return this.allowInventoryStack;
    }

    public int getStateCount() {
        return this.stateCount;
    }

    public int getEffectM() {
        return this.effectM;
    }

    public int getEffectF() {
        return this.effectF;
    }

    public ItemInteraction getInteractionType() {
        return this.interactionType;
    }

    public TIntArrayList getVendingItems() {
        return this.vendingItems;
    }

    public int getRandomVendingItem() {
        return this.vendingItems.get(Emulator.getRandom().nextInt(this.vendingItems.size()));
    }

    public double[] getMultiHeights() {
        return this.multiHeights;
    }

    public String getCustomParams() {
        return this.customParams;
    }

    public String getClothingOnWalk() {
        return this.clothingOnWalk;
    }

    public int getRotations() {
        return this.rotations;
    }

    @Override // com.eu.habbo.messages.ISerialize
    public void serialize(ServerMessage serverMessage) {
        serverMessage.appendString(this.type.code.toLowerCase());
        if (this.type == FurnitureType.BADGE) {
            serverMessage.appendString(this.customParams);
            return;
        }
        serverMessage.appendInt(Integer.valueOf(this.spriteId));
        if (getName().contains("wallpaper_single") || getName().contains("floor_single") || getName().contains("landscape_single")) {
            serverMessage.appendString(this.name.split("_")[2]);
        } else if (this.type == FurnitureType.ROBOT || this.name.equalsIgnoreCase("poster") || this.name.startsWith("SONG ")) {
            serverMessage.appendString(this.customParams);
        } else {
            serverMessage.appendString(Emulator.PREVIEW);
        }
        serverMessage.appendInt((Integer) 1);
        serverMessage.appendBoolean(false);
    }
}

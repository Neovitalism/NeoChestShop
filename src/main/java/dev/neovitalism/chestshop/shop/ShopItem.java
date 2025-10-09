package dev.neovitalism.chestshop.shop;

import me.neovitalism.neoapi.config.Configuration;
import me.neovitalism.neoapi.helpers.ItemHelper;
import me.neovitalism.neoapi.utils.ColorUtil;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class ShopItem {
    private final String itemName;
    private final Item item;
    private final Integer customModelData;
    private final int quantity;
    private final ItemStack displayItem;

    private ShopItem(String itemName, Item item, @Nullable Integer customModelData, int quantity, ItemStack displayItem) {
        this.itemName = itemName;
        this.item = item;
        this.customModelData = customModelData;
        this.quantity = quantity;
        this.displayItem = displayItem;
    }

    public boolean matches(ItemStack item) {
        if (!item.isOf(this.item)) return false;
        CustomModelDataComponent data = item.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        Integer cmd = (data == null) ? null : data.value();
        if (!Objects.equals(this.customModelData, cmd)) return false;
        String itemName = ColorUtil.serialize(item.getName());
        return this.itemName.equals(itemName);
    }

    public String getItemName() {
        return this.itemName;
    }

    private String identifier() {
        return ItemHelper.getIdentifier(this.item).toString();
    }

    public String getMaterialString() {
        String material = this.identifier();
        if (this.customModelData == null) return material;
        return material + ":" + this.customModelData;
    }

    public int quantity() {
        return this.quantity;
    }

    public ItemStack getDisplayItem() {
        return this.displayItem;
    }

    public Configuration toConfig() {
        Configuration config = new Configuration();
        config.set("item-name", this.itemName);
        config.set("identifier", this.identifier());
        config.set("custom-model-data", this.customModelData);
        config.set("quantity", this.quantity);
        config.set("display-item", ItemHelper.toString(this.displayItem));
        return config;
    }

    public static ShopItem fromItem(ItemStack item, int quantity) {
        String itemName = ColorUtil.serialize(item.getName());
        CustomModelDataComponent data = item.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        Integer cmd = (data == null) ? null : data.value();
        ItemStack displayItem = item.copyWithCount(1);
        ItemHelper.setFireResistant(displayItem, true);
        return new ShopItem(itemName, item.getItem(), cmd, quantity, displayItem);
    }

    public static ShopItem fromConfig(Configuration config) {
        String itemName = config.getString("item-name");
        String identifier = config.getString("identifier");
        Item item = Registries.ITEM.get(Identifier.of(identifier));
        int customModelData = config.getInt("custom-model-data", -1);
        int quantity = config.getInt("quantity");
        String displayItemNBT = config.getString("display-item", null);
        ItemStack displayItem;
        if (displayItemNBT != null) {
            displayItem = ItemHelper.fromString(displayItemNBT);
        } else { // Sole reason we need this is because it didn't always exist.
            displayItem = new ItemStack(item);
            if (customModelData != -1) ItemHelper.setCustomModelData(displayItem, customModelData);
            ItemHelper.setFireResistant(displayItem, true);
        }
        return new ShopItem(itemName, item, (customModelData == -1) ? null : customModelData, quantity, displayItem);
    }
}

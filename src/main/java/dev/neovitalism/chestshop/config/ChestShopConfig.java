package dev.neovitalism.chestshop.config;

import me.neovitalism.neoapi.config.Configuration;
import me.neovitalism.neoapi.helpers.ItemHelper;
import me.neovitalism.neoapi.lang.LangManager;
import me.neovitalism.neoapi.objects.Sound;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.CustomModelDataComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.List;

public class ChestShopConfig {
    private static String economyName;
    private static List<String> signDisplay;
    private static boolean shouldShopSignsGlow;
    private static List<String> blacklistedItems;

    private static Sound shopCreatedSound;
    private static Sound shopBrokenSound;
    private static Sound displayChangedSound;
    private static Sound itemPurchasedSound;
    private static Sound itemSoldSound;

    private static LangManager langManager;

    public static void reload(Configuration config) {
        ChestShopConfig.economyName = config.getString("economy-name");
        ChestShopConfig.signDisplay = config.getStringList("sign-display");
        ChestShopConfig.shouldShopSignsGlow = config.getBoolean("should-shop-signs-glow");
        ChestShopConfig.blacklistedItems = config.getStringList("blacklisted-items");

        ChestShopConfig.shopCreatedSound = config.getSound("sounds.shop-created");
        ChestShopConfig.shopBrokenSound = config.getSound("sounds.shop-broken");
        ChestShopConfig.displayChangedSound = config.getSound("sounds.display-changed");
        ChestShopConfig.itemPurchasedSound = config.getSound("sounds.item-purchased");
        ChestShopConfig.itemSoldSound = config.getSound("sounds.item-sold");

        ChestShopConfig.langManager = config.getLangManager("lang", false);
    }

    public static String getEconomyName() {
        return ChestShopConfig.economyName;
    }

    public static List<String> getSignDisplay() {
        return ChestShopConfig.signDisplay;
    }

    public static boolean shouldShopsGlow() {
        return ChestShopConfig.shouldShopSignsGlow;
    }

    public static boolean isItemBlacklisted(ItemStack item) {
        String identifier = ItemHelper.getIdentifier(item.getItem()).toString();
        if (ChestShopConfig.blacklistedItems.contains(identifier)) return true;
        CustomModelDataComponent cmd = item.get(DataComponentTypes.CUSTOM_MODEL_DATA);
        if (cmd == null) return false;
        if (ChestShopConfig.blacklistedItems.contains(identifier + ":*")) return true;
        return ChestShopConfig.blacklistedItems.contains(identifier + ":" + cmd.value());
    }

    public static void sendShopCreatedSound(ServerPlayerEntity player) {
        if (player == null || ChestShopConfig.shopCreatedSound == null) return;
        ChestShopConfig.shopCreatedSound.sendToPlayer(player);
    }

    public static void sendShopBrokenSound(ServerPlayerEntity player) {
        if (player == null || ChestShopConfig.shopBrokenSound == null) return;
        ChestShopConfig.shopBrokenSound.sendToPlayer(player);
    }

    public static void sendDisplayChangedSound(ServerPlayerEntity player) {
        if (player == null || ChestShopConfig.displayChangedSound == null) return;
        ChestShopConfig.displayChangedSound.sendToPlayer(player);
    }

    public static void sendItemPurchasedSound(ServerPlayerEntity player) {
        if (player == null || ChestShopConfig.itemPurchasedSound == null) return;
        ChestShopConfig.itemPurchasedSound.sendToPlayer(player);
    }

    public static void sendItemSoldSound(ServerPlayerEntity player) {
        if (player == null || ChestShopConfig.itemSoldSound == null) return;
        ChestShopConfig.itemSoldSound.sendToPlayer(player);
    }

    public static LangManager getLangManager() {
        return ChestShopConfig.langManager;
    }
}

package dev.neovitalism.chestshop.shop;

import dev.neovitalism.chestshop.NeoChestShop;
import dev.neovitalism.chestshop.api.inventory.InventoryBlockHandler;
import dev.neovitalism.chestshop.api.inventory.InventoryBlockRegistry;
import dev.neovitalism.chestshop.config.ChestShopConfig;
import dev.neovitalism.chestshop.config.display.DisplayOption;
import dev.neovitalism.chestshop.config.display.DisplayRegistry;
import dev.neovitalism.chestshop.utils.DisplayHelper;
import dev.neovitalism.chestshop.utils.InventoryHelper;
import me.neovitalism.neoapi.config.Configuration;
import me.neovitalism.neoapi.lang.LangManager;
import me.neovitalism.neoapi.objects.Location;
import me.neovitalism.neoapi.player.PlayerManager;
import me.neovitalism.neoapi.utils.ColorUtil;
import me.neovitalism.neoapi.utils.StringUtil;
import me.neovitalism.neoapi.utils.UUIDCache;
import net.minecraft.block.entity.SignText;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;

import java.math.BigDecimal;
import java.util.*;

public class ContainerShop {
    private final UUID ownerUUID;
    private final String ownerName;
    private final ShopItem shopItem;

    private final double buyPrice;
    private final double sellPrice;

    private final Location signLocation;
    private final Location containerLocation;
    private final InventoryBlockHandler blockHandler;

    private String displayOption = null;
    private List<UUID> displayEntities = new ArrayList<>();

    public ContainerShop(ServerPlayerEntity owner, ShopItem shopItem, double buyPrice, double sellPrice, Location signLocation, Location containerLocation) {
        this.ownerUUID = owner.getUuid();
        this.ownerName = owner.getName().getString();
        this.shopItem = shopItem;

        this.buyPrice = Math.max(-1, buyPrice);
        this.sellPrice = Math.max(-1, sellPrice);

        this.signLocation = signLocation;
        this.containerLocation = containerLocation;
        this.blockHandler = InventoryBlockRegistry.getHandler(this.containerLocation.getBlockEntity());

        this.nextDisplayOption();
    }

    public ContainerShop(Configuration config) {
        this.ownerUUID = config.getUUID("owner");
        this.ownerName = UUIDCache.getUsernameFromUUID(this.ownerUUID);
        this.shopItem = ShopItem.fromConfig(config.getSection("shop-item"));
        this.buyPrice = config.getDouble("buy-price");
        this.sellPrice = config.getDouble("sell-price");
        this.signLocation = config.getLocation("location");
        this.containerLocation = config.getLocation("container-location");
        this.blockHandler = InventoryBlockRegistry.getHandler(config.getString("block-handler"));
        this.displayOption = config.getString("display-option");
        this.displayEntities = config.getStringList("display-entities").stream().map(UUID::fromString).toList();
    }

    public boolean isOwner(UUID playerUUID) {
        return this.ownerUUID.equals(playerUUID);
    }

    public String getOwnerName() {
        return this.ownerName;
    }

    public Location getSignLocation() {
        return this.signLocation;
    }

    public Location getContainerLocation() {
        return this.containerLocation;
    }

    public void nextDisplayOption() {
        this.deleteEntities();
        Map.Entry<String, DisplayOption> displayOption = DisplayRegistry.getNextDisplayOption(this.displayOption);
        this.displayOption = displayOption.getKey();
        float yaw = DisplayHelper.getSignDirection(this.signLocation.getBlockState()).getOpposite().asRotation();
        Location displayLoc = this.blockHandler.getDisplayLocation(this.containerLocation, yaw);
        this.displayEntities = displayOption.getValue().spawn(displayLoc, this.shopItem);
        ShopRegistry.inst().save(this);
    }

    public void deleteEntities() {
        for (UUID displayEntity : this.displayEntities) {
            Entity entity = this.containerLocation.getWorld().getEntity(displayEntity);
            if (entity != null) entity.discard();
        }
    }

    private Inventory getInventory() {
        return this.blockHandler.getInventory(this.containerLocation);
    }

    public SignText buildText(String description) {
        LangManager lang = ChestShopConfig.getLangManager();
        List<Text> signDisplay = ChestShopConfig.getSignDisplay().stream().map(line -> switch (line.toUpperCase(Locale.ENGLISH)) {
            case "SHOP" -> lang.getLangSafely("parsed-shop-text");
            case "ITEM" -> this.shopItem.getItemName();
            case "QUANTITY" -> StringUtil.replaceReplacements(lang.getLangSafely("quantity-text"),
                    Map.of("{quantity}", String.valueOf(this.shopItem.quantity())));
            case "PRICE" -> {
                Map<String, String> replacements = new HashMap<>();
                replacements.put("{symbol}", NeoChestShop.getEconomy().getSymbol());
                boolean hasBuy = this.buyPrice != -1, hasSell = this.sellPrice != -1;
                String langKey = "";
                if (hasBuy) {
                    replacements.put("{buy-price}", StringUtil.fixTrailingZeros(this.buyPrice));
                    langKey = "buy-text";
                }
                if (hasSell) {
                    replacements.put("{sell-price}", StringUtil.fixTrailingZeros(this.sellPrice));
                    langKey = (hasBuy) ? "buy-sell-text" : "sell-text";
                }
                yield StringUtil.replaceReplacements(lang.getLangSafely(langKey), replacements);
            }
            case "DESCRIPTION" -> description;
            case "ITEM_MATERIAL" -> this.shopItem.getMaterialString();
            case "OWNER" -> this.ownerName;
            default -> line;
        }).map(ColorUtil::parseColourToText).toList();
        Text[] text = signDisplay.toArray(new Text[0]);
        return new SignText(text, text, DyeColor.BLACK, ChestShopConfig.shouldShopsGlow());
    }

    public void purchase(ServerPlayerEntity player) {
        if (NeoChestShop.getEconomy() == null) {
            ChestShopConfig.getLangManager().sendLang(player, "economy-not-loaded", null);
            return;
        }

        if (this.buyPrice == -1) {
            ChestShopConfig.getLangManager().sendLang(player, "cannot-buy", null);
            return;
        }
        if (!NeoChestShop.getEconomy().canAfford(player.getUuid(), BigDecimal.valueOf(this.buyPrice))) {
            ChestShopConfig.getLangManager().sendLang(player, "self-cannot-afford", null);
            return;
        }

        Inventory inventory = this.getInventory();
        int count = InventoryHelper.countInventory(inventory, this.shopItem);
        if (count < this.shopItem.quantity()) {
            ChestShopConfig.getLangManager().sendLang(player, "out-of-stock", null);
            return;
        }

        Inventory playerInventory = player.getInventory();
        if (!InventoryHelper.canFitMatchingItems(playerInventory, inventory, this.shopItem)) {
            ChestShopConfig.getLangManager().sendLang(player, "self-inventory-full", null);
            return;
        }
        List<ItemStack> items = InventoryHelper.getMatchingItems(inventory, this.shopItem, true);
        NeoChestShop.getEconomy().removeBalance(player.getUuid(), BigDecimal.valueOf(this.buyPrice));
        NeoChestShop.getEconomy().addBalance(this.ownerUUID, BigDecimal.valueOf(this.buyPrice));
        InventoryHelper.insert(playerInventory, items);

        Map<String, String> replacements = new HashMap<>();
        replacements.put("{quantity}", String.valueOf(this.shopItem.quantity()));
        replacements.put("{item-name}", this.shopItem.getItemName());
        replacements.put("{owner}", this.ownerName);
        replacements.put("{symbol}", NeoChestShop.getEconomy().getSymbol());
        replacements.put("{cost}", StringUtil.fixTrailingZeros(this.buyPrice));
        replacements.put("{economy}", NeoChestShop.getEconomy().getEconomyName(this.buyPrice));
        ChestShopConfig.getLangManager().sendLang(player, "successfully-purchased", replacements);

        replacements.put("{purchaser}", player.getName().getString());
        this.sendOwnerMessage("player-purchased", replacements);

        ChestShopConfig.sendItemPurchasedSound(player);
        ChestShopConfig.sendItemSoldSound(this.getOwnerEntity());
    }

    public void sell(ServerPlayerEntity player) {
        if (NeoChestShop.getEconomy() == null) {
            ChestShopConfig.getLangManager().sendLang(player, "economy-not-loaded", null);
            return;
        }

        if (this.sellPrice == -1) {
            ChestShopConfig.getLangManager().sendLang(player, "cannot-sell", null);
            return;
        }
        if (!NeoChestShop.getEconomy().canAfford(this.ownerUUID, BigDecimal.valueOf(this.sellPrice))) {
            ChestShopConfig.getLangManager().sendLang(player, "owner-cannot-afford", Map.of("{owner}", this.ownerName));
            return;
        }

        Inventory playerInventory = player.getInventory();
        int count = InventoryHelper.countInventory(playerInventory, this.shopItem);
        if (count < this.shopItem.quantity()) {
            ChestShopConfig.getLangManager().sendLang(player, "not-enough-items", null);
            return;
        }

        Inventory inventory = this.getInventory();
        if (!InventoryHelper.canFitMatchingItems(inventory, playerInventory, this.shopItem)) {
            ChestShopConfig.getLangManager().sendLang(player, "shop-inventory-full", null);
            return;
        }

        List<ItemStack> items = InventoryHelper.getMatchingItems(playerInventory, this.shopItem, true);
        NeoChestShop.getEconomy().removeBalance(this.ownerUUID, BigDecimal.valueOf(this.sellPrice));
        NeoChestShop.getEconomy().addBalance(player.getUuid(), BigDecimal.valueOf(this.sellPrice));
        InventoryHelper.insert(inventory, items);

        Map<String, String> replacements = new HashMap<>();
        replacements.put("{quantity}", String.valueOf(this.shopItem.quantity()));
        replacements.put("{item-name}", this.shopItem.getItemName());
        replacements.put("{owner}", this.ownerName);
        replacements.put("{symbol}", NeoChestShop.getEconomy().getSymbol());
        replacements.put("{cost}", StringUtil.fixTrailingZeros(this.sellPrice));
        replacements.put("{economy}", NeoChestShop.getEconomy().getEconomyName(this.sellPrice));
        ChestShopConfig.getLangManager().sendLang(player, "successfully-sold", replacements);

        replacements.put("{seller}", player.getName().getString());
        this.sendOwnerMessage("player-sold", replacements);

        ChestShopConfig.sendItemPurchasedSound(this.getOwnerEntity());
        ChestShopConfig.sendItemSoldSound(player);
    }

    private ServerPlayerEntity getOwnerEntity() {
        return PlayerManager.getPlayer(this.ownerUUID);
    }

    private void sendOwnerMessage(String langKey, Map<String, String> replacements) {
        ServerPlayerEntity player = this.getOwnerEntity();
        if (player == null) return;
        if (PlayerManager.containsTag(player, "ncs.ignore")) return;
        ChestShopConfig.getLangManager().sendLang(player, langKey, replacements);
    }

    public Configuration toConfig() {
        Configuration config = new Configuration();
        config.set("owner", this.ownerUUID.toString());
        config.set("shop-item", this.shopItem.toConfig());
        config.set("buy-price", this.buyPrice);
        config.set("sell-price", this.sellPrice);
        config.set("location", this.signLocation.toConfiguration());
        config.set("container-location", this.containerLocation.toConfiguration());
        config.set("block-handler", this.blockHandler.getName());
        config.set("display-option", this.displayOption);
        config.set("display-entities", this.displayEntities.stream().map(UUID::toString).toList());
        return config;
    }
}

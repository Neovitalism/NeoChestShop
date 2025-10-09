package dev.neovitalism.chestshop.shop;

import dev.neovitalism.chestshop.config.ChestShopConfig;
import me.neovitalism.neoapi.objects.Location;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

public class ShopBuilder {
    public final Location signLocation;
    public final Location containerLocation;

    public final double buyPrice;
    public final double sellPrice;
    public final int quantity;

    public final String description;

    public ShopBuilder(Location signLocation, Location containerLocation, double buyPrice, double sellPrice, int quantity, String description) {
        this.signLocation = signLocation;
        this.containerLocation = containerLocation;
        this.buyPrice = buyPrice;
        this.sellPrice = sellPrice;
        this.quantity = quantity;
        this.description = description;
    }

    public boolean is(Location location) {
        return this.signLocation.isEqualToCoordinatesOf(location);
    }

    public void buildAndRegister(ServerPlayerEntity player, ItemStack item) {
        ShopItem shopItem = ShopItem.fromItem(item, this.quantity);
        ContainerShop shop = new ContainerShop(player, shopItem, this.buyPrice, this.sellPrice, this.signLocation, this.containerLocation);
        SignBlockEntity sign = (SignBlockEntity) this.signLocation.getBlockEntity();
        SignText text = shop.buildText(this.description);
        sign.setText(text, true);
        sign.setText(text, false);
        sign.setWaxed(true);
        ShopRegistry.removeBuilder(player.getUuid());
        ShopRegistry.inst().add(shop);
        ChestShopConfig.sendShopCreatedSound(player);
        // Could add a particle here at some point.
    }
}

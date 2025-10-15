package dev.neovitalism.chestshop.mixins;

import dev.neovitalism.chestshop.NeoChestShop;
import dev.neovitalism.chestshop.api.inventory.InventoryBlockRegistry;
import dev.neovitalism.chestshop.config.ChestShopConfig;
import dev.neovitalism.chestshop.shop.ShopBuilder;
import dev.neovitalism.chestshop.shop.ShopRegistry;
import dev.neovitalism.chestshop.utils.DisplayHelper;
import me.neovitalism.neoapi.objects.Location;
import me.neovitalism.neoapi.player.PlayerManager;
import me.neovitalism.neoapi.utils.ColorUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mixin(SignBlockEntity.class)
public abstract class SignBlockEntityMixin extends BlockEntity {
    public SignBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Shadow
    public abstract boolean setText(SignText text, boolean front);

    @Redirect(
            method = "changeText",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/block/entity/SignBlockEntity;setText(Lnet/minecraft/block/entity/SignText;Z)Z"
            )
    )
    public boolean neoChestShop$onPlayerTextChange(SignBlockEntity instance, SignText text, boolean front) {
        if (NeoChestShop.getEconomy() == null) return this.setText(text, front);
        UUID editor = instance.getEditor();
        if (editor == null) return this.setText(text, front);
        List<String> lines = Arrays.stream(text.getMessages(false)).map(ColorUtil::serialize).toList();
        ServerPlayerEntity player = PlayerManager.getPlayer(editor);
        if (player == null) return this.setText(text, front);
        Location chestLocation = null;
        float buyPrice = -1, sellPrice = -1; // what owner is selling for, what owner is buying for
        int quantity = -1;
        String description = null;
        for (int i = 0; i < 4; i++) {
            String line = lines.get(i);
            String spaceless = line.replace(" ", "");
            if (i == 0) {
                if (!spaceless.equalsIgnoreCase("[Shop]")) return this.setText(text, front);
                chestLocation = this.findRelatedChest(front);
                if (chestLocation == null) {
                    ChestShopConfig.getLangManager().sendLang(player, "no-container-found", null);
                    return this.setText(text, front);
                }
                if (ShopRegistry.getShopForContainer(chestLocation) != null) {
                    ChestShopConfig.getLangManager().sendLang(player, "shop-already-exists", null);
                    return this.setText(text, front);
                }
            } else if (i == 1) {
                if (NeoChestShop.getEconomy() == null) {
                    ChestShopConfig.getLangManager().sendLang(player, "economy-not-loaded", null);
                    return this.setText(text, front);
                }
                if (spaceless.isEmpty()) {
                    ChestShopConfig.getLangManager().sendLang(player, "price-required", Map.of("{symbol}", NeoChestShop.getEconomy().getSymbol()));
                    return this.setText(text, front);
                }
                String[] split = spaceless.split(":");
                if (split.length > 2) {
                    ChestShopConfig.getLangManager().sendLang(player, "invalid-price-format", Map.of("{symbol}", NeoChestShop.getEconomy().getSymbol()));
                    return this.setText(text, front);
                }
                try {
                    if (split[0].isEmpty()) {
                        buyPrice = -1;
                    } else {
                        buyPrice = Float.parseFloat(split[0]);
                        if (buyPrice <= 0) {
                            ChestShopConfig.getLangManager().sendLang(player, "price-too-low", Map.of("{symbol}", NeoChestShop.getEconomy().getSymbol()));
                            return this.setText(text, front);
                        }
                    }
                    if (split.length == 2) {
                        sellPrice = Float.parseFloat(split[1]);
                        if (sellPrice <= 0) {
                            ChestShopConfig.getLangManager().sendLang(player, "price-too-low", Map.of("{symbol}", NeoChestShop.getEconomy().getSymbol()));
                            return this.setText(text, front);
                        }
                    } else if (buyPrice == -1) {
                        ChestShopConfig.getLangManager().sendLang(player, "invalid-price-format", Map.of("{symbol}", NeoChestShop.getEconomy().getSymbol()));
                        return this.setText(text, front);
                    }
                } catch (NumberFormatException e) {
                    ChestShopConfig.getLangManager().sendLang(player, "invalid-price-format", Map.of("{symbol}", NeoChestShop.getEconomy().getSymbol()));
                    return this.setText(text, front);
                }
            } else if (i == 2) {
                if (spaceless.isEmpty()) {
                    ChestShopConfig.getLangManager().sendLang(player, "quantity-required", Map.of("{symbol}", NeoChestShop.getEconomy().getSymbol()));
                    return this.setText(text, front);
                }
                try {
                    quantity = Integer.parseInt(spaceless);
                    if (quantity <= 0) {
                        ChestShopConfig.getLangManager().sendLang(player, "quantity-too-low", null);
                        return this.setText(text, front);
                    }
                } catch (NumberFormatException e) {
                    ChestShopConfig.getLangManager().sendLang(player, "invalid-quantity", Map.of("{arg}", line));
                    return this.setText(text, front);
                }
            } else description = line;
        }
        ShopBuilder builder = new ShopBuilder(this.getLocation(), chestLocation, buyPrice, sellPrice, quantity, description);
        ShopRegistry.addBuilder(editor, builder);
        ChestShopConfig.getLangManager().sendLang(player, "shop-initialization", null);
        return this.setText(text, front);
    }

    @Inject(
            method = "isWaxed",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    public void neoChestShop$keepShopsWaxed(CallbackInfoReturnable<Boolean> cir) {
        if (ShopRegistry.getShopForSign(this.getLocation()) != null) {
            cir.setReturnValue(true);
            cir.cancel();
        }
    }

    @Unique
    private Location getLocation() {
        return Location.from((ServerWorld) this.world, this.pos);
    }

    @Unique
    private Location findRelatedChest(boolean front) {
        if (this.world == null) return null;
        ServerWorld world = (ServerWorld) this.world;

        Direction facing = DisplayHelper.getSignDirection(this.getCachedState());
        if (!front) facing = facing.getOpposite();

        BlockPos targetPos = this.getPos().offset(facing.getOpposite());
        BlockEntity entity = world.getBlockEntity(targetPos);
        if (InventoryBlockRegistry.isInventoryBlock(entity)) return Location.from(world, targetPos); // Behind the sign.

        targetPos = targetPos.up();
        entity = world.getBlockEntity(targetPos);
        if (InventoryBlockRegistry.isInventoryBlock(entity)) return Location.from(world, targetPos); // Behind the sign and up one.

        targetPos = targetPos.down(3).offset(facing);
        entity = world.getBlockEntity(targetPos);
        if (InventoryBlockRegistry.isInventoryBlock(entity)) return Location.from(world, targetPos); // Two below the sign.

        targetPos = targetPos.offset(facing.getOpposite());
        entity = world.getBlockEntity(targetPos);
        if (InventoryBlockRegistry.isInventoryBlock(entity)) return Location.from(world, targetPos); // Behind the sign and down two.
        return null;
    }
}

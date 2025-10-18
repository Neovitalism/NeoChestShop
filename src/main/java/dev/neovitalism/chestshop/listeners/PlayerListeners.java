package dev.neovitalism.chestshop.listeners;

import dev.neovitalism.chestshop.api.inventory.InventoryBlockHandler;
import dev.neovitalism.chestshop.api.inventory.InventoryBlockRegistry;
import dev.neovitalism.chestshop.config.ChestShopConfig;
import dev.neovitalism.chestshop.shop.ContainerShop;
import dev.neovitalism.chestshop.shop.ShopBuilder;
import dev.neovitalism.chestshop.shop.ShopRegistry;
import me.neovitalism.neoapi.async.NeoAPIExecutorManager;
import me.neovitalism.neoapi.async.NeoExecutor;
import me.neovitalism.neoapi.objects.Location;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.HitResult;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PlayerListeners {
    private static final NeoExecutor ASYNC_EXEC = NeoAPIExecutorManager.createScheduler("NeoChestShop-Updater-Thread", 1);

    public static void init() {
        PlayerListeners.onHitBlock();
        PlayerListeners.onUseBlock();
        PlayerListeners.onBreakBlock();
        PlayerListeners.onDisconnect();
    }

    private static void onHitBlock() {
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (hand == Hand.OFF_HAND) return ActionResult.PASS;
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (!(blockEntity instanceof SignBlockEntity entity)) return ActionResult.PASS;
            Location here = Location.from((ServerWorld) world, pos);
            ContainerShop shop = ShopRegistry.getShopForSign(here);
            if (shop == null || shop.isOwner(player.getUuid()) || AdminMode.has(player.getUuid())) return ActionResult.PASS; // Owners and admins can break shops.
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            shop.sell(serverPlayer);
            PlayerListeners.ASYNC_EXEC.scheduleTaskAsync(() -> {
                // This is because of items that break the sign very quickly, leaving it blank for the viewing player.
                serverPlayer.networkHandler.sendPacket(entity.toUpdatePacket());
            }, 200, TimeUnit.MILLISECONDS);
            return ActionResult.FAIL;
        });
    }

    private static void onUseBlock() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (hand == Hand.OFF_HAND) return ActionResult.PASS;
            if (hitResult == null || hitResult.getType() == HitResult.Type.MISS) return ActionResult.PASS;
            Location here = Location.from((ServerWorld) world, hitResult.getBlockPos());
            BlockEntity entity = here.getBlockEntity();
            if (entity == null) return ActionResult.PASS;
            if (entity instanceof SignBlockEntity) {
                ShopBuilder builder = ShopRegistry.getBuilder(player.getUuid());
                if (builder != null && builder.is(here)) {
                    ItemStack item = player.getStackInHand(hand);
                    if (item.isEmpty()) {
                        ChestShopConfig.getLangManager().sendLang((ServerPlayerEntity) player, "no-item-in-hand", null);
                        return ActionResult.FAIL;
                    }
                    if (ChestShopConfig.isItemBlacklisted(item)) {
                        ChestShopConfig.getLangManager().sendLang((ServerPlayerEntity) player, "item-blacklisted", null);
                        return ActionResult.FAIL;
                    }
                    builder.buildAndRegister((ServerPlayerEntity) player, item);
                    return ActionResult.FAIL; // So the block doesn't get affected.
                }
                ContainerShop shop = ShopRegistry.getShopForSign(here);
                if (shop == null) return ActionResult.PASS;
                if (shop.isOwner(player.getUuid())) {
                    shop.nextDisplayOption();
                    ChestShopConfig.getLangManager().sendLang((ServerPlayerEntity) player, "changed-display", null);
                    ChestShopConfig.sendDisplayChangedSound((ServerPlayerEntity) player);
                } else {
                    shop.purchase((ServerPlayerEntity) player);
                }
                return ActionResult.FAIL; // Consume the event.
            }
            InventoryBlockHandler handler = InventoryBlockRegistry.getHandler(entity);
            if (handler == null) return ActionResult.PASS;
            ContainerShop shop = ShopRegistry.getShopForContainer(here);
            if (shop != null) {
                if (shop.isOwner(player.getUuid()) || AdminMode.has(player.getUuid())) return ActionResult.PASS;
                ChestShopConfig.getLangManager().sendLang((ServerPlayerEntity) player, "not-your-shop", null);
                return ActionResult.FAIL;
            }
            for (Location loc : handler.getOtherInventoryLocations(entity)) {
                shop = ShopRegistry.getShopForContainer(loc);
                if (shop == null) continue;
                if (shop.isOwner(player.getUuid()) || AdminMode.has(player.getUuid())) return ActionResult.PASS;
                ChestShopConfig.getLangManager().sendLang((ServerPlayerEntity) player, "not-your-shop", null);
                return ActionResult.FAIL;
            }
            return ActionResult.PASS;
        });
    }

    private static void onBreakBlock() {
        PlayerBlockBreakEvents.BEFORE.register((world, player, pos, state, blockEntity) -> {
            if (blockEntity == null) return true;
            Location here = Location.from((ServerWorld) world, pos);
            if (blockEntity instanceof SignBlockEntity) {
                ShopBuilder builder = ShopRegistry.getBuilder(player.getUuid());
                if (builder != null && builder.is(here)) {
                    ShopRegistry.removeBuilder(player.getUuid());
                    return true;
                }
                ContainerShop shop = ShopRegistry.getShopForSign(here);
                if (shop == null) return true;
                if (!shop.isOwner(player.getUuid()) && !AdminMode.has(player.getUuid())) {
                    ChestShopConfig.getLangManager().sendLang((ServerPlayerEntity) player, "not-your-shop", null);
                    return false;
                }
                ShopRegistry.inst().remove(here);
                String langKey = (AdminMode.has(player.getUuid())) ? "admin-removed-shop" : "shop-removed";
                ChestShopConfig.getLangManager().sendLang((ServerPlayerEntity) player, langKey, Map.of("{owner}", shop.getOwnerName()));
                ChestShopConfig.sendShopBrokenSound((ServerPlayerEntity) player);
                return true;
            }
            InventoryBlockHandler handler = InventoryBlockRegistry.getHandler(blockEntity);
            if (handler == null) return true;
            ContainerShop shop = ShopRegistry.getShopForContainer(here);
            if (shop != null) {
                if (shop.isOwner(player.getUuid()) || AdminMode.has(player.getUuid())) {
                    ChestShopConfig.getLangManager().sendLang((ServerPlayerEntity) player, "break-sign-first", null);
                } else {
                    ChestShopConfig.getLangManager().sendLang((ServerPlayerEntity) player, "not-your-shop", null);
                }
                return false;
            }
            for (Location loc : handler.getOtherInventoryLocations(blockEntity)) {
                shop = ShopRegistry.getShopForContainer(loc);
                if (shop == null) continue;
                if (!shop.isOwner(player.getUuid())) return false;
            }
            return true;
        });
    }

    private static void onDisconnect() {
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            ShopRegistry.removeBuilder(handler.getPlayer().getUuid());
        });
    }
}

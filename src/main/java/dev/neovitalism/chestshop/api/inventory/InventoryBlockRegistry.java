package dev.neovitalism.chestshop.api.inventory;

import me.neovitalism.neoapi.objects.Location;
import net.minecraft.block.BlockState;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BarrelBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.block.entity.ShulkerBoxBlockEntity;
import net.minecraft.block.enums.ChestType;
import net.minecraft.inventory.Inventory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// We don't want just any containers, or only chests. This should also allow for custom server-sided addons to make their own inventories.
public final class InventoryBlockRegistry {
    private static final Map<String, InventoryBlockHandler> INVENTORY_BLOCK_HANDLERS = new HashMap<>();

    public static void register(InventoryBlockHandler handler) {
        InventoryBlockRegistry.INVENTORY_BLOCK_HANDLERS.put(handler.getName(), handler);
    }

    public static InventoryBlockHandler getHandler(BlockEntity blockEntity) {
        for (InventoryBlockHandler handler : InventoryBlockRegistry.INVENTORY_BLOCK_HANDLERS.values()) {
            if (handler.handles(blockEntity)) return handler;
        }
        return null;
    }

    public static InventoryBlockHandler getHandler(String handlerName) {
        return InventoryBlockRegistry.INVENTORY_BLOCK_HANDLERS.get(handlerName);
    }

    public static boolean isInventoryBlock(BlockEntity blockEntity) {
        return InventoryBlockRegistry.INVENTORY_BLOCK_HANDLERS.values().stream().anyMatch(h -> h.handles(blockEntity));
    }

    static {
        InventoryBlockRegistry.register(new InventoryBlockHandler() {
            @Override
            public String getName() {
                return "chest";
            }

            @Override
            public boolean handles(@Nullable BlockEntity blockEntity) {
                return blockEntity instanceof ChestBlockEntity;
            }

            @Override
            public Inventory getInventory(Location loc) {
                BlockState blockState = loc.getBlockState();
                ChestBlock chest = (ChestBlock) blockState.getBlock();
                return ChestBlock.getInventory(chest, blockState, loc.getWorld(), loc.getBlockPos(), true);
            }

            @Override
            public List<Location> getOtherInventoryLocations(BlockEntity blockEntity) {
                if (!(blockEntity instanceof ChestBlockEntity chest)) return Collections.emptyList();
                BlockState state = chest.getCachedState();
                ChestType type = state.get(ChestBlock.CHEST_TYPE);
                if (type == ChestType.SINGLE) return Collections.emptyList();
                Direction facing = state.get(ChestBlock.FACING);
                facing = (type == ChestType.LEFT) ? facing.rotateYClockwise() : facing.rotateYCounterclockwise();
                BlockPos pos = chest.getPos().offset(facing);
                return List.of(Location.from((ServerWorld) chest.getWorld(), pos));
            }
        });

        InventoryBlockRegistry.register(new InventoryBlockHandler() {
            @Override
            public String getName() {
                return "barrel";
            }

            @Override
            public boolean handles(@Nullable BlockEntity blockEntity) {
                return blockEntity instanceof BarrelBlockEntity;
            }
        });

        InventoryBlockRegistry.register(new InventoryBlockHandler() {
            @Override
            public String getName() {
                return "shulker";
            }

            @Override
            public boolean handles(@Nullable BlockEntity blockEntity) {
                return blockEntity instanceof ShulkerBoxBlockEntity;
            }
        });
    }
}

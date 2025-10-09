package dev.neovitalism.chestshop.hooks;

import com.cobblemon.mod.common.block.entity.GildedChestBlockEntity;
import dev.neovitalism.chestshop.api.inventory.InventoryBlockHandler;
import dev.neovitalism.chestshop.api.inventory.InventoryBlockRegistry;
import net.minecraft.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

public class CobblemonHook {
    public static void registerGildedChest() {
        InventoryBlockRegistry.register(new InventoryBlockHandler() {
            @Override
            public String getName() {
                return "gilded";
            }

            @Override
            public boolean handles(@Nullable BlockEntity blockEntity) {
                return blockEntity instanceof GildedChestBlockEntity;
            }
        });
    }
}

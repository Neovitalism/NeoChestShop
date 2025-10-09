package dev.neovitalism.chestshop.mixins;

import dev.neovitalism.chestshop.api.inventory.InventoryBlockHandler;
import dev.neovitalism.chestshop.api.inventory.InventoryBlockRegistry;
import dev.neovitalism.chestshop.shop.ShopRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.neovitalism.neoapi.objects.Location;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Explosion.class)
public abstract class ExplosionMixin {
    @Shadow
    @Final
    private World world;

    @Shadow
    @Final
    private ObjectArrayList<BlockPos> affectedBlocks;

    @Inject(
            method = "affectWorld",
            at = @At(
                    value = "HEAD"
            )
    )
    public void neoChestShop$protectShops(boolean particles, CallbackInfo ci) {
        this.affectedBlocks.removeIf(blockPos -> {
            Location loc = Location.from((ServerWorld) this.world, blockPos);
            BlockEntity blockEntity = loc.getBlockEntity();
            if (blockEntity == null) return false;
            if (blockEntity instanceof SignBlockEntity && ShopRegistry.getShopForSign(loc) != null) return true;
            InventoryBlockHandler handler = InventoryBlockRegistry.getHandler(blockEntity);
            if (handler == null) return false;
            if (ShopRegistry.getShopForContainer(loc) != null) return true;
            for (Location otherLoc : handler.getOtherInventoryLocations(blockEntity)) {
                if (ShopRegistry.getShopForContainer(otherLoc) != null) return true;
            }
            return false;
        });
    }
}

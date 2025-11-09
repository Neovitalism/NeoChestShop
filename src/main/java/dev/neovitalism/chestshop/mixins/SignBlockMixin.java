package dev.neovitalism.chestshop.mixins;

import dev.neovitalism.chestshop.shop.ShopRegistry;
import me.neovitalism.neoapi.objects.Location;
import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignBlock.class)
public abstract class SignBlockMixin {
    @Inject(
            method = "canPlaceAt",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    public void neoChestShop$preventShopFalling(BlockState state, WorldView world, BlockPos pos, CallbackInfoReturnable<Boolean> cir) {
        if (!(world instanceof ServerWorld)) return; // Prevents a WorldGen crash.
        if (ShopRegistry.getShopForSign(Location.from((ServerWorld) world, pos)) == null) return;
        cir.setReturnValue(true);
        cir.cancel();
    }
}

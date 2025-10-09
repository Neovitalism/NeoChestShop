package dev.neovitalism.chestshop.mixins;

import dev.neovitalism.chestshop.shop.ShopRegistry;
import me.neovitalism.neoapi.objects.Location;
import net.minecraft.block.piston.PistonHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PistonHandler.class)
public abstract class PistonHandlerMixin {
    @Shadow
    @Final
    private World world;

    @Shadow
    @Final
    private BlockPos posTo;

    @Inject(
            method = "calculatePush",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
            ),
            cancellable = true
    )
    public void neoChestShop$blockMovingShops(CallbackInfoReturnable<Boolean> cir) {
        Location location = Location.from((ServerWorld) this.world, this.posTo);
        if (ShopRegistry.getShopForContainer(location) == null) return;
        cir.setReturnValue(false);
        cir.cancel();
    }
}

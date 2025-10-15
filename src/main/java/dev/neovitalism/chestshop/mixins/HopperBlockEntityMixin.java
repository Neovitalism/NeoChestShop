package dev.neovitalism.chestshop.mixins;

import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin {
    @Inject(
            method = "extract(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/entity/ItemEntity;)Z",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    private static void neoChestShop$removeDisplayItems(Inventory inventory, ItemEntity itemEntity, CallbackInfoReturnable<Boolean> cir) {
        if (!itemEntity.getName().getString().equals("NeoChestShop Display Entity")) return;
        cir.setReturnValue(false);
        cir.cancel();
    }
}

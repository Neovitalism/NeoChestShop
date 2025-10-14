package dev.neovitalism.chestshop.mixins;

import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.Ownable;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin extends Entity implements Ownable {
    public ItemEntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Inject(
            method = "tick",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    public void neoChestShop$tick(CallbackInfo ci) {
        if (!this.isDisplayEntity()) return;
        this.setVelocity(0, 0, 0);
        ci.cancel();
    }

    @Override
    public boolean isCollidable() {
        return !this.isDisplayEntity() && super.isCollidable();
    }

    @Inject(
            method = "damage",
            at = @At(
                    value = "HEAD"
            ),
            cancellable = true
    )
    public void neoChestShop$damage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!this.isDisplayEntity()) return;
        cir.setReturnValue(false);
        cir.cancel();
    }

    @Override
    public boolean isPushedByFluids() {
        return !this.isDisplayEntity() && super.isPushedByFluids();
    }

    @Override
    public PistonBehavior getPistonBehavior() {
        return (this.isDisplayEntity()) ? PistonBehavior.IGNORE : super.getPistonBehavior();
    }

    @Unique
    private boolean isDisplayEntity() {
        return this.getName().getString().equals("NeoChestShop Display Entity");
    }
}

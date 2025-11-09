package dev.neovitalism.chestshop.mixins;

import dev.neovitalism.chestshop.shop.ShopRegistry;
import me.neovitalism.neoapi.objects.Location;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.WoodType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldView;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(WallSignBlock.class)
public abstract class WallSignBlockMixin extends AbstractSignBlock {
    protected WallSignBlockMixin(WoodType type, Settings settings) {
        super(type, settings);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (!(world instanceof ServerWorld)) return false; // Prevents a WorldGen crash.
        if (ShopRegistry.getShopForSign(Location.from((ServerWorld) world, pos)) != null) return true;
        return super.canPlaceAt(state, world, pos);
    }
}

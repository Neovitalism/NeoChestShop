package dev.neovitalism.chestshop.utils;

import dev.neovitalism.chestshop.mixins.DisplayEntityAccessor;
import me.neovitalism.neoapi.helpers.ItemHelper;
import me.neovitalism.neoapi.objects.Location;
import me.neovitalism.neoapi.utils.ColorUtil;
import net.minecraft.block.AbstractSignBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import org.joml.Vector3f;

public class DisplayHelper {
    public static ItemEntity toItemEntity(Location loc, ItemStack item) {
        ItemHelper.setFireResistant(item, true);
        ItemEntity itemEntity = new ItemEntity(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), item, 0, 0, 0);
        itemEntity.setPickupDelayInfinite();
        itemEntity.setNeverDespawn();
        itemEntity.setNoGravity(true);
        itemEntity.setCustomName(ColorUtil.parseColourToText("<gradient:#7E50C7:#A76E1F>NeoChestShop</gradient> &#696969Display Entity"));
        return itemEntity;
    }

    public static DisplayEntity.ItemDisplayEntity toDisplayEntity(Location loc, ItemStack item, float scale) {
        DisplayEntity.ItemDisplayEntity itemDisplay = new DisplayEntity.ItemDisplayEntity(EntityType.ITEM_DISPLAY, loc.getWorld());
        itemDisplay.setPosition(loc.getX(), loc.getY(), loc.getZ());
        itemDisplay.setPitch(loc.getPitch());
        itemDisplay.setYaw(loc.getYaw());
        itemDisplay.getStackReference(0).set(item);
        itemDisplay.getDataTracker().set(DisplayEntityAccessor.getSCALE(), new Vector3f(scale, scale, scale));
        itemDisplay.setCustomName(ColorUtil.parseColourToText("<gradient:#7E50C7:#A76E1F>NeoChestShop</gradient> &#696969Display Entity"));
        return itemDisplay;
    }

    public static Direction getSignDirection(BlockState state) {
        AbstractSignBlock sign = (AbstractSignBlock) state.getBlock();
        float rotation = sign.getRotationDegrees(state);
        return Direction.fromRotation(rotation);
    }
}

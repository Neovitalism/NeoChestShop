package dev.neovitalism.chestshop.api.inventory;

import me.neovitalism.neoapi.objects.Location;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.inventory.Inventory;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

public abstract class InventoryBlockHandler {
    public abstract String getName();

    public abstract boolean handles(@Nullable BlockEntity blockEntity);

    public Inventory getInventory(Location location) {
        return (Inventory) location.getBlockEntity();
    }

    public Location getDisplayLocation(Location loc, float yaw) {
        return new Location(loc.getWorld(), loc.getX(), loc.getY(), loc.getZ(), 0.0f, yaw).centered().shifted(0, 1.5, 0);
    }

    public List<Location> getOtherInventoryLocations(BlockEntity blockEntity) {
        return Collections.emptyList();
    }
}

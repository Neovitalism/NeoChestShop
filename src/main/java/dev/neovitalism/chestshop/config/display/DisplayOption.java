package dev.neovitalism.chestshop.config.display;

import dev.neovitalism.chestshop.shop.ShopItem;
import dev.neovitalism.chestshop.utils.DisplayHelper;
import me.neovitalism.neoapi.config.Configuration;
import me.neovitalism.neoapi.helpers.ItemHelper;
import me.neovitalism.neoapi.objects.Location;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;

import java.util.List;
import java.util.UUID;

public class DisplayOption {
    private final List<EntityOption> entityOptions;

    public DisplayOption(Configuration config) {
        this.entityOptions = config.getList("entities", EntityOption::new);
    }

    public List<UUID> spawn(Location location, ShopItem def) {
        return this.entityOptions.stream().map(o -> o.spawn(location, def)).toList();
    }

    private static class EntityOption {
        private final EntityType type;
        private final String identifier;
        private final int customModelData;
        private final float scale;

        private final float transformX;
        private final float transformY;
        private final float transformZ;
        private final float transformPitch;
        private final float transformYaw;

        public EntityOption(Configuration config) {
            this.type = EntityType.getByName(config.getString("type", "DISPLAY"));
            this.identifier = config.getString("identifier", null);
            this.customModelData = config.getInt("custom-model-data", -1);
            this.scale = config.getFloat("scale", 1.0F);

            this.transformX = config.getFloat("transformations.x");
            this.transformY = config.getFloat("transformations.y");
            this.transformZ = config.getFloat("transformations.z");
            this.transformPitch = config.getFloat("transformations.pitch");
            this.transformYaw = config.getFloat("transformations.yaw");
        }

        public UUID spawn(Location loc, ShopItem def) {
            Location location = loc.copy().shifted(this.transformX, this.transformY, this.transformZ, this.transformPitch, this.transformYaw);
            ItemStack item;
            if (this.identifier == null) {
                item = def.getDisplayItem();
            } else {
                item = ItemHelper.createItemStack(this.identifier);
                if (this.customModelData != -1) ItemHelper.setCustomModelData(item, customModelData);
            }
            Entity entity = null;
            if (this.type == EntityType.DISPLAY) {
                entity = DisplayHelper.toDisplayEntity(location, item, this.scale);
            } else if (this.type == EntityType.ITEM) {
                entity = DisplayHelper.toItemEntity(location, item);
            }
            assert entity != null;
            location.getWorld().spawnEntity(entity);
            return entity.getUuid();
        }
    }

    private enum EntityType {
        ITEM,
        DISPLAY;

        private static EntityType getByName(String name) {
            for (EntityType v : EntityType.values()) {
                if (v.name().equalsIgnoreCase(name)) return v;
            }
            return EntityType.DISPLAY;
        }
    }
}

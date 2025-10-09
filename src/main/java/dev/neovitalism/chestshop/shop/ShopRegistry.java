package dev.neovitalism.chestshop.shop;

import dev.neovitalism.chestshop.NeoChestShop;
import me.neovitalism.neoapi.async.NeoAPIExecutorManager;
import me.neovitalism.neoapi.async.NeoExecutor;
import me.neovitalism.neoapi.config.Configuration;
import me.neovitalism.neoapi.objects.Location;
import me.neovitalism.neoapi.objects.LocationMap;
import me.neovitalism.neoapi.storage.AbstractStorage;
import me.neovitalism.neoapi.storage.StorageType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShopRegistry extends AbstractStorage {
    private static ShopRegistry instance = null;

    private static final NeoExecutor ASYNC_EXEC = NeoAPIExecutorManager.createScheduler("NeoChestShop-Saving-Thread", 2);
    private static final String TABLE_KEY = "neo_chest_shop";

    private static final LocationMap<ContainerShop> SHOPS = new LocationMap<>("shop");
    private static final LocationMap<ContainerShop> CONTAINER_LOCATIONS = new LocationMap<>("container_location");
    private static final Map<UUID, ShopBuilder> SHOP_BUILDERS = new HashMap<>();

    public static void init() {
        if (ShopRegistry.instance != null) return;
        new ShopRegistry().load();
    }

    private ShopRegistry() {
        super(NeoChestShop.inst(), true);
        this.setAsyncExec(ShopRegistry.ASYNC_EXEC);
        ShopRegistry.instance = this;
        ShopRegistry.SHOPS.setToConfigFunction(ContainerShop::toConfig);
        ShopRegistry.SHOPS.setFromConfigFunction(ContainerShop::new);
    }

    public static ContainerShop getShopForSign(Location location) {
        return ShopRegistry.SHOPS.get(location);
    }

    public static ContainerShop getShopForContainer(Location location) {
        return ShopRegistry.CONTAINER_LOCATIONS.get(location);
    }

    public static void addBuilder(UUID playerUUID, ShopBuilder builder) {
        ShopRegistry.SHOP_BUILDERS.put(playerUUID, builder);
    }

    public static ShopBuilder getBuilder(UUID playerUUID) {
        return ShopRegistry.SHOP_BUILDERS.get(playerUUID);
    }

    public static void removeBuilder(UUID playerUUID) {
        ShopRegistry.SHOP_BUILDERS.remove(playerUUID);
    }

    protected void add(ContainerShop shop) {
        ShopRegistry.SHOPS.put(shop.getSignLocation(), shop);
        ShopRegistry.CONTAINER_LOCATIONS.put(shop.getContainerLocation(), shop);
        this.save(shop);
    }

    protected void save(ContainerShop shop) {
        if (this.storageType == StorageType.MARIADB) {
            ShopRegistry.ASYNC_EXEC.runTaskAsync(() -> {
                ShopRegistry.SHOPS.addToDatabase(this.databaseConnection, ShopRegistry.TABLE_KEY, shop.getSignLocation(), shop);
            });
        } else this.markToSave();
    }

    public void remove(Location location) {
        ContainerShop shop = ShopRegistry.SHOPS.remove(location);
        ShopRegistry.CONTAINER_LOCATIONS.remove(shop.getContainerLocation());
        shop.deleteEntities();
        if (this.storageType == StorageType.MARIADB) {
            ShopRegistry.ASYNC_EXEC.runTaskAsync(() -> {
                ShopRegistry.SHOPS.removeFromDatabase(this.databaseConnection, ShopRegistry.TABLE_KEY, location);
            });
        } else this.markToSave();
    }

    @Override
    public String getFileName() {
        return "data/shops.yml";
    }

    @Override
    public Map<String, String> getTables() {
        return Map.of(ShopRegistry.TABLE_KEY, ShopRegistry.SHOPS.getTableArguments());
    }

    @Override
    public void load() {
        super.load();
        if (this.storageType == StorageType.MARIADB) {
            ShopRegistry.SHOPS.populateFromDB(this.databaseConnection, ShopRegistry.TABLE_KEY);
        }
        for (ContainerShop value : ShopRegistry.SHOPS.values()) ShopRegistry.CONTAINER_LOCATIONS.put(value.getContainerLocation(), value);
    }

    @Override
    public void load(Configuration config) {
        ShopRegistry.SHOPS.load(config);
    }

    @Override
    public void loadToDB(Configuration config) {
        this.load(config);
        ShopRegistry.SHOPS.sendAllToDB(this.databaseConnection, ShopRegistry.TABLE_KEY);
    }

    @Override
    protected Configuration toConfig() {
        return ShopRegistry.SHOPS.toConfig();
    }

    public static ShopRegistry inst() {
        return ShopRegistry.instance;
    }
}

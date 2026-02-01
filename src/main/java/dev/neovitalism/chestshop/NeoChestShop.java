package dev.neovitalism.chestshop;

import dev.neovitalism.chestshop.api.economy.Economy;
import dev.neovitalism.chestshop.command.NeoChestShopCommand;
import dev.neovitalism.chestshop.config.ChestShopConfig;
import dev.neovitalism.chestshop.config.display.DisplayRegistry;
import dev.neovitalism.chestshop.economy.BEconomy;
import dev.neovitalism.chestshop.economy.ImpactorEconomy;
import dev.neovitalism.chestshop.hooks.CobblemonHook;
import dev.neovitalism.chestshop.listeners.PlayerListeners;
import dev.neovitalism.chestshop.shop.ShopRegistry;
import me.neovitalism.neoapi.modloading.NeoMod;
import me.neovitalism.neoapi.modloading.command.CommandRegistryInfo;
import me.neovitalism.neoapi.utils.ServerUtil;
import me.neovitalism.neoapi.utils.UUIDCache;
import net.fabricmc.loader.api.FabricLoader;

import java.util.function.Function;

public class NeoChestShop extends NeoMod {
    private static NeoChestShop instance;

    private static Function<String, Economy> economyFunction = null;
    private static Economy economy = null;

    @Override
    public String getModID() {
        return "NeoChestShop";
    }

    @Override
    public String getModPrefix() {
        return "&#696969[&#7E50C7N&#8253B8e&#8555A8o&#895899C&#8D5B8Ah&#915E7Be&#94606Bs&#98635Ct&#9C664DS&#A0693Eh&#A36B2Eo&#A76E1Fp&#696969]&f ";
    }

    @Override
    public void onInitialize() {
        super.onInitialize();
        UUIDCache.setCacheToFile(true);
        NeoChestShop.instance = this;
    }

    @Override
    public void onServerStart() {
        this.registerEconomyProvider();
        super.onServerStart();
        PlayerListeners.init();
        if (ServerUtil.isModLoaded("cobblemon")) CobblemonHook.registerGildedChest();
        ShopRegistry.init();
        this.getLogger().info("Loaded!");
    }

    private void registerEconomyProvider() {
        if (NeoChestShop.economy == null) {
            if (FabricLoader.getInstance().isModLoaded("impactor")) {
                this.getLogger().info("Found impactor as an economy provider.");
                NeoChestShop.setEconomyFunction(ImpactorEconomy::new);
            } else if (FabricLoader.getInstance().isModLoaded("beconomy")) {
                this.getLogger().info("Found beconomy as an economy provider.");
                NeoChestShop.setEconomyFunction(BEconomy::new);
            } else {
                this.getLogger().error("No economy provider found! This mod will cease to function. Existing shops will still be protected.");
            }
        } else {
            this.getLogger().info("Using " + NeoChestShop.getEconomy().getClass().getSimpleName() + " as a custom economy provider.");
        }
    }

    @Override
    public void onServerStopping() {
        ShopRegistry.inst().shutdown();
    }

    @Override
    public void configManager() {
        String oldEconomyName = (NeoChestShop.economy == null) ? null : ChestShopConfig.getEconomyName();
        ChestShopConfig.reload(this.getConfig("config.yml", true));
        String economyName = ChestShopConfig.getEconomyName();
        if (!economyName.equals(oldEconomyName)) NeoChestShop.economy = NeoChestShop.economyFunction.apply(economyName);
        DisplayRegistry.reload(this.getConfig("display-types.yml", true));
    }

    @Override
    public void registerCommands(CommandRegistryInfo info) {
        new NeoChestShopCommand(info.getDispatcher());
    }

    public static NeoChestShop inst() {
        return NeoChestShop.instance;
    }

    public static void setEconomyFunction(Function<String, Economy> economyFunction) {
        NeoChestShop.economyFunction = economyFunction;
    }

    public static Economy getEconomy() {
        return NeoChestShop.economy;
    }
}

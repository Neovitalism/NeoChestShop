package dev.neovitalism.chestshop.economy;

import dev.neovitalism.chestshop.api.economy.Economy;
import org.beconomy.api.EconomyAPI;

import java.math.BigDecimal;
import java.util.UUID;

public class BEconomy extends Economy {
    private final EconomyAPI api;
    private final String economyName;
    private final String symbol;

    public BEconomy(String economyName) {
        super(economyName);
        this.api = org.beconomy.api.BEconomy.INSTANCE.getAPI();
        this.economyName = economyName;
        this.symbol = this.api.getCurrencySymbol(this.economyName);
    }

    @Override
    public String getEconomyName() {
        return this.economyName;
    }

    @Override
    public String getPluralEconomyName() {
        return this.economyName;
    }

    @Override
    public String getSymbol() {
        return this.symbol;
    }

    @Override
    public BigDecimal getBalance(UUID playerUUID) {
        return this.api.getBalance(playerUUID, this.economyName);
    }

    @Override
    public void addBalance(UUID playerUUID, BigDecimal amount) {
        this.api.addBalance(playerUUID, amount, this.economyName);
    }

    @Override
    public void removeBalance(UUID playerUUID, BigDecimal amount) {
        this.api.decreaseBalance(playerUUID, this.economyName, amount);
    }
}

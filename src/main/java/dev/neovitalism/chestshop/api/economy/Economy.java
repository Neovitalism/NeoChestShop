package dev.neovitalism.chestshop.api.economy;

import java.math.BigDecimal;
import java.util.UUID;

public abstract class Economy {
    public Economy(String economyName) {}

    public abstract String getEconomyName();
    public abstract String getPluralEconomyName();
    public abstract String getSymbol();

    public String getEconomyName(double cost) {
        return (cost == 1) ? this.getEconomyName() : this.getPluralEconomyName();
    }

    public abstract BigDecimal getBalance(UUID playerUUID);
    public boolean canAfford(UUID playerUUID, BigDecimal amount) {
        return this.getBalance(playerUUID).compareTo(amount) >= 0;
    }
    public abstract void addBalance(UUID playerUUID, BigDecimal amount);
    public void removeBalance(UUID playerUUID, BigDecimal amount) {
        this.addBalance(playerUUID, amount.negate());
    }
}

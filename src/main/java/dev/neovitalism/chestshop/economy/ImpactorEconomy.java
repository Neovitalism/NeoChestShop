package dev.neovitalism.chestshop.economy;

import dev.neovitalism.chestshop.api.economy.Economy;
import me.neovitalism.neoapi.utils.ColorUtil;
import net.impactdev.impactor.api.economy.EconomyService;
import net.impactdev.impactor.api.economy.accounts.Account;
import net.impactdev.impactor.api.economy.currency.Currency;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;

import java.math.BigDecimal;
import java.util.UUID;

public class ImpactorEconomy extends Economy {
    private final Currency currency;

    @SuppressWarnings("PatternValidation")
    public ImpactorEconomy(String economyName) {
        super(economyName);
        this.currency = EconomyService.instance().currencies().currency(Key.key("impactor", economyName)).orElse(null);
    }

    @Override
    public String getEconomyName() {
        return ColorUtil.serialize(this.currency.singular());
    }

    @Override
    public String getPluralEconomyName() {
        return ColorUtil.serialize(this.currency.plural());
    }

    @Override
    public String getSymbol() {
        return ColorUtil.serialize(this.currency.symbol());
    }

    @Override
    public BigDecimal getBalance(UUID playerUUID) {
        Account account = this.getAccount(playerUUID);
        if (account == null) return null;
        return account.balance();
    }

    @Override
    public void addBalance(UUID playerUUID, BigDecimal amount) {
        Account account = this.getAccount(playerUUID);
        if (account == null) return;
        account.deposit(amount);
    }

    @Override
    public void removeBalance(UUID playerUUID, BigDecimal amount) {
        Account account = this.getAccount(playerUUID);
        if (account == null) return;
        account.withdraw(amount);
    }

    private Account getAccount(UUID uuid) {
        return EconomyService.instance().account(this.currency, uuid).join();
    }
}

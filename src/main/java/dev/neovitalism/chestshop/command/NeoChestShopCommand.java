package dev.neovitalism.chestshop.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.neovitalism.chestshop.NeoChestShop;
import dev.neovitalism.chestshop.config.ChestShopConfig;
import dev.neovitalism.chestshop.listeners.AdminMode;
import me.neovitalism.neoapi.modloading.command.ReloadCommand;
import me.neovitalism.neoapi.permissions.NeoPermission;
import me.neovitalism.neoapi.player.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

public class NeoChestShopCommand extends ReloadCommand {
    public NeoChestShopCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        super(NeoChestShop.inst(), dispatcher, "chestshop");
    }

    @Override
    public NeoPermission[] getBasePermissions() {
        return NeoPermission.add(super.getBasePermissions(), "neochestshop.admin", "neochestshop.togglenotifs");
    }

    @Override
    public LiteralArgumentBuilder<ServerCommandSource> getCommand(LiteralArgumentBuilder<ServerCommandSource> command) {
        return super.getCommand(command).then(literal("adminmode")
                .requires(NeoPermission.of("neochestshop.admin")::matches)
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                    String key = (AdminMode.toggleAdminMode(player.getUuid())) ? "admin-mode-on" : "admin-mode-off";
                    ChestShopConfig.getLangManager().sendLang(player, key, null);
                    return Command.SINGLE_SUCCESS;
                })
        ).then(literal("toggle-notifications")
                .requires(NeoPermission.of("neochestshop.togglenotifs")::matches)
                .executes(ctx -> {
                    ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                    boolean hasTag = PlayerManager.toggleTag(player, "ncs.ignore");
                    String key = hasTag ? "now-ignoring-notifs" : "no-longer-ignoring-notifs";
                    ChestShopConfig.getLangManager().sendLang(player, key, null);
                    return Command.SINGLE_SUCCESS;
                })
        );
    }
}

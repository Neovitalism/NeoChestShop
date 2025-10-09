package dev.neovitalism.chestshop.listeners;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AdminMode {
    public static final List<UUID> ADMIN_USERS = new ArrayList<>();

    public static boolean toggleAdminMode(UUID playerUUID) {
        if (AdminMode.ADMIN_USERS.contains(playerUUID)) {
            AdminMode.ADMIN_USERS.remove(playerUUID);
            return false;
        }
        AdminMode.ADMIN_USERS.add(playerUUID);
        return true;
    }

    public static boolean has(UUID playerUUID) {
        return AdminMode.ADMIN_USERS.contains(playerUUID);
    }

}

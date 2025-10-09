package dev.neovitalism.chestshop.config.display;

import me.neovitalism.neoapi.config.Configuration;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

public class DisplayRegistry {
    private static LinkedHashMap<String, DisplayOption> displayOptions;

    public static void reload(Configuration config) {
        DisplayRegistry.displayOptions = config.getOrderedMap("display-types", DisplayOption::new);
    }

    public static Map.Entry<String, DisplayOption> getNextDisplayOption(@Nullable String previous) {
        if (previous == null) return DisplayRegistry.displayOptions.firstEntry();
        boolean matched = false;
        for (Map.Entry<String, DisplayOption> entry : DisplayRegistry.displayOptions.entrySet()) {
            if (entry.getKey().equals(previous)) {
                matched = true;
            } else if (matched) {
                return entry;
            }
        }
        return DisplayRegistry.displayOptions.firstEntry();
    }
}

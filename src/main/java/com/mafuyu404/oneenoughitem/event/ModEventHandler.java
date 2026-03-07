package com.mafuyu404.oneenoughitem.event;

import com.mafuyu404.oelib.core.DataManager;
import com.mafuyu404.oelib.event.DataReloadEvent;
import com.mafuyu404.oelib.event.Events;
import com.mafuyu404.oneenoughitem.Oneenoughitem;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.init.ItemRedirector;
import com.mafuyu404.oneenoughitem.init.ReplacementCache;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;

public class ModEventHandler {

    public static void register() {
        Events.on(DataReloadEvent.EVENT)
                .normal()
                .register(ModEventHandler::onDataReload);
    }

    public static void onDataReload(Class<?> dataClass, int loadedCount, int invalidCount) {
        if (dataClass == Replacements.class) {
            rebuildReplacementCache();
            // 初始化物品重定向器
            ItemRedirector.initialize();
            Oneenoughitem.LOGGER.info("Replacement cache rebuilt due to data reload: {} entries loaded, {} invalid",
                    loadedCount, invalidCount);
        }
    }

    private static void rebuildReplacementCache() {
        DataManager<Replacements> manager = DataManager.get(Replacements.class);
        if (manager != null) {
            ReplacementCache.clearCache();

            MinecraftServer server = manager.getCurrentServer();
            if (server != null) {
                HolderLookup.RegistryLookup<Item> registryLookup = server.registryAccess().lookupOrThrow(Registries.ITEM);

                var replacements = manager.getDataList();
                for (Replacements replacement : replacements) {
                    ReplacementCache.putReplacement(replacement, registryLookup);
                }

                Oneenoughitem.LOGGER.debug("Rebuilt replacement cache with {} rules from OELib data manager",
                        replacements.size());
            } else {
                // Use BuiltInRegistries for client-side fallback - simple direct mapping
                var replacements = manager.getDataList();
                for (Replacements replacement : replacements) {
                    var matchItems = replacement.matchItems();
                    if (matchItems.size() == 1 && !matchItems.get(0).startsWith("#")) {
                        ReplacementCache.putReplacementDirect(matchItems.get(0), replacement.resultItems());
                    } else {
                        Oneenoughitem.LOGGER.warn("Skipping complex replacement on client: {}", replacement);
                    }
                }

                Oneenoughitem.LOGGER.debug("Rebuilt replacement cache with {} rules using direct mapping",
                        replacements.size());
            }
        } else {
            Oneenoughitem.LOGGER.warn("No replacement data manager found in OELib");
        }
    }
}

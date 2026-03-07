package com.mafuyu404.oneenoughitem.event;

import com.mafuyu404.oelib.core.DataManager;
import com.mafuyu404.oelib.event.DataReloadEvent;
import com.mafuyu404.oelib.event.Events;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.init.ItemRedirector;
import com.mafuyu404.oneenoughitem.init.ReplacementCache;
import com.mafuyu404.oneenoughitem.util.OEILog;
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
            OEILog.info("Data reload event received for: {}", dataClass.getSimpleName());
            rebuildReplacementCache();
            // 初始化物品重定向器
            ItemRedirector.initialize();
            OEILog.info("Replacement cache rebuilt: {} entries loaded, {} invalid",
                    loadedCount, invalidCount);
        }
    }

    private static void rebuildReplacementCache() {
        DataManager<Replacements> manager = DataManager.get(Replacements.class);
        if (manager != null) {
            ReplacementCache.clearCache();

            MinecraftServer server = manager.getCurrentServer();
            if (server != null) {
                // 服务端逻辑
                OEILog.info("=== 服务端：开始重建替换缓存 ===");
                HolderLookup.RegistryLookup<Item> registryLookup = server.registryAccess().lookupOrThrow(Registries.ITEM);

                var replacements = manager.getDataList();
                int ruleCount = 0;
                for (Replacements replacement : replacements) {
                    OEILog.info("处理替换规则 #{}: {} -> {}", 
                            ++ruleCount, 
                            replacement.matchItems(), 
                            replacement.resultItems());
                    ReplacementCache.putReplacement(replacement, registryLookup);
                }

                OEILog.info("服务端缓存重建完成，共 {} 条规则", replacements.size());
            } else {
                // 客户端逻辑
                OEILog.info("=== 客户端：使用降级模式重建替换缓存 ===");
                var replacements = manager.getDataList();
                int processedCount = 0;
                int skippedCount = 0;
                
                for (Replacements replacement : replacements) {
                    var matchItems = replacement.matchItems();
                    if (matchItems.size() == 1 && !matchItems.get(0).startsWith("#")) {
                        OEILog.info("客户端添加简单替换 #{}: {} -> {}", 
                                ++processedCount, 
                                matchItems.get(0), 
                                replacement.resultItems());
                        ReplacementCache.putReplacementDirect(matchItems.get(0), replacement.resultItems());
                    } else {
                        OEILog.warn("客户端跳过复杂替换 #{}: {} (原因：多物品或标签)", 
                                ++skippedCount, matchItems);
                    }
                }

                OEILog.info("客户端缓存重建完成，处理 {} 条，跳过 {} 条", processedCount, skippedCount);
            }
        } else {
            OEILog.error("未找到 OELib 的数据管理器");
        }
    }
}

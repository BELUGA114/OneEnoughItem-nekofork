package com.mafuyu404.oneenoughitem.event;

import com.mafuyu404.oelib.core.DataManager;
import com.mafuyu404.oelib.event.DataReloadEvent;
import com.mafuyu404.oelib.event.Events;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.init.ItemRedirector;
import com.mafuyu404.oneenoughitem.init.ReplacementCache;
import com.mafuyu404.oneenoughitem.init.Utils;
import com.mafuyu404.oneenoughitem.util.OEILog;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;

import java.util.List;

public class ModEventHandler {

    public static void register() {
        Events.on(DataReloadEvent.EVENT)
                .normal()
                .register(ModEventHandler::onDataReload);
        
        // 注册服务器关闭事件，用于在世界卸载时清除缓存
        ServerLifecycleEvents.SERVER_STOPPING.register(ModEventHandler::onServerStopping);
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
                // 客户端逻辑 - 终于支持复杂替换了喵
                OEILog.info("=== 客户端：重建替换缓存（支持复杂替换） ===");
                var replacements = manager.getDataList();
                int processedCount = 0;
                int failedCount = 0;
                
                // 使用 BuiltInRegistries 进行简单的 registry lookup
                HolderLookup.RegistryLookup<Item> clientRegistryLookup = BuiltInRegistries.ITEM.asLookup();
                
                for (Replacements replacement : replacements) {
                    try {
                        // 尝试解析所有匹配物品（包括标签和多物品）
                        List<Item> resolvedItems = Utils.resolveItemList(replacement.matchItems(), clientRegistryLookup);
                        
                        if (!resolvedItems.isEmpty()) {
                            // 成功解析，添加到缓存
                            for (Item item : resolvedItems) {
                                String itemId = Utils.getItemRegistryName(item);
                                if (itemId != null) {
                                    ReplacementCache.putReplacementDirect(itemId, replacement.resultItems());
                                    processedCount++;
                                }
                            }
                            OEILog.debug("客户端处理替换：{} -> {} (共 {} 个物品)", 
                                    replacement.matchItems(), replacement.resultItems(), resolvedItems.size());
                        } else {
                            OEILog.warn("客户端无法解析替换规则：{} (物品或标签不存在)", replacement.matchItems());
                            failedCount++;
                        }
                    } catch (Exception e) {
                        OEILog.error(e, "客户端处理替换时出错：" + replacement);
                        failedCount++;
                    }
                }

                OEILog.info("客户端缓存重建完成，成功 {} 条，失败 {} 条", processedCount, failedCount);
            }
            
            // 输出数据来源信息，帮助调试
            OEILog.info("数据来源：已从 DataManager 加载所有 replacements (包含内置资源和 datapacks)");
            OEILog.info("注意：Minecraft 会自动优先加载 datapack 中的数据，后加载的会覆盖先加载的");
        } else {
            OEILog.error("未找到 OELib 的数据管理器");
        }
    }
    
    /**
     * 服务器停止时调用，清除所有替换缓存
     * 这样可以确保切换存档时，旧存档的配置不会影响新存档
     */
   private static void onServerStopping(MinecraftServer server) {
        OEILog.info("服务器正在停止，清除所有替换缓存...");
        ReplacementCache.clearCache();
        ItemRedirector.clear();
        OEILog.info("缓存已清空，准备下次加载");
    }
}

package com.mafuyu404.oneenoughitem.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mafuyu404.oelib.core.DataManager;
import com.mafuyu404.oelib.event.DataReloadEvent;
import com.mafuyu404.oelib.event.Events;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.init.ItemRedirector;
import com.mafuyu404.oneenoughitem.init.ReplacementCache;
import com.mafuyu404.oneenoughitem.init.Utils;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;

public class ModEventHandler {
    private static final Logger LOGGER = LogManager.getLogger("oneenoughitem");

    public static void register() {
        Events.on(DataReloadEvent.EVENT)
                .normal()
                .register(ModEventHandler::onDataReload);
        
        // 注册服务器关闭事件，用于在世界卸载时清除缓存
        ServerLifecycleEvents.SERVER_STOPPING.register(ModEventHandler::onServerStopping);
    }

    public static void onDataReload(Class<?> dataClass, int loadedCount, int invalidCount) {
        if (dataClass == Replacements.class) {
            // 清除旧缓存
            Utils.clearTagCache();
            ReplacementCache.clearCache();
            rebuildReplacementCache();
            // 初始化物品重定向器
            ItemRedirector.initialize();
        }
    }

    private static void rebuildReplacementCache() {
        DataManager<Replacements> manager = DataManager.get(Replacements.class);
        if (manager == null) {
            LOGGER.error("未找到 OELib 的数据管理器");
            return;
        }

        Utils.clearTagCache();
        int count;

        MinecraftServer server = manager.getCurrentServer();
        if (server != null) {
            HolderLookup.RegistryLookup<Item> registryLookup = server.registryAccess().lookupOrThrow(Registries.ITEM);
            count = ReplacementCache.rebuildFromManager(manager, registryLookup);
            LOGGER.debug("服务端缓存重建完成，共 {} 条规则", count);
        } else {
            HolderLookup.RegistryLookup<Item> clientRegistryLookup = BuiltInRegistries.ITEM.asLookup();
            count = ReplacementCache.rebuildFromManager(manager, clientRegistryLookup);
            LOGGER.debug("客户端缓存重建完成（通过 ModEventHandler），共 {} 条规则", count);
        }

        ItemRedirector.initialize();
        LOGGER.debug("重建替换缓存完成，ItemRedirector 已同步");
    }
    
    /**
     * 服务器停止时调用，清除所有替换缓存
     * 这样可以确保切换存档时，旧存档的配置不会影响新存档
     */
   private static void onServerStopping(MinecraftServer server) {
        ReplacementCache.clearCache();
        ItemRedirector.clear();
        Utils.clearTagCache();
    }
}

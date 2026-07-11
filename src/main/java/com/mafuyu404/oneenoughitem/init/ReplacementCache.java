package com.mafuyu404.oneenoughitem.init;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mafuyu404.oneenoughitem.data.Replacements;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.IdentityHashMap;

public class ReplacementCache {
    private static final Logger LOGGER = LogManager.getLogger("oneenoughitem");
    // 字符串 ID 映射缓存（原有）
    private static final Map<String, String> ItemMapCache = new HashMap<>();
    
    // 物品实例直接缓存（新增优化）- 避免重复的字符串解析和 Registry 查找
    private static final Map<Item, Item> ItemInstanceCache = new IdentityHashMap<>();

    public static void putReplacement(Replacements replacement, HolderLookup.RegistryLookup<Item> registryLookup) {
        List<Item> resolvedItems = Utils.resolveItemList(replacement.matchItems(), registryLookup);
        for (Item item : resolvedItems) {
            String id = Utils.getItemRegistryName(item);
            if (id != null) {
                LOGGER.debug("缓存替换规则：{} -> {}", id, replacement.resultItems());
                ItemMapCache.put(id, replacement.resultItems());
                
                // 同时添加到物品实例缓存（优化查找性能）
                Item targetItem = Utils.getItemById(replacement.resultItems());
                if (targetItem != null) {
                    ItemInstanceCache.put(item, targetItem);
                    LOGGER.debug("添加物品实例缓存：{} -> {}", id, replacement.resultItems());
                }
            }
        }
    }

    public static void clearCache() {
        int clearedCount = ItemMapCache.size();
        int instanceCleared = ItemInstanceCache.size();
        LOGGER.debug("清除缓存，共 {} 条替换规则，{} 个物品实例映射", clearedCount, instanceCleared);
        ItemMapCache.clear();
        ItemInstanceCache.clear();
    }

    public static String matchItem(String id) {
        return ItemMapCache.getOrDefault(id, null);
    }
    
    /**
     * 直接从物品实例查找替换（高性能版本）
     * @param originalItem 原始物品实例
     * @return 替换后的物品实例，如果没有替换规则则返回 null
     */
    public static Item matchItemDirect(Item originalItem) {
        if (originalItem == null) {
            return null;
        }
        Item targetItem = ItemInstanceCache.get(originalItem);
        if (targetItem != null) {
            LOGGER.debug("物品实例缓存命中：{} -> {}",
                Utils.getItemRegistryName(originalItem), 
                Utils.getItemRegistryName(targetItem));
        }
        return targetItem;
    }
    
    public static Map<String, String> getCacheContents() {
        return new HashMap<>(ItemMapCache);
    }

    /**
     * 从 DataManager 重建全部缓存
     * 消除 ModEventHandler 和 ModClientEventHandler 中的重复逻辑
     */
    public static int rebuildFromManager(
            com.mafuyu404.oelib.core.DataManager<Replacements> manager,
            net.minecraft.core.HolderLookup.RegistryLookup<net.minecraft.world.item.Item> registryLookup) {
        clearCache();

        var replacements = manager.getDataList();
        for (Replacements replacement : replacements) {
            putReplacement(replacement, registryLookup);
        }

        LOGGER.debug("缓存重建完成，共 {} 条替换规则", replacements.size());
        return replacements.size();
    }
}
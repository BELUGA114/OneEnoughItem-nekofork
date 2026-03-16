package com.mafuyu404.oneenoughitem.init;

import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.util.OEILog;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.IdentityHashMap;

public class ReplacementCache {
    // 字符串 ID 映射缓存（原有）
    private static final Map<String, String> ItemMapCache = new HashMap<>();
    
    // 物品实例直接缓存（新增优化）- 避免重复的字符串解析和 Registry 查找
    private static final Map<Item, Item> ItemInstanceCache = new IdentityHashMap<>();

    public static void putReplacement(Replacements replacement, HolderLookup.RegistryLookup<Item> registryLookup) {
        List<Item> resolvedItems = Utils.resolveItemList(replacement.matchItems(), registryLookup);
        for (Item item : resolvedItems) {
            String id = Utils.getItemRegistryName(item);
            if (id != null) {
                OEILog.debug("缓存替换规则：{} -> {}", id, replacement.resultItems());
                ItemMapCache.put(id, replacement.resultItems());
                
                // 同时添加到物品实例缓存（优化查找性能）
                Item targetItem = Utils.getItemById(replacement.resultItems());
                if (targetItem != null) {
                    ItemInstanceCache.put(item, targetItem);
                    OEILog.debug("添加物品实例缓存：{} -> {}", id, replacement.resultItems());
                }
            }
        }
    }

    public static void putReplacementDirect(String sourceItemId, String targetItemId) {
        if (sourceItemId != null && targetItemId != null) {
            OEILog.debug("直接添加缓存：{} -> {}", sourceItemId, targetItemId);
            ItemMapCache.put(sourceItemId, targetItemId);
            
            // 同时添加到物品实例缓存（优化）
            Item sourceItem = Utils.getItemById(sourceItemId);
            Item targetItem = Utils.getItemById(targetItemId);
            if (sourceItem != null && targetItem != null) {
                ItemInstanceCache.put(sourceItem, targetItem);
            }
        }
    }

    public static void putReplacementsBatch(Map<String, String> mappings) {
        ItemMapCache.putAll(mappings);
    }

    public static void clearCache() {
        int clearedCount = ItemMapCache.size();
        int instanceCleared = ItemInstanceCache.size();
        OEILog.info("清除缓存，共 {} 条替换规则，{} 个物品实例映射", clearedCount, instanceCleared);
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
            OEILog.debug("物品实例缓存命中：{} -> {}",
                Utils.getItemRegistryName(originalItem), 
                Utils.getItemRegistryName(targetItem));
        }
        return targetItem;
    }
    
    /**
     * 获取物品实例缓存内容（用于调试）
     */
    public static Map<Item, Item> getInstanceCacheContents() {
        return new IdentityHashMap<>(ItemInstanceCache);
    }

    public static Map<String, String> getCacheContents() {
        return new HashMap<>(ItemMapCache);
    }
}
package com.mafuyu404.oneenoughitem.init;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.ApiStatus;
import java.util.IdentityHashMap;
import java.util.Map;

/*
 * 物品重定向器
 * 在物品注册阶段建立物品映射，实现最早的替换时机
 * 
 * 工作原理:
 * 1. 游戏启动时，所有物品都会注册到 BuiltInRegistries.ITEM
 * 2. 我们维护一个映射表：原物品 -> 目标物品
 * 3. 在所有获取物品的地方，通过 lookup 方法返回目标物品
 */
public class ItemRedirector {
    private static final Logger LOGGER = LogManager.getLogger("oneenoughitem");
    
    /*
     * 存储物品重定向映射：源物品 -> 目标物品
     * 使用 IdentityHashMap 确保对象级别的比较
     */
    private static final Map<Item, Item> redirectMap = new IdentityHashMap<>();
    
    /*
     * 标记是否已初始化
     */
    private static boolean initialized = false;
    
    /*
     * 初始化物品重定向系统
     * 应该在数据加载完成后调用
     */
    public static void initialize() {
        if (initialized) {
            LOGGER.warn("ItemRedirector already initialized");
            return;
        }

        
        // 从 ReplacementCache 加载映射
        Map<String, String> cacheContents = ReplacementCache.getCacheContents();
        
        int successCount = 0;
        int failCount = 0;
        
        for (Map.Entry<String, String> entry : cacheContents.entrySet()) {
            String sourceId = entry.getKey();
            String targetId = entry.getValue();
            
            ResourceLocation sourceLoc = ResourceLocation.parse(sourceId);
            ResourceLocation targetLoc = ResourceLocation.parse(targetId);
            
            Item sourceItem = BuiltInRegistries.ITEM.get(sourceLoc);
            Item targetItem = BuiltInRegistries.ITEM.get(targetLoc);
            
            if (sourceItem != Items.AIR && targetItem != Items.AIR) {       //过滤掉无效的物品 ID
                redirectMap.put(sourceItem, targetItem);
                LOGGER.debug("物品替换：{} -> {}", sourceId, targetId);
                successCount++;
            } else {
                LOGGER.warn("替换失败：{} 或 {} 未找到（可能是无效的物品 ID）", sourceId, targetId);
                failCount++;
            }
        }
        
        initialized = true;
        LOGGER.debug("Item redirector initialized with {} redirects (成功：{}, 失败：{})",
                redirectMap.size(), successCount, failCount);
    }
    
    /**
     * 查找并返回重定向后的物品
     * 优先使用物品实例缓存（高性能），如果没有则回退到字符串 ID 查找
     * 
     * @param original 原始物品
     * @return 重定向后的物品或原物品
     */
    public static Item lookup(Item original) {
        if (!initialized || original == null) {
            return original;
        }
        
        // 1. 优先检查物品实例缓存（最快）
        Item redirected = ReplacementCache.matchItemDirect(original);
        if (redirected != null) {
            String sourceId = getItemId(original);
            String targetId = getItemId(redirected);
            LOGGER.debug("物品实例缓存命中：{} -> {}", sourceId, targetId);
            return redirected;
        }
        
        // 2. 回退到旧的 redirectMap（兼容性）
        redirected = redirectMap.get(original);
        if (redirected != null) {
            String sourceId = getItemId(original);
            String targetId = getItemId(redirected);
            LOGGER.debug("物品查找触发替换：{} -> {}", sourceId, targetId);
            return redirected;
        }
        
        return original;
    }
    
    /**
     * 直接添加重定向规则（不通过缓存）
     * 
     * @param sourceItem 源物品
     * @param targetItem 目标物品
     */
    public static void addRedirect(Item sourceItem, Item targetItem) {
        if (sourceItem != null && targetItem != null) {
            String sourceId = getItemId(sourceItem);
            String targetId = getItemId(targetItem);
            redirectMap.put(sourceItem, targetItem);
            LOGGER.debug("添加物品替换：{} -> {}", sourceId, targetId);
        }
    }
    
    /**
     * 清除所有重定向规则
     */
    public static void clear() {
        redirectMap.clear();
        initialized = false;
    }
    
    /**
     * 检查是否有重定向规则
     */
    public static boolean hasRedirects() {
        return !redirectMap.isEmpty();
    }
    
    /**
     * 获取物品的注册表 ID
     */
    private static String getItemId(Item item) {
        ResourceLocation loc = BuiltInRegistries.ITEM.getKey(item);
        return loc != null ? loc.toString() : "unknown";
    }
    
    /**
     * 内部方法：获取重定向映射（仅供内部使用）
     */
    @ApiStatus.Internal
    public static Map<Item, Item> getRedirectMap() {
        return redirectMap;
    }
}

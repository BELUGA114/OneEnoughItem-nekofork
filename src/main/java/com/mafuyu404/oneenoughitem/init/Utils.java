package com.mafuyu404.oneenoughitem.init;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class Utils {
    private static final Logger LOGGER = LogManager.getLogger("oneenoughitem");
    // 标签解析缓存 - 避免重复解析相同的标签
    private static final Map<ResourceLocation, Collection<Item>> TAG_CACHE = new HashMap<>();
    public static String getItemRegistryName(Item item) {
        if (item == null) return null;
        ResourceLocation registryName = BuiltInRegistries.ITEM.getKey(item);
        return registryName.toString();
    }

    public static Item getItemById(String registryName) {
        if (registryName == null || registryName.isEmpty()) return null;

        try {
            ResourceLocation resourceLocation = ResourceLocation.parse(registryName);
            if (!BuiltInRegistries.ITEM.containsKey(resourceLocation)) return null;
            return BuiltInRegistries.ITEM.get(resourceLocation);
        } catch (Exception e) {
            LOGGER.warn("getItemById: Failed to resolve item ID '{}'", registryName, e);
            return null;
        }
    }

    public static Collection<Item> getItemsOfTag(ResourceLocation tagId, HolderLookup.RegistryLookup<Item> registryLookup) {
        // 先检查缓存
        Collection<Item> cached = TAG_CACHE.get(tagId);
        if (cached != null) {
            return cached;
        }
        
        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagId);
        Collection<Item> result = new HashSet<>();

        LOGGER.debug("Attempting to resolve tag: {}", tagId);

        var tagOptional = registryLookup.get(tagKey);
        if (tagOptional.isPresent()) {
            var holderSet = tagOptional.get();
            for (var holder : holderSet) {
                result.add(holder.value());
            }
            LOGGER.debug("Tag {} resolved to {} items: {}",
                    tagId, result.size(),
                    result.stream().map(Utils::getItemRegistryName).toList());
            
            // 添加到缓存
            TAG_CACHE.put(tagId, result);
        } else {
            LOGGER.warn("Tag {} not found in registry lookup", tagId);
        }

        return result;
    }

    public static boolean isTagExists(ResourceLocation tagId, HolderLookup.RegistryLookup<Item> registryLookup) {
        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tagId);
        return registryLookup.get(tagKey).isPresent();
    }
    
    /**
     * 清除标签缓存（在数据重载时调用）
     */
    public static void clearTagCache() {
        TAG_CACHE.clear();
    }


    public static List<Item> resolveItemList(List<String> identifiers, HolderLookup.RegistryLookup<Item> registryLookup) {
        List<Item> result = new ArrayList<>();

        for (String id : identifiers) {
            if (id == null || id.isEmpty()) continue;

            if (id.startsWith("#")) {
                ResourceLocation tagId = ResourceLocation.tryParse(id.substring(1));
                if (tagId == null) {
                    LOGGER.warn("Invalid tag ID format: {}", id);
                    continue;
                }

                Collection<Item> tagItems = getItemsOfTag(tagId, registryLookup);
                if (tagItems.isEmpty()) {
                    LOGGER.warn("Tag {} is empty or not found", tagId);
                } else {
                    result.addAll(tagItems);
                }
            } else {
                Item item = getItemById(id);
                if (item != null) {
                    result.add(item);
                } else {
                    LOGGER.warn("Item ID not found: {}", id);
                }
            }
        }

        return result;
    }
}
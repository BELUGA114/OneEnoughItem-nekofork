package com.mafuyu404.oneenoughitem.client.gui.cache;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;

public class EditorCache {
    private static final Logger LOGGER = LogManager.getLogger("oneenoughitem");
    private static final String CACHE_FILENAME = "oneenoughitem_editor_cache.dat";
    
    /**
     * 获取缓存文件路径（按存档隔离）
     */
    private static Path getCacheFilePath() {
        try {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            
            // 如果在单人游戏中，使用存档级别的缓存
            if (mc.getSingleplayerServer() != null) {
                Path worldPath = mc.getSingleplayerServer().getWorldPath(
                    net.minecraft.world.level.storage.LevelResource.ROOT);
                Path cachePath = worldPath.resolve(CACHE_FILENAME);
                LOGGER.debug("Using world-specific cache: {}", cachePath);
                return cachePath;
            }
            
            // 如果在多人游戏中，不使用缓存或使用全局缓存
            if (mc.level != null && mc.getConnection() != null) {
                LOGGER.debug("Multiplayer detected, using global cache");
                return Paths.get("config", "oneenoughitem_" + CACHE_FILENAME);
            }
            
            // 主菜单或其他情况，使用全局缓存
            LOGGER.debug("Using global cache in config directory");
            return Paths.get("config", "oneenoughitem_" + CACHE_FILENAME);
            
        } catch (Exception e) {
            LOGGER.error("Failed to determine cache path, using default", e);
            return Paths.get("config", "oneenoughitem_" + CACHE_FILENAME);
        }
    }

    public record CacheData(Set<String> matchItems, Set<String> matchTags, String resultItem, String resultTag,
                            String fileName) {
    }

    public static void saveCache(Set<Item> matchItems, Set<ResourceLocation> matchTags,
                                 Item resultItem, ResourceLocation resultTag, String fileName) {
        Path cacheFile = getCacheFilePath();
        try {
            Files.createDirectories(cacheFile.getParent());

            try (DataOutputStream dos = new DataOutputStream(
                    new BufferedOutputStream(Files.newOutputStream(cacheFile)))) {

                dos.writeInt(matchItems.size());
                for (Item item : matchItems) {
                    ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                    dos.writeUTF(id.toString());
                }

                dos.writeInt(matchTags.size());
                for (ResourceLocation tag : matchTags) {
                    dos.writeUTF(tag.toString());
                }

                if (resultItem != null) {
                    ResourceLocation id = BuiltInRegistries.ITEM.getKey(resultItem);
                    dos.writeUTF(id.toString());
                } else {
                    dos.writeUTF("");
                }

                dos.writeUTF(resultTag != null ? resultTag.toString() : "");

                dos.writeUTF(fileName != null ? fileName : "");

                dos.flush();
            }

            LOGGER.info("Editor cache saved to: {}", cacheFile);

        } catch (IOException e) {
            LOGGER.error("Failed to save editor cache", e);
        }
    }

    public static CacheData loadCache() {
        Path cacheFile = getCacheFilePath();
        if (!Files.exists(cacheFile)) {
            LOGGER.debug("Cache file not found: {}", cacheFile);
            return null;
        }

        try (DataInputStream dis = new DataInputStream(
                new BufferedInputStream(Files.newInputStream(cacheFile)))) {

            Set<String> matchItems = readStringSet(dis);
            Set<String> matchTags = readStringSet(dis);

            String resultItem = dis.readUTF();
            if (resultItem.isEmpty()) {
                resultItem = null;
            }

            String resultTag = dis.readUTF();
            if (resultTag.isEmpty()) {
                resultTag = null;
            }

            String fileName = dis.readUTF();
            if (fileName.isEmpty()) {
                fileName = null;
            }

            LOGGER.info("Editor cache loaded from: {}", cacheFile);
            return new CacheData(matchItems, matchTags, resultItem, resultTag, fileName);

        } catch (IOException e) {
            LOGGER.error("Failed to load editor cache from: " + cacheFile, e);
            return null;
        }
    }

    private static Set<String> readStringSet(DataInputStream dis) throws IOException {
        Set<String> result = new HashSet<>();
        int count = dis.readInt();
        for (int i = 0; i < count; i++) {
            String str = dis.readUTF();
            if (!str.isEmpty()) {
                result.add(str);
            }
        }
        return result;
    }


    public static void clearCache() {
        Path cacheFile = getCacheFilePath();
        try {
            if (Files.exists(cacheFile)) {
                Files.delete(cacheFile);
                LOGGER.info("Editor cache cleared: {}", cacheFile);
            } else {
                LOGGER.debug("Cache file does not exist, nothing to clear: {}", cacheFile);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to clear editor cache from: " + cacheFile, e);
        }
    }
}
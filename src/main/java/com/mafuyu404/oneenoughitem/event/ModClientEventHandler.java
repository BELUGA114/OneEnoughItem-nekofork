package com.mafuyu404.oneenoughitem.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mafuyu404.oelib.core.DataManager;
import com.mafuyu404.oelib.event.Events;
import com.mafuyu404.oneenoughitem.client.util.ModernFixDetector;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.init.ItemRedirector;
import com.mafuyu404.oneenoughitem.init.ReplacementCache;
import com.mafuyu404.oneenoughitem.init.Utils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;

/**
 * 模组客户端事件处理器
 * 仅在客户端环境加载
 */
@Environment(EnvType.CLIENT)
public class ModClientEventHandler {
    private static final Logger LOGGER = LogManager.getLogger("oneenoughitem");

    public static void register() {
        Events.on(ClientPlayConnectionEvents.JOIN)
                .highest()
                .register(ModClientEventHandler::onPlayerJoin);
        
        // 注册客户端断开连接事件，用于在退出世界时清除缓存
        ClientPlayConnectionEvents.DISCONNECT.register(ModClientEventHandler::onPlayerDisconnect);
    }

    public static void onPlayerJoin(ClientPacketListener handler, PacketSender sender, Minecraft client) {
        if (ModernFixDetector.shouldShowWarning()) {
            ModernFixDetector.markWarningShown();

            LocalPlayer player = client.player;
            if (player == null) return;

            MutableComponent line1 = Component.translatable("oneenoughitem.modernfix.warning.line1")
                    .withStyle(ChatFormatting.AQUA);

            MutableComponent line2Start = Component.translatable("oneenoughitem.modernfix.warning.line2")
                    .withStyle(ChatFormatting.AQUA);

            MutableComponent clickableLink = Component.translatable("oneenoughitem.modernfix.warning.link")
                    .withStyle(ChatFormatting.AQUA, ChatFormatting.UNDERLINE)
                    .withStyle(style -> style
                            .withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, ModernFixDetector.getConfigPath().toString()))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                    Component.translatable("oneenoughitem.modernfix.warning.hover", ModernFixDetector.getConfigPath().toString())
                                            .withStyle(ChatFormatting.GRAY))));

            MutableComponent line2End = Component.translatable("oneenoughitem.modernfix.warning.suffix")
                    .withStyle(ChatFormatting.AQUA);

            MutableComponent line2 = line2Start.append(clickableLink).append(line2End);

            player.sendSystemMessage(line1);
            player.sendSystemMessage(line2);
        }

        // Rebuild replacement cache on client join
        rebuildReplacementCache();
    }

    private static void rebuildReplacementCache() {
        DataManager<Replacements> manager = DataManager.get(Replacements.class);
        if (manager == null) {
            LOGGER.warn("在 OELib 中找不到替代数据管理器");
            return;
        }

        ReplacementCache.clearCache();
        Utils.clearTagCache();

        MinecraftServer server = manager.getCurrentServer();
        if (server != null) {
            HolderLookup.RegistryLookup<Item> registryLookup = server.registryAccess().lookupOrThrow(Registries.ITEM);
            ReplacementCache.rebuildFromManager(manager, registryLookup);
        } else {
            HolderLookup.RegistryLookup<Item> clientRegistryLookup = BuiltInRegistries.ITEM.asLookup();
            ReplacementCache.rebuildFromManager(manager, clientRegistryLookup);
        }

        ItemRedirector.initialize();
        LOGGER.debug("客户端缓存重建完成，ItemRedirector 已同步");
    }
    
    /**
     * 客户端断开连接时调用，清除所有替换缓存
     * 这样可以确保切换存档时，旧存档的配置不会影响新存档
     */
  private static void onPlayerDisconnect(ClientPacketListener handler, Minecraft client) {
        ReplacementCache.clearCache();
        ItemRedirector.clear();
        Utils.clearTagCache();
        LOGGER.info("客户端缓存已清空");
    }
}

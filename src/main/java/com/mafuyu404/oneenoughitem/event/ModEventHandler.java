package com.mafuyu404.oneenoughitem.event;

import com.mafuyu404.oelib.core.DataManager;
import com.mafuyu404.oelib.event.DataReloadEvent;
import com.mafuyu404.oelib.event.Events;
import com.mafuyu404.oneenoughitem.Oneenoughitem;
import com.mafuyu404.oneenoughitem.client.util.ModernFixDetector;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.init.ItemRedirector;
import com.mafuyu404.oneenoughitem.init.ReplacementCache;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;

public class ModEventHandler {

    public static void register() {
        Events.on(DataReloadEvent.EVENT)
                .normal()
                .register(ModEventHandler::onDataReload);

        Events.on(ClientPlayConnectionEvents.JOIN)
                .highest()
                .register(ModEventHandler::onPlayerJoin);
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
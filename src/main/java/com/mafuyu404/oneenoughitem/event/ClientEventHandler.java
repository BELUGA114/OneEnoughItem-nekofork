package com.mafuyu404.oneenoughitem.event;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mafuyu404.oneenoughitem.client.ModKeyMappings;
import com.mafuyu404.oneenoughitem.client.gui.ReplacementEditorScreen;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

/**
 * 客户端事件处理器
 * 仅在客户端环境加载
 */
@Environment(EnvType.CLIENT)
public class ClientEventHandler {
    private static final Logger LOGGER = LogManager.getLogger("oneenoughitem");

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (ModKeyMappings.OPEN_EDITOR.consumeClick()) {
                if (client.screen == null && hasCtrlDown(client)) {
                    if (isSingleplayer()) {
                        LOGGER.debug("Opening GUI in singleplayer mode");
                        client.setScreen(new ReplacementEditorScreen());
                    } else {
                        // 在服务器中禁用 GUI，显示提示消息
                        if (client.player != null) {
                            LOGGER.warn("GUI access blocked in multiplayer mode");
                            client.player.displayClientMessage(
                                Component.translatable(
                                    "message.oneenoughitem.gui_disabled_in_server"
                                ).withStyle(ChatFormatting.RED),
                                true
                            );      //括号危机awa
                        }
                    }
                }
            }
        });
    }

    private static boolean isSingleplayer() {
        Minecraft client = Minecraft.getInstance();
        // 检查是否在单人游戏中
        // getConnection() != null 表示已连接到一个世界（包括单人游戏的内置服务器）
        if (client.getConnection() == null) {
            // 没有连接，肯定不是单人游戏（可能在主菜单）
            return false;
        }
        
        // 检查是否是单人游戏的内置服务器
        // 在单人游戏中，getSingleplayerServer() 会返回非 null 值
        try {
            return client.getSingleplayerServer() != null;
        } catch (Exception e) {
            // getSingleplayerServer() 在非单人环境下可能抛出异常，回退到返回 false
            LOGGER.warn("Failed to check singleplayer status, assuming multiplayer", e);
            return false;
        }
    }

    private static boolean hasCtrlDown(Minecraft client) {
        long window = client.getWindow().getWindow();
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }
}

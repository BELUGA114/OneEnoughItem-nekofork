package com.mafuyu404.oneenoughitem.event;

import com.mafuyu404.oneenoughitem.client.ModKeyMappings;
import com.mafuyu404.oneenoughitem.client.gui.ReplacementEditorScreen;
import com.mafuyu404.oneenoughitem.util.OEILog;
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

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (ModKeyMappings.OPEN_EDITOR.consumeClick()) {
                if (client.screen == null && hasCtrlDown(client)) {
                    if (isSingleplayer()) {
                        OEILog.debug("Opening GUI in singleplayer mode");
                        client.setScreen(new ReplacementEditorScreen());
                    } else {
                        // 在服务器中禁用 GUI，显示提示消息
                        if (client.player != null) {
                            OEILog.warn("GUI access blocked in multiplayer mode");
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
        return client.getConnection() == null;
    }

    private static boolean hasCtrlDown(Minecraft client) {
        long window = client.getWindow().getWindow();
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }
}

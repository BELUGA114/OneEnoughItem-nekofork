package com.mafuyu404.oneenoughitem.event;

import com.mafuyu404.oneenoughitem.client.ModKeyMappings;
import com.mafuyu404.oneenoughitem.client.gui.ReplacementEditorScreen;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ClientEventHandler {

    public static void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (ModKeyMappings.OPEN_EDITOR.consumeClick()) {
                if (client.screen == null && hasCtrlDown(client)) {
                    if (isSingleplayer()) {
                        client.setScreen(new ReplacementEditorScreen());
                    } else {
                        // 在服务器中禁用 GUI，显示提示消息
                        if (client.player != null) {
                            client.player.displayClientMessage(
                                Component.translatable(
                                    "message.oneenoughitem.gui_disabled_in_server"
                                ).withStyle(ChatFormatting.RED),
                                true
                            );
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

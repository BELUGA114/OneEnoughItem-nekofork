package com.mafuyu404.oneenoughitem.client;

import com.mafuyu404.oneenoughitem.util.OEILog;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import org.lwjgl.glfw.GLFW;

/**
 * 模组按键绑定
 * 仅在客户端环境加载
 */
@Environment(EnvType.CLIENT)
public class ModKeyMappings {
    public static final String CATEGORY = "key.categories.oneenoughitem";
    public static KeyMapping OPEN_EDITOR;

    public static void register() {
        OPEN_EDITOR = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.oneenoughitem.open_editor",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                CATEGORY
        ));
    }
}
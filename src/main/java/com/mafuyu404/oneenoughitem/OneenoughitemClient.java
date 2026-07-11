package com.mafuyu404.oneenoughitem;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mafuyu404.oneenoughitem.client.ModKeyMappings;
import com.mafuyu404.oneenoughitem.event.ClientEventHandler;
import com.mafuyu404.oneenoughitem.event.ModClientEventHandler;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/*
 * 客户端初始化入口
 * 仅在客户端环境加载
 */
@Environment(EnvType.CLIENT)
public class OneenoughitemClient implements ClientModInitializer {
    private static final Logger LOGGER = LogManager.getLogger("oneenoughitem");
    @Override
    public void onInitializeClient() {
        
        try {
            ModKeyMappings.register();
            ClientEventHandler.register();
            ModClientEventHandler.register();
            LOGGER.info("OneEnoughItem Client Initialized Successfully");
        } catch (Exception e) {
            LOGGER.error("Critical error during client initialization!", e);
            throw new RuntimeException("Failed to initialize OneEnoughItem Client", e);
        }
    }
}
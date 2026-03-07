package com.mafuyu404.oneenoughitem;

import com.mafuyu404.oneenoughitem.client.ModKeyMappings;
import com.mafuyu404.oneenoughitem.event.ClientEventHandler;
import com.mafuyu404.oneenoughitem.event.ModClientEventHandler;
import com.mafuyu404.oneenoughitem.util.OEILog;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * 客户端初始化入口
 * 仅在客户端环境加载
 */
@Environment(EnvType.CLIENT)
public class OneenoughitemClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        OEILog.info("=== OneEnoughItem Client Initializing ===");
        OEILog.info("Client Environment: {}", OEILog.getEnvSide());
        
        try {
            ModKeyMappings.register();
            OEILog.debug("Key mappings registered");
            
            ClientEventHandler.register();
            OEILog.debug("Client event handler registered");
            
            ModClientEventHandler.register();
            OEILog.debug("Mod client event handler registered");
            
            OEILog.info("=== OneEnoughItem Client Initialized Successfully ===");
        } catch (Exception e) {
            OEILog.error(e, "Critical error during client initialization!");
            throw new RuntimeException("Failed to initialize OneEnoughItem Client", e);
        }
    }
}
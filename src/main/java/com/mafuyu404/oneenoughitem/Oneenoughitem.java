package com.mafuyu404.oneenoughitem;

import com.mafuyu404.oelib.core.DataRegistry;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.event.ModEventHandler;
import com.mafuyu404.oneenoughitem.util.OEILog;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Oneenoughitem implements ModInitializer {
    public static final String MODID = "oneenoughitem";
    public static final Logger LOGGER = LogManager.getLogger();

    @Override
    public void onInitialize() {
        // 初始化日志系统
        OEILog.setDevelopmentMode(); // 开发模式：开启所有日志级别
        OEILog.info("OneEnoughItem NekoFork initializing...");
        OEILog.info("Environment: {}", OEILog.getEnvSide());
        OEILog.info("Log level config: {}", OEILog.getLogLevelConfig());

        /*

        OEILog.setDevelopmentMode();    // 开发模式：开启所有级别
        OEILog.setProductionMode();     // 生产模式：只保留 INFO+
        OEILog.setQuietMode();          // 安静模式：只保留 ERROR
        OEILog.setLevelEnabled("DEBUG", false); // 单独控制某个级别

        */
        try {
            DataRegistry.register(Replacements.class);
            ModEventHandler.register();
            OEILog.info("OneEnoughItem NekoFork initialized successfully");
        } catch (Exception e) {
            OEILog.error(e, "Critical error during initialization!");
            throw new RuntimeException("Failed to initialize OneEnoughItem", e);
        }
    }
}
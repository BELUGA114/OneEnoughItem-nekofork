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
        // ============ 选择日志模式 ============

        //开发模式（最详细 - TRACE + DEBUG + INFO + WARN + ERROR）
        //OEILog.devMode();

        //调试模式（DEBUG + INFO + WARN + ERROR）- 日常开发
        OEILog.debugMode();

        //普通模式（INFO + WARN + ERROR）- 发布
        //OEILog.normalMode();

        //精简模式（WARN + ERROR）- 警告和错误
        //OEILog.minimalMode();

        //生产模式（只记录 ERROR）- 服务器或性能敏感
        //OEILog.productionMode();

        //安静模式（关闭所有日志）- 完全静默
        //OEILog.silentMode();

        OEILog.info("OneEnoughItem NekoFork initializing...");
        OEILog.info("Environment: {}", OEILog.getEnvSide());
        OEILog.info("Log level config: {}", OEILog.getLogLevelConfig());

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

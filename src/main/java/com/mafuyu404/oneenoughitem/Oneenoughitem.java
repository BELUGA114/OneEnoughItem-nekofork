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
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @Override
    public void onInitialize() {
        // ============ 选择日志模式 ============

        //开发模式（DEBUG + INFO + WARN + ERROR）
        //OEILog.devMode();

        //普通模式（INFO + WARN + ERROR）
        OEILog.normalMode();

        //安静模式（WARN + ERROR）
        //OEILog.quietMode();

        //禁用模式（不输出任何日志）
        //OEILog.offMode();

        OEILog.debug("仙人仙人");

        try {
            DataRegistry.register(Replacements.class);
            ModEventHandler.register();
        } catch (Exception e) {
            OEILog.error("Critical error during initialization!", e);
            throw new RuntimeException("Failed to initialize OneEnoughItem", e);
        }
    }
}

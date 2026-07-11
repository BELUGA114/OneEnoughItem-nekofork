package com.mafuyu404.oneenoughitem;

import com.mafuyu404.oelib.core.DataRegistry;
import com.mafuyu404.oneenoughitem.data.Replacements;
import com.mafuyu404.oneenoughitem.event.ModEventHandler;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Oneenoughitem implements ModInitializer {
    public static final String MODID = "oneenoughitem";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    @Override
    public void onInitialize() {
        try {
            DataRegistry.register(Replacements.class);
            ModEventHandler.register();
        } catch (Exception e) {
            LOGGER.error("Critical error during initialization!", e);
            throw new RuntimeException("Failed to initialize OneEnoughItem", e);
        }
    }
}

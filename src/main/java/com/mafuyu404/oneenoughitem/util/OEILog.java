package com.mafuyu404.oneenoughitem.util;

import com.mafuyu404.oneenoughitem.Oneenoughitem;
import org.apache.logging.log4j.Logger;
import java.util.function.Supplier;


public final class OEILog {

    private static final Logger LOGGER = Oneenoughitem.LOGGER;
    private static final String PREFIX = "[OneEnoughItem] ";

    // 日志输出模式
    public enum LogMode {
        DEV,     // 开发模式：DEBUG + INFO + WARN + ERROR
        NORMAL,  // 普通模式：INFO + WARN + ERROR
        QUIET,   // 安静模式：WARN + ERROR
        OFF      // 关闭模式：不输出任何日志
    }

    private static LogMode logMode = LogMode.NORMAL;

    private OEILog() {}

    // ==================== 模式切换 ====================

    public static void devMode() {
        logMode = LogMode.DEV;
    }

    public static void normalMode() {
        logMode = LogMode.NORMAL;
    }

    public static void quietMode() {
        logMode = LogMode.QUIET;
    }

    public static void offMode() {
        logMode = LogMode.OFF;
        // 不输出任何日志
    }

    // ==================== 日志方法 ====================

    public static void debug(String message, Object... params) {
        if (logMode == LogMode.DEV) {
            LOGGER.debug(PREFIX + message, params);
        }
    }

    public static void debug(Supplier<String> supplier) {
        if (logMode == LogMode.DEV) {
            LOGGER.debug(PREFIX + supplier.get());
        }
    }

    public static void info(String message, Object... params) {
        if (logMode == LogMode.DEV || logMode == LogMode.NORMAL) {
            LOGGER.info(PREFIX + message, params);
        }
    }

    public static void warn(String message, Object... params) {
        if (logMode != LogMode.OFF) {
            LOGGER.warn(PREFIX + message, params);
        }
    }

    public static void error(String message, Object... params) {
        if (logMode != LogMode.OFF) {
            LOGGER.error(PREFIX + message, params);
        }
    }

    public static void error(String message, Throwable t) {
        if (logMode != LogMode.OFF) {
            LOGGER.error(PREFIX + message, t);
        }
    }
}
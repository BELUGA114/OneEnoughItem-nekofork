package com.mafuyu404.oneenoughitem.util;

import com.mafuyu404.oneenoughitem.Oneenoughitem;
import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.Logger;

/**
 * 统一的日志工具类
 * 提供多级别日志记录，自动标注客户端/服务端环境
 */
public class OEILog {
    private static final Logger LOGGER = Oneenoughitem.LOGGER;
    
    // 日志级别开关
    private static boolean enableTrace = false;
    private static boolean enableDebug = true;
    private static boolean enableInfo = true;
    private static boolean enableWarn = true;
    private static boolean enableError = true;
    
    /**
     * 获取当前运行环境的标识
     */
    public static String getEnvSide() {
        if (isClientSide()) {
            return "[CLIENT]";
        } else if (isServerSide()) {
            return "[SERVER]";
        } else {
            return "[UNKNOWN]";
        }
    }
    
    /**
     * 判断是否在客户端
     */
    public static boolean isClientSide() {
        try {
            Minecraft.getInstance();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 判断是否在服务端（包括内置服务器）
     */
    public static boolean isServerSide() {
        if (!isClientSide()) {
            // 无法获取客户端实例，是纯服务端
            return true;
        }
        
        try {
            Minecraft mc = Minecraft.getInstance();
            // 检查是否是单人游戏的内置服务器
            return mc.getSingleplayerServer() != null || mc.level != null;
        } catch (Exception e) {
            return true;
        }
    }
    
    /**
     * 获取世界/存档信息
     */
    public static String getWorldInfo() {
        // 客户端环境
        if (isClientSide()) {
            try {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level != null) {
                    // 有世界加载
                    if (mc.getSingleplayerServer() != null) {
                        return "[单人游戏]";
                    } else if (mc.getConnection() != null) {
                        return "[多人游戏]";
                    }
                } else {
                    return "[主菜单]";
                }
            } catch (Exception e) {
                // 忽略异常，继续下面的检查
            }
        }
        
        // 服务端环境（包括内置服务器）
        if (isServerSide()) {
            try {
                Minecraft mc = Minecraft.getInstance();
                if (mc.getSingleplayerServer() != null && mc.level != null) {
                    return "[单人游戏]";
                }
            } catch (Exception e) {
                // 纯服务端环境，没有 Minecraft 实例
                return "[专用服务器]";
            }
        }
        
        return "[无世界]";
    }
    
    /**
     * 格式化日志消息，添加环境和世界信息前缀
     */
    private static String formatMessage(Object message) {
        return getEnvSide() + " " + getWorldInfo() + " " + message;
    }
    
    /**
     * 格式化带参数的日志消息
     */
    private static String formatMessageWithParams(String message, Object... params) {
        // 先在消息前添加环境和世界信息
        String prefix = getEnvSide() + " " + getWorldInfo() + " ";
        // 手动替换 {} 占位符
        StringBuilder result = new StringBuilder(prefix);
        int paramIndex = 0;
        int messageIndex = 0;
        
        while (messageIndex < message.length()) {
            int placeholderIndex = message.indexOf("{}", messageIndex);
            if (placeholderIndex == -1 || paramIndex >= params.length) {
                // 没有更多占位符或参数已用完
                result.append(message.substring(messageIndex));
                break;
            }
            // 添加占位符前的文本
            result.append(message, messageIndex, placeholderIndex);
            // 添加参数
            result.append(params[paramIndex++]);
            messageIndex = placeholderIndex + 2;
        }
        
        return result.toString();
    }
    
    // ==================== TRACE 级别 ====================
    
    public static void trace(Object message) {
        if (enableTrace) {
            LOGGER.trace(formatMessage(message.toString()));
        }
    }
    
    public static void trace(String message, Object... params) {
        if (enableTrace) {
            LOGGER.trace(formatMessageWithParams(message, params));
        }
    }
    
    public static void trace(Throwable t, String message) {
        if (enableTrace) {
            LOGGER.trace(formatMessageWithParams(message), t);
        }
    }
    
    // ==================== DEBUG 级别 ====================
    
    public static void debug(Object message) {
        if (enableDebug) {
            LOGGER.debug(formatMessage(message.toString()));
        }
    }
    
    public static void debug(String message, Object... params) {
        if (enableDebug) {
            LOGGER.debug(formatMessageWithParams(message, params));
        }
    }
    
    public static void debug(Throwable t, String message) {
        if (enableDebug) {
            LOGGER.debug(formatMessageWithParams(message), t);
        }
    }
    
    // ==================== INFO 级别 ====================
    
    public static void info(Object message) {
        if (enableInfo) {
            LOGGER.info(formatMessage(message.toString()));
        }
    }
    
    public static void info(String message, Object... params) {
        if (enableInfo) {
            LOGGER.info(formatMessageWithParams(message, params));
        }
    }
    
    public static void info(Throwable t, String message) {
        if (enableInfo) {
            LOGGER.info(formatMessageWithParams(message), t);
        }
    }
    
    // ==================== WARN 级别 ====================
    
    public static void warn(Object message) {
        if (enableWarn) {
            LOGGER.warn(formatMessage(message.toString()));
        }
    }
    
    public static void warn(String message, Object... params) {
        if (enableWarn) {
            LOGGER.warn(formatMessageWithParams(message, params));
        }
    }
    
    public static void warn(Throwable t, String message) {
        if (enableWarn) {
            LOGGER.warn(formatMessageWithParams(message), t);
        }
    }
    
    // ==================== ERROR 级别 ====================
    
    public static void error(Object message) {
        if (enableError) {
            LOGGER.error(formatMessage(message.toString()));
        }
    }
    
    public static void error(String message, Object... params) {
        if (enableError) {
            LOGGER.error(formatMessageWithParams(message, params));
        }
    }
    
    public static void error(Throwable t, String message) {
        if (enableError) {
            LOGGER.error(formatMessageWithParams(message), t);
        }
    }
    
    // ==================== 日志级别控制 ====================
    
    /**
     * 设置所有日志级别的开关
     */
    public static void setAllLevels(boolean enabled) {
        enableTrace = enabled;
        enableDebug = enabled;
        enableInfo = enabled;
        enableWarn = enabled;
        enableError = enabled;
    }
    
    /**
     * 设置特定日志级别的开关
     */
    public static void setLevelEnabled(String levelName, boolean enabled) {
        switch (levelName.toUpperCase()) {
            case "TRACE" -> enableTrace = enabled;
            case "DEBUG" -> enableDebug = enabled;
            case "INFO" -> enableInfo = enabled;
            case "WARN" -> enableWarn = enabled;
            case "ERROR" -> enableError = enabled;
        }
    }
    
    /**
     * 生产模式：只保留 INFO 及以上级别
     */
    public static void setProductionMode() {
        enableTrace = false;
        enableDebug = false;
        enableInfo = true;
        enableWarn = true;
        enableError = true;
    }
    
    /**
     * 开发模式：开启所有级别
     */
    public static void setDevelopmentMode() {
        enableTrace = true;
        enableDebug = true;
        enableInfo = true;
        enableWarn = true;
        enableError = true;
    }
    
    /**
     * 安静模式：只保留 ERROR
     */
    public static void setQuietMode() {
        enableTrace = false;
        enableDebug = false;
        enableInfo = false;
        enableWarn = false;
        enableError = true;
    }
    
    /**
     * 获取当前日志配置信息
     */
    public static String getLogLevelConfig() {
        return String.format(
            "Log Config [TRACE:%b, DEBUG:%b, INFO:%b, WARN:%b, ERROR:%b]",
            enableTrace, enableDebug, enableInfo, enableWarn, enableError
        );
    }
    
    /**
     * 检查 TRACE 级别是否启用
     */
    public static boolean isTraceEnabled() {
        return enableTrace;
    }
    
    /**
     * 检查 DEBUG 级别是否启用
     */
    public static boolean isDebugEnabled() {
        return enableDebug;
    }
    
    /**
     * 检查 INFO 级别是否启用
     */
    public static boolean isInfoEnabled() {
        return enableInfo;
    }
    
    /**
     * 检查 WARN 级别是否启用
     */
    public static boolean isWarnEnabled() {
        return enableWarn;
    }
    
    /**
     * 检查 ERROR 级别是否启用
     */
    public static boolean isErrorEnabled() {
        return enableError;
    }
}

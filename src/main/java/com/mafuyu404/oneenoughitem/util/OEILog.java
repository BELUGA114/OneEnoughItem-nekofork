package com.mafuyu404.oneenoughitem.util;

import com.mafuyu404.oneenoughitem.Oneenoughitem;
import org.apache.logging.log4j.Logger;

/**
 * 统一的日志工具类
 * 提供多级别日志记录，自动标注客户端/服务端环境
 */
public class OEILog {
    private static final Logger LOGGER = Oneenoughitem.LOGGER;
    
    // 日志级别开关
    private static boolean enableTrace = false;     //Trace太多了，先关着
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
     * 使用反射避免服务端加载客户端类
     */
    public static boolean isClientSide() {
        try {
            // 使用反射延迟加载 Minecraft 类，避免服务端崩溃
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
            java.lang.reflect.Method getInstanceMethod = minecraftClass.getMethod("getInstance");
            getInstanceMethod.invoke(null);
            return true;
        } catch (ClassNotFoundException e) {
            // 找不到 Minecraft 类，说明在服务端
            return false;
        } catch (Exception e) {
            // 其他异常（如调用失败），也认为在服务端
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
            // 只有在确认是客户端后才使用 Minecraft 类
            Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
            java.lang.reflect.Method getInstanceMethod = minecraftClass.getMethod("getInstance");
            Object mc = getInstanceMethod.invoke(null);
            
            if (mc == null) {
                return true;
            }
            
            // 检查是否是单人游戏的内置服务器
            java.lang.reflect.Method getSingleplayerServerMethod = minecraftClass.getMethod("getSingleplayerServer");
            Object server = getSingleplayerServerMethod.invoke(mc);
            
            java.lang.reflect.Method getLevelMethod = minecraftClass.getMethod("getLevel");
            Object level = getLevelMethod.invoke(mc);
            
            return server != null || level != null;
        } catch (Exception e) {
            // 反射调用失败，默认认为是服务端
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
                Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
                java.lang.reflect.Method getInstanceMethod = minecraftClass.getMethod("getInstance");
                Object mc = getInstanceMethod.invoke(null);
                
                if (mc == null) {
                    return "[无世界]";
                }
                
                java.lang.reflect.Method getLevelMethod = minecraftClass.getMethod("getLevel");
                Object level = getLevelMethod.invoke(mc);
                
                if (level != null) {
                    // 有世界加载
                    java.lang.reflect.Method getSingleplayerServerMethod = minecraftClass.getMethod("getSingleplayerServer");
                    Object singleplayerServer = getSingleplayerServerMethod.invoke(mc);
                    
                    if (singleplayerServer != null) {
                        return "[单人游戏]";
                    }
                    
                    java.lang.reflect.Method getConnectionMethod = minecraftClass.getMethod("getConnection");
                    Object connection = getConnectionMethod.invoke(mc);
                    
                    if (connection != null) {
                        return "[多人游戏]";
                    }
                }
                
                return "[主菜单]";
            } catch (Exception e) {
                // 忽略异常，继续下面的检查
            }
        }
        
        // 服务端环境（包括内置服务器）
        if (isServerSide()) {
            try {
                // 尝试检查是否是单人游戏的内置服务器
                Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
                java.lang.reflect.Method getInstanceMethod = minecraftClass.getMethod("getInstance");
                Object mc = getInstanceMethod.invoke(null);
                
                if (mc != null) {
                    java.lang.reflect.Method getSingleplayerServerMethod = minecraftClass.getMethod("getSingleplayerServer");
                    Object singleplayerServer = getSingleplayerServerMethod.invoke(mc);
                    
                    java.lang.reflect.Method getLevelMethod = minecraftClass.getMethod("getLevel");
                    Object level = getLevelMethod.invoke(mc);
                    
                    if (singleplayerServer != null && level != null) {
                        return "[单人游戏]";
                    }
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
    //trace 方案暂时没有被使用，因为 trace 级别日志太多，影响游戏性能，但是暂时保留
    public static void trace(Object message) {
        if (enableTrace && LOGGER.isTraceEnabled()) {
            LOGGER.trace(formatMessage(message.toString()));
        }
    }
    
    public static void trace(String message, Object... params) {
        if (enableTrace && LOGGER.isTraceEnabled()) {
            LOGGER.trace(formatMessageWithParams(message, params));
        }
    }
        
    public static void trace(Throwable t, String message) {
        if (enableTrace && LOGGER.isTraceEnabled()) {
            LOGGER.trace(formatMessageWithParams(message), t);
        }
    }
    
    // ==================== DEBUG 级别 ====================
    
    public static void debug(Object message) {
        if (enableDebug && LOGGER.isDebugEnabled()) {
            LOGGER.debug(formatMessage(message.toString()));
        }
    }
    
    public static void debug(String message, Object... params) {
        if (enableDebug && LOGGER.isDebugEnabled()) {
            LOGGER.debug(formatMessageWithParams(message, params));
        }
    }
    
    public static void debug(Throwable t, String message) {
        if (enableDebug && LOGGER.isDebugEnabled()) {
            LOGGER.debug(formatMessageWithParams(message), t);
        }
    }
    
    // ==================== INFO 级别 ====================
    
    public static void info(Object message) {
        if (enableInfo && LOGGER.isInfoEnabled()) {
            LOGGER.info(formatMessage(message.toString()));
        }
    }
    
    public static void info(String message, Object... params) {
        if (enableInfo && LOGGER.isInfoEnabled()) {
            LOGGER.info(formatMessageWithParams(message, params));
        }
    }
    
    public static void info(Throwable t, String message) {
        if (enableInfo && LOGGER.isInfoEnabled()) {
            LOGGER.info(formatMessageWithParams(message), t);
        }
    }
    
    // ==================== WARN 级别 ====================
    
    public static void warn(Object message) {
        if (enableWarn && LOGGER.isWarnEnabled()) {
            LOGGER.warn(formatMessage(message.toString()));
        }
    }
    
    public static void warn(String message, Object... params) {
        if (enableWarn && LOGGER.isWarnEnabled()) {
            LOGGER.warn(formatMessageWithParams(message, params));
        }
    }
    
    public static void warn(Throwable t, String message) {
        if (enableWarn && LOGGER.isWarnEnabled()) {
            LOGGER.warn(formatMessageWithParams(message), t);
        }
    }
    
    // ==================== ERROR 级别 ====================
    
    public static void error(Object message) {
        if (enableError && LOGGER.isErrorEnabled()) {
            LOGGER.error(formatMessage(message.toString()));
        }
    }
    
    public static void error(String message, Object... params) {
        if (enableError && LOGGER.isErrorEnabled()) {
            LOGGER.error(formatMessageWithParams(message, params));
        }
    }
    
    public static void error(Throwable t, String message) {
        if (enableError && LOGGER.isErrorEnabled()) {
            LOGGER.error(formatMessageWithParams(message), t);
        }
    }
    
    // ==================== 日志级别控制 ====================
    

    //设置所有日志级别的开关

    public static void setAllLevels(boolean enabled) {
        enableTrace = enabled;
        enableDebug = enabled;
        enableInfo = enabled;
        enableWarn = enabled;
        enableError = enabled;
    }
    

    //设置特定日志级别的开关

    public static void setLevelEnabled(String levelName, boolean enabled) {
        switch (levelName.toUpperCase()) {
            case "TRACE" -> enableTrace = enabled;
            case "DEBUG" -> enableDebug = enabled;
            case "INFO" -> enableInfo = enabled;
            case "WARN" -> enableWarn = enabled;
            case "ERROR" -> enableError = enabled;
        }
    }
    

    //生产模式：只保留 INFO 及以上级别

    public static void setProductionMode() {
        enableTrace = false;
        enableDebug = false;
        enableInfo = true;
        enableWarn = true;
        enableError = true;
    }
    

    //开发模式：开启所有级别

    public static void setDevelopmentMode() {
        enableTrace = true;
        enableDebug = true;
        enableInfo = true;
        enableWarn = true;
        enableError = true;
    }
    

    //安静模式：只保留 ERROR

    public static void setQuietMode() {
        enableTrace = false;
        enableDebug = false;
        enableInfo = false;
        enableWarn = false;
        enableError = true;
    }


    //获取当前日志配置信息

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

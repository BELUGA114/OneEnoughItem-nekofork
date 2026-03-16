package com.mafuyu404.oneenoughitem.util;

import com.mafuyu404.oneenoughitem.Oneenoughitem;
import org.apache.logging.log4j.Logger;

/**
 * 统一的日志工具类
 * 提供多级别日志记录，自动标注客户端/服务端环境
 * 专注于日志功能，环境检测委托给 EnvironmentInfo
 */
public class OEILog {
    private static final Logger LOGGER = Oneenoughitem.LOGGER;
    
    // 日志级别枚举
    public enum LogLevel {
        DEBUG,  // 调试信息 (ordinal: 0)
        INFO,   // 一般信息 (ordinal: 1)
        WARN,   // 警告信息 (ordinal: 2)
        ERROR,  // 错误信息 (ordinal: 3)
        OFF;    // 关闭所有日志 (ordinal: 4)
        
        /**
         * 判断当前级别是否可以记录指定级别的日志
         */
        public boolean isEnabled(LogLevel targetLevel) {
            if (this == OFF) return false;
            return this.ordinal() <= targetLevel.ordinal();
        }
    }
    
    // 当前日志级别 - 默认 INFO
    private static LogLevel currentLevel = LogLevel.INFO;
    
    /**
     * 获取当前运行环境的标识（委托给 EnvironmentInfo）
     */
    public static String getEnvSide() {
        return EnvironmentInfo.getEnvSide();
    }
    
    /**
     * 获取世界/存档信息（委托给 EnvironmentInfo）
     */
    public static String getWorldInfo() {
        return EnvironmentInfo.getWorldInfo();
    }
    
    /**
     * 格式化日志消息，添加环境和世界信息前缀
     */
    private static String formatMessage(Object message) {
        StringBuilder sb = new StringBuilder();
        sb.append(getEnvSide()).append(' ').append(getWorldInfo()).append(' ');
        sb.append(message);
        return sb.toString();
    }
    
    /**
     * 格式化带参数的日志消息（简化版，直接使用 Log4j 占位符）
     */
    private static String formatMessageWithParams(String message, Object... params) {
        StringBuilder prefix = new StringBuilder();
        prefix.append(getEnvSide()).append(' ').append(getWorldInfo()).append(' ');
        return prefix.toString() + message;
    }
    
    // ==================== DEBUG 级别 ====================
    
    public static void debug(Object message) {
        if (currentLevel.isEnabled(LogLevel.DEBUG) && LOGGER.isDebugEnabled()) {
            LOGGER.debug(formatMessage(message.toString()));
        }
    }
    
    public static void debug(String message, Object... params) {
        if (currentLevel.isEnabled(LogLevel.DEBUG) && LOGGER.isDebugEnabled()) {
            LOGGER.debug(formatMessageWithParams(message), params);
        }
    }
    
    public static void debug(Throwable t, String message) {
        if (currentLevel.isEnabled(LogLevel.DEBUG) && LOGGER.isDebugEnabled()) {
            LOGGER.debug(formatMessageWithParams(message), t);
        }
    }
    
    // ==================== INFO 级别 ====================
    
    public static void info(Object message) {
        if (currentLevel.isEnabled(LogLevel.INFO) && LOGGER.isInfoEnabled()) {
            LOGGER.info(formatMessage(message.toString()));
        }
    }
    
    public static void info(String message, Object... params) {
        if (currentLevel.isEnabled(LogLevel.INFO) && LOGGER.isInfoEnabled()) {
            LOGGER.info(formatMessageWithParams(message), params);
        }
    }
    
    public static void info(Throwable t, String message) {
        if (currentLevel.isEnabled(LogLevel.INFO) && LOGGER.isInfoEnabled()) {
            LOGGER.info(formatMessageWithParams(message), t);
        }
    }
    
    // ==================== WARN 级别 ====================
    
    public static void warn(Object message) {
        if (currentLevel.isEnabled(LogLevel.WARN) && LOGGER.isWarnEnabled()) {
            LOGGER.warn(formatMessage(message.toString()));
        }
    }
    
    public static void warn(String message, Object... params) {
        if (currentLevel.isEnabled(LogLevel.WARN) && LOGGER.isWarnEnabled()) {
            LOGGER.warn(formatMessageWithParams(message), params);
        }
    }
    
    public static void warn(Throwable t, String message) {
        if (currentLevel.isEnabled(LogLevel.WARN) && LOGGER.isWarnEnabled()) {
            LOGGER.warn(formatMessageWithParams(message), t);
        }
    }
    
    // ==================== ERROR 级别 ====================
    
    public static void error(Object message) {
        if (currentLevel.isEnabled(LogLevel.ERROR) && LOGGER.isErrorEnabled()) {
            LOGGER.error(formatMessage(message.toString()));
        }
    }
    
    public static void error(String message, Object... params) {
        if (currentLevel.isEnabled(LogLevel.ERROR) && LOGGER.isErrorEnabled()) {
            LOGGER.error(formatMessageWithParams(message), params);
        }
    }
    
    public static void error(Throwable t, String message) {
        if (currentLevel.isEnabled(LogLevel.ERROR) && LOGGER.isErrorEnabled()) {
            LOGGER.error(formatMessageWithParams(message), t);
        }
    }
    
    // ==================== 日志级别控制 ====================
    
    /**
     * 设置日志级别
     * @param level 日志级别
     */
    public static void setLogLevel(LogLevel level) {
        currentLevel = level;
        LOGGER.info("日志级别已设置为：{}", level);
    }
    
    /**
     * 设置日志级别（使用字符串）
     * @param levelName 级别名称（不区分大小写）
     */
    public static void setLogLevel(String levelName) {
        try {
            LogLevel level = LogLevel.valueOf(levelName.toUpperCase());
            setLogLevel(level);
        } catch (IllegalArgumentException e) {
            warn("无效的日志级别：{}, 使用 INFO 级别", levelName);
            setLogLevel(LogLevel.INFO);
        }
    }
    
    /**
     * 获取当前日志级别
     * @return 当前日志级别
     */
    public static LogLevel getCurrentLogLevel() {
        return currentLevel;
    }
    
    /**
     * 获取当前日志配置信息（用于显示）
     * @return 日志配置字符串
     */
    public static String getLogLevelConfig() {
        return "Log Level: " + currentLevel;
    }
    
    // ==================== 预设日志模式 ====================
    
    /**
     * 开发模式：开启所有日志级别 (DEBUG, INFO, WARN, ERROR)
     */
    public static void devMode() {
        setLogLevel(LogLevel.DEBUG);
    }
    public static void debugMode() {
        setLogLevel(LogLevel.DEBUG);
    }
    public static void normalMode() {
        setLogLevel(LogLevel.INFO);
    }
    public static void minimalMode() {
        setLogLevel(LogLevel.WARN);
    }
    public static void productionMode() {
        setLogLevel(LogLevel.ERROR);
    }
    public static void silentMode() {
        setLogLevel(LogLevel.OFF);
    }
}

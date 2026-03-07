package com.mafuyu404.oneenoughitem.util;

/**
 * OEILog 配置助手
 * 提供便捷的日志级别预设配置
 *
 */
public class OEILogConfig {
    
    /**
     * 日志级别枚举
     */
    public enum LogLevel {
        TRACE,  // 最详细的调试信息
        DEBUG,  // 调试信息
        INFO,   // 一般信息
        WARN,   // 警告信息
        ERROR,  // 错误信息
        OFF     // 关闭所有日志
    }
    
    /**
     * 设置日志级别
     * 会自动启用该级别及其以上级别的所有日志
     * 
     * @param level 日志级别
     */
    public static void setLogLevel(LogLevel level) {
        switch (level) {
            case TRACE -> {
                OEILog.setAllLevels(true);
            }
            case DEBUG -> {
                OEILog.setAllLevels(true);
                OEILog.setLevelEnabled("TRACE", false);
            }
            case INFO -> {
                OEILog.setAllLevels(true);
                OEILog.setLevelEnabled("TRACE", false);
                OEILog.setLevelEnabled("DEBUG", false);
            }
            case WARN -> {
                OEILog.setAllLevels(true);
                OEILog.setLevelEnabled("TRACE", false);
                OEILog.setLevelEnabled("DEBUG", false);
                OEILog.setLevelEnabled("INFO", false);
            }
            case ERROR -> {
                OEILog.setAllLevels(true);
                OEILog.setLevelEnabled("TRACE", false);
                OEILog.setLevelEnabled("DEBUG", false);
                OEILog.setLevelEnabled("INFO", false);
                OEILog.setLevelEnabled("WARN", false);
            }
            case OFF -> {
                OEILog.setAllLevels(false);
            }
        }
    }
    
    /**
     * 设置日志级别（使用字符串）
     * 
     * @param levelName 级别名称（不区分大小写）
     */
    public static void setLogLevel(String levelName) {
        try {
            LogLevel level = LogLevel.valueOf(levelName.toUpperCase());
            setLogLevel(level);
        } catch (IllegalArgumentException e) {
            OEILog.warn("无效的日志级别：{}, 使用 INFO 级别", levelName);
            setLogLevel(LogLevel.INFO);
        }
    }
    
    /**
     * 获取当前日志级别
     * 
     * @return 当前日志级别
     */
    public static LogLevel getCurrentLogLevel() {
        // 从高到低检查，返回第一个未启用的前一个级别
        if (!OEILog.isErrorEnabled()) return LogLevel.OFF;
        if (!OEILog.isWarnEnabled()) return LogLevel.ERROR;
        if (!OEILog.isInfoEnabled()) return LogLevel.WARN;
        if (!OEILog.isDebugEnabled()) return LogLevel.INFO;
        if (!OEILog.isTraceEnabled()) return LogLevel.DEBUG;
        return LogLevel.TRACE;
    }
    
    // ==================== 预设模式 ====================
    
    /**
     * 开发模式：开启所有日志级别（包括 TRACE）
     * 适合模组开发者和测试人员使用
     */
    public static void devMode() {
        setLogLevel(LogLevel.TRACE);
    }
    
    /**
     * 调试模式：开启 DEBUG 及以上级别
     * 适合需要详细调试信息但不需要 TRACE 的场景
     */
    public static void debugMode() {
        setLogLevel(LogLevel.DEBUG);
    }
    
    /**
     * 普通模式：只开启 INFO 及以上级别
     * 适合普通玩家日常使用
     */
    public static void normalMode() {
        setLogLevel(LogLevel.INFO);
    }
    
    /**
     * 精简模式：只开启 WARN 和 ERROR
     * 适合只想看警告和错误的场景
     */
    public static void minimalMode() {
        setLogLevel(LogLevel.WARN);
    }
    
    /**
     * 生产模式：只记录 ERROR
     * 适合服务器或性能敏感的场景
     */
    public static void productionMode() {
        setLogLevel(LogLevel.ERROR);
    }
    
    /**
     * 安静模式：关闭所有日志
     * 适合完全不需要日志的场景
     */
    public static void silentMode() {
        setLogLevel(LogLevel.OFF);
    }
}

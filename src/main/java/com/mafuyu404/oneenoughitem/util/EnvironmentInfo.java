package com.mafuyu404.oneenoughitem.util;

/**
 * 环境信息工具类
 * 负责检测客户端/服务端环境和世界状态
 * 使用缓存机制，避免重复的反射调用
 */
public class EnvironmentInfo {
    
    // 环境检测结果缓存
    private static volatile Boolean cachedIsClientSide = null;
    private static volatile Boolean cachedIsServerSide = null;
    private static volatile String cachedEnvSide = null;
    private static volatile String cachedWorldInfo = null;
    
    /**
     * 判断是否在客户端（带缓存）
     * @return true-客户端，false-服务端
     */
    public static boolean isClientSide() {
        if (cachedIsClientSide != null) {
            return cachedIsClientSide;
        }
        
        synchronized (EnvironmentInfo.class) {
            if (cachedIsClientSide != null) {
                return cachedIsClientSide;
            }
            
            try {
                Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
                java.lang.reflect.Method getInstanceMethod = minecraftClass.getMethod("getInstance");
                getInstanceMethod.invoke(null);
                cachedIsClientSide = true;
                return true;
            } catch (Exception e) {
                cachedIsClientSide = false;
                return false;
            }
        }
    }
    
    /**
     * 判断是否在服务端（包括内置服务器，带缓存）
     * @return true-服务端，false-纯客户端
     */
    public static boolean isServerSide() {
        if (cachedIsServerSide != null) {
            return cachedIsServerSide;
        }
        
        synchronized (EnvironmentInfo.class) {
            if (cachedIsServerSide != null) {
                return cachedIsServerSide;
            }
            
            if (!isClientSide()) {
                cachedIsServerSide = true;
                return true;
            }
            
            try {
                Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
                java.lang.reflect.Method getInstanceMethod = minecraftClass.getMethod("getInstance");
                Object mc = getInstanceMethod.invoke(null);
                
                if (mc == null) {
                    cachedIsServerSide = true;
                    return true;
                }
                
                // 检查是否是单人游戏的内置服务器
                java.lang.reflect.Method getSingleplayerServerMethod = minecraftClass.getMethod("getSingleplayerServer");
                Object server = getSingleplayerServerMethod.invoke(mc);
                
                java.lang.reflect.Method getLevelMethod = minecraftClass.getMethod("getLevel");
                Object level = getLevelMethod.invoke(mc);
                
                boolean isServer = server != null || level != null;
                cachedIsServerSide = isServer;
                return isServer;
            } catch (Exception e) {
                cachedIsServerSide = true;
                return true;
            }
        }
    }
    
    /**
     * 获取环境标识（带缓存）
     * @return [CLIENT] / [SERVER] / [UNKNOWN]
     */
    public static String getEnvSide() {
        if (cachedEnvSide != null) {
            return cachedEnvSide;
        }
        
        synchronized (EnvironmentInfo.class) {
            if (cachedEnvSide != null) {
                return cachedEnvSide;
            }
            
            if (isClientSide()) {
                cachedEnvSide = "[CLIENT]";
            } else if (isServerSide()) {
                cachedEnvSide = "[SERVER]";
            } else {
                cachedEnvSide = "[UNKNOWN]";
            }
            return cachedEnvSide;
        }
    }
    
    /**
     * 获取世界信息（简化版，只在必要时检测）
     * @return 世界状态描述
     */
    public static String getWorldInfo() {
        if (cachedWorldInfo != null) {
            return cachedWorldInfo;
        }
        
        synchronized (EnvironmentInfo.class) {
            if (cachedWorldInfo != null) {
                return cachedWorldInfo;
            }
            
            String worldInfo = detectWorldInfo();
            cachedWorldInfo = worldInfo;
            return worldInfo;
        }
    }
    
    /**
     * 实际的世界信息检测逻辑
     */
    private static String detectWorldInfo() {
        // 优先检查客户端环境
        if (isClientSide()) {
            try {
                Class<?> minecraftClass = Class.forName("net.minecraft.client.Minecraft");
                java.lang.reflect.Method getInstanceMethod = minecraftClass.getMethod("getInstance");
                Object mc = getInstanceMethod.invoke(null);

                return "[单人游戏]";
            } catch (Exception e) {
                // 忽略异常
            }
        }
        
        // 纯服务端环境（没有客户端实例）
        if (!isClientSide() && isServerSide()) {
            return "[专用服务器]";
        }
        
        return "[无世界]";
    }
    
    /*
     * 清除缓存（用于环境变化时重新检测）
     */
    public static void clearCache() {
        cachedIsClientSide = null;
        cachedIsServerSide = null;
        cachedEnvSide = null;
        cachedWorldInfo = null;
    }
}

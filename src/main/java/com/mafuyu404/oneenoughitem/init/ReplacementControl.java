package com.mafuyu404.oneenoughitem.init;

import java.util.function.Supplier;

/**
 * 物品替换控制器
 * 用于在特定场景（如 GUI 显示）下跳过物品替换
 */
public class ReplacementControl {
    private static final ThreadLocal<Boolean> SKIP_REPLACEMENT = ThreadLocal.withInitial(() -> false);

    /**
     * 设置是否跳过物品替换
     * @param skip true - 跳过替换，false - 正常替换
     */
    public static void setSkipReplacement(boolean skip) {
        SKIP_REPLACEMENT.set(skip);
    }

    /**
     * 检查是否应该跳过物品替换
     * @return true - 跳过替换，false - 正常替换
     */
    public static boolean shouldSkipReplacement() {
        return SKIP_REPLACEMENT.get();
    }

    /**
     * 清除跳过替换状态
     */
    public static void clearSkipReplacement() {
        SKIP_REPLACEMENT.set(false);
    }

    /**
     * 在跳过替换的状态下执行操作
     * @param supplier 要执行的操作
     * @param <T> 返回值类型
     * @return 操作结果
     */
    public static <T> T withSkipReplacement(Supplier<T> supplier) {
        setSkipReplacement(true);
        try {
            return supplier.get();
        } finally {
            clearSkipReplacement();
        }
    }

    /**
     * 在跳过替换的状态下执行操作（无返回值）
     * @param runnable 要执行的操作
     */
    public static void runWithSkipReplacement(Runnable runnable) {
        setSkipReplacement(true);
        try {
            runnable.run();
        } finally {
            clearSkipReplacement();
        }
    }
}

# OneEnoughItem Mod 问题排查（个人学习）

## 项目概述
- **Mod名称**: OneEnoughItem (Fabric Mod for Minecraft 1.21.1)
- **功能**: 基于数据驱动的物品替换系统，使用OELib库加载替换规则。
- **预期效果**: 根据配置的规则，在游戏中自动替换特定物品为其他物品。

## 核心组件
- **数据类**: `Replacements` (record)，包含 `matchItems` (List<String>) 和 `resultItems` (String)。
- **注解**: `@DataDriven(modid="oneenoughitem", folder="replacements", syncToClient=true, validator=ReplacementValidator.class, supportArray=true)`
- **事件处理**: `ModEventHandler` 监听数据重载事件，重建缓存。
- **缓存**: `ReplacementCache` 使用HashMap存储物品ID映射。
- **Mixin**: `ItemStackMixin` 在ItemStack构造函数中注入替换逻辑。
- **验证**: `ReplacementValidator` 检查目标物品存在性和源物品有效性。

## 问题描述 (更新)
- **现象**: 游戏中物品替换不生效。
- **日志证据 (最新)**: `(oelib) Loading Replacements data from 1 files: [oneenoughitem:replacements/example.json]` 和 `Loaded 1 valid Replacements entries, 0 invalid entries were skipped`。
- **当前状态**: OELib 数据加载正常，但替换仍未生效。

## 排查过程 (更新)
1. **OELib 问题解决**:
   - 已经解决，往下看

2. **替换逻辑分析**:
   - 数据重载时，`ModEventHandler.onDataReload()` 调用 `rebuildReplacementCache()`
   - `ReplacementCache.putReplacement()` 解析 `matchItems`，将 "minecraft:egg" 映射到 "minecraft:diamond"
   - `ItemStackMixin.performReplacement()` 在 ItemStack 构造函数中检查缓存并替换物品

3. **缓存填充问题**:
   - 发现 `onDataReload` 未触发，导致缓存为空。
   - 添加客户端加入事件 `onPlayerJoin` 中也重建缓存。
   - 修改 `rebuildReplacementCache` 支持客户端无服务器实例时使用 `BuiltInRegistries`。
   - 缓存现在正确填充: `{minecraft:egg=minecraft:diamond}`

4. **创造模式物品栏替换尝试**:
   - 移除 `isInCreativeModeTabBuilding()` 跳过，允许在创造模式标签页构建时替换。
   - 测试结果: 导致崩溃 `IllegalStateException: Accidentally adding the same item stack twice`。
   - 原因: 替换导致同一物品 (如钻石) 被添加两次到创造模式标签页。
   - 结论: 无法完美解决，恢复跳过以确保稳定性。

## 调试建议
- 启用 DEBUG 日志，检查 `performReplacement()` 输出。
- 测试捡起鸡蛋是否变为钻石。
- 添加缓存内容输出，验证映射存在。
- 检查堆栈跟踪，确认替换时机。

## 假设与下一步
- **假设**: 替换逻辑正确，但未在预期场景触发，或缓存未正确填充。
- **下一步**: 在 OELib 中检查数据同步，验证客户端缓存。

## 最终结论
- 替换功能在游戏中正常工作 (捡起物品时替换)。
- 创造模式物品栏替换会导致崩溃，无法实现。
- 项目稳定，可用于基础物品替换。

## 关键文件路径
- 数据文件: `src/main/resources/data/oneenoughitem/replacements/example.json`
- 缓存: `ReplacementCache.java`
- Mixin: `ItemStackMixin.java`
- 事件: `ModEventHandler.java`
- 验证: `ReplacementValidator.java`
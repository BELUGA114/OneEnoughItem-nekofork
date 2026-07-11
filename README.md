# OneEnoughItem - Fabric 物品统一系统

[![GitHub Stars](https://img.shields.io/github/stars/BELUGA114/OneEnoughItem-nekofork?style=flat-square)](https://github.com/BELUGA114/OneEnoughItem-nekofork/stargazers)
[![License](https://img.shields.io/github/license/BELUGA114/OneEnoughItem-nekofork?style=flat-square)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-blue)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-Legacy-lightgrey)](https://fabricmc.net/)

> Fork 自 [OneEnoughItem](https://github.com/Tower-of-Sighs/OneEnoughItem)，原项目 Fabric 分支已停更

## 简介

OEI 解决整合包中多个 Mod 添加相同物品的问题——把所有来源的物品实例统一替换为一种

例如 `modA:silver_ingot`、`modB:silver_ingot`、`modC:silver_ingot` -> 统一为 `modA:silver_ingot`。只需一个 JSON 文件即可完成配置

### 核心功能

- 统一多个 Mod 的相同物品（矿物、农作物、食物等）
- 全局物品替换：掉落物、容器、生物掉落自动生效
- 基于 Mixin 和对象级映射，几乎不影响游戏性能

### 重要限制

一旦配置替换（如 `鸡蛋 -> 钻石`），被替换的物品将**永远不会以物品形式出现在世界上**。这意味着你无法获得真正的鸡蛋，也无法用鸡蛋合成蛋糕等物品

Fabric 版本**无法修改配方显示**，JEI/REI 仍显示原配方。这是平台技术限制，不是 bug

## 快速开始

### 安装依赖

- [Fabric API](https://modrinth.com/mod/fabric-api)
- [OELib](https://github.com/BELUGA114/OELib-nekofork/releases)

### 创建配置文件

在数据包中创建 `data/oneenoughitem/replacements/<name>.json`：

```json
[
  {
    "matchItems": ["mod_a:silver_ingot", "mod_b:silver_ingot"],
    "resultItems": "mod_a:silver_ingot"
  }
]
```

**字段说明：**
- `matchItems`：要替换的物品 ID 列表，支持标签 `#namespace:tag`
- `resultItems`：替换后的目标物品 ID

### 加载方式

| 方式 | 说明 |
|---|---|
| 打包进 Mod JAR | 放在 `src/main/resources/data/oneenoughitem/replacements/`，编译后自动携带 |
| 数据包 | 放在 `world/datapacks/<name>/data/oneenoughitem/replacements/` |

数据包配置优先级高于 Mod 内置配置，多个数据包时后加载的覆盖先加载的

## 配置示例

批量替换：

```json
[
  {
    "matchItems": ["mod_a:tin_ingot", "mod_b:tin_ingot"],
    "resultItems": "mod_a:tin_ingot"
  },
  {
    "matchItems": ["#c:silver_ingots"],
    "resultItems": "mod_a:silver_ingot"
  }
]
```

## 技术细节

Minecraft 物品替换流程：

```
ItemStack.<init>() 构造
  -> Mixin 注入 performReplacement()
    -> ItemRedirector 实例映射查找（最快）
    -> ReplacementCache ID 映射查找（回退）
    -> 修改 this.item，完成替换
```

Fabric 版本不支持配方替换的原因：
1. 配方加载早于 OEI 配置读取，无法回溯修改
2. 修改 `Ingredient.getItems()` 会导致客户端-服务端网络编码异常

如需配方也替换，推荐使用原作者的 NeoForge 版本

## 常见问题

**Q: 配置后物品消失了？**
A: 检查 `resultItems` 指向的目标物品是否存在，如果目标 Mod 未安装，物品会消失

**Q: 如何修改配方？**
A: Fabric 版本不支持，OEI 负责物品替换

**Q: 能只替换特定来源的物品吗？**
A: 不能。替换是全局永久的，所有来源都会被替换

## 相关链接

- [原项目](https://github.com/Tower-of-Sighs/OneEnoughItem)
- [OELib](https://github.com/BELUGA114/OELib-nekofork)
- [Fabric 文档](https://fabricmc.net/wiki/)

---

*OneEnoughItem 1.0.4 for Minecraft 1.21.1 (Fabric)*

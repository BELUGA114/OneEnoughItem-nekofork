# OneEnoughItem - Fabric 物品统一系统

[![GitHub Stars](https://img.shields.io/github/stars/BELUGA114/OneEnoughItem-nekofork?style=flat-square)](https://github.com/BELUGA114/OneEnoughItem-nekofork/stargazers)
[![License](https://img.shields.io/github/license/BELUGA114/OneEnoughItem-nekofork?style=flat-square)](LICENSE)
[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-blue)](https://www.minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-Legacy-lightgrey)](https://fabricmc.net/)

> **⚠️ 项目声明**
> 
> 本项目 Fork 自 [OneEnoughItem](https://github.com/Tower-of-Sighs/OneEnoughItem)
> 
> 由于原项目的 Fabric 分支已停止维护，因此 Fork 过来自用。

---

## ✨ TL;DR

**OEI** 用于解决 **整合包中多个 Mod 添加相同物品的问题**。

**例如：**

* `modA:silver_ingot`
* `modB:silver_ingot`
* `modC:silver_ingot`

**统一为：**

```
modA:silver_ingot
```

**只需一个 JSON 文件** 即可完成配置。

---

# 📚 目录

* [简介](#-简介)
* [核心功能](#-核心功能)
* [Fabric vs NeoForge](#-fabric-vs-neoforge-版本对比)
* [快速开始](#-快速开始)
* [配置示例](#-配置示例)
* [技术细节与架构对比](#-技术细节与架构对比)
* [适用场景](#-适用场景)
* [常见问题](#-常见问题)

---

## 📖 简介

**OneEnoughItem (OEI)** 是一个基于数据驱动的物品替换系统，专为解决 Minecraft Mod 整合包中的物品重复问题而生。

### 💡 核心功能

- ✅ **统一多个 Mod 的相同物品**（主要用途）
  - 例如：三个 Mod 都有"银锭"，统一为其中一种
  - 例如：多个农业Mod的番茄、玉米统一为一种
  
- ✅ **全局物品替换**
  - 所有掉落物、容器、生物掉落自动替换
  - 配置简单，只需 JSON 文件即可
  
- ✅ **高性能、低开销**
  - 基于 Mixin 和对象级映射
  - 几乎不影响游戏性能

### ⚠️ 重要限制

**请在使用前务必了解：**

一旦配置了物品替换（如 `鸡蛋 → 钻石`），被替换的物品将**永远不会以物品形式出现在世界上**。

这意味着：
- ✅ 所有掉落物中的鸡蛋都会变成钻石
- ✅ 所有容器中的鸡蛋都会变成钻石
- ❌ **你将无法获得任何真正的鸡蛋**
- ❌ **因此无法用鸡蛋合成南瓜派、蛋糕等物品**

**Fabric 版本无法修改配方显示**，JEI/REI 等查看器仍然显示原配方。

---

## 🔄 Fabric vs NeoForge 版本对比

**重要提示**：本 Mod 有 Fabric 和 NeoForge 两个版本，功能存在显著差异。

| 功能特性 | Fabric 版本 | NeoForge 版本 |
|---------|-----------|-------------|
| **物品实例替换** | ✅ 完整支持 | ✅ 完整支持 |
| ├─ 掉落物替换 | ✅ | ✅ |
| ├─ 容器物品替换 | ✅ | ✅ |
| ├─ 生物掉落替换 | ✅ | ✅ |
| └─ 实体手持物品替换 | ✅ | ✅ |
| **配方系统替换** | ❌ **不支持** | ✅ **完整支持** |
| ├─ JEI/REI 配方显示 | ❌ 仍显示原配方 | ✅ 自动替换 |
| └─ 合成时自动替换 | ❌ 仍需原物品 | ✅ 自动识别 |
| **标签系统替换** | ❌ 不支持 | ✅ 支持 |
| **性能开销** | 🟢 极低 | 🟡 低 |

### 💡 核心差异说明

**Fabric 版本的限制**：
- ❌ 只能替换物品实例，无法修改配方
- ❌ JEI/REI 仍显示原始配方
- ❌ 合成时仍需要原始物品
- ⚠️ 这是 Fabric 平台的技术限制，非 Mod 本身问题

**NeoForge 版本的优势**：
- ✅ 三层替换架构（JSON 层 + 实例层 + 标签层）
- ✅ 在配方加载前修改 JSON，天然替换
- ✅ JEI/REI 完整兼容

---

## 🚀 快速开始

### 1️⃣ 安装依赖

确保已安装以下 Mod：
- [Fabric API](https://modrinth.com/mod/fabric-api)
- [OELib](https://github.com/BELUGA114/OELib-nekofork/releases)

### 2️⃣ 创建配置文件

在你的 Minecraft 实例或整合包中创建：

```
data/oneenoughitem/replacements/example.json
```

**完整路径示例**：
- 整合包开发：`src/main/resources/data/oneenoughitem/replacements/example.json`
- 数据包：`your_datapack/data/oneenoughitem/replacements/example.json`

### 3️⃣ 配置格式

```json
[
  {
    "matchItems": ["kaleidoscope_cookery:tomato"],
    "resultItems": "farmersdelight:tomato"
  },
  {
    "matchItems": ["kaleidoscope_cookery:rice"],
    "resultItems": "farmersdelight:rice"
  }
]
```

**字段说明**：
- `matchItems`: 要替换的物品列表（数组）
- `resultItems`: 替换后的目标物品 ID

### 4️⃣ 加载配置

#### 方法 A: 打包进 Mod Jar（开发者）

**说明**：将配置文件直接放进编译好的 Mod 文件里，用户安装后自动就有配置了。

**原理**：Gradle 会自动把 `src/main/resources` 下的所有文件打包进最终的 `.jar` 文件，**不需要额外配置**。

**步骤**：
1. 在开发环境中，将配置文件放在：
   ```
   src/main/resources/data/oneenoughitem/replacements/example.json
   ```
2. 编译 Mod：
   ```bash
   ./gradlew build
   ```
3. 编译后的 jar 文件内部会自动包含配置文件：
   ```
   OneEnoughItem-xxx.jar
   ├── assets/
   ├── data/
   │   └── oneenoughitem/
   │       └── replacements/
   │           └── example.json  ← 自动在这里
   └── ...
   ```
4. 用户安装这个 Mod 后，游戏会自动加载配置

**适用场景**：整合包作者预置配置、Mod 联动

#### 方法 B: 使用数据包 (Datapack)

将配置文件放入数据包结构：
```
your_datapack/
├── pack.mcmeta
└── data/
    └── oneenoughitem/
        └── replacements/
            └── example.json
```

**放置位置**：
- **服务端/单人游戏**: `world/datapacks/OEI/data/oneenoughitem/replacements/`
- **单人游戏存档**: `.minecraft/saves/你的世界/datapacks/OEI/data/oneenoughitem/replacements/`

**优先级说明**：
- ✅ **数据包配置 > Mod 内置配置**
- ✅ 如果两处都有相同的替换规则，**数据包的会覆盖 Mod 的**
- ✅ 多个数据包时，后加载的覆盖先加载的（可通过 `pack.mcmeta` 的 `position` 控制）

**示例场景**：
```json
// src/main/resources/data/oneenoughitem/replacements/example.json (Mod 内置)
[
  {"matchItems": ["modA:apple"], "resultItems": "minecraft:diamond"}
]

// world/datapacks/OEI/data/oneenoughitem/replacements/example.json (数据包)
[
  {"matchItems": ["modA:apple"], "resultItems": "minecraft:emerald"}  // 这个会覆盖上面的！
]
```

最终生效的是：`modA:apple → minecraft:emerald`

用 `/datapack` 命令管理数据包加载顺序和启用状态。

### 5️⃣ 验证效果

进入游戏测试：
- ✅ 被替换的物品 → 变成目标物品
- ✅ 打开箱子 → 物品应已替换
- ⚠️ JEI/REI 配方显示 → 仍显示原配方（**Fabric 是这样的**）
- ℹ️ 如需配方也替换 → 请使用 NeoForge 版本

---

## 📦 配置示例

### 批量替换（多个物品→一个目标）

```json
[
  {
    "matchItems": [
      "mod_a:silver_ingot",
      "mod_b:silver_ingot",
      "mod_c:silver_ingot"
    ],
    "resultItems": "mod_a:silver_ingot"
  }
]
```



### 跨 Mod 物品替换

```json
[
  {
    "matchItems": ["unwanted_mod:item"],
    "resultItems": "preferred_mod:better_item"
  }
]
```

---

## 🔧 技术细节与架构对比

### 🏗️ Fabric 版本架构（单层替换）

```
游戏启动流程:
1. 物品注册 → BuiltInRegistries.ITEM 初始化
2. 配方加载 ← ❌ 此时还没有读取 OEI 配置
3. 数据重载事件触发
4. OEI 读取 replacements/*.json
5. 建立 ReplacementCache
6. 游戏开始
   └→ 此时配方已加载完成，无法修改！

物品替换流程:
玩家捡起物品
    ↓
ItemStack.<init>() 被调用
    ↓
ItemStackMixin.performReplacement()
    ├→ 查询 ReplacementCache
    ├→ 发现 kaleidoscope_cookery:tomato → farmersdelight:tomato
    └→ 修改 this.item
    ↓
物品被替换 ✅

但是:
JEI 查看配方
    ↓
显示的是 kaleidoscope_cookery:tomato ❌
因为配方在加载时没有被修改
```

### 🏗️ NeoForge 版本架构（三层替换）

```
阶段 1: 游戏启动 - JSON 层替换（最关键）
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
资源包重载
    ↓
SimpleJsonResourceReloadListener.prepare()
    ├→ 读取 recipes/farmersdelight/tomato_sauce.json
    │  原始内容：{"ingredient": {"item": "kaleidoscope_cookery:tomato"}}
    ↓
Mixin 拦截
    ├→ 扫描 OEI 配置
    ├→ 发现映射：kaleidoscope_cookery:tomato → farmersdelight:tomato
    └→ 修改 JSON: {"ingredient": {"item": "farmersdelight:tomato"}}
    ↓
RecipeManager.apply()
    └→ 使用修改后的 JSON 构建配方 ✅

阶段 2: 玩家捡起物品 - 实例层替换
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
ItemStack.<init>()
    ↓
ItemStackMixin.performReplacement()
    └→ 再次确保替换（双重保险）✅

阶段 3: JEI 查询 - 标签扩展层
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
JEI 调用：itemStack.is(Items.TOMATO)
    ↓
ItemStackMixin.extend()
    ├→ 追踪源物品
    └→ 让替换后的物品也能通过原始物品的查询 ✅
```

### ❓ 为什么 Fabric 做不到配方替换？

**1. 加载时机问题**
```
Fabric API 有自己的数据加载系统
├── ConditionContext 独立处理条件
├── DataProvider 独立提供数据
└── 配方加载流程与 JSON 解析分离得更早
    └── 难以找到统一的拦截点
    └── 等 OEI 读到配置时，配方早就建好了
```

**2. 网络同步限制（致命）**
```
Fabric 曾尝试修改 Ingredient.getItems()
    ↓
结果：服务器崩溃
    ↓
错误信息:
[Server thread/ERROR]: Error sending packet clientbound/minecraft:update_recipes
io.netty.handler.codec.EncoderException: Failed to encode packet
Caused by: CancellationException: The call getItems is not cancellable.

根本原因:
- Ingredient.getItems() 用于网络序列化配方数据
- Fabric API 的 CustomIngredientPacketCodec 依赖这个方法
- 修改返回值会导致客户端 - 服务器数据包不一致
- 网络编码验证失败，连接被终止
```

**3. Hook 点可用性**

| Hook 点 | Fabric | NeoForge | 用途 |
|--------|--------|----------|------|
| `SimpleJsonResourceReloadListener.prepare()` | ❌ 不可用 | ✅ 可用 | JSON 层替换 |
| `TagLoader.load()` | ⚠️ 部分可用 | ✅ 完整可用 | 标签替换 |
| `ItemStack.<init>()` | ✅ 可用 | ✅ 可用 | 实例替换 |
| `Ingredient.getItems()` | ❌ 会崩溃 | ✅ 不需要用 | （NeoForge 已规避） |

### 💡 NeoForge 的巧妙设计

```
NeoForge 根本不需要修改 Ingredient
因为 JSON 已经在加载时被修改了
├── 配方对象构建时使用的就是修改后的 JSON
├── Ingredient 天然就包含替换后的物品
└── 网络同步时完全正常，无需任何 Hack
```

---

## ⚙️ 适用场景

### ✅ 推荐使用

- **统一多个 Mod 的相同物品**（主要用途）
  - 矿物统一（三种银、四种铅）
  - 农作物统一（多种番茄、玉米）
  - 食物统一（多种牛奶、面包）
  
- **移除不需要的物品**
  - 某个 Mod的无用材料 → 有用资源


### ❌ 不适合的场景

- **保留基础物品的功能性**
  - ⚠️ 不要替换鸡蛋、小麦等用于多种合成的基础物品
  - 除非你确定不再需要用它合成其他物品
  
- **希望 JEI/REI 显示修改后的配方**
  - 本 Mod 只替换物品实例，不改变配方显示
  
- **临时性替换**
  - 替换是全局永久的，无法局部生效

---

### 🔧 技术细节（可选阅读）

### 为什么 Fabric 版本不能修改配方？

Minecraft 的配方系统在数据重载**之前**就已经加载完成，而 OEI 的配置加载较晚，无法回溯修改已加载的配方引用。

此外，强行修改配方的网络序列化方法（`Ingredient.getItems()`）会导致连接异常。

### 💡 关于原项目 NeoForge 版的说明

原项目（NeoForge 版本）支持完整的配方同步，利用了 NeoForge 平台的独特优势：

1. **更早的 Hook 点**：`SimpleJsonResourceReloadListener.prepare()` 可在配方加载前修改 JSON
2. **三层架构**：JSON 层 + 实例层 + 标签层，互相兜底
3. **顺势而为**：修改 JSON → 自然构建正确配方，无需强行回溯
4. **规避网络问题**：不需要修改 Ingredient，网络同步天然一致

**架构对比**：
- **NeoForge**：三层替换，功能完整，但性能开销略高
- **Fabric**：单层替换，简单稳定，但缺少配方替换

**建议方案**：
- 如果确实需要配方替换功能，推荐：
  1. **首选**：使用 NeoForge 版本
  2. **次选**：配合 **KubeJS** 修改配方（OEI 负责物品替换）
  3. **等待**：未来 Fabric API 可能提供更强大的配方事件 Hook

### 与其他方案的对比

| 方案 | 优点 | 缺点 |
|------|------|------|
| **OEI** | 简单、稳定、性能好 | 无法修改配方显示 |
| **KubeJS** | 功能强大、可修改配方 | 学习成本高、配置复杂 |
| **CraftTweaker** | 专注配方修改 | 需要额外学习语法 |
| **数据包** | 原版支持 | 只能影响部分配方 |

**建议**：如需完整的物品 + 配方替换，推荐配合 KubeJS 使用。

---

## ❓ 常见问题

**Q: NeoForge 版本和 Fabric 版本有什么区别？**  
A: NeoForge 版本支持三层替换（JSON 层 + 实例层 + 标签层），可以完整替换配方；Fabric 版本只有实例层替换，无法修改配方。详见上方的「Fabric vs NeoForge 版本对比」表格。

**Q: 有没有推荐的替代方案？**  
A: 如果需要完整的物品 + 配方替换： 使用 NeoForge 版本

**Q: 配置后物品消失了怎么办？**  
A: 检查 `resultItems` 指向的目标物品是否存在。如果目标 Mod 未安装，物品会消失。

---

## 🔗 相关链接

- [原项目地址](https://github.com/Tower-of-Sighs/OneEnoughItem)
- [OELib](https://github.com/BELUGA114/OELib-nekofork)
- [Fabric 官方文档](https://fabricmc.net/wiki/)

---

*适用版本：OneEnoughItem 1.0.3+ for Minecraft 1.21.1 (Fabric)*

# 超级放大维度 Ultra Amplified Dimension（Forge 1.20.1）

[English](README.md) | **中文**

比放大世界更极端的维度：多层浮空地形、加强群系（含下界/末地区域）、自定义结构与高密度世界生成。

本仓库是 TelepathicGrunt 原版模组的 **社区移植**，目标版本为 **Minecraft 1.20.1 / Forge**。

| | |
|---|---|
| Minecraft | 1.20.1 |
| 模组加载器 | Forge 47.x |
| 许可证 | [LGPL-3.0](LICENSE) |
| 原作者 | [TelepathicGrunt](https://www.curseforge.com/members/telepathicgrunt) |
| 1.20.1 移植 | [JSenyu](https://github.com/JSenyu) |

## 许可证

本项目（及原版）采用 **GNU LGPL v3.0**。

你可以在 GitHub 开源、发布二进制（含 GitHub Releases）、用于整合包，但需保留许可证与署名（见 `LICENSE`、`NOTICE`）。

## 进入维度

搭建放大传送门框架：

- 磨制花岗岩 ×8
- 磨制安山岩台阶 ×10
- 中心：磨制闪长岩 ×1

用打火石（或 `#ultra_amplified_dimension:portal_activation_items` 中的物品）右击中心激活，再右击传送门方块（不潜行）传送。

框架与激活物品可通过数据包 tag 修改。

## 世界类型

创建世界 → 世界类型中可选 **超级放大 / Ultra Amplified**（默认排在首位）。  
服务端：`level-type=ultra_amplified_dimension:ultra_amplified`

## 配置说明

配置文件：`config/ultra_amplified_dimension.toml`  
（文件内注释为简短英文；详细中文说明见下表。）  
大部分地形/群系生成仍由 `data/ultra_amplified_dimension/` 数据包驱动。

### 通用维度选项（General Dimension Options）

| 选项 | 默认 | 说明 |
|------|------|------|
| `heavyFog` | `false` | 浓重雾气（不是远处区块的距离雾）。 |
| `cloudHeight` | `245` | 云层高度。 |
| `netherLighting` | `false` | 下界风格光照。 |
| `skyType` | `NORMAL` | 天空类型：`NORMAL` / `END` / `NONE`。 |
| `allowNetherPortal` | `false` | 允许在 UAD 内建造下界传送门。 |
| `forceExitToOverworld` | `false` | 放大传送门离开时始终回到主世界。 |

### 群系大小（Biome Size）

| 选项 | 默认 | 说明 |
|------|------|------|
| `biomeSize` | `89` | 群系尺度 `1–357`。仅对新区块 / 新世界生效，需重启。 |
| `subBiomeRate` | `0.44` | 子群系出现概率。 |
| `mutatedBiomeRate` | `0.42` | 变异 / 稀有群系概率。 |

**与方块的换算**（噪声特征尺度，不是严格正方形边长）：

| 含义 | 公式 | 默认 `89` | 上限 `357` |
|------|------|-----------|------------|
| 单个群系大致跨度 | `biomeSize × 5.6` 方块 | ≈ **500** 格 | ≈ **2000** 格 |
| 气候大区大致跨度 | `biomeSize × 16.8` 方块 | ≈ 1500 格 | ≈ 6000 格 |

生成时在四分之一坐标（1 quart = 4 方块）上采样：`regionScale = 4.2 × biomeSize`，`biomeScale = regionScale / 3`。

### 世界集成（World Integration）

改配置后请**完全退出游戏再进**，且仅对**新世界**生效。

| 选项 | 默认 | 说明 |
|------|------|------|
| `enableUadDimension` | `true` | 是否启用额外 UAD 维度。`false` 时无法通过传送门前往该维度。 |
| `setUadAsDefaultDimension` | `false` | 「默认」世界类型的**主世界**换成 UAD；原版主世界保留为 `original_overworld`，放大传送门可往返。若开启下一选项则忽略本项。 |
| `overrideVanillaOverworld` | `false` | 主世界直接变成 UAD（整档只要 UAD）；**禁用**放大传送门。优先级高于上一选项。 |
| `generateBelowZero` | `false` | `false`：UAD 地形地板在 Y=0，以下为空；`true`：地形延伸到 Y=-64..0（与上方放大地形同一套噪声）。仅对新区块生效。 |

**怎么选：**

| 需求 | 做法 |
|------|------|
| 出生即 UAD，但仍想用传送门回原版主世界 | 开 `setUadAsDefaultDimension`，用「默认」世界类型新建 |
| 整档只要 UAD，不要传送门往返 | 开 `overrideVanillaOverworld`，用「默认」世界类型新建 |
| 不改配置，单次选用 UAD | 创建世界时手选世界类型「Ultra Amplified」 |

手选 UAD 世界类型不依赖上述两个开关；它们只影响「默认」世界类型的主世界生成器。

## 构建

需要 **JDK 17+**（Gradle 可用 JDK 21；模组目标 Java 17）。

```bat
gradlew.bat build
```

产物：

```text
build/libs/ultra_amplified_dimension-<version>.jar
```

## 致谢

- **TelepathicGrunt** — 原版模组、设计与 LGPL 发布
- **JSenyu** — 1.20.1 Forge 移植与维护
- Forge / Mixin 生态与社区协助

原版 CurseForge：  
https://www.curseforge.com/minecraft/mc-mods/ultra-amplified-mod

## 问题反馈

请在本仓库 Issues 反馈 **本 1.20.1 移植版** 的问题，并尽量附上 `crash-reports/` 中的崩溃报告。

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
| `biomeSize` | `20` | 群系尺度 `1–20`（默认 20，接近 1.16.5 巨型群系）。仅对新区块 / 新世界生效，需重启。 |
| `subBiomeRate` | `0.44` | 子群系出现概率。 |
| `mutatedBiomeRate` | `0.42` | 变异 / 稀有群系概率。 |

### 世界集成（World Integration）

| 选项 | 默认 | 说明 |
|------|------|------|
| `enableUadDimension` | `true` | 是否启用额外 UAD 维度。`false` 时无法通过传送门前往该维度。 |
| `setUadAsDefaultDimension` | `false` | 「默认」世界类型的主世界改为 UAD；原版主世界保留为 `original_overworld`，传送门可往返。若开启覆盖主世界则忽略本项。 |
| `overrideVanillaOverworld` | `false` | 直接用 UAD 地形替换主世界生成（结构仍会按 UAD 群系生成）；禁用放大传送门。优先级高于上一选项。 |

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

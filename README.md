# Ultra Amplified Dimension (Forge 1.20.1)

**English** | [中文](README_zh.md)

A dimension more extreme than Amplified: layered floating landscapes, boosted biomes (including Nether/End regions), custom structures, and dense worldgen.

This repository is a **community port** of TelepathicGrunt's Ultra Amplified Dimension to **Minecraft 1.20.1 / Forge**.

| | |
|---|---|
| Minecraft | 1.20.1 |
| Mod loader | Forge 47.x |
| License | [LGPL-3.0](LICENSE) |
| Original author | [TelepathicGrunt](https://www.curseforge.com/members/telepathicgrunt) |
| 1.20.1 port | [JSenyu](https://github.com/JSenyu) |

## License

This project (and the original) is licensed under **GNU LGPL v3.0**.

You **may** publish the source on GitHub, redistribute binaries (including GitHub Releases), and use the mod in modpacks, as long as you keep the license/attribution (see `LICENSE` and `NOTICE`).

## Entering the dimension

Build the Amplified Portal:

- 8× Polished Granite
- 10× Polished Andesite Slab
- 1× Polished Diorite (center)

Right-click the center with Flint and Steel (or another item in `#ultra_amplified_dimension:portal_activation_items`) to create the portal, then right-click the portal block (not sneaking) to teleport.

Portal frame / activation items can be changed with datapack tags.

## World type

Create World → World Type includes **Ultra Amplified** (listed first).  
Dedicated server: `level-type=ultra_amplified_dimension:ultra_amplified`

## Configuration

File: `config/ultra_amplified_dimension.toml`  
Most worldgen remains datapack-driven under `data/ultra_amplified_dimension/`.

### General Dimension Options

| Option | Default | Description |
|--------|---------|-------------|
| `heavyFog` | `false` | Heavy fog (not distance fog). |
| `cloudHeight` | `245` | Cloud height. |
| `netherLighting` | `false` | Nether-style lighting. |
| `skyType` | `NORMAL` | `NORMAL`, `END`, or `NONE`. |
| `allowNetherPortal` | `false` | Allow Nether portals inside UAD. |
| `forceExitToOverworld` | `false` | Amplified portal always exits to Overworld. |

### Biome Size

| Option | Default | Description |
|--------|---------|-------------|
| `biomeSize` | `89` | Biome scale `1–357`. New chunks only. |
| `subBiomeRate` | `0.44` | Sub-biome chance. |
| `mutatedBiomeRate` | `0.42` | Mutated biome chance. |

**Block conversion** (noise feature scale, not a hard square edge):

| Meaning | Formula | Default `89` | Max `357` |
|---------|---------|--------------|-----------|
| Typical biome span | `biomeSize × 5.6` blocks | ≈ **500** | ≈ **2000** |
| Climate region span | `biomeSize × 16.8` blocks | ≈ 1500 | ≈ 6000 |

Sampling uses quarter coords (1 quart = 4 blocks): `regionScale = 4.2 × biomeSize`, `biomeScale = regionScale / 3`.

### World Integration

| Option | Default | Description |
|--------|---------|-------------|
| `enableUadDimension` | `true` | Extra UAD dimension. `false` disables portal travel to it. |
| `setUadAsDefaultDimension` | `false` | Default world type uses UAD as Overworld; vanilla kept as `original_overworld`; portals remain. Ignored if override is on. |
| `overrideVanillaOverworld` | `false` | Replace Overworld generation with UAD; disables amplified portals. Overrides the option above. |

## Building

Requirements: **JDK 17+** (JDK 21 works for Gradle; the mod targets Java 17).

```bat
gradlew.bat build
```

Output jar:

```text
build/libs/ultra_amplified_dimension-<version>.jar
```

## Credits

- **TelepathicGrunt** — original mod, design, and LGPL release
- **JSenyu** — 1.20.1 Forge port and maintenance
- Forge / Mixin ecosystem and community helpers

Original CurseForge listing:  
https://www.curseforge.com/minecraft/mc-mods/ultra-amplified-mod

## Issues

Please report bugs for **this 1.20.1 port** in this repository's Issues tab. Include crash reports from `crash-reports/` when possible.

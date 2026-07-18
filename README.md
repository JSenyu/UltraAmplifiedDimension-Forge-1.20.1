# Ultra Amplified Dimension (Forge 1.20.1)

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

## Configuration

- Mod config: `ultra_amplified_dimension.toml` (fog, nether portals, cloud height, force exit to Overworld, etc.)
- Most worldgen is datapack-driven under `data/ultra_amplified_dimension/`

## Building

Requirements: **JDK 17+** (JDK 21 works for Gradle; the mod targets Java 17).

```bat
gradlew.bat build
```

Output jar:

```text
build/libs/ultra_amplified_dimension-<version>.jar
```

For IDE runs, mixin remapping properties are already set in `build.gradle`.

## Credits

- **TelepathicGrunt** — original mod, design, and LGPL release
- **JSenyu** — 1.20.1 Forge port and maintenance
- Forge / Mixin ecosystem and community helpers

Original CurseForge listing:  
https://www.curseforge.com/minecraft/mc-mods/ultra-amplified-mod

## Issues

Please report bugs for **this 1.20.1 port** in this repository's Issues tab. Include crash reports from `crash-reports/` when possible.

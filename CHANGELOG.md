### (V.10.0.0 Changes) (1.20.1 Minecraft / Forge)

##### Port:
- Ported Ultra Amplified Dimension from Forge 1.16.5 to Forge 1.20.1
- Rebuilt worldgen datapacks for 1.20 registries (biomes, features, placements, structures, carvers, noise settings)
- Restored custom UAD terrain fill (legacy noise + structure terraforming) with void below Y=0 and sea level 75
- Ported structure height adjustments for UAD (mineshaft, stronghold, mansion, fortress, ocean pieces, etc.)
- Fixed player-placed big cactus body/corner missing models (horizontal facing)
- Fixed CocoaDecorator / BeehiveDecorator crashes when trees place with empty log lists
- Kept LGPL-3.0 license and original authorship attribution

##### Notes:
- Prefer a new world (or regenerating UAD chunks) after updating from older test builds
- Chunk loading can still be heavy at high render distance (amplified terrain + dense features)

---

### Older changelogs (1.16.5)

See `CHANGELOG_OLD.md` for TelepathicGrunt's 1.16.5 release history.

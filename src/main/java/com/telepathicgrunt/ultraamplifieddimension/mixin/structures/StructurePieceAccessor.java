package com.telepathicgrunt.ultraamplifieddimension.mixin.structures;

import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StructurePiece.class)
public interface StructurePieceAccessor {
    @Invoker("getBlock")
    BlockState uad_callGetBlock(BlockGetter level, int x, int y, int z, BoundingBox box);

    @Invoker("getWorldX")
    int uad_callGetWorldX(int x, int z);

    @Invoker("getWorldY")
    int uad_callGetWorldY(int y);

    @Invoker("getWorldZ")
    int uad_callGetWorldZ(int x, int z);

    @Invoker("generateUpperHalfSphere")
    void uad_callGenerateUpperHalfSphere(WorldGenLevel level, BoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState state, boolean excludeAir);

    @Invoker("generateBox")
    void uad_callGenerateBox(WorldGenLevel level, BoundingBox box, int minX, int minY, int minZ, int maxX, int maxY, int maxZ, BlockState boundary, BlockState inside, boolean existingOnly);

    @Invoker("placeBlock")
    void uad_callPlaceBlock(WorldGenLevel level, BlockState state, int x, int y, int z, BoundingBox box);
}

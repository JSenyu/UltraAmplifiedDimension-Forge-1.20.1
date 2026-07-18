package com.telepathicgrunt.ultraamplifieddimension.mixin.structures;

import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.structures.MineshaftPieces;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(MineshaftPieces.MineShaftRoom.class)
public interface MineshaftRoomAccessor {
    @Accessor("childEntranceBoxes")
    List<BoundingBox> uad_getChildEntranceBoxes();
}

package com.telepathicgrunt.ultraamplifieddimension.world.features;

import com.mojang.serialization.Codec;
import com.telepathicgrunt.ultraamplifieddimension.UltraAmplifiedDimension;
import com.telepathicgrunt.ultraamplifieddimension.utils.GeneralUtils;
import com.telepathicgrunt.ultraamplifieddimension.world.features.configs.NbtDungeonConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Rotation;

import java.util.List;
import java.util.stream.Collectors;

public class NbtDungeon extends Feature<NbtDungeonConfig> {

    public NbtDungeon(Codec<NbtDungeonConfig> configFactory) {
        super(configFactory);
    }

    @Override
    public boolean place(FeaturePlaceContext<NbtDungeonConfig> context) {
        WorldGenLevel world = context.level();
        RandomSource random = context.random();
        BlockPos position = context.origin();
        NbtDungeonConfig config = context.config();

        ResourceLocation nbtRL = GeneralUtils.getRandomEntry(config.nbtResourcelocationsAndWeights, random);

        StructureTemplateManager structureManager = world.getLevel().getStructureManager();
        StructureTemplate template = structureManager.get(nbtRL).orElse(null);
        if (template == null) {
            UltraAmplifiedDimension.LOGGER.error("Identifier to the specified nbt file was not found! : {}", nbtRL);
            return false;
        }
        Rotation rotation = Rotation.getRandom(random);

        BlockPos halfLengths = new BlockPos(
                template.getSize().getX() / 2,
                template.getSize().getY() / 2,
                template.getSize().getZ() / 2);

        BlockPos fullLengths = new BlockPos(
                template.getSize().getX(),
                template.getSize().getY(),
                template.getSize().getZ());

        BlockPos halfLengthsRotated = new BlockPos(
                fullLengths.getX() / 2,
                fullLengths.getY() / 2,
                fullLengths.getZ() / 2);

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(position);
        ChunkAccess cachedChunk = world.getChunk(mutable);

        int xMin = -halfLengthsRotated.getX();
        int xMax = halfLengthsRotated.getX();
        int zMin = -halfLengthsRotated.getZ();
        int zMax = halfLengthsRotated.getZ();
        int wallOpenings = 0;
        int ceilingOpenings = 0;
        int ceiling = template.getSize().getY();

        for (int x = xMin; x <= xMax; x++) {
            for (int z = zMin; z <= zMax; z++) {
                for (int y = 0; y <= ceiling; y++) {
                    mutable.set(position).move(x, y, z);
                    if (mutable.getX() >> 4 != cachedChunk.getPos().x || mutable.getZ() >> 4 != cachedChunk.getPos().z) {
                        cachedChunk = world.getChunk(mutable);
                    }

                    BlockState state = cachedChunk.getBlockState(mutable);

                    if (config.airRequirementIsNowWater ? state.isAir() || state.getFluidState().is(FluidTags.LAVA) : !state.getFluidState().isEmpty()) {
                        return false;
                    } else if (!GeneralUtils.isFullCube(world, mutable, state)) {
                        if (y == 0 && !state.canOcclude()) {
                            return false;
                        } else if (state.is(BlockTags.LEAVES)) {
                            continue;
                        } else if (y == ceiling) {
                            ceilingOpenings++;
                        }
                    }

                    if ((x == xMin || x == xMax || z == zMin || z == zMax) && y == 1 && isValidNonSolidBlock(config, state)) {
                        BlockState aboveState = cachedChunk.getBlockState(mutable);
                        if (config.airRequirementIsNowWater ?
                                !aboveState.getFluidState().isEmpty() :
                                aboveState.isAir()) {
                            wallOpenings++;
                        }
                    }

                    if (wallOpenings > config.maxAirSpace || ceilingOpenings > config.maxAirSpace) {
                        return false;
                    }
                }
            }
        }

        if (wallOpenings >= config.minAirSpace) {
            position = position.above(config.structureYOffset);

            StructurePlaceSettings placementSettings = new StructurePlaceSettings()
                    .setRotation(rotation)
                    .setRotationPivot(halfLengths)
                    .setIgnoreEntities(false);

            var processorRegistry = world.registryAccess().registryOrThrow(Registries.PROCESSOR_LIST);
            ResourceKey<StructureProcessorList> emptyProcessorKey = ResourceKey.create(Registries.PROCESSOR_LIST, ResourceLocation.withDefaultNamespace("empty"));

            processorRegistry.getHolder(ResourceKey.create(Registries.PROCESSOR_LIST, config.processor))
                    .or(() -> processorRegistry.getHolder(emptyProcessorKey))
                    .ifPresent(holder -> holder.value().list().forEach(placementSettings::addProcessor));

            BlockPos placePos = mutable.set(position).move(-halfLengths.getX(), 0, -halfLengths.getZ());
            template.placeInWorld(world, placePos, placePos, placementSettings, random, 2);

            placementSettings.clearProcessors();
            processorRegistry.getHolder(ResourceKey.create(Registries.PROCESSOR_LIST, config.postProcessor))
                    .or(() -> processorRegistry.getHolder(emptyProcessorKey))
                    .ifPresent(holder -> holder.value().list().forEach(placementSettings::addProcessor));

            if (!placementSettings.getProcessors().isEmpty()) {
                List<StructureTemplate.StructureBlockInfo> blockInfos = template.filterBlocks(placePos, placementSettings, Blocks.STRUCTURE_VOID);
                blockInfos = StructureTemplate.processBlockInfos(world, placePos, placePos, placementSettings, blockInfos);
                for (StructureTemplate.StructureBlockInfo blockInfo : blockInfos) {
                    world.setBlock(blockInfo.pos(), blockInfo.state(), 2);
                    if (blockInfo.nbt() != null) {
                        BlockEntity blockEntity = world.getBlockEntity(blockInfo.pos());
                        if (blockEntity != null) {
                            blockEntity.load(blockInfo.nbt());
                        }
                    }
                }
            }

            spawnLootBlocks(world, random, position, config, fullLengths, halfLengthsRotated, mutable);
            return true;
        }

        return false;
    }

    private boolean isValidNonSolidBlock(NbtDungeonConfig config, BlockState state) {
        if (config.airRequirementIsNowWater) {
            return !state.getFluidState().isEmpty();
        }
        return state.isAir();
    }

    private void setMobSpawnerEntity(RandomSource random, NbtDungeonConfig config, SpawnerBlockEntity blockEntity) {
        EntityType<?> entity = GeneralUtils.getRandomEntry(config.spawnerResourcelocationsAndWeights, random);
        if (entity != null) {
            blockEntity.setEntityId(entity, random);
        } else {
            UltraAmplifiedDimension.LOGGER.warn("EntityType in a dungeon does not exist in registry! : {}",
                    config.spawnerResourcelocationsAndWeights.stream()
                            .map(n -> BuiltInRegistries.ENTITY_TYPE.getKey(n.getFirst()).toString())
                            .collect(Collectors.joining(", ", "{", "}")));
        }
    }

    private void solidifyBlock(WorldGenLevel world, BlockPos pos) {
        BlockState blockBelow = world.getBlockState(pos);
        if (blockBelow.hasProperty(SlabBlock.TYPE)) {
            world.setBlock(pos, blockBelow.setValue(SlabBlock.TYPE, SlabType.DOUBLE), 3);
        }
    }

    private void spawnLootBlocks(WorldGenLevel world, RandomSource random, BlockPos position, NbtDungeonConfig config, BlockPos fullLengths, BlockPos halfLengths, BlockPos.MutableBlockPos mutable) {
        boolean isPlacingChestLikeBlock = config.lootBlock.getBlock() instanceof ChestBlock;

        for (int currentChestCount = 0; currentChestCount < config.maxNumOfChests; ++currentChestCount) {
            for (int currentChestAttempt = 0; currentChestAttempt < fullLengths.getX() + fullLengths.getZ() + halfLengths.getY(); ++currentChestAttempt) {
                if (currentChestCount == config.maxNumOfChests) {
                    return;
                }

                mutable.set(position).move(
                        random.nextInt(Math.max(fullLengths.getX() - 2, 1)) - halfLengths.getX() + 1,
                        random.nextInt(Math.max(fullLengths.getY() - 1, 1)),
                        random.nextInt(Math.max(fullLengths.getZ() - 2, 1)) - halfLengths.getZ() + 1);

                BlockState currentBlock = world.getBlockState(mutable);
                if (isValidNonSolidBlock(config, currentBlock)) {
                    if (world.getBlockState(mutable.move(Direction.DOWN)).isFaceSturdy(world, mutable, Direction.UP)) {
                        mutable.move(Direction.UP);
                        boolean isOnWall = false;

                        for (Direction neighborDirection : Direction.Plane.HORIZONTAL) {
                            mutable.move(neighborDirection);
                            BlockState neighboringState = world.getBlockState(mutable);
                            mutable.move(neighborDirection.getOpposite());

                            if (isPlacingChestLikeBlock && neighboringState.getBlock() instanceof ChestBlock) {
                                if (neighboringState.getValue(ChestBlock.TYPE) == ChestType.SINGLE) {
                                    BlockState currentStateForChest = GeneralUtils.orientateChest(world, mutable, config.lootBlock);
                                    Direction currentDirection = currentStateForChest.getValue(HorizontalDirectionalBlock.FACING);

                                    if (neighborDirection.getAxis() == currentDirection.getAxis()) {
                                        currentDirection = currentDirection.getClockWise();
                                        BlockPos wallCheckPos = mutable.relative(currentDirection);
                                        BlockPos wallCheckPos2 = wallCheckPos.relative(neighborDirection);
                                        BlockState blockState = world.getBlockState(wallCheckPos);
                                        BlockState blockState2 = world.getBlockState(wallCheckPos2);

                                        if ((blockState.canOcclude() && !(blockState.getBlock() instanceof SpawnerBlock)) ||
                                                (blockState2.canOcclude() && !(blockState2.getBlock() instanceof SpawnerBlock))) {
                                            currentDirection = currentDirection.getOpposite();
                                        }
                                    }

                                    boolean chestTyping = neighborDirection.getAxisDirection() == currentDirection.getAxisDirection();
                                    if (neighborDirection.getAxis() == Direction.Axis.Z) {
                                        chestTyping = !chestTyping;
                                    }

                                    world.setBlock(mutable,
                                            config.lootBlock
                                                    .setValue(ChestBlock.WATERLOGGED, currentBlock.getFluidState().is(FluidTags.WATER))
                                                    .setValue(ChestBlock.FACING, currentDirection)
                                                    .setValue(ChestBlock.TYPE, chestTyping ? ChestType.RIGHT : ChestType.LEFT),
                                            2);

                                    world.setBlock(mutable.relative(neighborDirection),
                                            neighboringState
                                                    .setValue(ChestBlock.FACING, currentDirection)
                                                    .setValue(ChestBlock.TYPE, chestTyping ? ChestType.LEFT : ChestType.RIGHT),
                                            2);

                                    RandomizableContainerBlockEntity.setLootTable(world, random, mutable, config.chestIdentifier);
                                    solidifyBlock(world, mutable.below());

                                    currentChestCount++;
                                    isOnWall = false;
                                    break;
                                }
                            } else if (GeneralUtils.isFullCube(world, mutable, neighboringState) && !(neighboringState.getBlock() instanceof SpawnerBlock)) {
                                isOnWall = true;
                            }
                        }

                        if (isOnWall) {
                            BlockState lootBlock = config.lootBlock;
                            if (lootBlock.hasProperty(BlockStateProperties.WATERLOGGED)) {
                                lootBlock = lootBlock.setValue(BlockStateProperties.WATERLOGGED, currentBlock.getFluidState().is(FluidTags.WATER));
                            }
                            if (lootBlock.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                                lootBlock = GeneralUtils.orientateChest(world, mutable, lootBlock);
                            }

                            world.setBlock(mutable, lootBlock, 2);
                            RandomizableContainerBlockEntity.setLootTable(world, random, mutable, config.chestIdentifier);

                            mutable.move(Direction.DOWN);
                            if (lootBlock.is(Blocks.SHULKER_BOX)) {
                                world.setBlock(mutable, Blocks.SPAWNER.defaultBlockState(), 2);
                                BlockEntity blockEntity = world.getBlockEntity(mutable);
                                if (blockEntity instanceof SpawnerBlockEntity spawnerBlockEntity) {
                                    setMobSpawnerEntity(random, config, spawnerBlockEntity);
                                }
                            } else {
                                solidifyBlock(world, mutable);
                            }

                            currentChestCount++;
                            break;
                        }
                    }
                }
            }
        }
    }
}

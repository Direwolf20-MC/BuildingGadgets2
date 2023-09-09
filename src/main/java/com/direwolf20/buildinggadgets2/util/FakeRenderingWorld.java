package com.direwolf20.buildinggadgets2.util;

import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkSource;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.LevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Predicate;

public class FakeRenderingWorld implements LevelAccessor {
    public final HashMap<BlockPos, BlockState> positions = new HashMap<>();
    private Level realWorld;
    private BlockPos lookingAt;

    public FakeRenderingWorld(Level rWorld, ArrayList<StatePos> coordinates, BlockPos lookingAt) {
        this.realWorld = rWorld;
        this.lookingAt = lookingAt;
        for (StatePos statePos : coordinates) {
            this.setBlock(statePos.pos, statePos.state, 0);
        }
        for (StatePos statePos : coordinates) {
            try {
                BlockState adjustedState = Block.updateFromNeighbourShapes(statePos.state, this, statePos.pos);
                this.setBlock(statePos.pos, adjustedState, 0);
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

    @Nullable
    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        BlockState blockState = getBlockState(pos);
        if (blockState.hasBlockEntity()) {
            BlockEntity blockEntity = ((EntityBlock) blockState.getBlock()).newBlockEntity(pos.offset(lookingAt), blockState);
            blockEntity.setLevel(this.realWorld);
            return blockEntity;
        }
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return positions.containsKey(pos) ? positions.get(pos) : realWorld.getBlockState(pos.offset(lookingAt));
    }

    public BlockState getBlockStateWithoutReal(BlockPos pos) {
        return positions.containsKey(pos) ? positions.get(pos) : Blocks.AIR.defaultBlockState();
    }

    @Override
    public void scheduleTick(BlockPos p_186461_, Block p_186462_, int p_186463_) {
        //noOp
    }

    @Override
    public boolean setBlock(BlockPos p_46944_, BlockState p_46945_, int p_46946_) {
        return this.setBlock(p_46944_, p_46945_, p_46946_, 512);
    }

    @Override
    public boolean setBlock(BlockPos pos, BlockState state, int p_46949_, int p_46950_) {
        positions.put(pos, state);
        return true;
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return Fluids.EMPTY.defaultFluidState();
    }

    @Override
    public int getHeight() {
        return realWorld.getHeight();
    }

    @Override
    public RegistryAccess registryAccess() {
        return null;
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return null;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public ChunkAccess getChunk(int p_46823_, int p_46824_, ChunkStatus p_46825_, boolean p_46826_) {
        return null;
    }

    @Override
    public int getHeight(Heightmap.Types p_46827_, int p_46828_, int p_46829_) {
        return 0;
    }

    @Override
    public int getSkyDarken() {
        return 0;
    }

    @Override
    public BiomeManager getBiomeManager() {
        return realWorld.getBiomeManager();
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int p_204159_, int p_204160_, int p_204161_) {
        return null;
    }

    @Override
    public boolean isClientSide() {
        return true;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public DimensionType dimensionType() {
        return realWorld.dimensionType();
    }

    @Override
    public int getMinBuildHeight() {
        return realWorld.getMinBuildHeight();
    }

    @Override
    public long nextSubTickCount() {
        return 0;
    }

    @Override
    public LevelTickAccess<Block> getBlockTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public LevelTickAccess<Fluid> getFluidTicks() {
        return BlackholeTickAccess.emptyLevelList();
    }

    @Override
    public LevelData getLevelData() {
        return this.realWorld.getLevelData();
    }

    @Override
    public DifficultyInstance getCurrentDifficultyAt(BlockPos p_46800_) {
        return null;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public MinecraftServer getServer() {
        return null;
    }

    @Override
    public ChunkSource getChunkSource() {
        return null;
    }

    @Override
    public RandomSource getRandom() {
        return null;
    }

    @Override
    public void playSound(@org.jetbrains.annotations.Nullable Player p_46775_, BlockPos p_46776_, SoundEvent p_46777_, SoundSource p_46778_, float p_46779_, float p_46780_) {

    }

    @Override
    public void addParticle(ParticleOptions p_46783_, double p_46784_, double p_46785_, double p_46786_, double p_46787_, double p_46788_, double p_46789_) {

    }

    @Override
    public void levelEvent(@org.jetbrains.annotations.Nullable Player p_46771_, int p_46772_, BlockPos p_46773_, int p_46774_) {

    }

    @Override
    public void gameEvent(GameEvent p_220404_, Vec3 p_220405_, GameEvent.Context p_220406_) {

    }

    @Override
    public float getShade(Direction pDirection, boolean pShade) {
        ClientLevel clientLevel = (ClientLevel) realWorld;
        boolean flag = clientLevel.effects().constantAmbientLight();
        if (!pShade) {
            return flag ? 0.9F : 1.0F;
        } else {
            switch (pDirection) {
                case DOWN:
                    return flag ? 0.9F : 0.5F;
                case UP:
                    return flag ? 0.9F : 1.0F;
                case NORTH:
                case SOUTH:
                    return 0.8F;
                case WEST:
                case EAST:
                    return 0.6F;
                default:
                    return 1.0F;
            }
        }
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return Minecraft.getInstance().level.getLightEngine();
    }

    @Override
    public WorldBorder getWorldBorder() {
        return null;
    }

    @Override
    public List<Entity> getEntities(@org.jetbrains.annotations.Nullable Entity p_45936_, AABB p_45937_, Predicate<? super Entity> p_45938_) {
        return null;
    }

    @Override
    public <T extends Entity> List<T> getEntities(EntityTypeTest<Entity, T> p_151464_, AABB p_151465_, Predicate<? super T> p_151466_) {
        return null;
    }

    @Override
    public List<? extends Player> players() {
        return null;
    }

    @Override
    public boolean isStateAtPosition(BlockPos p_46938_, Predicate<BlockState> p_46939_) {
        return p_46939_.test(getBlockState(p_46938_));
    }

    @Override
    public boolean isFluidAtPosition(BlockPos p_151584_, Predicate<FluidState> p_151585_) {
        return false;
    }

    @Override
    public boolean removeBlock(BlockPos p_46951_, boolean p_46952_) {
        return false;
    }

    @Override
    public boolean destroyBlock(BlockPos p_46957_, boolean p_46958_, @org.jetbrains.annotations.Nullable Entity p_46959_, int p_46960_) {
        return false;
    }
}

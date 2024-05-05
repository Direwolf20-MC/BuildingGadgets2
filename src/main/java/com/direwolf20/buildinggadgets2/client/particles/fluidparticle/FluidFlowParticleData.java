package com.direwolf20.buildinggadgets2.client.particles.fluidparticle;

import com.direwolf20.buildinggadgets2.client.particles.ModParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.fluids.FluidStack;

import javax.annotation.Nonnull;

public class FluidFlowParticleData implements ParticleOptions {
    public static final MapCodec<FluidFlowParticleData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    FluidStack.CODEC.fieldOf("fluidStack").forGetter(p -> p.fluidStack),
                    Codec.BOOL.fieldOf("doGravity").forGetter(p -> p.doGravity),
                    Codec.BOOL.fieldOf("shrinking").forGetter(p -> p.shrinking)
            ).apply(instance, FluidFlowParticleData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidFlowParticleData> STREAM_CODEC = StreamCodec.composite(
            FluidStack.STREAM_CODEC,
            FluidFlowParticleData::getFluidStack,
            ByteBufCodecs.BOOL,
            FluidFlowParticleData::isDoGravity,
            ByteBufCodecs.BOOL,
            FluidFlowParticleData::isShrinking,
            FluidFlowParticleData::new
    );

    private final FluidStack fluidStack;
    public final boolean doGravity;
    public final boolean shrinking;

    public FluidFlowParticleData(FluidStack fluidStack, boolean doGravity, boolean shrinking) {
        this.fluidStack = fluidStack.copy(); //Forge: Fix stack updating after the fact causing particle changes.
        this.doGravity = doGravity;
        this.shrinking = shrinking;
    }

    @Nonnull
    @Override
    public ParticleType<FluidFlowParticleData> getType() {
        return ModParticles.FLUIDFLOWPARTICLE.get();
    }


    public FluidStack getFluidStack() {
        return this.fluidStack;
    }

    public boolean isDoGravity() {
        return doGravity;
    }

    public boolean isShrinking() {
        return shrinking;
    }
}

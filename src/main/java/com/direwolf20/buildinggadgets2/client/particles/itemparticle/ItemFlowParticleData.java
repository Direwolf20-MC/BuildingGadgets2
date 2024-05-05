package com.direwolf20.buildinggadgets2.client.particles.itemparticle;

import com.direwolf20.buildinggadgets2.client.particles.ModParticles;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ItemFlowParticleData implements ParticleOptions {
    public static final MapCodec<ItemFlowParticleData> MAP_CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    ItemStack.CODEC.fieldOf("itemStack").forGetter(p -> p.itemStack),
                    Codec.BOOL.fieldOf("doGravity").forGetter(p -> p.doGravity),
                    Codec.BOOL.fieldOf("shrinking").forGetter(p -> p.shrinking)
            ).apply(instance, ItemFlowParticleData::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ItemFlowParticleData> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC,
            ItemFlowParticleData::getItemStack,
            ByteBufCodecs.BOOL,
            ItemFlowParticleData::isDoGravity,
            ByteBufCodecs.BOOL,
            ItemFlowParticleData::isShrinking,
            ItemFlowParticleData::new
    );

    private final ItemStack itemStack;
    public final boolean doGravity;
    public final boolean shrinking;

    public ItemFlowParticleData(ItemStack itemStack, boolean doGravity, boolean shrinking) {
        this.itemStack = itemStack.copy(); //Forge: Fix stack updating after the fact causing particle changes.
        this.doGravity = doGravity;
        this.shrinking = shrinking;
    }

    @Nonnull
    @Override
    public ParticleType<ItemFlowParticleData> getType() {
        return ModParticles.ITEMFLOWPARTICLE.get();
    }

    public ItemStack getItemStack() {
        return this.itemStack;
    }

    public boolean isDoGravity() {
        return doGravity;
    }

    public boolean isShrinking() {
        return shrinking;
    }
}


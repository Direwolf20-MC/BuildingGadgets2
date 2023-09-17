package com.direwolf20.buildinggadgets2.client.particles.fluidparticle;

import com.direwolf20.buildinggadgets2.client.particles.ModParticles;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.arguments.item.ItemInput;
import net.minecraft.commands.arguments.item.ItemParser;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nonnull;
import java.util.Locale;

public class FluidFlowParticleData implements ParticleOptions {
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

    @Override
    public void writeToNetwork(FriendlyByteBuf buffer) {
        buffer.writeFluidStack(this.fluidStack);
    }

    @Nonnull
    @Override
    public String writeToString() {
        return String.format(Locale.ROOT, "%s %b %b",
                this.getType(), this.doGravity, this.shrinking);
    }

    @OnlyIn(Dist.CLIENT)
    public FluidStack getFluidStack() {
        return this.fluidStack;
    }

    public static final Deserializer<FluidFlowParticleData> DESERIALIZER = new Deserializer<FluidFlowParticleData>() {
        @Nonnull
        @Override
        public FluidFlowParticleData fromCommand(ParticleType<FluidFlowParticleData> particleTypeIn, StringReader reader) throws CommandSyntaxException {
            reader.expect(' ');
            ItemParser.ItemResult itemparser$itemresult = ItemParser.parseForItem(BuiltInRegistries.ITEM.asLookup(), reader);
            ItemStack itemstack = (new ItemInput(itemparser$itemresult.item(), itemparser$itemresult.nbt())).createItemStack(1, false);


            reader.expect(' ');
            boolean doGravity = reader.readBoolean();
            reader.expect(' ');
            boolean building = reader.readBoolean();
            return new FluidFlowParticleData(FluidStack.EMPTY, doGravity, building);
        }

        @Override
        public FluidFlowParticleData fromNetwork(ParticleType<FluidFlowParticleData> particleTypeIn, FriendlyByteBuf buffer) {
            return new FluidFlowParticleData(buffer.readFluidStack(), buffer.readBoolean(), buffer.readBoolean());
        }
    };
}

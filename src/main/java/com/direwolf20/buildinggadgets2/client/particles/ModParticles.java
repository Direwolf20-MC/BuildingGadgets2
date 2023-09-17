package com.direwolf20.buildinggadgets2.client.particles;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.client.particles.fluidparticle.FluidFlowParticleData;
import com.direwolf20.buildinggadgets2.client.particles.fluidparticle.FluidFlowParticleType;
import com.direwolf20.buildinggadgets2.client.particles.itemparticle.ItemFlowParticleData;
import com.direwolf20.buildinggadgets2.client.particles.itemparticle.ItemFlowParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, BuildingGadgets2.MODID);
    public static final RegistryObject<ParticleType<ItemFlowParticleData>> ITEMFLOWPARTICLE = PARTICLE_TYPES.register("itemflowparticle", ItemFlowParticleType::new);
    public static final RegistryObject<ParticleType<FluidFlowParticleData>> FLUIDFLOWPARTICLE = PARTICLE_TYPES.register("fluidflowparticle", FluidFlowParticleType::new);
}
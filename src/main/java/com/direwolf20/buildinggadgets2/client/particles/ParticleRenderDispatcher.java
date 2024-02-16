package com.direwolf20.buildinggadgets2.client.particles;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.client.particles.fluidparticle.FluidFlowParticle;
import com.direwolf20.buildinggadgets2.client.particles.itemparticle.ItemFlowParticle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@Mod.EventBusSubscriber(modid = BuildingGadgets2.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ParticleRenderDispatcher {

    @SubscribeEvent
    public static void registerProviders(RegisterParticleProvidersEvent evt) {
        evt.registerSpecial(ModParticles.ITEMFLOWPARTICLE.get(), ItemFlowParticle.FACTORY);
        evt.registerSpecial(ModParticles.FLUIDFLOWPARTICLE.get(), FluidFlowParticle.FACTORY);
    }
}

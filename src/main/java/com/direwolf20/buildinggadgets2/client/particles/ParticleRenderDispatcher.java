package com.direwolf20.buildinggadgets2.client.particles;

import com.direwolf20.buildinggadgets2.api.BuildingGadgets2Api;
import com.direwolf20.buildinggadgets2.client.particles.fluidparticle.FluidFlowParticle;
import com.direwolf20.buildinggadgets2.client.particles.itemparticle.ItemFlowParticle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = BuildingGadgets2Api.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ParticleRenderDispatcher {

    @SubscribeEvent
    public static void registerProviders(RegisterParticleProvidersEvent evt) {
        evt.registerSpecial(ModParticles.ITEMFLOWPARTICLE.get(), ItemFlowParticle.FACTORY);
        evt.registerSpecial(ModParticles.FLUIDFLOWPARTICLE.get(), FluidFlowParticle.FACTORY);
    }
}

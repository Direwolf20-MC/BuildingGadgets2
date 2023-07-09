package com.direwolf20.buildinggadgets2.client;

import com.direwolf20.buildinggadgets2.common.BuildingGadgets2;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public interface OurSounds {
    DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, BuildingGadgets2.MODID);

    RegistryObject<SoundEvent> BEEP = REGISTRY.register("beep", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(BuildingGadgets2.MODID, "beep")));

    static void playSound(SoundEvent sound, float pitch) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch));
    }

    static void playSound(SoundEvent sound) {
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, 1.0f));
    }
}

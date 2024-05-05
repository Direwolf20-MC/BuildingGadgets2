package com.direwolf20.buildinggadgets2.common.network.handler;

import com.direwolf20.buildinggadgets2.api.gadgets.GadgetModes;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.network.data.ModeSwitchPayload;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.modes.BaseMode;
import com.google.common.collect.ImmutableSortedSet;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketModeSwitch {
    public static final PacketModeSwitch INSTANCE = new PacketModeSwitch();

    public static PacketModeSwitch get() {
        return INSTANCE;
    }

    public void handle(final ModeSwitchPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack gadgetStack = BaseGadget.getGadget(player);
            if (gadgetStack.isEmpty()) return;

            // This is safe as it's checked higher up
            BaseGadget actualGadget = (BaseGadget) gadgetStack.getItem();
            if (payload.rotate()) {
                actualGadget.rotateModes(gadgetStack);
            }

            ResourceLocation modeId = payload.modeId();
            ImmutableSortedSet<BaseMode> modesForGadget = GadgetModes.INSTANCE.getModesForGadget(actualGadget.gadgetTarget());

            var modeToUse = modesForGadget
                    .stream()
                    .filter(e -> e.getId().equals(modeId))
                    .findFirst()
                    .orElse(modesForGadget.first());

            GadgetNBT.setMode(gadgetStack, modeToUse);
        });
    }
}

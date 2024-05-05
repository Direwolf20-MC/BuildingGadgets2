package com.direwolf20.buildinggadgets2.common.network.handler;

import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.network.data.DestructionRangesPayload;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketDestructionRanges {
    public static final PacketDestructionRanges INSTANCE = new PacketDestructionRanges();

    public static PacketDestructionRanges get() {
        return INSTANCE;
    }

    public void handle(final DestructionRangesPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack gadgetStack = BaseGadget.getGadget(player);
            if (gadgetStack.isEmpty()) return;

            GadgetNBT.setToolValue(gadgetStack, payload.left(), GadgetNBT.IntSettings.LEFT.getName());
            GadgetNBT.setToolValue(gadgetStack, payload.right(), GadgetNBT.IntSettings.RIGHT.getName());
            GadgetNBT.setToolValue(gadgetStack, payload.up(), GadgetNBT.IntSettings.UP.getName());
            GadgetNBT.setToolValue(gadgetStack, payload.down(), GadgetNBT.IntSettings.DOWN.getName());
            GadgetNBT.setToolValue(gadgetStack, payload.depth(), GadgetNBT.IntSettings.DEPTH.getName());
        });
    }
}

package com.direwolf20.buildinggadgets2.common.network.handler;

import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.network.data.ToggleSettingPayload;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class PacketToggleSetting {
    public static final PacketToggleSetting INSTANCE = new PacketToggleSetting();

    public static PacketToggleSetting get() {
        return INSTANCE;
    }

    public void handle(final ToggleSettingPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack gadgetStack = BaseGadget.getGadget(player);
            if (gadgetStack.isEmpty()) return;

            GadgetNBT.toggleSetting(gadgetStack, payload.setting());
        });
    }
}

package com.direwolf20.buildinggadgets2.common.network.handler.gadgetaction;

import com.direwolf20.buildinggadgets2.common.network.data.GadgetActionPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

public record GadgetActionContext(
        PlayPayloadContext context,
        GadgetActionPayload payload,
        ItemStack gadget,
        Player player
) {
}

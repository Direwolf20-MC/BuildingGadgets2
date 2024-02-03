package com.direwolf20.buildinggadgets2.common.network.newpackets.handler;

import com.direwolf20.buildinggadgets2.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets2.common.network.newpackets.data.SendCopyDataPayload;
import com.direwolf20.buildinggadgets2.common.network.newpackets.data.SendPasteBatchesPayload;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;

import java.util.ArrayList;
import java.util.Objects;

public class PacketSendPasteBatches {
    public static final PacketSendPasteBatches INSTANCE = new PacketSendPasteBatches();

    public static PacketSendPasteBatches get() {
        return INSTANCE;
    }

    public void handle(final SendPasteBatchesPayload payload, final PlayPayloadContext context) {
        context.workHandler().submitAsync(() -> {
            var sender = context.player();
            if (sender.isEmpty()) {
                return;
            }

            AbstractContainerMenu container = sender.get().containerMenu;
            if (container == null || !(container instanceof TemplateManagerContainer))
                return;

            ItemStack templateStack = container.getSlot(1).getItem();

            if (templateStack.isEmpty())
                return;

            if (templateStack.is(Items.PAPER)) {
                container.setItem(1, container.getStateId(), new ItemStack(Registration.Template.get()));
                templateStack = container.getSlot(1).getItem();
            }

            BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(sender.get().level().getServer()).overworld());
            boolean complete = bg2Data.addToPasteChunks(payload.copyUUID(), payload.position(), payload.numberOfPackets(), payload.data());
            if (!complete) {
                return;
            }

            CompoundTag assembledTag = bg2Data.getAssembledTag(payload.copyUUID());
            ArrayList<StatePos> buildList = BG2Data.statePosListFromNBTMapArray(assembledTag);
            bg2Data.addToCopyPaste(GadgetNBT.getUUID(templateStack), buildList);
            GadgetNBT.setCopyUUID(templateStack, payload.copyUUID());

            //Update the client - Yes - even though this came from the client!! This is to make sure the server sanity checked the blocks list
            CompoundTag tag = bg2Data.getCopyPasteListAsNBTMap(GadgetNBT.getUUID(templateStack), false);
            ((ServerPlayer) sender.get()).connection.send(new SendCopyDataPayload(GadgetNBT.getUUID(templateStack), GadgetNBT.getCopyUUID(templateStack), tag));
        });
    }
}

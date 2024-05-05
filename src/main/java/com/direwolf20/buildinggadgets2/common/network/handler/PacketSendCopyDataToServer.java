package com.direwolf20.buildinggadgets2.common.network.handler;

import com.direwolf20.buildinggadgets2.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.network.data.SendCopyDataPayload;
import com.direwolf20.buildinggadgets2.common.network.data.SendCopyDataToServerPayload;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.ArrayList;
import java.util.Objects;

public class PacketSendCopyDataToServer {
    public static final PacketSendCopyDataToServer INSTANCE = new PacketSendCopyDataToServer();

    public static PacketSendCopyDataToServer get() {
        return INSTANCE;
    }

    public void handle(final SendCopyDataToServerPayload payload, final IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            ItemStack gadgetStack = BaseGadget.getGadget(player);
            if (gadgetStack.isEmpty()) return;

            AbstractContainerMenu container = context.player().containerMenu;
            if (container == null || !(container instanceof TemplateManagerContainer))
                return;

            ItemStack templateStack = container.getSlot(1).getItem();

            if (templateStack.isEmpty())
                return;

            if (templateStack.is(Items.PAPER)) {
                container.setItem(1, container.getStateId(), new ItemStack(Registration.Template.get()));
                templateStack = container.getSlot(1).getItem();
            }

            BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(context.player().level().getServer()).overworld());

            ArrayList<StatePos> buildList = BG2Data.statePosListFromNBTMapArray(payload.compoundTag());
            bg2Data.addToCopyPaste(GadgetNBT.getUUID(templateStack), buildList);
            GadgetNBT.setCopyUUID(templateStack);

            //Update the client - Yes - even though this came from the client!! This is to make sure the server sanity checked the blocks list
            CompoundTag tag = bg2Data.getCopyPasteListAsNBTMap(GadgetNBT.getUUID(templateStack), false);

            ((ServerPlayer) context.player()).connection.send(
                    new SendCopyDataPayload(
                            GadgetNBT.getUUID(templateStack),
                            GadgetNBT.getCopyUUID(templateStack),
                            tag
                    )
            );
        });
    }
}

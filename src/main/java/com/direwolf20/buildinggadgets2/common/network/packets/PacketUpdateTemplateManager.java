package com.direwolf20.buildinggadgets2.common.network.packets;

import com.direwolf20.buildinggadgets2.common.containers.TemplateManagerContainer;
import com.direwolf20.buildinggadgets2.common.network.PacketHandler;
import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
import com.direwolf20.buildinggadgets2.setup.Registration;
import com.direwolf20.buildinggadgets2.util.GadgetNBT;
import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Supplier;

public class PacketUpdateTemplateManager {
    BlockPos blockPos;
    int mode;

    public PacketUpdateTemplateManager(BlockPos blockPos, int mode) {
        this.blockPos = blockPos;
        this.mode = mode;
    }

    public static PacketUpdateTemplateManager decode(FriendlyByteBuf buf) {
        return new PacketUpdateTemplateManager(buf.readBlockPos(), buf.readInt());
    }

    public static void encode(PacketUpdateTemplateManager message, FriendlyByteBuf buf) {
        buf.writeBlockPos(message.blockPos);
        buf.writeInt(message.mode);
    }

    public static void copyData(ServerPlayer sender, ItemStack sourceStack, ItemStack targetStack) {
        UUID sourceUUID = GadgetNBT.getUUID(sourceStack);
        BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(sender.level().getServer()).overworld());
        ArrayList<StatePos> buildList = bg2Data.getCopyPasteList(sourceUUID, false);
        UUID targetUUID = GadgetNBT.getUUID(targetStack);
        GadgetNBT.setCopyUUID(targetStack); //This UUID will be used to determine if the copy/paste we are rendering from the cache is old or not.
        bg2Data.addToCopyPaste(targetUUID, buildList);

        //Ensure client has the updated values for both objects
        CompoundTag tag = bg2Data.getCopyPasteListAsNBTMap(sourceUUID, false);
        PacketHandler.sendTo(new PacketSendCopyData(sourceUUID, GadgetNBT.getCopyUUID(sourceStack), tag), sender);
        tag = bg2Data.getCopyPasteListAsNBTMap(targetUUID, false);
        PacketHandler.sendTo(new PacketSendCopyData(targetUUID, GadgetNBT.getCopyUUID(targetStack), tag), sender);
    }

    public static void handle(PacketUpdateTemplateManager message, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer sender = context.get().getSender();
            if (sender == null) {
                return;
            }

            AbstractContainerMenu container = sender.containerMenu;
            if (container == null || !(container instanceof TemplateManagerContainer))
                return;

            ItemStack gadgetStack = container.getSlot(0).getItem();
            ItemStack templateStack = container.getSlot(1).getItem();
            if (message.mode == 0) { //Save
                if (templateStack.isEmpty())
                    return;

                if (templateStack.is(Items.PAPER)) {
                    container.setItem(1, container.getStateId(), new ItemStack(Registration.Template.get()));
                    templateStack = container.getSlot(1).getItem();
                }

                //Todo Template Name

                if (gadgetStack.isEmpty())
                    return;

                copyData(sender, gadgetStack, templateStack);
            } else if (message.mode == 1) { //Load
                if (templateStack.isEmpty() || gadgetStack.isEmpty())
                    return;

                copyData(sender, templateStack, gadgetStack);
            }
        });

        context.get().setPacketHandled(true);
    }
}

//package com.direwolf20.buildinggadgets2.common.network.packets;
//
//import com.direwolf20.buildinggadgets2.common.events.ServerTickHandler;
//import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
//import com.direwolf20.buildinggadgets2.common.items.GadgetCutPaste;
//import com.direwolf20.buildinggadgets2.common.worlddata.BG2Data;
//import com.direwolf20.buildinggadgets2.util.GadgetNBT;
//import com.direwolf20.buildinggadgets2.util.datatypes.StatePos;
//import com.direwolf20.buildinggadgets2.util.datatypes.TagPos;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.network.chat.Component;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.item.ItemStack;
//import net.minecraftforge.network.NetworkEvent;
//
//import java.util.ArrayList;
//import java.util.Objects;
//import java.util.UUID;
//import java.util.function.Supplier;
//
//public class PacketRotate {
//    public PacketRotate() {
//    }
//
//    public static PacketRotate decode(FriendlyByteBuf buf) {
//        return new PacketRotate();
//    }
//
//    public static void encode(PacketRotate message, FriendlyByteBuf buf) {
//
//    }
//
//    public static void handle(PacketRotate message, Supplier<NetworkEvent.Context> context) {
//        context.get().enqueueWork(() -> {
//            ServerPlayer sender = context.get().getSender();
//            if (sender == null) {
//                return;
//            }
//
//            ItemStack gadget = BaseGadget.getGadget(sender);
//            if (gadget.isEmpty() || !(gadget.getItem() instanceof BaseGadget actualGadget)) {
//                return;
//            }
//
//            UUID gadgetUUID = GadgetNBT.getUUID(gadget);
//
//            if (gadget.getItem() instanceof GadgetCutPaste) {
//                if (ServerTickHandler.gadgetWorking(gadgetUUID)) {
//                    sender.displayClientMessage(Component.translatable("buildinggadgets2.messages.cutinprogress"), true);
//                    return; //If the gadget is mid cut, don't sync data
//                }
//            }
//
//
//            BG2Data bg2Data = BG2Data.get(Objects.requireNonNull(sender.level().getServer()).overworld());
//
//            ArrayList<StatePos> currentPosList = bg2Data.getCopyPasteList(gadgetUUID, false);
//            ArrayList<TagPos> tagListMutable = bg2Data.peekTEMap(gadgetUUID);
//
//            ArrayList<StatePos> newPosList = StatePos.rotate90Degrees(currentPosList, tagListMutable);
//
//            bg2Data.addToCopyPaste(gadgetUUID, newPosList);
//            GadgetNBT.setCopyUUID(gadget);
//
//            //Handle TE Data - do nothing if null or empty
//            if (tagListMutable == null || tagListMutable.isEmpty()) return;
//            bg2Data.addToTEMap(gadgetUUID, tagListMutable);
//
//
//        });
//
//        context.get().setPacketHandled(true);
//    }
//}

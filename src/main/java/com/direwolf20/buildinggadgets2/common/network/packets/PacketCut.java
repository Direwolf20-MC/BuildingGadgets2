//package com.direwolf20.buildinggadgets2.common.network.packets;
//
//import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
//import com.direwolf20.buildinggadgets2.common.items.GadgetCutPaste;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.item.ItemStack;
//import net.minecraftforge.network.NetworkEvent;
//
//import java.util.function.Supplier;
//
//// PORTED!
//public class PacketCut {
//
//    public PacketCut() {
//    }
//
//    public static PacketCut decode(FriendlyByteBuf buf) {
//        return new PacketCut();
//    }
//
//    public static void encode(PacketCut message, FriendlyByteBuf buf) {
//    }
//
//    public static void handle(PacketCut message, Supplier<NetworkEvent.Context> context) {
//        context.get().enqueueWork(() -> {
//            ServerPlayer sender = context.get().getSender();
//            if (sender == null) {
//                return;
//            }
//
//            ItemStack gadget = BaseGadget.getGadget(sender);
//            if (gadget.isEmpty() || !(gadget.getItem() instanceof GadgetCutPaste actualGadget)) {
//                return;
//            }
//
//            actualGadget.cutAndStore(sender, gadget);
//        });
//
//        context.get().setPacketHandled(true);
//    }
//}

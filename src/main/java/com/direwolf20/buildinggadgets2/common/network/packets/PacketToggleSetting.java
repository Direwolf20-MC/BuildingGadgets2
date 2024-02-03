//package com.direwolf20.buildinggadgets2.common.network.packets;
//
//import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
//import com.direwolf20.buildinggadgets2.util.GadgetNBT;
//import net.minecraft.network.FriendlyByteBuf;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.item.ItemStack;
//import net.minecraftforge.network.NetworkEvent;
//
//import java.util.function.Supplier;
//
//public class PacketToggleSetting {
//    String setting;
//
//    public PacketToggleSetting(String setting) {
//        this.setting = setting;
//    }
//
//    public static PacketToggleSetting decode(FriendlyByteBuf buf) {
//        return new PacketToggleSetting(buf.readUtf());
//    }
//
//    public static void encode(PacketToggleSetting message, FriendlyByteBuf buf) {
//        buf.writeUtf(message.setting);
//    }
//
//    public static void handle(PacketToggleSetting message, Supplier<NetworkEvent.Context> context) {
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
//            String setting = message.setting;
//            GadgetNBT.toggleSetting(gadget, setting);
//            //sender.displayClientMessage(Component.literal(Character.toUpperCase(setting.charAt(0)) + setting.substring(1) + " changed to " + newSetting), true);
//        });
//
//        context.get().setPacketHandled(true);
//    }
//}

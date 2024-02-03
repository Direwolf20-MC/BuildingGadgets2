package com.direwolf20.buildinggadgets2.common.network;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.network.newpackets.handler.gadgetaction.PacketGadgetAction;
import com.direwolf20.buildinggadgets2.common.network.newpackets.data.GadgetActionPayload;
import com.direwolf20.buildinggadgets2.common.network.packets.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = Integer.toString(2);
    private static short index = 0;

    public static void registerNetworking(final RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar(BuildingGadgets2.MODID);

        registrar.play(GadgetActionPayload.ID, GadgetActionPayload::new, handler -> handler.server(PacketGadgetAction.get()::handle));
    }

    public static final SimpleChannel HANDLER = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(BuildingGadgets2.MODID, "main_network_channel"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    public static void register() {
        int id = 0;

        // Server side
//        HANDLER.registerMessage(id++, PacketModeSwitch.class, PacketModeSwitch::encode, PacketModeSwitch::decode, PacketModeSwitch::handle);
//        HANDLER.registerMessage(id++, PacketRangeChange.class, PacketRangeChange::encode, PacketRangeChange::decode, PacketRangeChange::handle);
//        HANDLER.registerMessage(id++, PacketRenderChange.class, PacketRenderChange::encode, PacketRenderChange::decode, PacketRenderChange::handle);
//        HANDLER.registerMessage(id++, PacketAnchor.class, PacketAnchor::encode, PacketAnchor::decode, PacketAnchor::handle);
//        HANDLER.registerMessage(id++, PacketUndo.class, PacketUndo::encode, PacketUndo::decode, PacketUndo::handle);
//        HANDLER.registerMessage(id++, PacketCut.class, PacketCut::encode, PacketCut::decode, PacketCut::handle);
//        HANDLER.registerMessage(id++, PacketToggleSetting.class, PacketToggleSetting::encode, PacketToggleSetting::decode, PacketToggleSetting::handle);
        HANDLER.registerMessage(id++, PacketRequestCopyData.class, PacketRequestCopyData::encode, PacketRequestCopyData::decode, PacketRequestCopyData::handle);
//        HANDLER.registerMessage(id++, PacketCopyCoords.class, PacketCopyCoords::encode, PacketCopyCoords::decode, PacketCopyCoords::handle);
//        HANDLER.registerMessage(id++, PacketRelativePaste.class, PacketRelativePaste::encode, PacketRelativePaste::decode, PacketRelativePaste::handle);
//        HANDLER.registerMessage(id++, PacketDestructionRanges.class, PacketDestructionRanges::encode, PacketDestructionRanges::decode, PacketDestructionRanges::handle);
        HANDLER.registerMessage(id++, PacketUpdateTemplateManager.class, PacketUpdateTemplateManager::encode, PacketUpdateTemplateManager::decode, PacketUpdateTemplateManager::handle);
//        HANDLER.registerMessage(id++, PacketSendCopyDataToServer.class, PacketSendCopyDataToServer::encode, PacketSendCopyDataToServer::decode, PacketSendCopyDataToServer::handle);
        HANDLER.registerMessage(id++, PacketSendPasteBatches.class, PacketSendPasteBatches::encode, PacketSendPasteBatches::decode, PacketSendPasteBatches::handle);
//        HANDLER.registerMessage(id++, PacketRotate.class, PacketRotate::encode, PacketRotate::decode, PacketRotate::handle);

        //Client Side
        HANDLER.registerMessage(id++, PacketSendCopyData.class, PacketSendCopyData::encode, PacketSendCopyData::decode, PacketSendCopyData.Handler::handle);

    }

    public static void sendTo(Object msg, ServerPlayer player) {
        if (!(player instanceof FakePlayer))
            HANDLER.sendTo(msg, player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
    }

    public static void sendToServer(Object msg) {
        HANDLER.sendToServer(msg);
    }
}

package com.direwolf20.buildinggadgets2.common.network;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.direwolf20.buildinggadgets2.common.network.newpackets.data.*;
import com.direwolf20.buildinggadgets2.common.network.newpackets.handler.PacketRequestCopyData;
import com.direwolf20.buildinggadgets2.common.network.newpackets.handler.PacketSendCopyData;
import com.direwolf20.buildinggadgets2.common.network.newpackets.handler.PacketSendPasteBatches;
import com.direwolf20.buildinggadgets2.common.network.newpackets.handler.PacketUpdateTemplateManager;
import com.direwolf20.buildinggadgets2.common.network.newpackets.handler.gadgetaction.PacketGadgetAction;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

public class PacketHandler {
    public static void registerNetworking(final RegisterPayloadHandlerEvent event) {
        final IPayloadRegistrar registrar = event.registrar(BuildingGadgets2.MODID);

        registrar.play(GadgetActionPayload.ID, GadgetActionPayload::new, handler -> handler.server(PacketGadgetAction.get()::handle));
        registrar.play(RequestCopyDataPayload.ID, RequestCopyDataPayload::new, handler -> handler.server(PacketRequestCopyData.get()::handle));
        registrar.play(SendPasteBatchesPayload.ID, SendPasteBatchesPayload::new, handler -> handler.server(PacketSendPasteBatches.get()::handle));
        registrar.play(UpdateTemplateManagerPayload.ID, UpdateTemplateManagerPayload::new, handler -> handler.server(PacketUpdateTemplateManager.INSTANCE::handle));
        registrar.play(SendCopyDataPayload.ID, SendCopyDataPayload::new, handler -> handler.client(PacketSendCopyData.get()::handle));
    }
}

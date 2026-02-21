package com.direwolf20.buildinggadgets2.common.network.packets;

import com.direwolf20.buildinggadgets2.common.network.PacketHandler;
import com.direwolf20.buildinggadgets2.integration.AE2Integration;
import com.direwolf20.buildinggadgets2.integration.AE2Methods;
import com.direwolf20.buildinggadgets2.util.DimBlockPos;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/** Client asks for AE2 counts for all these items in one go; server replies with one PacketUpdateAE2Count. */
public class PacketRequestAE2Count {
    private final DimBlockPos boundPos;
    private final List<ItemStack> stacks;

    public PacketRequestAE2Count(DimBlockPos boundPos, List<ItemStack> stacks) {
        this.boundPos = boundPos;
        this.stacks = stacks;
    }

    public static void encode(PacketRequestAE2Count msg, FriendlyByteBuf buffer) {
        buffer.writeBlockPos(msg.boundPos.blockPos);
        buffer.writeResourceLocation(msg.boundPos.levelKey.location());
        buffer.writeCollection(msg.stacks, FriendlyByteBuf::writeItem);
    }

    public static PacketRequestAE2Count decode(FriendlyByteBuf buffer) {
        // Read order must match encode order
        BlockPos pos = buffer.readBlockPos();
        ResourceKey<Level> levelKey = ResourceKey.create(Registries.DIMENSION, buffer.readResourceLocation());
        
        return new PacketRequestAE2Count(
            new DimBlockPos(levelKey, pos), // Order matches your DimBlockPos constructor
            buffer.readCollection(ArrayList::new, FriendlyByteBuf::readItem)
        );
    }

    public static void handle(PacketRequestAE2Count msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            ServerPlayer player = context.get().getSender();
            if (player == null || !AE2Integration.isLoaded()) return;

            Map<String, Integer> countsMap = new HashMap<>();
            for (ItemStack stack : msg.stacks) {
                long count = AE2Methods.countInAE2(msg.boundPos, player, stack);
                String itemKey = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
                countsMap.put(itemKey, (int) count);
            }

            PacketHandler.sendTo(new PacketUpdateAE2Count(countsMap), player);
        });
        context.get().setPacketHandled(true);
    }
}
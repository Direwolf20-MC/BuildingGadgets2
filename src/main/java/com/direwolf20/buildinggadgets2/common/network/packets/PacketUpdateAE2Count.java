package com.direwolf20.buildinggadgets2.common.network.packets;

import com.direwolf20.buildinggadgets2.client.screen.MaterialListGUI;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Server sends this back with AE2 counts so the material list can show "available" without the client touching AE2. */
public class PacketUpdateAE2Count {
    // Cache so we can show counts on list refresh without re-asking the server. Cleared when the GUI closes.
    public static Map<String, Integer> clientCache = new HashMap<>();
    private final Map<String, Integer> counts;

    public PacketUpdateAE2Count(Map<String, Integer> counts) {
        this.counts = counts;
    }

    public static void encode(PacketUpdateAE2Count msg, FriendlyByteBuf buffer) {
        buffer.writeMap(msg.counts, FriendlyByteBuf::writeUtf, FriendlyByteBuf::writeInt);
    }

    public static PacketUpdateAE2Count decode(FriendlyByteBuf buffer) {
        return new PacketUpdateAE2Count(buffer.readMap(FriendlyByteBuf::readUtf, FriendlyByteBuf::readInt));
    }

    public static void handle(PacketUpdateAE2Count msg, Supplier<NetworkEvent.Context> context) {
        context.get().enqueueWork(() -> {
            msg.counts.forEach((key, count) -> {
                clientCache.put(key, count);

                if (Minecraft.getInstance().screen instanceof MaterialListGUI gui) {
                    if (gui.getScrollingList() != null) {
                        gui.getScrollingList().refreshItemCount(key, count);
                    }
                }
            });
        });
        context.get().setPacketHandled(true);
    }
}
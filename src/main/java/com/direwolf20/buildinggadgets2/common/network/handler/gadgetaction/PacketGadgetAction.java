package com.direwolf20.buildinggadgets2.common.network.handler.gadgetaction;

import com.direwolf20.buildinggadgets2.common.items.BaseGadget;
import com.direwolf20.buildinggadgets2.common.network.data.GadgetActionPayload;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.PlayPayloadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PacketGadgetAction {
    private static final Logger LOGGER = LoggerFactory.getLogger(PacketGadgetAction.class);
    private static final PacketGadgetAction instance = new PacketGadgetAction();

    public static PacketGadgetAction get() {
        return instance;
    }

    public void handle(final GadgetActionPayload payload, final PlayPayloadContext context) {
        context.workHandler().submitAsync(() -> {
            try {
                ActionGadget action = payload.actionName();

                var gadget = context.player()
                        .map(LivingEntity::getMainHandItem)
                        .filter(stack -> !stack.isEmpty())
                        .filter(stack -> stack.getItem() instanceof BaseGadget)
                        .orElse(ItemStack.EMPTY);

                if (gadget.isEmpty()) {
                    LOGGER.error("Received action {} but no gadget was found", payload.actionName());
                    return;
                }

                action.getHandler().accept(new GadgetActionContext(
                        context,
                        payload,
                        gadget,
                        context.player().get()
                ));
            } catch (IllegalArgumentException e) {
                LOGGER.error("Received unknown action {}", payload.actionName());
            }
        });
    }
}

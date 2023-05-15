package com.direwolf20.buildinggadgets2.client.renderer;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraftforge.client.model.pipeline.VertexConsumerWrapper;

public class DireVertexConsumer extends VertexConsumerWrapper {
    private float alpha;

    public DireVertexConsumer(VertexConsumer parent, float alpha) {
        super(parent);
        this.alpha = alpha;
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        parent.color(r, g, b, Math.round((float) 255 * alpha));
        return this;
    }
}

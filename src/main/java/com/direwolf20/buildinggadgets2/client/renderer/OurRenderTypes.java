package com.direwolf20.buildinggadgets2.client.renderer;

import com.direwolf20.buildinggadgets2.BuildingGadgets2;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.LayeringTransform;
import net.minecraft.client.renderer.rendertype.RenderSetup;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;

public class OurRenderTypes {
    static final RenderPipeline TRANSLUCENT_BLOCK_NO_CULL = RenderPipelines.TRANSLUCENT_BLOCK.toBuilder()
            .withLocation(Identifier.fromNamespaceAndPath(BuildingGadgets2.MODID, "pipeline/translucent_block_no_cull"))
            .withCull(false)
            .build();

    public static final RenderPipeline DEBUG_TRIANGLE_STRIP = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath(BuildingGadgets2.MODID, "pipeline/debug_triangle_strip"))
            .withCull(false)
            .withVertexFormat(DefaultVertexFormat.POSITION_COLOR, VertexFormat.Mode.TRIANGLE_STRIP)
            .build();

    public static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(TRANSLUCENT_BLOCK_NO_CULL);
        event.registerPipeline(DEBUG_TRIANGLE_STRIP);
    }

    public static final RenderType RenderBlock = RenderType.create("GadgetRenderBlock",
            RenderSetup.builder(RenderPipelines.TRANSLUCENT_BLOCK)
                    .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
                    .useLightmap()
                    .useOverlay()
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup());

    public static final RenderType RenderBlockFade = RenderType.create("GadgetRenderBlockFade",
            RenderSetup.builder(RenderPipelines.TRANSLUCENT_BLOCK)
                    .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
                    .useLightmap()
                    .useOverlay()
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup());

    public static final RenderType RenderBlockBackface = RenderType.create("GadgetRenderBlockBackface",
            RenderSetup.builder(TRANSLUCENT_BLOCK_NO_CULL)
                    .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
                    .useLightmap()
                    .useOverlay()
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup());

    public static final RenderType RenderBlockFadeNoCull = RenderType.create("GadgetRenderBlockFadeNoCull",
            RenderSetup.builder(TRANSLUCENT_BLOCK_NO_CULL)
                    .withTexture("Sampler0", TextureAtlas.LOCATION_BLOCKS)
                    .useLightmap()
                    .useOverlay()
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup());

    public static final RenderType TRIANGLE_STRIP = RenderType.create("GadgetTriangleStrip",
            RenderSetup.builder(DEBUG_TRIANGLE_STRIP)
                    .createRenderSetup());

    public static final RenderType MissingBlockOverlay = RenderType.create("GadgetMissingBlockOverlay",
            RenderSetup.builder(RenderPipelines.DEBUG_QUADS)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .createRenderSetup());

    public static final RenderType TRANSPARENT_BOX = RenderType.create("GadgetTransparentBox",
            RenderSetup.builder(RenderPipelines.DEBUG_QUADS)
                    .setLayeringTransform(LayeringTransform.VIEW_OFFSET_Z_LAYERING)
                    .sortOnUpload()
                    .createRenderSetup());
}

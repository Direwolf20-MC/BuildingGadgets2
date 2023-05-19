package com.direwolf20.buildinggadgets2.client.renderer;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.Map;

import static net.minecraft.client.renderer.RenderType.entityTranslucentCull;

public class MyRenderMethods {
    private static Map<BlockEntityType<?>, BlockEntityRenderer<?>> renderers = ImmutableMap.of();

    // TODO: Replace with native method
    public static void renderModelBrightnessColorQuads(PoseStack.Pose matrixEntry, VertexConsumer builder, float red, float green, float blue, float alpha, List<BakedQuad> listQuads, int combinedLightsIn, int combinedOverlayIn) {
        for (BakedQuad bakedquad : listQuads) {
            float f;
            float f1;
            float f2;

            if (bakedquad.isTinted()) {
                f = red * 1f;
                f1 = green * 1f;
                f2 = blue * 1f;
            } else {
                f = 1f;
                f1 = 1f;
                f2 = 1f;
            }

            builder.putBulkData(matrixEntry, bakedquad, f, f1, f2, alpha, combinedLightsIn, combinedOverlayIn, true);
        }
    }

    public static void renderBETransparent(BlockState pState, PoseStack pPoseStack, MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay, float alpha) {
        MultiplyAlphaRenderTypeBuffer multiplyAlphaRenderTypeBuffer = new MultiplyAlphaRenderTypeBuffer(pBufferSource, alpha);
        ItemStack stack = new ItemStack(pState.getBlock());
        net.minecraftforge.client.extensions.common.IClientItemExtensions.of(stack).getCustomRenderer().renderByItem(stack, ItemTransforms.TransformType.NONE, pPoseStack, multiplyAlphaRenderTypeBuffer, pPackedLight, pPackedOverlay);
    }

    /*public static void renderSingleBlock(BlockState pState, PoseStack pPoseStack, BufferBuilder vertexConsumer, int pPackedLight, int pPackedOverlay, net.minecraftforge.client.model.data.ModelData modelData, net.minecraft.client.renderer.RenderType renderTypeIn, BakedModel ibakedmodel) {
        RenderShape rendershape = pState.getRenderShape();
        BlockColors blockColors = Minecraft.getInstance().getBlockColors();
        DireModelBlockRenderer modelBlockRenderer = new DireModelBlockRenderer(blockColors);
        //modelBlockRenderer.setAlpha(0.5f);
        DireVertexConsumer direVertexConsumer = new DireVertexConsumer(vertexConsumer);

        if (rendershape != RenderShape.INVISIBLE) {
            switch (rendershape) {
                case MODEL:
                    int i = blockColors.getColor(pState, (BlockAndTintGetter) null, (BlockPos) null, 0);
                    float f = (float) (i >> 16 & 255) / 255.0F;
                    float f1 = (float) (i >> 8 & 255) / 255.0F;
                    float f2 = (float) (i & 255) / 255.0F;
                    for (net.minecraft.client.renderer.RenderType rt : ibakedmodel.getRenderTypes(pState, RandomSource.create(42), modelData))
                        modelBlockRenderer.renderModel(pPoseStack.last(), direVertexConsumer, pState, ibakedmodel, f, f1, f2, pPackedLight, pPackedOverlay, modelData, rt);
                    break;
                case ENTITYBLOCK_ANIMATED:
                    if (!pState.hasBlockEntity()) return;
                    MultiBufferSource.BufferSource buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
                    ItemStack stack = new ItemStack(pState.getBlock());
                    Vec3 projectedView = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
                    BlockEntity blockEntity = ((EntityBlock) pState.getBlock()).newBlockEntity(new BlockPos(projectedView.x, projectedView.y, projectedView.z), pState);
                    blockEntity.setLevel(Minecraft.getInstance().level);
                    BlockEntityRenderDispatcher blockEntityRenderDispatcher = Minecraft.getInstance().getBlockEntityRenderDispatcher();
                    blockEntityRenderDispatcher.render(blockEntity, 0, pPoseStack, buffersource);
                    //net.minecraftforge.client.extensions.common.IClientItemExtensions.of(stack).getCustomRenderer().renderByItem(stack, ItemTransforms.TransformType.NONE, pPoseStack, buffersource, pPackedLight, pPackedOverlay);
            }

        }
    }*/

    public static class MultiplyAlphaRenderTypeBuffer implements MultiBufferSource {
        private final MultiBufferSource inner;
        private final float constantAlpha;

        public MultiplyAlphaRenderTypeBuffer(MultiBufferSource inner, float constantAlpha) {
            this.inner = inner;
            this.constantAlpha = constantAlpha;
        }

        @Override
        public VertexConsumer getBuffer(RenderType type) {
            RenderType localType = type;
            if (localType instanceof RenderType.CompositeRenderType) {
                // all of this requires a lot of AT's so be aware of that on ports
                ResourceLocation texture = ((RenderStateShard.TextureStateShard) ((RenderType.CompositeRenderType) localType).state.textureState).texture
                        .orElse(InventoryMenu.BLOCK_ATLAS);

                localType = entityTranslucentCull(texture);
            } else if (localType.toString().equals(Sheets.translucentCullBlockSheet().toString())) {
                localType = Sheets.translucentCullBlockSheet();
            }

            return new DireVertexConsumer(this.inner.getBuffer(localType), constantAlpha);
        }
    }

    /*public static class MultiplyAlphaRenderTypeBuffer2 extends MultiBufferSource.BufferSource {
        private static final ChunkBufferBuilderPack fixedBufferPack = new ChunkBufferBuilderPack();
        private static final SortedMap<RenderType, BufferBuilder> fixedBuffers = Util.make(new Object2ObjectLinkedOpenHashMap<>(), (p_110100_) -> {
            p_110100_.put(Sheets.solidBlockSheet(), fixedBufferPack.builder(RenderType.solid()));
            p_110100_.put(Sheets.cutoutBlockSheet(), fixedBufferPack.builder(RenderType.cutout()));
            p_110100_.put(Sheets.bannerSheet(), fixedBufferPack.builder(RenderType.cutoutMipped()));
            p_110100_.put(Sheets.translucentCullBlockSheet(), fixedBufferPack.builder(RenderType.translucent()));
            put(p_110100_, Sheets.shieldSheet());
            put(p_110100_, Sheets.bedSheet());
            put(p_110100_, Sheets.shulkerBoxSheet());
            put(p_110100_, Sheets.signSheet());
            put(p_110100_, Sheets.chestSheet());
            put(p_110100_, RenderType.translucentNoCrumbling());
            put(p_110100_, RenderType.armorGlint());
            put(p_110100_, RenderType.armorEntityGlint());
            put(p_110100_, RenderType.glint());
            put(p_110100_, RenderType.glintDirect());
            put(p_110100_, RenderType.glintTranslucent());
            put(p_110100_, RenderType.entityGlint());
            put(p_110100_, RenderType.entityGlintDirect());
            put(p_110100_, RenderType.waterMask());
            ModelBakery.DESTROY_TYPES.forEach((p_173062_) -> {
                put(p_110100_, p_173062_);
            });
        });

        private final float constantAlpha;
        private VertexBuffer vertexBuffer;

        public MultiplyAlphaRenderTypeBuffer2(BufferBuilder bufferBuilder, float constantAlpha, VertexBuffer vertexBuffer) {
            super(bufferBuilder, fixedBuffers);
            this.constantAlpha = constantAlpha;
            this.vertexBuffer = vertexBuffer;
        }

        private static void put(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> pMapBuilders, RenderType pRenderType) {
            pMapBuilders.put(pRenderType, new BufferBuilder(pRenderType.bufferSize()));
        }
        private BufferBuilder getBuilderRaw(RenderType pRenderType) {
            return fixedBuffers.getOrDefault(pRenderType, this.builder);
        }

        @Override
        public VertexConsumer getBuffer(RenderType pRenderType) {
            return super.getBuffer(pRenderType);
        }

        @Override
        public void endLastBatch() {
            super.endLastBatch();

        }
        @Override
        public void endBatch() {
            super.endBatch();
        }
        @Override
        public void endBatch(RenderType pRenderType) {
            BufferBuilder bufferbuilder = this.getBuilderRaw(pRenderType);
            boolean flag = Objects.equals(this.lastState, pRenderType.asOptional());
            if (flag || bufferbuilder != this.builder) {
                if (this.startedBuffers.remove(bufferbuilder)) {
                    this.end(pRenderType, bufferbuilder, 0, 0, 0);
                    if (flag) {
                        this.lastState = Optional.empty();
                    }

                }
            }
        }
        public void end(RenderType pRenderType, BufferBuilder pBuffer, int pCameraX, int pCameraY, int pCameraZ) {
            if (pBuffer.building()) {
                if (pRenderType.sortOnUpload) {
                    pBuffer.setQuadSortOrigin((float)pCameraX, (float)pCameraY, (float)pCameraZ);
                }

                BufferBuilder.RenderedBuffer bufferbuilder$renderedbuffer = pBuffer.end();
                pRenderType.setupRenderState();
                vertexBuffer.bind();
                vertexBuffer.upload(bufferbuilder$renderedbuffer);
                VertexBuffer.unbind();
                pRenderType.clearRenderState();
            }
        }
    }*/

    /*public static class MultiplyAlphaRenderTypeBuffer extends MultiBufferSource.BufferSource {
        private static final ChunkBufferBuilderPack fixedBufferPack = new ChunkBufferBuilderPack();
        private static final SortedMap<RenderType, BufferBuilder> fixedBuffers = Util.make(new Object2ObjectLinkedOpenHashMap<>(), (p_110100_) -> {
            p_110100_.put(Sheets.solidBlockSheet(), fixedBufferPack.builder(RenderType.solid()));
            p_110100_.put(Sheets.cutoutBlockSheet(), fixedBufferPack.builder(RenderType.cutout()));
            p_110100_.put(Sheets.bannerSheet(), fixedBufferPack.builder(RenderType.cutoutMipped()));
            p_110100_.put(Sheets.translucentCullBlockSheet(), fixedBufferPack.builder(RenderType.translucent()));
            put(p_110100_, Sheets.shieldSheet());
            put(p_110100_, Sheets.bedSheet());
            put(p_110100_, Sheets.shulkerBoxSheet());
            put(p_110100_, Sheets.signSheet());
            put(p_110100_, Sheets.chestSheet());
            put(p_110100_, RenderType.translucentNoCrumbling());
            put(p_110100_, RenderType.armorGlint());
            put(p_110100_, RenderType.armorEntityGlint());
            put(p_110100_, RenderType.glint());
            put(p_110100_, RenderType.glintDirect());
            put(p_110100_, RenderType.glintTranslucent());
            put(p_110100_, RenderType.entityGlint());
            put(p_110100_, RenderType.entityGlintDirect());
            put(p_110100_, RenderType.waterMask());
            ModelBakery.DESTROY_TYPES.forEach((p_173062_) -> {
                put(p_110100_, p_173062_);
            });
        });

        private final float constantAlpha;
        private VertexConsumer vertexConsumer;

        public MultiplyAlphaRenderTypeBuffer(BufferBuilder bufferBuilder, float constantAlpha, VertexConsumer vertexConsumer) {
            super(bufferBuilder, fixedBuffers);
            this.constantAlpha = constantAlpha;
            this.vertexConsumer = vertexConsumer;
        }

        private static void put(Object2ObjectLinkedOpenHashMap<RenderType, BufferBuilder> pMapBuilders, RenderType pRenderType) {
            pMapBuilders.put(pRenderType, new BufferBuilder(pRenderType.bufferSize()));
        }
        private BufferBuilder getBuilderRaw(RenderType pRenderType) {
            return this.fixedBuffers.getOrDefault(pRenderType, this.builder);
        }

        @Override
        public VertexConsumer getBuffer(RenderType pRenderType) {
            Optional<RenderType> optional = pRenderType.asOptional();
            BufferBuilder bufferbuilder = this.getBuilderRaw(pRenderType);
            if (!Objects.equals(this.lastState, optional) || !pRenderType.canConsolidateConsecutiveGeometry()) {
                if (this.lastState.isPresent()) {
                    RenderType rendertype = this.lastState.get();
                    if (!this.fixedBuffers.containsKey(rendertype)) {
                        this.endBatch(rendertype);
                    }
                }

                if (this.startedBuffers.add(bufferbuilder)) {
                    bufferbuilder.begin(pRenderType.mode(), pRenderType.format());
                }

                this.lastState = optional;
            }

            return bufferbuilder;
        }

        @Override
        public void endLastBatch() {
            if (this.lastState.isPresent()) {
                RenderType rendertype = this.lastState.get();
                if (!this.fixedBuffers.containsKey(rendertype)) {
                    this.endBatch(rendertype);
                }

                this.lastState = Optional.empty();
            }

        }
        @Override
        public void endBatch() {
            this.lastState.ifPresent((p_109917_) -> {
                VertexConsumer vertexconsumer = this.getBuffer(p_109917_);
                if (vertexconsumer == this.builder) {
                    this.endBatch(p_109917_);
                }

            });

            for(RenderType rendertype : this.fixedBuffers.keySet()) {
                this.endBatch(rendertype);
            }

        }
        @Override
        public void endBatch(RenderType pRenderType) {
            BufferBuilder bufferbuilder = this.getBuilderRaw(pRenderType);
            boolean flag = Objects.equals(this.lastState, pRenderType.asOptional());
            if (flag || bufferbuilder != this.builder) {
                if (this.startedBuffers.remove(bufferbuilder)) {
                    pRenderType.end(bufferbuilder, 0, 0, 0);
                    if (flag) {
                        this.lastState = Optional.empty();
                    }

                }
            }
        }
    }*/
}

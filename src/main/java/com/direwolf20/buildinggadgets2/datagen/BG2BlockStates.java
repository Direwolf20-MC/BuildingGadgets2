package com.direwolf20.buildinggadgets2.datagen;

import com.direwolf20.buildinggadgets2.setup.Registration;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.resources.Identifier;

public final class BG2BlockStates {

    private final BlockModelGenerators blockModels;

    public BG2BlockStates(BlockModelGenerators blockModels) {
        this.blockModels = blockModels;
    }

    public void run() {
        // render_block: single cube_all. Its render-type (cutout) is controlled by the
        // block entity renderer at runtime, not by the blockstate JSON.
        blockModels.createTrivialCube(Registration.RenderBlock.get());

        // template_manager: horizontally-facing orientable cube with per-face textures
        // (side/front/top/bottom). Mirrors vanilla's furnace blockstate layout.
        Identifier templateManagerModel = ModelTemplates.CUBE_ORIENTABLE_TOP_BOTTOM.create(
                Registration.TemplateManager.get(),
                TextureMapping.orientableCube(Registration.TemplateManager.get()),
                blockModels.modelOutput);
        MultiVariant templateManagerVariant = BlockModelGenerators.plainVariant(templateManagerModel);
        blockModels.blockStateOutput.accept(
                MultiVariantGenerator.dispatch(Registration.TemplateManager.get(), templateManagerVariant)
                        .with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
    }
}

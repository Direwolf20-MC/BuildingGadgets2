package com.direwolf20.buildinggadgets2.common.network.handler.gadgetaction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public class GadgetActionCodecs {
    public record BiPos(BlockPos startPos, BlockPos endPos) {
         public static Codec<BiPos> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                 BlockPos.CODEC.fieldOf("startPos").forGetter(BiPos::startPos),
                 BlockPos.CODEC.fieldOf("endPos").forGetter(BiPos::endPos)
         ).apply(instance, BiPos::new));
    }

    public record DestructionRanges(
      int left, int right,
      int up, int down,
      int depth
    ) {
        public static Codec<DestructionRanges> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("left").forGetter(DestructionRanges::left),
                Codec.INT.fieldOf("right").forGetter(DestructionRanges::right),
                Codec.INT.fieldOf("up").forGetter(DestructionRanges::up),
                Codec.INT.fieldOf("down").forGetter(DestructionRanges::down),
                Codec.INT.fieldOf("depth").forGetter(DestructionRanges::depth)
        ).apply(instance, DestructionRanges::new));
    }

    public record ModeSwitch(
            boolean rotate,
            ResourceLocation modeId
    ) {
        public static Codec<ModeSwitch> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.fieldOf("rotate").forGetter(ModeSwitch::rotate),
                ResourceLocation.CODEC.fieldOf("modeId").forGetter(ModeSwitch::modeId)
        ).apply(instance, ModeSwitch::new));
    }
}

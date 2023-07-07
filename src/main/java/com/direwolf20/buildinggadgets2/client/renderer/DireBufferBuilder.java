package com.direwolf20.buildinggadgets2.client.renderer;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.ints.IntConsumer;
import org.apache.commons.lang3.ArrayUtils;

public class DireBufferBuilder extends BufferBuilder {
    //This class exists because sorting in vanilla minecraft is the opposite of how we want to do it
    //So override the sort method (Which needs lots of ATs) and add a reversal line
    public DireBufferBuilder(int pCapacity) {
        super(pCapacity);
    }

    @Override
    public void putSortedQuadIndices(VertexFormat.IndexType pIndexType) {
        if (this.sortingPoints != null && this.sorting != null) {
            int[] aint = this.sorting.sort(this.sortingPoints);
            IntConsumer intconsumer = this.intConsumer(this.nextElementByte, pIndexType);
            // Reverse the order of the sorted indices. The whole reason this class exists is this one line!
            ArrayUtils.reverse(aint);
            for (int i : aint) {
                intconsumer.accept(i * this.mode.primitiveStride + 0);
                intconsumer.accept(i * this.mode.primitiveStride + 1);
                intconsumer.accept(i * this.mode.primitiveStride + 2);
                intconsumer.accept(i * this.mode.primitiveStride + 2);
                intconsumer.accept(i * this.mode.primitiveStride + 3);
                intconsumer.accept(i * this.mode.primitiveStride + 0);
            }

        } else {
            throw new IllegalStateException("Sorting state uninitialized");
        }
        /*float[] afloat = new float[this.sortingPoints.length];
        int[] aint = new int[this.sortingPoints.length];

        for (int i = 0; i < this.sortingPoints.length; aint[i] = i++) {
            //TODO Fix Sorting
            float f = this.sortingPoints[i].x() - this.sortX;
            float f1 = this.sortingPoints[i].y() - this.sortY;
            float f2 = this.sortingPoints[i].z() - this.sortZ;
            afloat[i] = f * f + f1 * f1 + f2 * f2;
        }

        IntArrays.mergeSort(aint, (p_166784_, p_166785_) -> {
            return Floats.compare(afloat[p_166785_], afloat[p_166784_]);
        });
        IntConsumer intconsumer = this.intConsumer(this.nextElementByte, pIndexType);

        // Reverse the order of the sorted indices. The whole reason this class exists is this one line!
        ArrayUtils.reverse(aint);

        for (int j : aint) {
            intconsumer.accept(j * this.mode.primitiveStride + 0);
            intconsumer.accept(j * this.mode.primitiveStride + 1);
            intconsumer.accept(j * this.mode.primitiveStride + 2);
            intconsumer.accept(j * this.mode.primitiveStride + 2);
            intconsumer.accept(j * this.mode.primitiveStride + 3);
            intconsumer.accept(j * this.mode.primitiveStride + 0);
        }*/

    }
}

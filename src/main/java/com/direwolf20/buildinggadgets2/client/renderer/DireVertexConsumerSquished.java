package com.direwolf20.buildinggadgets2.client.renderer;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraftforge.client.model.pipeline.VertexConsumerWrapper;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class DireVertexConsumerSquished extends VertexConsumerWrapper {
    private final float minX, minY, minZ, maxX, maxY, maxZ;
    private final Matrix4f matrix4f;
    float minU = 0;
    float maxU = 1;
    float minV = 0;
    float maxV = 1;
    public boolean adjustUV = false;
    Direction direction = null;

    public DireVertexConsumerSquished(VertexConsumer parent, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, Matrix4f matrix4f) {
        super(parent);
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.maxX = maxX;
        this.maxY = maxY;
        this.maxZ = maxZ;
        this.matrix4f = matrix4f;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setUV(float minU, float maxU, float minV, float maxV) {
        this.minU = minU;
        this.maxU = maxU;
        this.minV = minV;
        this.maxV = maxV;
    }

    @Override
    public VertexConsumer vertex(double x, double y, double z) {
        Matrix4f inverseMatrix = new Matrix4f(matrix4f);
        inverseMatrix.invert();

        Vector4f originalVector = inverseMatrix.transform(new Vector4f((float) x, (float) y, (float) z, 1.0f));
        float adjustedX = originalVector.x * (maxX - minX) + minX;
        float adjustedY = originalVector.y * (maxY - minY) + minY;
        float adjustedZ = originalVector.z * (maxZ - minZ) + minZ;

        Vector4f vector4f = matrix4f.transform(new Vector4f(adjustedX, adjustedY, adjustedZ, 1.0F));
        parent.vertex(vector4f.x, vector4f.y, vector4f.z);
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        if (adjustUV) {
            //Growing up from ground!
            /*
            float uDistanceFromStart = u - minU;  // How far is u from its starting value?
            float vDistanceFromStart = v - minV;  // How far is v from its starting value?

            float adjustedUDistance = uDistanceFromStart; // Adjust for the block width
            float adjustedVDistance = vDistanceFromStart * (maxY);    // Adjust for the block height

            float adjustedU = minU + adjustedUDistance;
            float adjustedV = minV + adjustedVDistance;

            parent.uv(adjustedU, adjustedV);*/

            //Building above!
            float vDistanceToEnd = maxV - v;      // How far is v from its end value?
            float adjustedVDistance = vDistanceToEnd * (maxY); // Adjust for the block height
            float adjustedV = maxV - adjustedVDistance; // Subtracting because we're adjusting from the end.

            parent.uv(u, adjustedV);
        } else {
            parent.uv(u, v);
        }
        return this;
    }

    @Override
    public void putBulkData(PoseStack.Pose pPoseEntry, BakedQuad pQuad, float[] pColorMuls, float pRed, float pGreen, float pBlue, float alpha, int[] pCombinedLights, int pCombinedOverlay, boolean pMulColor) {
        float[] afloat = new float[]{pColorMuls[0], pColorMuls[1], pColorMuls[2], pColorMuls[3]};
        int[] aint = new int[]{pCombinedLights[0], pCombinedLights[1], pCombinedLights[2], pCombinedLights[3]};
        int[] aint1 = pQuad.getVertices();
        Vec3i vec3i = pQuad.getDirection().getNormal();
        Matrix4f matrix4f = pPoseEntry.pose();
        Vector3f vector3f = pPoseEntry.normal().transform(new Vector3f((float) vec3i.getX(), (float) vec3i.getY(), (float) vec3i.getZ()));
        int i = 8;
        int j = aint1.length / 8;

        try (MemoryStack memorystack = MemoryStack.stackPush()) {
            ByteBuffer bytebuffer = memorystack.malloc(DefaultVertexFormat.BLOCK.getVertexSize());
            IntBuffer intbuffer = bytebuffer.asIntBuffer();

            for (int k = 0; k < j; ++k) {
                intbuffer.clear();
                intbuffer.put(aint1, k * 8, 8);
                float f = bytebuffer.getFloat(0);// * (maxX - minX) + minX;
                float f1 = bytebuffer.getFloat(4);// * (maxY - minY) + minY;
                float f2 = bytebuffer.getFloat(8);// * (maxZ - minZ) + minZ;
                float f3;
                float f4;
                float f5;
                if (pMulColor) {
                    float f6 = (float) (bytebuffer.get(12) & 255) / 255.0F;
                    float f7 = (float) (bytebuffer.get(13) & 255) / 255.0F;
                    float f8 = (float) (bytebuffer.get(14) & 255) / 255.0F;
                    f3 = f6 * afloat[k] * pRed;
                    f4 = f7 * afloat[k] * pGreen;
                    f5 = f8 * afloat[k] * pBlue;
                } else {
                    f3 = afloat[k] * pRed;
                    f4 = afloat[k] * pGreen;
                    f5 = afloat[k] * pBlue;
                }

                int l = applyBakedLighting(pCombinedLights[k], bytebuffer);
                float f9 = bytebuffer.getFloat(16);
                float f10 = bytebuffer.getFloat(20);
                Vector4f vector4f = matrix4f.transform(new Vector4f(f, f1, f2, 1.0F));
                applyBakedNormals(vector3f, bytebuffer, pPoseEntry.normal());
                float vertexAlpha = pMulColor ? alpha * (float) (bytebuffer.get(15) & 255) / 255.0F : alpha;
                this.vertex(vector4f.x(), vector4f.y(), vector4f.z(), f3, f4, f5, vertexAlpha, f9, f10, pCombinedOverlay, l, vector3f.x(), vector3f.y(), vector3f.z());
            }
        }

    }
}

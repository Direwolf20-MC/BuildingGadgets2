package com.direwolf20.buildinggadgets2.client.particles.fluidparticle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;


import java.util.Random;

public class FluidFlowParticle extends BreakingItemParticle {

    private double targetX, targetY, targetZ;
    Random random = new Random();
    private float partSize;
    private boolean doGravity;
    private boolean shrinking;
    private FluidStack fluidStack;

    public FluidFlowParticle(ClientLevel world, double x, double y, double z, FluidStack fluidStack, boolean gravity, boolean shrinking) {
        super(world, x, y, z, ItemStack.EMPTY);
        this.fluidStack = fluidStack;
        this.doGravity = gravity;
        this.shrinking = shrinking;
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        if (shrinking) {
            this.targetX = x + 1.75f;
            this.targetY = y + 1.75f;
            this.targetZ = z + 1.75f;
        } else {
            double randomX = random.nextFloat();
            double randomY = random.nextFloat();
            double randomZ = random.nextFloat();
            this.xo = x + randomX;
            this.yo = y + randomY;
            this.zo = z + randomZ;
            this.setPos(xo, yo, zo);
            this.targetX = x;
            this.targetY = y;
            this.targetZ = z;
        }
        Vec3 target = new Vec3(targetX, targetY, targetZ);
        Vec3 source = new Vec3(this.x, this.y, this.z);
        Vec3 path = target.subtract(source).normalize().multiply(1, 1, 1);
        float minSize = 0.25f;
        float maxSize = 0.5f;
        this.partSize = minSize + random.nextFloat() * (maxSize - minSize);
        float speedModifier = (1f - 0.5f) * (partSize - minSize) / (maxSize - minSize) + 0.25f;
        int ticksPerBlock = 15;
        float speedAdjust = ticksPerBlock * (1 / speedModifier);
        this.xd += path.x / speedAdjust;
        this.yd += path.y / speedAdjust;
        this.zd += path.z / speedAdjust;
        this.lifetime = 30;
        int longLifeChance = random.nextInt(20);
        if (longLifeChance == 0)
            this.lifetime = 120;
        this.scale(partSize);
        this.partSize = quadSize;
        this.setSprite(Minecraft.getInstance().getTextureAtlas(TextureAtlas.LOCATION_BLOCKS).apply(IClientFluidTypeExtensions.of(fluidStack.getFluid()).getStillTexture(fluidStack)));
        int i = IClientFluidTypeExtensions.of(fluidStack.getFluid()).getTintColor(fluidStack);
        this.rCol *= (float) (i >> 16 & 255) / 255.0F;
        this.gCol *= (float) (i >> 8 & 255) / 255.0F;
        this.bCol *= (float) (i & 255) / 255.0F;

        if (gravity) {
            this.xd = 0;
            this.yd = 0;
            this.zd = 0;
            this.gravity = 0.0625f;
            this.hasPhysics = true;
            this.age = this.lifetime / 2;
            this.scale(2f);
            this.partSize = quadSize;
            updateColorAndGravity();
        } else {
            this.gravity = 0.0f;
            this.hasPhysics = false;
        }

        if (!shrinking)
            updateColorAndGravity();
    }

    @Override
    public void tick() {
        this.xo = this.x;
        this.yo = this.y;
        this.zo = this.z;
        if (this.age++ >= this.lifetime) {
            this.remove();
        } else {
            this.yd -= 0.04D * (double) this.gravity;
            this.move(this.xd, this.yd, this.zd);
        }
        if (!shrinking && this.y <= targetY)
            this.remove();
        updateColorAndGravity();
    }

    public void updateColorAndGravity() {
        float relativeAge = (float) ((this.lifetime - this.age)) / this.lifetime; //1.0 -> 0.0
        float shrink = Mth.lerp(relativeAge, 0.1f, 1);
        this.quadSize = partSize * shrink;

        float adjustedAge = (float) Math.pow(relativeAge, 2);
        if (shrinking) {
            this.rCol = Mth.lerp(adjustedAge, 0, this.rCol);
            this.gCol = Mth.lerp(adjustedAge, 0, this.gCol);
            this.bCol = Mth.lerp(adjustedAge, 0, this.bCol);
        } else {
            int i = IClientFluidTypeExtensions.of(fluidStack.getFluid()).getTintColor(fluidStack);
            float targetRed = (float) (i >> 16 & 255) / 255.0F;
            float targetGreen = (float) (i >> 8 & 255) / 255.0F;
            float targetBlue = (float) (i & 255) / 255.0F;
            this.rCol = Mth.lerp(adjustedAge, targetRed, this.rCol);
            this.gCol = Mth.lerp(adjustedAge, targetGreen, this.gCol);
            this.bCol = Mth.lerp(adjustedAge, targetBlue, this.bCol);
        }

        if (relativeAge < 0.5f) {
            adjustedAge = (float) Math.pow(relativeAge / 0.5f, 2);
            if (shrinking)
                this.alpha = Mth.lerp(adjustedAge, 0.4f, 1);
            else
                this.alpha = Mth.lerp(adjustedAge, 1f, 0.2f);
        }
        if (!doGravity) {
            int gravityChance = random.nextInt(2);
            if (relativeAge < 0.75f && gravityChance == 0) {
                this.gravity = 0.05f;
            }
        }
    }

    @Override //Performance Reasons
    protected int getLightColor(float pPartialTick) {
        return 0xF00080;
    }

    public static ParticleProvider<FluidFlowParticleData> FACTORY =
            (data, world, x, y, z, xSpeed, ySpeed, zSpeed) ->
                    new FluidFlowParticle(world, x, y, z, data.getFluidStack(), data.doGravity, data.shrinking);
}

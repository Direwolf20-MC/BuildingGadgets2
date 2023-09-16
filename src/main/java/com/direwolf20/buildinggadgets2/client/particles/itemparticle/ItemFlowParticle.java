package com.direwolf20.buildinggadgets2.client.particles.itemparticle;


import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.BreakingItemParticle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.Random;

public class ItemFlowParticle extends BreakingItemParticle {

    private double targetX, targetY, targetZ;
    Random random = new Random();
    private float partSize;

    public ItemFlowParticle(ClientLevel world, double x, double y, double z, ItemStack itemStack) {
        super(world, x, y, z, itemStack);
        this.xd = 0;
        this.yd = 0;
        this.zd = 0;
        this.targetX = x + 1.75f;
        this.targetY = y + 1.75f;
        this.targetZ = z + 1.75f;
        Vec3 target = new Vec3(targetX, targetY, targetZ);
        Vec3 source = new Vec3(this.x, this.y, this.z);
        Vec3 path = target.subtract(source).normalize().multiply(1, 1, 1);
        this.gravity = 0.0f;
        double distance = target.distanceTo(source);
        //System.out.println(source +":"+target);
        this.hasPhysics = false;
        //float minSize = 0.10f;
        //float maxSize = 0.25f;
        float minSize = 0.25f;
        float maxSize = 0.5f;
        this.partSize = minSize + random.nextFloat() * (maxSize - minSize);
        float speedModifier = (1f - 0.5f) * (partSize - minSize) / (maxSize - minSize) + 0.25f;
        int ticksPerBlock = 15;
        float speedAdjust = ticksPerBlock * (1 / speedModifier);
        this.xd += path.x / speedAdjust;
        this.yd += path.y / speedAdjust;
        this.zd += path.z / speedAdjust;
        //this.lifetime = (int) (distance * speedAdjust);
        this.lifetime = 40;
        this.scale(partSize);
        this.partSize = quadSize;
        if (this.sprite == null) {
            this.setSprite(Minecraft.getInstance().getItemRenderer().getModel(new ItemStack(Blocks.COBBLESTONE), world, (LivingEntity) null, 0).getParticleIcon());
        }

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
        float relativeAge = (float) ((this.lifetime - this.age)) / this.lifetime; //1.0 -> 0.0
        float shrink = Mth.lerp(relativeAge, 0.1f, 1);
        this.quadSize = partSize * shrink;

        if (relativeAge < 1) {
            float adjustedAge = (float) Math.pow((float) Math.pow(relativeAge, 1.5), 2);
            this.rCol = Mth.lerp(adjustedAge, 0, 1);
            this.gCol = Mth.lerp(adjustedAge, 0, 1);
            this.bCol = Mth.lerp(adjustedAge, 0, 1);
        }

        if (relativeAge < 0.5f) {
            float adjustedAge = (float) Math.pow(relativeAge / 0.5f, 2);
            this.alpha = Mth.lerp(adjustedAge, 0.4f, 1);
        }

        int gravityChance = random.nextInt(10);
        if (relativeAge < 0.75f && gravityChance == 0)
            this.gravity = 0.02f;
    }

    @Override //Performance Reasons
    protected int getLightColor(float pPartialTick) {
        return 0xF00080;
    }

    public static ParticleProvider<ItemFlowParticleData> FACTORY =
            (data, world, x, y, z, xSpeed, ySpeed, zSpeed) ->
                    new ItemFlowParticle(world, x, y, z, data.getItemStack());
}


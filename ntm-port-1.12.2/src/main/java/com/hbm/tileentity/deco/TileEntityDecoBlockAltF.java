package com.hbm.tileentity.deco;

import java.util.HashSet;
import java.util.List;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class TileEntityDecoBlockAltF extends TileEntity implements net.minecraft.util.ITickable {

	@Override
	public void update() {
		int strength = 4;
		float f = strength;
        new HashSet();
        int i;
        int j;
        int k;
        double d5;
        double d6;
        double d7;
        double wat = 4*2;
        strength *= 2.0F;
        i = MathHelper.floor(this.pos.getX() - wat - 1.0D);
        j = MathHelper.floor(this.pos.getX() + wat + 1.0D);
        k = MathHelper.floor(this.pos.getY() - wat - 1.0D);
        int i2 = MathHelper.floor(this.pos.getY() + wat + 1.0D);
        int l = MathHelper.floor(this.pos.getZ() - wat - 1.0D);
        int j2 = MathHelper.floor(this.pos.getZ() + wat + 1.0D);
        List list = this.world.getEntitiesWithinAABBExcludingEntity(null, AxisAlignedBB.getBoundingBox(i, k, l, j, i2, j2));
        new Vec3d(this.pos.getX(), this.pos.getY(), this.pos.getZ());

        for (int i1 = 0; i1 < list.size(); ++i1)
        {
            Entity entity = (Entity)list.get(i1);
            double d4 = entity.getDistance(this.pos.getX(), this.pos.getY(), this.pos.getZ()) / 4;

            if (d4 <= 1.0D)
            {
                d5 = entity.posX - this.pos.getX();
                d6 = entity.posY + entity.getEyeHeight() - this.pos.getY();
                d7 = entity.posZ - this.pos.getZ();
                double d9 = MathHelper.sqrt(d5 * d5 + d6 * d6 + d7 * d7);
                if (d9 < wat)
                {
                	if(entity instanceof EntityPlayer) {
                		((EntityPlayer)entity).addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.INSTANT_HEALTH, 5, 99));
                		((EntityPlayer)entity).addPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.SATURATION, 5, 99));
                    }
                }
            }
        }

        strength = (int)f;
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}
}

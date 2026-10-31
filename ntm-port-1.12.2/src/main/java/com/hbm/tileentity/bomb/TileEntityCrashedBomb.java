package com.hbm.tileentity.bomb;

import java.util.List;
import java.util.function.BiConsumer;

import com.hbm.blocks.bomb.BlockCrashedBomb.EnumDudType;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.EnumUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityCrashedBomb extends TileEntity implements net.minecraft.util.ITickable {

	@Override
	public void update() {

		if(!this.world.isRemote) {

			if(this.world.getTotalWorldTime() % 2 == 0) {
				EnumDudType type = EnumUtil.grabEnumSafely(EnumDudType.class, this.getBlockMetadata());

				if(type == type.BALEFIRE)	affectEntities((entity, intensity) -> { ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, 1F * intensity); }, 15D);
				if(type == type.NUKE)		affectEntities((entity, intensity) -> { ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, 0.25F * intensity); }, 10D);
				if(type == type.SALTED)		affectEntities((entity, intensity) -> { ContaminationUtil.contaminate(entity, HazardType.RADIATION, ContaminationType.CREATIVE, 0.5F * intensity); }, 10D);
			}
		}
	}

	public void affectEntities(BiConsumer<EntityLivingBase, Float> effect, double range) {
		List<EntityLivingBase> list = this.world.getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5, this.pos.getX() + 0.5, this.pos.getY() + 0.5, this.pos.getZ() + 0.5).grow(range, range, range));
		for(EntityLivingBase entity : list) {
			double dist = Math.sqrt(getDistanceSq(entity.posX, entity.posY + entity.height / 2, entity.posZ));
			if(dist > range) continue;
			float intensity = (float) (1D - dist / range);
			effect.accept(entity, intensity);
		}
	}

	@Override public AxisAlignedBB getRenderBoundingBox() { return TileEntity.INFINITE_EXTENT_AABB; }
	@Override @SideOnly(Side.CLIENT) public double getMaxRenderDistanceSquared() { return 65536.0D; }
}

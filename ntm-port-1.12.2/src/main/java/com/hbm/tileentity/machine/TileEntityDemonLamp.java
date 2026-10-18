package com.hbm.tileentity.machine;

import java.util.List;

import com.hbm.config.RadiationConfig;
import com.hbm.hazard.type.HazardTypeNeutron;
import com.hbm.util.ContaminationUtil;
import com.hbm.util.ContaminationUtil.ContaminationType;
import com.hbm.util.ContaminationUtil.HazardType;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class TileEntityDemonLamp extends TileEntity {

	@Override
	public void updateEntity() {
		
		if(!worldObj.isRemote) {
			radiate(worldObj, xCoord, yCoord, zCoord);
		}
	}
	
	private void radiate(World world, int x, int y, int z) {
		
		float rads = 100000F;
		double range = 25D;
		
		
		List<EntityLivingBase> entities = world.getEntitiesWithinAABB(EntityLivingBase.class, AxisAlignedBB.getBoundingBox(x + 0.5, y + 0.5, z + 0.5, x + 0.5, y + 0.5, z + 0.5).expand(range, range, range));
		
		for(EntityLivingBase e : entities) {
			
			Vec3d vec = new Vec3d(e.posX - (x + 0.5), (e.posY + e.getEyeHeight()) - (y + 0.5), e.posZ - (z + 0.5));
			double len = vec.length();
			vec = vec.normalize();
			
			float res = 0;
			
			for(int i = 1; i < len; i++) {

				int ix = (int)Math.floor(x + 0.5 + vec.x * i);
				int iy = (int)Math.floor(y + 0.5 + vec.y * i);
				int iz = (int)Math.floor(z + 0.5 + vec.z * i);
				
				res += world.getBlock(ix, iy, iz).getExplosionResistance(null);
			}
			
			if(res < 1)
				res = 1;
			
			float eRads = rads;
			eRads /= (float)res;
			eRads /= (float)(len * len);
			
			ContaminationUtil.contaminate(e, HazardType.RADIATION, ContaminationType.CREATIVE, eRads);
			ContaminationUtil.contaminate(e, HazardType.NEUTRON, ContaminationType.CREATIVE, eRads);
			if(e instanceof EntityPlayer && !RadiationConfig.disableNeutron) {
				EntityPlayer player = (EntityPlayer) e;
				for(int i = 0; i < player.inventory.mainInventory.length; i++) {
					HazardTypeNeutron.apply(player.inventory.getStackInSlot(i), eRads);
				}
				for(int i = 0; i < player.inventory.armorInventory.length; i++) {
					HazardTypeNeutron.apply(player.inventory.armorItemInSlot(i), eRads);
				}
			}
			
			if(len < 2) {
				e.attackEntityFrom(DamageSource.inFire, 100);
			}
		}
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}

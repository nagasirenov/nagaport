package com.hbm.tileentity.deco;

import java.util.List;

import com.hbm.entity.mob.glyphid.EntityGlyphid;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;

public class TileEntityLantern extends TileEntity implements net.minecraft.util.ITickable {
	
	@Override
	public void update() {
		
		if(!this.world.isRemote && this.world.getTotalWorldTime() % 20 == 0) {
			
			List<EntityGlyphid> glyphids = this.world.getEntitiesWithinAABB(EntityGlyphid.class, AxisAlignedBB.getBoundingBox(this.pos.getX() + 0.5, this.pos.getY() + 5.5, this.pos.getZ() + 0.5, this.pos.getX() + 0.5, this.pos.getY() + 5.5, this.pos.getZ() + 0.5).expand(7.5, 7.5, 7.5));
			
			for(EntityGlyphid glyphid : glyphids) {
				glyphid.addPotionEffect(new PotionEffect(Potion.blindness.id, 100, 0));
			}
		}
	}
	
	AxisAlignedBB bb = null;
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		
		if(bb == null) {
			bb = AxisAlignedBB.getBoundingBox(
					this.pos.getX(),
					this.pos.getY(),
					this.pos.getZ(),
					this.pos.getX() + 1,
					this.pos.getY() + 6,
					this.pos.getZ() + 1
					);
		}
		
		return bb;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return 65536.0D;
	}
}

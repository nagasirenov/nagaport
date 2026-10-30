package com.hbm.blocks;

import java.util.List;

import com.hbm.lib.ModDamageSource;
import com.hbm.main.MainRegistry;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class BlockGeysierDCM extends BlockContainer {
	// 1.7.10 type 0 cube; BlockContainer would make it INVISIBLE in 1.12 (render-type parity audit).
	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}


	public BlockGeysierDCM withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}


	public BlockGeysierDCM(Material rock) {
		super(rock);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityDCM();
	}

	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	public static class TileEntityDCM extends TileEntity implements net.minecraft.util.ITickable {

		public TileEntityDCM() {

		}

		@Override
		public void update() {
			if(!this.world.isRemote) {
				NBTTagCompound data = new NBTTagCompound();
				data.setDouble("posX", this.pos.getX() + 0.5);
				data.setDouble("posY", this.pos.getY() + 0);
				data.setDouble("posZ", this.pos.getZ() + 0.5);
				data.setString("type", "missileContrail");
				data.setFloat("scale", 1.5f);
				data.setDouble("moX", 0);
				data.setDouble("moY", 4);
				data.setDouble("moZ", 0);
				data.setInteger("maxAge", 100 + this.world.rand.nextInt(20));
				data.setInteger("color", 0xA4D7DD);
				MainRegistry.proxy.effectNT(data);

				vapor();
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

		// copy pasting without reading makes you create a fluid geyser that... zaps you to death with electricty?
		private void vapor() {
			List<Entity> entities = this.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(this.pos.getX() - 0.5, this.pos.getY() + 0.5, this.pos.getZ() - 0.5, this.pos.getX() + 1.5, this.pos.getY() + 60, this.pos.getZ() + 1.5));

			if(!entities.isEmpty()) {
				for(Entity e : entities) {
					if(e instanceof EntityLivingBase) {
						if(e.attackEntityFrom(ModDamageSource.acid, MathHelper.clamp(((EntityLivingBase) e).getMaxHealth() * 0.1F, (float) (3), (float) (20)))) {
							this.world.playSound(null, e.posX, e.posY, e.posZ, com.hbm.main.ModSoundEvents.resolve("random.fizz"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
						}
					}
				}
			}
		}

	}


}

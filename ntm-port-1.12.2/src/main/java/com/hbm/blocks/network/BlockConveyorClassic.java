package com.hbm.blocks.network;

import api.hbm.conveyor.IConveyorBelt;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.lib.RefStrings;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockConveyorClassic extends Block implements IConveyorBelt {

	@SideOnly(Side.CLIENT)
	protected IIcon sideIcon;

	public BlockConveyorClassic() {
		super(Material.iron);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		super.registerBlockIcons(iconRegister);
		this.sideIcon = iconRegister.registerIcon(RefStrings.MODID + ":conveyor_side");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {

		if((metadata == 2 || metadata == 3) && (side == 4 || side == 5))
			return this.sideIcon;
		if((metadata == 4 || metadata == 5) && (side == 2 || side == 3))
			return this.sideIcon;

		return super.getIcon(side, metadata);
	}

	@Override
	public boolean canItemStay(World world, int x, int y, int z, Vec3d itemPos) {
		return true;
	}

	@Override
	public Vec3d getTravelLocation(World world, int x, int y, int z, Vec3d itemPos, double speed) {

		ForgeDirection dir = getTravelDirection(world, x, y, z, itemPos, speed);
		//snapping point
		Vec3d snap = this.getClosestSnappingPosition(world, x, y, z, itemPos);
		itemPos = new Vec3d(MathHelper.clamp_double(itemPos.x, x, x + 1), itemPos.y, MathHelper.clamp_double(itemPos.z, z, z + 1));
		//snapping point + speed
		Vec3d dest = new Vec3d(snap.x - dir.offsetX * speed, snap.y - dir.offsetY * speed, snap.z - dir.offsetZ * speed);
		//delta to get to that point
		Vec3d motion = new Vec3d((dest.x - itemPos.x), (dest.y - itemPos.y), (dest.z - itemPos.z));
		double len = motion.length();
		//the effective destination towards "dest" after taking speed into consideration
		Vec3d ret = new Vec3d(itemPos.x + motion.x / len * speed, itemPos.y + motion.y / len * speed, itemPos.z + motion.z / len * speed);
		return ret;
	}

	public ForgeDirection getTravelDirection(World world, int x, int y, int z, Vec3d itemPos, double speed) {
		return ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z));
	}

	@Override
	public Vec3d getClosestSnappingPosition(World world, int x, int y, int z, Vec3d itemPos) {

		ForgeDirection dir = ForgeDirection.getOrientation(world.getBlockMetadata(x, y, z));

		double itemX = MathHelper.clamp_double(itemPos.x, x, x + 1);
		double itemZ = MathHelper.clamp_double(itemPos.z, z, z + 1);

		double posX = x + 0.5;
		double posZ = z + 0.5;

		if(dir.offsetX != 0) {
			posX = itemX;
		}
		if(dir.offsetZ != 0) {
			posZ = itemZ;
		}

		return new Vec3d(posX, y + 0.25, posZ);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {

		if(!world.isRemote) {

			if(entity instanceof EntityItem && entity.ticksExisted > 10 && !entity.isDead) {

				EntityMovingItem item = new EntityMovingItem(world);
				item.setItemStack(((EntityItem) entity).getEntityItem().copy());
				Vec3d pos = new Vec3d(entity.posX, entity.posY, entity.posZ);
				Vec3d snap = this.getClosestSnappingPosition(world, x, y, z, pos);
				item.setPositionAndRotation(snap.x, snap.y, snap.z, 0, 0);
				world.spawnEntityInWorld(item);

				entity.setDead();
			}
		}
	}

	@Override
	public int getRenderType() {
		return BlockConveyorBase.renderID;
	}

	@Override
	public boolean isOpaqueCube() {
		return false;
	}

	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}

	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.25F, 1.0F);
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 0.25, z + 1);
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {
		int i = MathHelper.floor_double(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		if(i == 0) {
			world.setBlockMetadataWithNotify(x, y, z, 2, 2);
		}
		if(i == 1) {
			world.setBlockMetadataWithNotify(x, y, z, 5, 2);
		}
		if(i == 2) {
			world.setBlockMetadataWithNotify(x, y, z, 3, 2);
		}
		if(i == 3) {
			world.setBlockMetadataWithNotify(x, y, z, 4, 2);
		}
	}
}

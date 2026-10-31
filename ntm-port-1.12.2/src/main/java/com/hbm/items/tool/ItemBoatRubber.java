package com.hbm.items.tool;

import java.util.List;

import com.hbm.entity.item.EntityBoatRubber;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class ItemBoatRubber extends Item {

	public ItemBoatRubber() {
		this.maxStackSize = 1;
		this.setCreativeTab(CreativeTabs.TRANSPORTATION);
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		float f = 1.0F;
		float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * f;
		float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * f;
		double posX = player.prevPosX + (player.posX - player.prevPosX) * (double) f;
		double posY = player.prevPosY + (player.posY - player.prevPosY) * (double) f + 1.62D - (double) player.getYOffset();
		double posZ = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) f;
		float compZ = MathHelper.cos(-yaw * 0.017453292F - (float) Math.PI);
		float compX = MathHelper.sin(-yaw * 0.017453292F - (float) Math.PI);
		float mult = -MathHelper.cos(-pitch * 0.017453292F);
		float lookY = MathHelper.sin(-pitch * 0.017453292F);
		float lookX = compX * mult;
		float lookZ = compZ * mult;
		double reach = 5.0D;

		Vec3d pos = new Vec3d(posX, posY, posZ);
		Vec3d target = pos.add((double) lookX * reach, (double) lookY * reach, (double) lookZ * reach);
		RayTraceResult mop = world.rayTraceBlocks(pos, target, true);

		if(mop == null) {
			return stack;

		} else {
			Vec3d look = player.getLook(f);
			boolean flag = false;
			double width = 1.0D;
			List list = world.getEntitiesWithinAABBExcludingEntity(player, player.getEntityBoundingBox().expand(look.x * reach, look.y * reach, look.z * reach).grow(width, width, width));

			for(int i = 0; i < list.size(); ++i) {
				Entity entity = (Entity) list.get(i);

				if(entity.canBeCollidedWith()) {
					float f10 = entity.getCollisionBorderSize();
					AxisAlignedBB axisalignedbb = entity.getEntityBoundingBox().grow((double) f10, (double) f10, (double) f10);

					if(axisalignedbb.contains(pos)) {
						flag = true;
					}
				}
			}

			if(flag) {
				return stack;

			} else {
				if(mop.typeOfHit == RayTraceResult.Type.BLOCK) {
					int x = mop.getBlockPos().getX();
					int y = mop.getBlockPos().getY();
					int z = mop.getBlockPos().getZ();

					if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.SNOW_LAYER) {
						--y;
					}

					EntityBoatRubber entityboat = new EntityBoatRubber(world, (double) ((float) x + 0.5F), (double) ((float) y + 1.0F), (double) ((float) z + 0.5F));
					entityboat.rotationYaw = (float) (((MathHelper.floor((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3) - 1) * 90);

					if(!world.getCollisionBoxes(entityboat, entityboat.getEntityBoundingBox().grow(-0.1D, -0.1D, -0.1D)).isEmpty()) {
						return stack;
					}

					if(!world.isRemote) {
						world.spawnEntity(entityboat);
					}

					if(!player.capabilities.isCreativeMode) {
						stack.shrink(1);
					}
				}

				return stack;
			}
		}
	}
}

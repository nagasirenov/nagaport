package com.hbm.items.special;

import java.util.List;

import com.hbm.entity.mob.EntityBFAngel;
import com.hbm.entity.mob.EntityDuck;
import com.hbm.entity.mob.EntityHunterChopper;
import com.hbm.entity.mob.EntityUFO;
import com.hbm.entity.mob.botprime.EntityBOTPrimeHead;
import com.hbm.items.ModItems;

import net.minecraft.block.Block;
import net.minecraft.block.BlockFence;
import com.hbm.blocks.generic.BlockMetalFence;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockLiquid;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IEntityLivingData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

public class ItemChopper extends Item {

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing face, float hitX, float hitY, float hitZ) {
		ItemStack stack = player.getHeldItem(hand);
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		
		if(world.isRemote) {
			return EnumActionResult.SUCCESS;
		} else {
			
			Block block = world.getBlockState(pos).getBlock();
			
			x += face.getXOffset();
			y += face.getYOffset();
			z += face.getZOffset();
			double offset = 0.0D;

			if(face == EnumFacing.UP && block instanceof BlockFence && !(block instanceof BlockMetalFence))
				offset = 0.5D;

			Entity entity = spawnCreature(world, stack.getItemDamage(), x + 0.5D, y + offset, z + 0.5D);

			if(entity != null) {
				if(entity instanceof EntityLivingBase && stack.hasDisplayName()) {
					((EntityLiving) entity).setCustomNameTag(stack.getDisplayName());
				}

				if(!player.capabilities.isCreativeMode) {
					stack.shrink(1);
				}
			}

			return EnumActionResult.SUCCESS;
		}
	}
	
	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		
		if(world.isRemote) {
			return new ActionResult<>(EnumActionResult.PASS, stack);
			
		} else {
			RayTraceResult movingobjectposition = this.rayTrace(world, player, true);

			if(movingobjectposition == null) {
				return new ActionResult<>(EnumActionResult.PASS, stack);
			} else {
				if(movingobjectposition.typeOfHit == RayTraceResult.Type.BLOCK) {
					net.minecraft.util.math.BlockPos pos = movingobjectposition.getBlockPos();
					int i = pos.getX();
					int j = pos.getY();
					int k = pos.getZ();

					if(!world.isBlockModifiable(player, pos)) {
						return new ActionResult<>(EnumActionResult.PASS, stack);
					}

					if(!player.canPlayerEdit(pos, movingobjectposition.sideHit, stack)) {
						return new ActionResult<>(EnumActionResult.PASS, stack);
					}

					if(world.getBlockState(pos).getBlock() instanceof BlockLiquid) {
						Entity entity = spawnCreature(world, stack.getItemDamage(), i, j, k);

						if(entity != null) {
							if(entity instanceof EntityLivingBase && stack.hasDisplayName()) {
								((EntityLiving) entity).setCustomNameTag(stack.getDisplayName());
							}

							if(!player.capabilities.isCreativeMode) {
								stack.shrink(1);
							}
							return new ActionResult<>(EnumActionResult.SUCCESS, stack);
						}
					}
				}

				return new ActionResult<>(EnumActionResult.PASS, stack);
			}
		}
	}

	public Entity spawnCreature(World world, int dmg, double x, double y, double z) {
		Entity entity = null;
		
		if(this == ModItems.spawn_chopper)
			entity = new EntityHunterChopper(world);
		
		if(this == ModItems.spawn_worm)
			entity = new EntityBOTPrimeHead(world);
		
		if(this == ModItems.spawn_ufo) {
			entity = new EntityUFO(world);
			((EntityUFO)entity).scanCooldown = 100;
			y += 35;
		}
		if(this == ModItems.spawn_angel) {
			entity = new EntityBFAngel(world);
			y += 25;
		}
		if(this == ModItems.spawn_duck)
			entity = new EntityDuck(world);
		
		if(entity != null) {

			EntityLiving entityliving = (EntityLiving) entity;
			entity.setLocationAndAngles(x, y, z, MathHelper.wrapDegrees(world.rand.nextFloat() * 360.0F), 0.0F);
			entityliving.rotationYawHead = entityliving.rotationYaw;
			entityliving.renderYawOffset = entityliving.rotationYaw;
			entityliving.onInitialSpawn(world.getDifficultyForLocation(new BlockPos(entity)), (IEntityLivingData) null);
			world.spawnEntity(entity);
		}

		return entity;
	}

	@Override
	public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {

		if(this == ModItems.spawn_worm) {
			list.add("Without a player in survival mode");
			list.add("to target, he struggles around a lot.");
			list.add("");
			list.add("He's doing his best so please show him");
			list.add("some consideration.");
		}
	}

}

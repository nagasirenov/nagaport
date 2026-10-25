package com.hbm.items.tool;

import java.util.List;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemBattery;
import com.hbm.util.BobMathUtil;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemAnchorRemote extends ItemBattery {

	public ItemAnchorRemote() {
		super(1_000_000, 10_000, 0);
	}

	@Override
	public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag) {

		long charge = maxCharge;

		if(itemstack.hasTagCompound())
			charge = getCharge(itemstack);

		if(itemstack.getItem() != ModItems.fusion_core && itemstack.getItem() != ModItems.energy_core) {
			list.add("Energy stored: " + BobMathUtil.getShortNumber(charge) + "/" + BobMathUtil.getShortNumber(maxCharge) + "HE");
		} else {
			String charge1 = BobMathUtil.getShortNumber((charge * 100) / this.maxCharge);
			list.add("Charge: " + charge1 + "%");
			list.add("(" + BobMathUtil.getShortNumber(charge) + "/" + BobMathUtil.getShortNumber(maxCharge) + "HE)");
		}

		list.add("Charge rate: " + BobMathUtil.getShortNumber(chargeRate) + "HE/t");
	}


		@Override
	public net.minecraft.util.EnumActionResult onItemUse(net.minecraft.entity.player.EntityPlayer player, net.minecraft.world.World world, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumHand hand, net.minecraft.util.EnumFacing facing, float hitX, float hitY, float hitZ) {
		net.minecraft.item.ItemStack stack = player.getHeldItem(hand);
		int side = facing != null ? facing.getIndex() : 0;
		return onItemUse(stack, player, world, pos.getX(), pos.getY(), pos.getZ(), side, hitX, hitY, hitZ) ? net.minecraft.util.EnumActionResult.SUCCESS : net.minecraft.util.EnumActionResult.PASS;
	}

public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ) {

		if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == ModBlocks.teleanchor) {

			if(!stack.hasTagCompound())
				stack.setTagCompound(new NBTTagCompound());

			stack.getTagCompound().setInteger("x", x);
			stack.getTagCompound().setInteger("y", y);
			stack.getTagCompound().setInteger("z", z);

			return true;
		}

		return false;
	}

	@Override
	public net.minecraft.util.ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, net.minecraft.util.EnumHand nativeHand) {
		ItemStack stack = player.getHeldItem(nativeHand);
		return new net.minecraft.util.ActionResult<ItemStack>(net.minecraft.util.EnumActionResult.PASS, onItemRightClick(stack, world, player));
	}

	public ItemStack onItemRightClick(ItemStack stack, World world, EntityPlayer player) {

		if(player.isSneaking() || world.isRemote) {
			return stack;
		}

		if(!stack.hasTagCompound()) {
			world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("random.orb"), net.minecraft.util.SoundCategory.PLAYERS, 0.25F, 0.75F);
			return stack;
		}

		if(this.getCharge(stack) < 10_000) {
			world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("random.orb"), net.minecraft.util.SoundCategory.PLAYERS, 0.25F, 0.75F);
			return stack;
		}

		int x = stack.getTagCompound().getInteger("x");
		int y = stack.getTagCompound().getInteger("y");
		int z = stack.getTagCompound().getInteger("z");

		world.getChunk(x >> 4, z >> 4);

		if(com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == ModBlocks.teleanchor) {

			if(player.isRiding()) {
				player.dismountRidingEntity();
			}

			world.newExplosion(player, x + 0.5, y + 1 + player.height / 2, z + 0.5, 2F, false, false);
			com.hbm.sound.VanillaWorldSoundEffects.play(world, player.posX, player.posY, player.posZ, "mob.endermen.portal", 1.0F, 1.0F);
			player.setPositionAndUpdate(x + 0.5, y + 1, z + 0.5);
			//world.playSoundEffect(player.posX, player.posY, player.posZ, "mob.endermen.portal", 1.0F, 1.0F);
			player.fallDistance = 0.0F;

			for(int i = 0; i < 32; ++i) {
				world.spawnParticle(net.minecraft.util.EnumParticleTypes.PORTAL, player.posX, player.posY + player.getRNG().nextDouble() * 2.0D, player.posZ, player.getRNG().nextGaussian(), 0.0D, player.getRNG().nextGaussian());
			}

			this.dischargeBattery(stack, 10_000);

		} else {
			world.playSound(null, player.posX, player.posY, player.posZ, com.hbm.main.ModSoundEvents.resolve("random.orb"), net.minecraft.util.SoundCategory.PLAYERS, 0.25F, 0.75F);
		}

		return stack;
	}
}

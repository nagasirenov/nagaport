package com.hbm.blocks.machine.rbmk;

import com.hbm.handler.BossSpawnHandler;
import com.hbm.items.machine.ItemRBMKLid;
import com.hbm.items.machine.ItemRBMKRod;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.rbmk.RBMKDials;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKRod;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos;

public class RBMKRod extends RBMKBase {

	public boolean moderated = false;

	public RBMKRod(boolean moderated) {
		super();
		this.moderated = moderated;
	}

	public IIcon inner;
	public IIcon fuel;

	@Override public boolean isOpaqueCube() { return false; }
	@Override public boolean renderAsNormalBlock() { return false; }

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= this.offset) return new TileEntityRBMKRod();
		if(hasExtra(meta)) return new TileEntityProxyCombo().inventory();
		return null;
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		if(meta >= this.offset && !RBMKDials.getMeltdownsDisabled(world)) {
			TileEntityRBMKRod tile = (TileEntityRBMKRod) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			if(tile != null && tile.explodeOnBroken) {
				if(tile.slots[0] != null && tile.slots[0].getItem() instanceof ItemRBMKRod && ItemRBMKRod.getHullHeat(tile.slots[0]) >= 1500) {
					tile.meltdown();
				}
			}
		}
		super.breakBlock(world, x, y, z, block, meta);
		world.removeTileEntity(new BlockPos(x, y, z));
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		super.registerBlockIcons(reg);
		this.inner = reg.registerIcon(this.getRBMKTexture() + "_inner");
		this.fuel = reg.registerIcon(this.getRBMKTexture() + "_fuel");
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;

		BossSpawnHandler.markFBI(player);

		if(world.isRemote) return true;

		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return false;

		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
		if(!(te instanceof TileEntityRBMKRod)) return false;
		TileEntityRBMKRod rbmk = (TileEntityRBMKRod) te;

		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() instanceof ItemRBMKLid) {
			if(!rbmk.hasLid()) return false;
		}

		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() instanceof ItemRBMKRod && rbmk.slots[0] == null) {
			rbmk.slots[0] = com.hbm.util.NativeHeldItem.getMainHandNullable(player).copy();
			rbmk.slots[0].setCount(1);
			if(!player.capabilities.isCreativeMode) com.hbm.util.NativeHeldItem.getMainHandNullable(player).shrink(1);
			world.playSound(null, x + 0.5, y + 0.5, z + 0.5, com.hbm.main.ModSoundEvents.resolve("hbm:item.upgradePlug"), net.minecraft.util.SoundCategory.BLOCKS, 1.0F, 1.0F);
			return false;
		}

		if(!player.isSneaking()) {
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos[0], pos[1], pos[2]);
			return true;
		} else {
			return true;
		}
	}

	@Override
	public int getRenderType(){
		return this.renderIDRods;
	}
}

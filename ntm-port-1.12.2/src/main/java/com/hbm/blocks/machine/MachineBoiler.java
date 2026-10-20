package com.hbm.blocks.machine;

import java.util.ArrayList;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.Mats;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class MachineBoiler extends Block {

	private final boolean isActive;

	@SideOnly(Side.CLIENT)
	private IIcon iconFront;
	@SideOnly(Side.CLIENT)
	private IIcon iconSide;

	public MachineBoiler(boolean blockState) {
		super(Material.IRON);
		isActive = blockState;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {

		if(this == ModBlocks.machine_boiler_off) {
			this.iconFront = iconRegister.registerIcon(RefStrings.MODID + (this.isActive ? ":machine_boiler_front_lit" : ":machine_boiler_front"));
			this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":machine_boiler_base");
			this.iconSide = iconRegister.registerIcon(RefStrings.MODID + ":machine_boiler_side");
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {

		if(side == 0 || side == 1)
			return this.blockIcon;

		return metadata == 0 && side == 3 ? this.iconFront : (side == metadata ? this.iconFront : this.iconSide);
	}

	@Override
	public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		ret.add(new ItemStack(ModItems.scraps, 3 + world.rand.nextInt(4), Mats.MAT_STEEL.id));
		return ret;
	}

	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);
		this.setDefaultDirection(world, x, y, z);
	}

	private void setDefaultDirection(World world, int x, int y, int z) {
		if(!world.isRemote) {
			Block block1 = com.hbm.world.WorldBlockState.read(world, x, y, z - 1).getBlock();
			Block block2 = com.hbm.world.WorldBlockState.read(world, x, y, z + 1).getBlock();
			Block block3 = com.hbm.world.WorldBlockState.read(world, x - 1, y, z).getBlock();
			Block block4 = com.hbm.world.WorldBlockState.read(world, x + 1, y, z).getBlock();

			byte b0 = 3;

			if(block1.func_149730_j() && !block2.func_149730_j()) {
				b0 = 3;
			}
			if(block2.func_149730_j() && !block1.func_149730_j()) {
				b0 = 2;
			}
			if(block3.func_149730_j() && !block4.func_149730_j()) {
				b0 = 5;
			}
			if(block4.func_149730_j() && !block3.func_149730_j()) {
				b0 = 4;
			}

			world.setBlockMetadataWithNotify(x, y, z, b0, 2);
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

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

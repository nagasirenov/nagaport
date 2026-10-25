package com.hbm.blocks.fluid;

import java.util.Random;

import com.hbm.lib.RefStrings;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidFinite;
import net.minecraftforge.fluids.Fluid;

public class GenericFiniteFluid extends BlockFluidFinite {

	@SideOnly(Side.CLIENT)
	public static IIcon stillIcon;
	@SideOnly(Side.CLIENT)
	public static IIcon flowingIcon;
	public Random rand = new Random();
	
	private String stillName;
	private String flowingName;

	public GenericFiniteFluid(Fluid fluid, Material material, String still, String flowing) {
		super(fluid, material);
		setCreativeTab(null);
		stillName = still;
		flowingName = flowing;
		displacements.put(this, false);
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return (side == 0 || side == 1) ? stillIcon : flowingIcon;
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		stillIcon = register.registerIcon(RefStrings.MODID + ":" + stillName);
		flowingIcon = register.registerIcon(RefStrings.MODID + ":" + flowingName);
	}
	
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta) {
		return this.quantaPerBlock - 1;
	}
}

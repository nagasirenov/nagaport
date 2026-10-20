package com.hbm.blocks.machine;

import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.TileEntityMachineDetector;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.tileentity.TileEntity;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class PowerDetector extends BlockContainer {

	@SideOnly(Side.CLIENT)
	private IIcon iconOn;

	public PowerDetector(Material mat) {
		super(mat);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		super.registerBlockIcons(iconRegister);
		this.iconOn = iconRegister.registerIcon(RefStrings.MODID + ":machine_detector");
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return metadata == 1 ? iconOn : blockIcon;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityMachineDetector();
	}
	
    public boolean canProvidePower(net.minecraft.block.state.IBlockState state) {
        return true;
    }
    
    public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return world.getBlockMetadata(x, y, z) == 1 ? 15 : 0;
    }

}

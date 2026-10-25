package com.hbm.blocks.network;

import com.hbm.lib.RefStrings;
import com.hbm.tileentity.network.TileEntityCableSwitch;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class CableSwitch extends BlockContainer {

	protected IIcon blockIcon;

	@SideOnly(Side.CLIENT)
	private IIcon iconOn;

	public CableSwitch(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconOn = iconRegister.registerIcon(RefStrings.MODID + ":cable_switch_on");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":cable_switch_off");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return metadata == 1 ? iconOn : blockIcon;
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityCableSwitch();
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;

		if(world.isRemote)
		{
			return true;
		} else if(!player.isSneaking())
		{
			int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
			if(meta == 0) {
				com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 1, 2);
				com.hbm.sound.WorldSoundEffects.play(world, x, y, z, "hbm:block.reactorStart", 1.0F, 1.0F);
			} else {
				com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 0, 2);
				com.hbm.sound.WorldSoundEffects.play(world, x, y, z, "hbm:block.reactorStart", 1.0F, 0.85F);
			}

			TileEntityCableSwitch te = (TileEntityCableSwitch) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			te.updateState();

			return true;
		} else {
			return false;
		}
	}
}

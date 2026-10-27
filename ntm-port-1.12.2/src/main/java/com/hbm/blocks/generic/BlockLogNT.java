package com.hbm.blocks.generic;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockLog;
import com.hbm.render.icon.IIconRegister;
import com.hbm.render.icon.IIcon;

public class BlockLogNT extends BlockLog {

	protected IIcon[] field_150167_a;
	protected IIcon[] field_150166_b;
	private String sideIcon;
	private String topIcon;

	public BlockLogNT(String sideIcon, String topIcon) {
		this.sideIcon = sideIcon;
		this.topIcon = topIcon;
		this.setDefaultState(this.blockState.getBaseState().withProperty(LOG_AXIS, BlockLog.EnumAxis.Y));
	}

	@Override
	public net.minecraft.block.state.IBlockState getStateFromMeta(int meta) {
		BlockLog.EnumAxis axis;
		switch(meta & 12) {
		case 4:
			axis = BlockLog.EnumAxis.X;
			break;
		case 8:
			axis = BlockLog.EnumAxis.Z;
			break;
		case 12:
			axis = BlockLog.EnumAxis.NONE;
			break;
		default:
			axis = BlockLog.EnumAxis.Y;
		}
		return this.getDefaultState().withProperty(LOG_AXIS, axis);
	}

	@Override
	public int getMetaFromState(net.minecraft.block.state.IBlockState state) {
		switch(state.getValue(LOG_AXIS)) {
		case X:
			return 4;
		case Z:
			return 8;
		case NONE:
			return 12;
		default:
			return 0;
		}
	}

	@Override
	protected net.minecraft.block.state.BlockStateContainer createBlockState() {
		return new net.minecraft.block.state.BlockStateContainer(this, LOG_AXIS);
	}

	/** NTM construction hook for the native protected sound setter. */
	public BlockLogNT withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		this.field_150167_a = new IIcon[1];
		this.field_150166_b = new IIcon[1];
		this.field_150167_a[0] = reg.registerIcon(sideIcon);
		this.field_150166_b[0] = reg.registerIcon(topIcon);
	}

}

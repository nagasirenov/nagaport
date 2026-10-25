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

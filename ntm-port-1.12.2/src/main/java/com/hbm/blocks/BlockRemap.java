package com.hbm.blocks;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

import java.util.ArrayList;
import java.util.Random;

public class BlockRemap extends Block implements ILookOverlay {

	public Block remapBlock;
	public int remapMeta;

	protected BlockRemap(Block block, int meta) {
		super(Material.TNT);
		this.remapBlock = block;
		this.remapMeta = meta;
		this.setTickRandomly(true);
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int meta, int side) {
		if(this.remapBlock instanceof BlockBase) return ((BlockBase) this.remapBlock).getIcon(side, meta);
		return null;
	}

	@Override
	public net.minecraft.item.Item getItemDropped(net.minecraft.block.state.IBlockState state, Random rand, int fortune) {
		int meta = state.getBlock().getMetaFromState(state);
		return this.remapBlock.getItemDropped(this.remapBlock.getStateFromMeta(meta), rand, fortune);
	}

	@Override
	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random rand) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();
		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y, z, this.remapBlock, this.remapMeta, 2);
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		ILookOverlay.printGeneric(event, "Compatibility block, will convert on update tick.", 0xffff00, 0x404000, new ArrayList());
	}
}

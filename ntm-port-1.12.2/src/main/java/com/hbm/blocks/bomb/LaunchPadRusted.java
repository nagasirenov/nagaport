package com.hbm.blocks.bomb;

import java.util.Random;

import com.hbm.blocks.BlockDummyable;
import com.hbm.interfaces.IBomb;
import com.hbm.tileentity.bomb.TileEntityLaunchPadRusted;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;

public class LaunchPadRusted extends BlockDummyable implements IBomb {

	public LaunchPadRusted(Material mat) {
		super(mat);
		this.bounding.add(new AxisAlignedBB(-1.5D, 0D, -1.5D, -0.5D, 1D, -0.5D));
		this.bounding.add(new AxisAlignedBB(0.5D, 0D, -1.5D, 1.5D, 1D, -0.5D));
		this.bounding.add(new AxisAlignedBB(-1.5D, 0D, 0.5D, -0.5D, 1D, 1.5D));
		this.bounding.add(new AxisAlignedBB(0.5D, 0D, 0.5D, 1.5D, 1D, 1.5D));
		this.bounding.add(new AxisAlignedBB(-0.5D, 0.5D, -1.5D, 0.5D, 1D, 1.5D));
		this.bounding.add(new AxisAlignedBB(-1.5D, 0.5D, -0.5D, 1.5D, 1D, 0.5D));
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityLaunchPadRusted();
		return null;
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState i, Random rand, int j) {
		return null;
	}
	
	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(); int y = nativePos.getY(); int z = nativePos.getZ();
		return this.standardOpenBehavior(world, x, y, z, player, 0);
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	public BombReturnCode explode(World world, int x, int y, int z) {
		
		if(!world.isRemote) {
			
			int[] corePos = findCore(world, x, y, z);
			if(corePos != null){
				TileEntity core = world.getTileEntity(new net.minecraft.util.math.BlockPos(corePos[0], corePos[1], corePos[2]));
				if(core instanceof TileEntityLaunchPadRusted){
					TileEntityLaunchPadRusted entity = (TileEntityLaunchPadRusted)core;
					return entity.launch();
				}
			}
		}
		
		return BombReturnCode.UNDEFINED;
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block blockIn){
		
		if(!world.isRemote){
			
			int[] corePos = findCore(world, x, y, z);
			if(corePos != null){
				TileEntity core = world.getTileEntity(new net.minecraft.util.math.BlockPos(corePos[0], corePos[1], corePos[2]));
				if(core instanceof TileEntityLaunchPadRusted){
					TileEntityLaunchPadRusted launchpad = (TileEntityLaunchPadRusted)core;
					launchpad.updateRedstonePower(x, y, z);
				}
			}
		}
		super.onNeighborBlockChange( world, x, y, z, blockIn);
	}
}

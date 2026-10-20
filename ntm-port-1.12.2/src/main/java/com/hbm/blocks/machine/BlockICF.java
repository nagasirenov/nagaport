package com.hbm.blocks.machine;

import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.RefStrings;
import com.hbm.render.block.ct.CTContext;
import com.hbm.render.block.ct.IBlockCT;
import com.hbm.tileentity.machine.TileEntityICFController;

import api.hbm.energymk2.IEnergyReceiverMK2;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockICF extends BlockContainer implements IBlockCT {
	
	
	public BlockICF(Material mat) {
		super(mat);
        setDefaultState(blockState.getBaseState().withProperty(CTContext.METADATA, 0));
	}

    @Override protected net.minecraft.block.state.BlockStateContainer createBlockState() { return CTContext.stateContainer(this); }
    @Override public net.minecraft.block.state.IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(CTContext.METADATA, metadata & 15); }
    @Override public int getMetaFromState(net.minecraft.block.state.IBlockState state) { return state.getValue(CTContext.METADATA); }
    @Override public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) { return net.minecraft.util.EnumBlockRenderType.MODEL; }
    @Override public net.minecraft.block.state.IBlockState getExtendedState(net.minecraft.block.state.IBlockState state, IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
        return CTContext.extend(state, world, pos, this);
    }

	
	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState i, Random rand, int j)  {
		return null;
	}

    @Override public int getCTMaterial(net.minecraft.block.state.IBlockState state) { return getMetaFromState(state) == 1 ? 1 : 0; }


	@Override
	public boolean canConnect(IBlockAccess world, int x, int y, int z, Block block) {
		return block == ModBlocks.icf_block || block == ModBlocks.icf_controller;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityBlockICF();
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
		
		TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		
		if(tile instanceof TileEntityBlockICF) {
			TileEntityBlockICF icf = (TileEntityBlockICF) tile;
			world.removeTileEntity(x, y, z);
			if(icf.block != null) {
				world.setBlock(x, y, z, icf.block, icf.meta, 3);
				TileEntity controller = world.getTileEntity(new net.minecraft.util.math.BlockPos(icf.coreX, icf.coreY, icf.coreZ));
				
				if(controller instanceof TileEntityICFController) {
					((TileEntityICFController) controller).assembled = false;
				}
			}
		} else {
			world.removeTileEntity(x, y, z);
		}
		super.breakBlock(world, x, y, z, block, meta);
	}
	
	public static class TileEntityBlockICF extends TileEntity implements IEnergyReceiverMK2, net.minecraft.util.ITickable {

		public Block block;
		public int meta;
		public int coreX;
		public int coreY;
		public int coreZ;
		
		@Override
		public void update() {
			
			if(!this.world.isRemote) {
				
				if(this.world.getTotalWorldTime() % 20 == 0 && block != null) {
					
					TileEntityICFController controller = getCore();
					
					if(controller != null) {
						if(!controller.assembled) {
							this.getBlockType().breakBlock(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.getBlockType(), this.getBlockMetadata());
						}
					} else if(this.world.getChunkProvider().chunkExists(coreX >> 4, coreZ >> 4)) {
						this.getBlockType().breakBlock(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ(), this.getBlockType(), this.getBlockMetadata());
					}
				}
			}
		}
		
		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			
			block = Block.getBlockById(nbt.getInteger("block"));
			if(block != Blocks.AIR) {
				meta = nbt.getInteger("meta");
				coreX = nbt.getInteger("cX");
				coreY = nbt.getInteger("cY");
				coreZ = nbt.getInteger("cZ");
			} else {
				block = null;
			}
		}
		
		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);

			if(block != null) {
				nbt.setInteger("block", Block.getIdFromBlock(block));
				nbt.setInteger("meta", meta);
				nbt.setInteger("cX", coreX);
				nbt.setInteger("cY", coreY);
				nbt.setInteger("cZ", coreZ);
			}
			return nbt;
		}
		
		@Override
		public void markDirty() {
			if(this.world != null) {
				this.world.markTileEntityChunkModified(this.pos.getX(), this.pos.getY(), this.pos.getZ(), this);
			}
		}
		
		public TileEntityICFController cachedCore;
		
		protected TileEntityICFController getCore() {
			
			if(cachedCore != null && !cachedCore.isInvalid()) return cachedCore;

			if(this.world.getChunkProvider().chunkExists(coreX >> 4, coreZ >> 4)) {
				
				TileEntity tile = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(coreX, coreY, coreZ));
				if(tile instanceof TileEntityICFController) {
					TileEntityICFController controller = (TileEntityICFController) tile;
					cachedCore = controller;
					return controller;
				}
			}
			
			return null;
		}

		@Override public long getPower() {
			if(this.getBlockMetadata() != 1) return 0;
			if(block == null) return 0;
			TileEntityICFController controller = this.getCore();
			if(controller != null) return controller.getPower();
			
			return 0;
		}

		@Override public void setPower(long power) {
			if(this.getBlockMetadata() != 1) return;
			if(block == null) return;
			TileEntityICFController controller = this.getCore();
			if(controller != null) controller.setPower(power);
		}

		@Override public long getMaxPower() {
			if(this.getBlockMetadata() != 1) return 0;
			if(block == null) return 0;
			TileEntityICFController controller = this.getCore();
			if(controller != null) return controller.getMaxPower();
			
			return 0;
		}

		public boolean isLoaded = true;
		
		@Override
		public boolean isLoaded() {
			return isLoaded;
		}

		@Override
		public void onChunkUnload() {
			super.onChunkUnload();
			this.isLoaded = false;
		}
		
		@Override
		public boolean canConnect(ForgeDirection dir) {
			if(this.getBlockMetadata() != 1) return false;
			return dir != ForgeDirection.UNKNOWN;
		}
	}
}

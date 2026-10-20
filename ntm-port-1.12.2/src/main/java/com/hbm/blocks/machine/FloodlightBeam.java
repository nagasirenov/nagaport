package com.hbm.blocks.machine;

import com.hbm.blocks.machine.Floodlight.TileEntityFloodlight;
import com.hbm.util.fauxpointtwelve.BlockPos;

import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class FloodlightBeam extends BlockBeamBase {

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFloodlightBeam();
	}
	
	public static class TileEntityFloodlightBeam extends TileEntity implements net.minecraft.util.ITickable {
		
		public TileEntityFloodlight cache;
		public int sourceX;
		public int sourceY;
		public int sourceZ;
		public int index;
		
		@Override
		public void update() {
			
			if(!this.world.isRemote && this.world.getTotalWorldTime() % 5 == 0) {
				
				if(cache == null) {
					
					if(this.world.getChunkProvider().chunkExists(sourceX >> 4, sourceZ >> 4)) {
						TileEntity tile = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(sourceX, sourceY, sourceZ));
						if(tile instanceof TileEntityFloodlight) {
							cache = (TileEntityFloodlight) tile; // chunk is loaded, tile exists -> cache
						} else {
							{ net.minecraft.world.World nativePlacementWorld = this.world; int nativePlacementX = this.pos.getX(); int nativePlacementY = this.pos.getY(); int nativePlacementZ = this.pos.getZ(); net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 2; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); } // chunk is loaded, tile does not exist -> delete self
						}
					}
				}
				
				if((cache != null && (cache.isInvalid() || !cache.isOn || !new BlockPos(this.pos.getX(), this.pos.getY(), this.pos.getZ()).equals(cache.lightPos[index]))) || sourceY == 0) {
					{ net.minecraft.world.World nativePlacementWorld = this.world; int nativePlacementX = this.pos.getX(); int nativePlacementY = this.pos.getY(); int nativePlacementZ = this.pos.getZ(); net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 2; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
				}
			}
		}
		
		public void setSource(TileEntityFloodlight floodlight, int x, int y, int z, int i) {
			cache = floodlight;
			sourceX = x;
			sourceY = y;
			sourceZ = z;
			index = i;
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.sourceX = nbt.getInteger("sourceX");
			this.sourceY = nbt.getInteger("sourceY");
			this.sourceZ = nbt.getInteger("sourceZ");
			this.index = nbt.getInteger("index");
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setInteger("sourceX", sourceX);
			nbt.setInteger("sourceY", sourceY);
			nbt.setInteger("sourceZ", sourceZ);
			nbt.setInteger("index", index);
			return nbt;
		}
	}
}

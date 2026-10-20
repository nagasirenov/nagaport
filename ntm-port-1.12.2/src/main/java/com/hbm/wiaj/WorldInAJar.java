package com.hbm.wiaj;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.init.Biomes;

/**
 * Small virtual block access used by jar scenes and construction previews.
 * It can handle blocks, and not a whole lot else.
 * @author hbm
 */
public class WorldInAJar implements IBlockAccess {
	
	public int sizeX;
	public int sizeY;
	public int sizeZ;
	
	public int lightlevel = 15;

	private Block[][][] blocks;
	private short[][][] meta;
	private TileEntity[][][] tiles;
	
	public WorldInAJar(int x, int y, int z) {
		this.sizeX = x;
		this.sizeY = y;
		this.sizeZ = z;
		
		this.blocks = new Block[x][y][z];
		this.meta = new short[x][y][z];
		this.tiles = new TileEntity[x][y][z];
	}
	
	public void nuke() {
		
		this.blocks = new Block[sizeX][sizeY][sizeZ];
		this.meta = new short[sizeX][sizeY][sizeZ];
		this.tiles = new TileEntity[sizeX][sizeY][sizeZ];
	}

	public Block getBlock(int x, int y, int z) {
		if(x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ)
			return Blocks.AIR;
		
		return this.blocks[x][y][z] != null ? this.blocks[x][y][z] : Blocks.AIR;
	}
	
	@Override
	public IBlockState getBlockState(BlockPos pos) {
		Block block = getBlock(pos.getX(), pos.getY(), pos.getZ());
		return block.getStateFromMeta(getBlockMetadata(pos.getX(), pos.getY(), pos.getZ()));
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos) {
		return getTileEntity(pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public WorldType getWorldType() {
		return WorldType.DEFAULT;
	}

	public void setBlock(int x, int y, int z, Block b, int meta) {
		if(x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ)
			return;

		this.blocks[x][y][z] = b;
		this.meta[x][y][z] = (short)meta;
	}

	public int getBlockMetadata(int x, int y, int z) {
		if(x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ)
			return 0;
		
		return this.meta[x][y][z];
	}

	//shaky, we may kick tile entities entirely and rely on outside-the-world tile actors for rendering
	//might still come in handy for manipulating things using dummy tiles, like cable connections
	public TileEntity getTileEntity(int x, int y, int z) {
		if(x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ)
			return null;
		
		return this.tiles[x][y][z];
	}
	
	public void setTileEntity(int x, int y, int z, TileEntity tile) {
		if(x < 0 || x >= sizeX || y < 0 || y >= sizeY || z < 0 || z >= sizeZ)
			return;
		
		this.tiles[x][y][z] = tile;
	}

	//always render fullbright, if the situation requires it we could add a very rudimentary system that
	//darkens blocks if there is a solid one above
	@Override
	@SideOnly(Side.CLIENT)
	public int getCombinedLight(BlockPos pos, int blockBrightness) {
		return lightlevel;
	}

	//redstone could theoretically be implemented, but we will wait for now
	@Override
	public int getStrongPower(BlockPos pos, EnumFacing side) {
		return 0;
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {
		IBlockState state = getBlockState(pos);
		return state.getBlock().isAir(state, this, pos);
	}

	//biomes don't matter to us, if the situation requires it we could implement a primitive biome mask
	@Override
	@SideOnly(Side.CLIENT)
	public Biome getBiome(BlockPos pos) {
		return Biomes.PLAINS;
	}

	@SideOnly(Side.CLIENT)
	public int getHeight() {
		return this.sizeY;
	}

	public boolean extendedLevelsInChunkCache() {
		return false;
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {
		if (side == null) return _default;
		IBlockState state = getBlockState(pos);
		return state.getBlock().isSideSolid(state, this, pos, side);
	}
}

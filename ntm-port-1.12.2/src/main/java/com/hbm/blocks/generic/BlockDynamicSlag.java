package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.material.MaterialShapes;
import com.hbm.inventory.material.Mats;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.inventory.material.NTMMaterial;
import com.hbm.items.machine.ItemScraps;
import com.hbm.render.icon.RGBMutatorInterpolatedComponentRemap;
import com.hbm.render.icon.TextureAtlasSpriteMutatable;
import com.hbm.render.icon.LegacyIconRegister;
import com.hbm.util.Compat;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockDynamicSlag extends BlockContainer {
	// 1.7.10 type 0 cube; BlockContainer would make it INVISIBLE in 1.12 (render-type parity audit).
	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.MODEL;
	}


	@SideOnly(Side.CLIENT)
	private IIcon blockIcon;

	private HashMap<NTMMaterial, IIcon> iconMap = new HashMap();

	public BlockDynamicSlag() {
		super(Material.IRON);
		this.useNeighborBrightness = true;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntitySlag();
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		if(!(reg instanceof LegacyIconRegister)) throw new IllegalArgumentException("BlockDynamicSlag requires the NTM texture-atlas adapter");
		if(getRegistryName() == null) throw new IllegalStateException("BlockDynamicSlag must be registered before its icon callback");
		LegacyIconRegister adapter = (LegacyIconRegister) reg;
		blockIcon = reg.registerIcon(getRegistryName().toString());
		iconMap.clear();
		for(NTMMaterial mat : Mats.orderedList) {
			if(mat.solidColorLight != mat.solidColorDark) {
				String placeholderName = getRegistryName().toString() + "-" + mat.names[0];
				TextureAtlasSpriteMutatable mutableIcon = new TextureAtlasSpriteMutatable(placeholderName, new RGBMutatorInterpolatedComponentRemap(0xFFFFFF, 0x505050, mat.solidColorLight, mat.solidColorDark)).setBlockAtlas();
				iconMap.put(mat, adapter.registerGeneratedSprite(mutableIcon));
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(IBlockAccess world, int x, int y, int z, int side) {

		TileEntitySlag tile = (TileEntitySlag) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(tile != null && tile.mat != null) {
			IIcon override = iconMap.get(tile.mat);
			if(override != null) {
				return override;
			}
		}

		return this.blockIcon;
	}

	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {

		TileEntitySlag tile = (TileEntitySlag) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(tile != null && tile.mat != null) {
			if(!iconMap.containsKey(tile.mat)) {
				return tile.mat.moltenColor;
			}
		}

		return 0xffffff;
	}

	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntitySlag tile = (TileEntitySlag) world.getTileEntity(pos);
		if(tile != null) {
			return new AxisAlignedBB(0F, 0F, 0F, 1F, (float) tile.amount / (float) TileEntitySlag.maxAmount, 1F);
		}
		return FULL_BLOCK_AABB;
	}

	@Override
	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random rand) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();

		TileEntity s = Compat.getTileStandard(world, x, y, z);
		TileEntity b = Compat.getTileStandard(world, x, y - 1, z);

		/* Error here, delete the block */
		if(s == null || !(s instanceof TileEntitySlag)) {
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			return;
		}

		TileEntitySlag self = (TileEntitySlag) s;

		/* Flow down */
		if(com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(x, y - 1, z)) && y > 0) {
			com.hbm.world.WorldBlockState.writeLegacyBlock(world, x, y - 1, z, ModBlocks.slag);
			TileEntitySlag tile = (TileEntitySlag) Compat.getTileStandard(world, x, y - 1, z);
			tile.mat = self.mat;
			tile.amount = self.amount;
			com.hbm.world.WorldBlockState.markBlockForUpdate(world, x, y - 1, z);
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			return;
		} else if(b instanceof TileEntitySlag) {

			TileEntitySlag below = (TileEntitySlag) b;

			if(below.mat == self.mat && below.amount < TileEntitySlag.maxAmount) {
				int transfer = Math.min(TileEntitySlag.maxAmount - below.amount, self.amount);
				below.amount += transfer;
				self.amount -= transfer;

				if(self.amount <= 0){
					com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
				} else {
					com.hbm.world.WorldBlockState.markBlockForUpdate(world, x, y, z);
				}

				com.hbm.world.WorldBlockState.markBlockForUpdate(world, x, y - 1, z);
				world.scheduleUpdate(new net.minecraft.util.math.BlockPos(x, y - 1, z), ModBlocks.slag, 1);
				return;
			}
		}

		/* Flow sideways, no neighbors */
		ForgeDirection[] sides = new ForgeDirection[] { ForgeDirection.NORTH, ForgeDirection.SOUTH, ForgeDirection.EAST, ForgeDirection.WEST };
		int count = 0;
		for(ForgeDirection dir : sides) {
			int iX = x + dir.offsetX;
			int iZ = z + dir.offsetZ;

			if(com.hbm.world.WorldBlockState.read(world, iX, y, iZ).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(iX, y, iZ))) {
				count++;
			}
		}

		if(self.amount >= self.maxAmount / 5 && count > 0) {
			int toSpread = Math.max(self.amount / (count * 2), 1);

			for(ForgeDirection dir : sides) {
				int iX = x + dir.offsetX;
				int iZ = z + dir.offsetZ;

				if(com.hbm.world.WorldBlockState.read(world, iX, y, iZ).getBlock().isReplaceable(world, new net.minecraft.util.math.BlockPos(iX, y, iZ))) {
					com.hbm.world.WorldBlockState.writeLegacyBlock(world, iX, y, iZ, ModBlocks.slag);
					TileEntitySlag tile = (TileEntitySlag) Compat.getTileStandard(world, iX, y, iZ);
					com.hbm.world.WorldBlockState.markBlockForUpdate(world, iX, y, iZ);
					world.scheduleUpdate(new net.minecraft.util.math.BlockPos(iX, y, iZ), ModBlocks.slag, 1);
					tile.mat = self.mat;
					tile.amount = toSpread;
					self.amount -= toSpread;
					com.hbm.world.WorldBlockState.markBlockForUpdate(world, x, y, z);
				}
			}
		}
	}


		@Override
	public void onBlockHarvested(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, net.minecraft.entity.player.EntityPlayer player) {
		int meta = state.getBlock().getMetaFromState(state);
		onBlockHarvested(worldIn, pos.getX(), pos.getY(), pos.getZ(), meta, player);
	}

public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player) {

		if(!player.capabilities.isCreativeMode) {
			harvesters.set(player);
			this.dropBlockAsItem(world, new net.minecraft.util.math.BlockPos(x, y, z), world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)), 0);
			harvesters.set(null);
		}
	}

	@Override
	public void harvestBlock(World worldIn, EntityPlayer player, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, @javax.annotation.Nullable net.minecraft.tileentity.TileEntity te, ItemStack stack) {
		player.addStat(StatList.getBlockStats(this));
		player.addExhaustion(0.025F);
	}


		@Override
	public void getDrops(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> drops, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, int fortune) {
		int meta = state.getBlock().getMetaFromState(state);
		net.minecraft.world.World w = world instanceof net.minecraft.world.World ? (net.minecraft.world.World) world : null;
		java.util.ArrayList<net.minecraft.item.ItemStack> ret = getDrops(w, pos.getX(), pos.getY(), pos.getZ(), meta, fortune);
		if(ret != null) drops.addAll(ret);
	}

public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();

		TileEntitySlag tile = (TileEntitySlag) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(tile != null && tile.mat != null && tile.amount > 0) {
			ret.add(ItemScraps.create(new MaterialStack(tile.mat, tile.amount)));
		}

		return ret;
	}

	@Override
	public ItemStack getPickBlock(net.minecraft.block.state.IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();

		TileEntitySlag tile = (TileEntitySlag) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(tile != null) {
			return ItemScraps.create(new MaterialStack(tile.mat, tile.amount));
		}

		return super.getPickBlock(state, target, world, pos, player);
	}

	public static class TileEntitySlag extends TileEntity {

		public NTMMaterial mat;
		public int amount;
		public static int maxAmount = MaterialShapes.BLOCK.q(16);


		@Override
		public SPacketUpdateTileEntity getUpdatePacket() {
			NBTTagCompound nbt = new NBTTagCompound();
			this.writeToNBT(nbt);
			return new SPacketUpdateTileEntity(this.getPos(), 0, nbt);
		}

		@Override
		public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
			this.readFromNBT(pkt.getNbtCompound());
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.mat = Mats.matById.get(nbt.getInteger("mat"));
			this.amount = nbt.getInteger("amount");
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			if(this.mat != null) nbt.setInteger("mat", this.mat.id);
			nbt.setInteger("amount", this.amount);
			return nbt;
		}
	}
}

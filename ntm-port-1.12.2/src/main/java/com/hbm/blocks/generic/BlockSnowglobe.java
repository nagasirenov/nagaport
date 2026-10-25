package com.hbm.blocks.generic;

import java.util.List;
import java.util.Random;

import com.hbm.inventory.gui.GUIScreenSnowglobe;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IGUIProvider;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockSnowglobe extends BlockContainer implements IGUIProvider {

	public BlockSnowglobe() {
		super(Material.GLASS);
	}

	@Override
	public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(net.minecraft.block.state.IBlockState state) {
		return false;
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState i, Random rand, int j) {
		return null;
	}

	@Override
	public ItemStack getPickBlock(net.minecraft.block.state.IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		int x = pos.getX();
		int y = pos.getY();
		int z = pos.getZ();
		TileEntitySnowglobe entity = (TileEntitySnowglobe) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		if(entity != null) return new ItemStack(this, 1, entity.type.ordinal());
		return super.getPickBlock(state, target, world, pos, player);
	}


		@Override
	public void onBlockHarvested(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, net.minecraft.entity.player.EntityPlayer player) {
		int meta = state.getBlock().getMetaFromState(state);
		onBlockHarvested(worldIn, pos.getX(), pos.getY(), pos.getZ(), meta, player);
	}

public void onBlockHarvested(World world, int x, int y, int z, int meta, EntityPlayer player) {

		if(!player.capabilities.isCreativeMode) {
			harvesters.set(player);
			if(!world.isRemote) {
				TileEntitySnowglobe entity = (TileEntitySnowglobe) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
				if(entity != null) {
					EntityItem item = new EntityItem(world, x + 0.5, y, z + 0.5, new ItemStack(this, 1, entity.type.ordinal()));
					item.motionX = 0;
					item.motionY = 0;
					item.motionZ = 0;
					world.spawnEntity(item);
				}
			}
			harvesters.set(null);
		}
	}

	@Override
	public void harvestBlock(World world, EntityPlayer player, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, net.minecraft.tileentity.TileEntity te, net.minecraft.item.ItemStack tool) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ(); int meta = state.getBlock().getMetaFromState(state);
		player.addStat(net.minecraft.stats.StatList.getBlockStats(this), 1);
		player.addExhaustion(0.025F);
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;


		if(world.isRemote) {
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
			return true;

		} else {
			return true;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubBlocks(net.minecraft.creativetab.CreativeTabs tab, net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> list) {
	net.minecraft.item.Item item = net.minecraft.item.Item.getItemFromBlock(this);
		for(int i = 1; i < SnowglobeType.values().length; i++) list.add(new ItemStack(item, 1, i));
	}

	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack stack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		int meta = MathHelper.floor((double)((player.rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
		com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, meta, 2);

		TileEntitySnowglobe bobble = (TileEntitySnowglobe) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		bobble.type = SnowglobeType.values()[Math.abs(stack.getItemDamage()) % SnowglobeType.values().length];
		bobble.markDirty();
	}

	private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(4F * 0.0625F, 0.0F, 4F * 0.0625F, 1.0F - 4F * 0.0625F, 0.3125F, 1.0F - 4F * 0.0625F);

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return BOUNDS;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntitySnowglobe();
	}

	public static class TileEntitySnowglobe extends TileEntity {

		public SnowglobeType type = SnowglobeType.NONE;


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
			this.type = SnowglobeType.values()[Math.abs(nbt.getByte("type")) % SnowglobeType.values().length];
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			nbt.setByte("type", (byte) type.ordinal());
			return nbt;
		}
	}

	public static enum SnowglobeType {
		NONE(			"NONE",				null),
		RIVETCITY(		"Rivet City",		"Welcome to Rivet City. Please wait while the bridge extends."),
		TENPENNYTOWER(	"Tenpenny Tower",	"Tenpenny Tower is the brainchild of Allistair Tenpenny, a British refugee who came to the Capital Wasteland seeking his fortune."),
		LUCKY38(		"Lucky 38",			"My guess? Leads to a big cashout at some casino - and if the \"38\" on it is any indication... well... Lucky 38 it is."),
		SIERRAMADRE(	"Sierra Madre",		"It's the moment you've been waiting for, the reason we're all here - the Gala Event, the Grand Opening of the Sierra Madre Casino."),
		PRYDWEN(		"The Prydwen",		"People of the Commonwealth. Do not interfere. Our intentions are peaceful. We are the Brotherhood of Steel.");

		public String label;
		public String inscription;

		private SnowglobeType(String label, String inscription) {
			this.label = label;
			this.inscription = inscription;
		}
	}

	@Override
	public Container provideContainer(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Object provideGUI(int ID, EntityPlayer player, World world, int x, int y, int z) {
		return new GUIScreenSnowglobe((TileEntitySnowglobe) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z)));
	}
}

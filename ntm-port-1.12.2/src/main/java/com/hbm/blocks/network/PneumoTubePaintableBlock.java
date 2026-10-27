package com.hbm.blocks.network;

import api.hbm.block.IToolable;
import com.hbm.blocks.IBlockMultiPass;
import com.hbm.blocks.MultipassContext;
import com.hbm.interfaces.ICopiable;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.TileEntityPneumoTube;
import com.hbm.util.Compat;
import com.hbm.util.PersistentRegistryRef;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import com.hbm.lib.ForgeDirection;

public class PneumoTubePaintableBlock extends BlockContainer implements IToolable, IBlockMultiPass {

    @Override protected net.minecraft.block.state.BlockStateContainer createBlockState() { return MultipassContext.stateContainer(this); }
    @Override public net.minecraft.block.state.IBlockState getStateFromMeta(int metadata) { return getDefaultState().withProperty(MultipassContext.METADATA, metadata & 15); }
    @Override public int getMetaFromState(net.minecraft.block.state.IBlockState state) { return state.getValue(MultipassContext.METADATA); }
    @Override public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) { return net.minecraft.util.EnumBlockRenderType.MODEL; }
    @Override public net.minecraft.util.BlockRenderLayer getRenderLayer() { return net.minecraft.util.BlockRenderLayer.CUTOUT_MIPPED; }
    @Override public net.minecraft.block.state.IBlockState getExtendedState(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
        return ((net.minecraftforge.common.property.IExtendedBlockState) state).withProperty(MultipassContext.PROPERTY, getMultipassContext(state, world, pos));
    }
    @Override public MultipassContext getMultipassContext(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
        int metadata = getMetaFromState(state);
        TileEntity tile = world.getTileEntity(pos);
        if(tile instanceof TileEntityPneumoTubePaintable) {
            TileEntityPneumoTubePaintable tube = (TileEntityPneumoTubePaintable) tile;
            return MultipassContext.of(metadata, tube.block == null ? null : tube.block.getStateFromMeta(tube.meta), true, 0xffffff, 0, tube.ejectionDir.ordinal(), tube.insertionDir.ordinal(), 0);
        }
        return MultipassContext.basic(metadata);
    }



	public PneumoTubePaintableBlock() { super(Material.IRON); }

	@Override
	public TileEntity createNewTileEntity(World world, int meta) { return new TileEntityPneumoTubePaintable(); }





	@Override
	public int getPasses() {return 2;};



	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {

		if(tool == ToolType.HAND_DRILL) {

			TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			if(tile instanceof TileEntityPneumoTubePaintable) {
				TileEntityPneumoTubePaintable tube = (TileEntityPneumoTubePaintable) tile;

				if(tube.block != null) {
					tube.block = null;
					com.hbm.world.TileEntityBlockUpdate.notifyAt(world, x, y, z);
					tube.markDirty();
				}
			}
		} else if(tool == ToolType.SCREWDRIVER) {

			if(world.isRemote) return true;
			TileEntityPneumoTube tube = (TileEntityPneumoTube) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

			ForgeDirection rot = player.isSneaking() ? tube.ejectionDir : tube.insertionDir;
			ForgeDirection oth = player.isSneaking() ? tube.insertionDir : tube.ejectionDir;

			for(int i = 0; i < 7; i++) {
				rot = ForgeDirection.getOrientation((rot.ordinal() + 1) % 7);
				if(rot == ForgeDirection.UNKNOWN) break; // unknown is always valid, simply disables this part
				if(rot == oth) continue; // skip if both positions collide
				TileEntity tile = Compat.getTileStandard(world, x + rot.offsetX, y + rot.offsetY, z + rot.offsetZ);
				if(tile instanceof TileEntityPneumoTube) continue;
				if(tile instanceof IInventory) break; // valid if connected to an IInventory
			}

			if(player.isSneaking())
				tube.ejectionDir = rot;
			else
				tube.insertionDir = rot;

			tube.markDirty();
			if(world instanceof WorldServer)
				((WorldServer) world).getPlayerChunkMap().markBlockForUpdate(new net.minecraft.util.math.BlockPos(x, y, z));

			return true;
		}
		return false;
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float fX, float fY, float fZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;


		ItemStack stack = com.hbm.util.NativeHeldItem.getMainHandNullable(player);
		if(stack != null && stack.getItem() instanceof ItemBlock) {
			ItemBlock ib = (ItemBlock) stack.getItem();
			Block block = net.minecraft.block.Block.getBlockFromItem(ib);

			if(BlockCablePaintable.allowedPaint(block, this)) {
				TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
				if(tile instanceof TileEntityPneumoTubePaintable) {
					TileEntityPneumoTubePaintable tube = (TileEntityPneumoTubePaintable) tile;

					if(tube.block == null) {
						tube.block = block;
						tube.meta = stack.getItemDamage() & 15;
						com.hbm.world.WorldBlockState.markBlockForUpdate(world, x, y, z);
						tube.markDirty();
						return true;
					}
				}
			}
		} else if(ToolType.getType(stack) == ToolType.SCREWDRIVER || ToolType.getType(stack) == ToolType.HAND_DRILL) return false;

		if(!player.isSneaking()) {
			TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			if(tile instanceof TileEntityPneumoTube) {
				TileEntityPneumoTube tube = (TileEntityPneumoTube) tile;
				if(tube.isCompressor()) {
					FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
					return true;
				} else if(tube.isEndpoint()) {
					FMLNetworkHandler.openGui(player, MainRegistry.instance, 1, world, x, y, z);
					return true;
				}
			}
		}

		return false;
	}

	public static class TileEntityPneumoTubePaintable extends TileEntityPneumoTube implements ICopiable {

		private Block block;
		private int meta;
		private Block lastBlock;
		private int lastMeta;
		private NBTTagCompound legacyRef;

		@Override
		public void update() {
			super.update();

			if(this.world.isRemote && (lastMeta != meta || lastBlock != block)) {
				com.hbm.world.WorldBlockState.markBlockForUpdate(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
				lastBlock = block;
				lastMeta = meta;
			}
		}

		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);
			this.block = PersistentRegistryRef.readBlock(nbt, "block");
			this.meta = nbt.getInteger("meta");
			this.legacyRef = PersistentRegistryRef.legacyOnly(nbt, "block");
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			if(block != null) PersistentRegistryRef.writeBlock(nbt, "block", block);
			else PersistentRegistryRef.preserveLegacy(this.legacyRef, nbt, "block");
			nbt.setInteger("meta", meta);
			return nbt;
		}

		@Override
		public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
			NBTTagCompound nbt = pkt.getNbtCompound();

			this.block = PersistentRegistryRef.readBlock(nbt, "block");
			this.meta = nbt.getInteger("meta");
			this.legacyRef = PersistentRegistryRef.legacyOnly(nbt, "block");
			super.onDataPacket(net, pkt);
		}

		@Override
		public SPacketUpdateTileEntity getUpdatePacket() {
			NBTTagCompound nbt = new NBTTagCompound();

			if(block != null) PersistentRegistryRef.writeBlock(nbt, "block", block);
			nbt.setInteger("meta", meta);
			nbt.setByte("insertionDir", (byte) insertionDir.ordinal());
			nbt.setByte("ejectionDir", (byte) ejectionDir.ordinal());
			return new SPacketUpdateTileEntity(this.getPos(), 0, nbt);
		}

		@Override
		public NBTTagCompound getSettings(World world, int x, int y, int z) {
			NBTTagCompound nbt = new NBTTagCompound();
			if(block != null) {
				PersistentRegistryRef.writeBlock(nbt, "paintblock", block);
				nbt.setInteger("paintmeta", meta);
			}
			return nbt;
		}

		@Override
		public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
			Block painted = PersistentRegistryRef.readBlock(nbt, "paintblock");
			if(painted != null) {
				this.block = painted;
				this.meta = nbt.getInteger("paintmeta");
			}
		}
	}
}

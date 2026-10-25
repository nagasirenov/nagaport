package com.hbm.blocks.network;

import api.hbm.block.IToolable;
import com.hbm.blocks.IBlockMultiPass;
import com.hbm.blocks.MultipassContext;
import com.hbm.interfaces.ICopiable;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.TileEntityLoadedBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import li.cil.oc.api.network.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import li.cil.oc.api.network.Message;
import li.cil.oc.api.network.Node;
import li.cil.oc.api.Network;
import li.cil.oc.api.network.Visibility;
import net.minecraftforge.fml.common.Optional;
import li.cil.oc.api.network.SidedEnvironment;
import net.minecraft.util.EnumFacing;
import li.cil.oc.api.internal.Colored;
import com.hbm.handler.CompatHandler.OCColors;
import net.minecraftforge.oredict.OreDictionary;

public class BlockOpenComputersCablePaintable extends BlockContainer implements IToolable, IBlockMultiPass {

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
        if(tile instanceof TileEntityOpenComputersCablePaintable) {
            TileEntityOpenComputersCablePaintable pipe = (TileEntityOpenComputersCablePaintable) tile;
            return MultipassContext.of(metadata, pipe.block == null ? null : pipe.block.getStateFromMeta(pipe.meta), true, pipe.getColor(), 0, 6, 6, 0);
        }
        return MultipassContext.basic(metadata);
    }



	public BlockOpenComputersCablePaintable() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityOpenComputersCablePaintable();
	}







	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float fX, float fY, float fZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;


		ItemStack stack = com.hbm.util.NativeHeldItem.getMainHandNullable(player);

		if (stack == null)
			return super.onBlockActivated(world, nativePos, nativeState, player, nativeHand, nativeFacing, fX, fY, fZ);

		if (stack.getItem() instanceof ItemBlock) {
			ItemBlock ib = (ItemBlock) stack.getItem();
			Block block = net.minecraft.block.Block.getBlockFromItem(ib);

			if(block.getDefaultState().isOpaqueCube() && block != this) {

				TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

				if(tile instanceof TileEntityOpenComputersCablePaintable) {
					TileEntityOpenComputersCablePaintable pipe = (TileEntityOpenComputersCablePaintable) tile;

					if(pipe.block == null) {
						pipe.block = block;
						pipe.meta = stack.getItemDamage() & 15;
						com.hbm.world.WorldBlockState.markBlockForUpdate(world, x, y, z);
						pipe.markDirty();
						return true;
					}
				}
			}
		} else if(stack != null && !stack.isEmpty()) {
			boolean isDye = false;
			int[] dicts = OreDictionary.getOreIDs(stack);
			for (int dict : dicts) {
				String dictName = OreDictionary.getOreName(dict);
				if (dictName.equals("dye"))
					isDye = true;
			}

			if (isDye) {
				TileEntityOpenComputersCablePaintable tile = (TileEntityOpenComputersCablePaintable) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
				tile.setColor(OCColors.fromDye(stack).getColor());
				com.hbm.world.WorldBlockState.markBlockForUpdate(world, x, y, z);
				tile.markDirty();
			}
		}

		return super.onBlockActivated(world, nativePos, nativeState, player, nativeHand, nativeFacing, fX, fY, fZ);
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {

		if(tool != ToolType.SCREWDRIVER) return false;

		TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(tile instanceof TileEntityOpenComputersCablePaintable) {
			TileEntityOpenComputersCablePaintable pipe = (TileEntityOpenComputersCablePaintable) tile;

			if(pipe.block != null) {
				pipe.block = null;
				com.hbm.world.TileEntityBlockUpdate.notifyAt(world, x, y, z);
				pipe.markDirty();
				return true;
			}
		}

		return false;
	}

	@Override
	public int getPasses() {
		return 3;
	}



	@Optional.InterfaceList({
		@Optional.Interface(iface = "li.cil.oc.api.network.Environment", modid = "OpenComputers"),
		@Optional.Interface(iface = "li.cil.oc.api.network.SidedEnvironment", modid = "OpenComputers"),
		@Optional.Interface(iface = "li.cil.oc.api.network.Colored", modid = "OpenComputers")
	})
	public static class TileEntityOpenComputersCablePaintable extends TileEntityLoadedBase implements Environment, SidedEnvironment, Colored, ICopiable, net.minecraft.util.ITickable {

		protected Node node;
		protected boolean addedToNetwork = false;

		private Block block;
		private int meta;
		private Block lastBlock;
		private int lastMeta;
		private OCColors color = OCColors.LIGHTGRAY;

		public TileEntityOpenComputersCablePaintable() {
			node = Network.newNode(this, Visibility.None).create();
		}

		@Override
		public void update() {


			if(this.world.isRemote && (lastBlock != block || lastMeta != meta)) {
				com.hbm.world.WorldBlockState.markBlockForUpdate(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
				lastBlock = block;
				lastMeta = meta;
			}

			if(!this.getWorld().isRemote && !addedToNetwork) {
				addedToNetwork = true;
				Network.joinOrCreateNetwork(this);
			}

		}

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
			int id = nbt.getInteger("block");
			this.block = id == 0 ? null : Block.getBlockById(id);
			this.meta = nbt.getInteger("meta");

			this.color = OCColors.fromInt(nbt.getInteger("dyeColor"));

			if (node != null && node.host() == this) {
				node.load(nbt.getCompoundTag("oc:node"));
			}
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			if(block != null) nbt.setInteger("block", Block.getIdFromBlock(block));
			nbt.setInteger("meta", meta);

			nbt.setInteger("dyeColor", color.getColor());

			if (node != null && node.host() == this) {
				final NBTTagCompound nodeNbt = new NBTTagCompound();
				node.save(nodeNbt);
				nbt.setTag("oc:node", nodeNbt);
			}
			return nbt;
		}

		public NBTTagCompound getSettings(World world, int x, int y, int z) {
			NBTTagCompound nbt = new NBTTagCompound();
			if(block != null) {
				nbt.setInteger("paintblock", Block.getIdFromBlock(block));
				nbt.setInteger("paintmeta", meta);
			}
			return nbt;
		}

		public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
			if(nbt.hasKey("paintblock")) {
				this.block = Block.getBlockById(nbt.getInteger("paintblock"));
				this.meta = nbt.getInteger("paintmeta");
				this.color = OCColors.fromInt(nbt.getInteger("dyeColor"));
			}
		}

		// OC Cable Things
		@Override
		public Node node() {
			return node;
		}

		public Node sidedNode(EnumFacing side) {
			if (side == null)
				return null;

			int neighborX = super.getPos().getX() + side.getXOffset();
			int neighborY = super.getPos().getY() + side.getYOffset();
			int neighborZ = super.getPos().getZ() + side.getZOffset();
			TileEntity neighbor = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(neighborX, neighborY, neighborZ));

			// If a cable does not support colors but is a valid cable block, allow it to connect regardless of color.
			if (!(neighbor instanceof Colored)) {
				if (neighbor instanceof Environment)
					return node;
				else
					return null;
			}

			Colored cable = (Colored) neighbor;
			if (cable.getColor() == color.getColor())
				return node;
			else
				return null;
		}

		@Override
		public void onConnect(Node node) {}

		@Override
		public void onDisconnect(Node node) {}

		@Override
		public void onMessage(Message message) {}

		@Override
		public void onChunkUnload() {
			super.onChunkUnload();
			if (node != null) node.remove();
		}

		public void invalidate() {
			super.invalidate();
			if (node != null) node.remove();
		}

		public boolean canConnect(EnumFacing side) {
			if (side == null)
				return false;

			int neighborX = super.getPos().getX() + side.getXOffset();
			int neighborY = super.getPos().getY() + side.getYOffset();
			int neighborZ = super.getPos().getZ() + side.getZOffset();
			TileEntity neighbor = this.world.getTileEntity(new net.minecraft.util.math.BlockPos(neighborX, neighborY, neighborZ));

			// If a cable does not support colors but is a valid cable block, allow it to connect regardless of color.
			if (!(neighbor instanceof Colored)) {
				return neighbor instanceof Environment;
			}

			Colored cable = (Colored) neighbor;
			return cable.getColor() == color.getColor();
		}

		public void setColor(int newColor) {
			color = OCColors.fromInt(newColor);
		}

		public int getColor() {
			return color.getColor();
		}

		@Optional.Method(modid = "OpenComputers")
		public boolean controlsConnectivity() {
			return true;
		}
	}
}

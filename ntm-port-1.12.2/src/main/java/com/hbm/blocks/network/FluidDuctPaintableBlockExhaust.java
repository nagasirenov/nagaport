package com.hbm.blocks.network;

import api.hbm.block.IToolable;

import com.hbm.blocks.IBlockMultiPass;
import com.hbm.blocks.MultipassContext;
import com.hbm.blocks.ILookOverlay;
import com.hbm.interfaces.ICopiable;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.network.TileEntityPipeExhaust;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
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
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

import java.util.ArrayList;
import java.util.List;

public class FluidDuctPaintableBlockExhaust extends FluidDuctBase implements IToolable, IBlockMultiPass, ILookOverlay {

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
        if(tile instanceof TileEntityPipeExhaustPaintable) {
            TileEntityPipeExhaustPaintable pipe = (TileEntityPipeExhaustPaintable) tile;
            return MultipassContext.of(metadata, pipe.block == null ? null : pipe.block.getStateFromMeta(pipe.meta), true, 0xffffff, 0, 6, 6, 0);
        }
        return MultipassContext.basic(metadata);
    }

	
	
	public FluidDuctPaintableBlockExhaust() {
        super(Material.IRON);
	}
	
	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityPipeExhaustPaintable();
	}





	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {

		if(tool != ToolType.SCREWDRIVER) return false;

		TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(tile instanceof TileEntityPipeExhaustPaintable) {
			TileEntityPipeExhaustPaintable pipe = (TileEntityPipeExhaustPaintable) tile;

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
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float fX, float fY, float fZ) {
		
		ItemStack stack = com.hbm.util.NativeHeldItem.getMainHandNullable(player);

		if(stack != null && stack.getItem() instanceof ItemBlock) {
			ItemBlock ib = (ItemBlock) stack.getItem();
			Block block = ib.field_150939_a;

			if(BlockCablePaintable.allowedPaint(block, this)) {

				TileEntity tile = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

				if(tile instanceof TileEntityPipeExhaustPaintable) {
					TileEntityPipeExhaustPaintable pipe = (TileEntityPipeExhaustPaintable) tile;

					if(pipe.block == null) {
						pipe.block = block;
						pipe.meta = stack.getItemDamage() & 15;
						world.markBlockForUpdate(x, y, z);
						pipe.markDirty();
						return true;
					}
				}
			}
		}

		return super.onBlockActivated(world, x, y, z, player, side, fX, fY, fZ);
	}

	@Override
	public int getPasses() {
		return 2;
	}



	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		List<String> text = new ArrayList();
		text.add(Fluids.SMOKE.getLocalizedName());
		text.add(Fluids.SMOKE_LEADED.getLocalizedName());
		text.add(Fluids.SMOKE_POISON.getLocalizedName());
		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}
	
	public static class TileEntityPipeExhaustPaintable extends TileEntityPipeExhaust implements ICopiable {

		private Block block;
		private int meta;
		private Block lastBlock;
		private int lastMeta;
		
		@Override
		public void update() {
			super.update();
			
			if(this.world.isRemote && (lastBlock != block || lastMeta != meta)) {
				this.world.markBlockForUpdate(this.pos.getX(), this.pos.getY(), this.pos.getZ());
				lastBlock = block;
				lastMeta = meta;
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
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);
			if(block != null) nbt.setInteger("block", Block.getIdFromBlock(block));
			nbt.setInteger("meta", meta);
			return nbt;
		}

		@Override
		public NBTTagCompound getSettings(World world, int x, int y, int z) {
			NBTTagCompound nbt = new NBTTagCompound();
			if(block != null) {
				nbt.setInteger("paintblock", Block.getIdFromBlock(block));
				nbt.setInteger("paintmeta", meta);
			}
			return nbt;
		}

		@Override
		public void pasteSettings(NBTTagCompound nbt, int index, World world, EntityPlayer player, int x, int y, int z) {
			if(nbt.hasKey("paintblock")) {
				this.block = Block.getBlockById(nbt.getInteger("paintblock"));
				this.meta = nbt.getInteger("paintmeta");
			}
		}
	}
}

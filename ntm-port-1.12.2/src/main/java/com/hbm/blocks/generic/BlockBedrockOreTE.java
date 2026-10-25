package com.hbm.blocks.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.hbm.blocks.IBlockMultiPass;
import com.hbm.blocks.MultipassContext;
import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.FluidContainerRegistry;
import com.hbm.inventory.FluidStack;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemDrillbit.EnumDrillType;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.util.EnumUtil;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.fluidmk2.IFillableItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAir;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;

public class BlockBedrockOreTE extends BlockContainer implements ILookOverlay, IBlockMultiPass {

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
        if(tile instanceof TileEntityBedrockOre) {
            TileEntityBedrockOre ore = (TileEntityBedrockOre) tile;
            return MultipassContext.of(metadata, null, true, ore.color, ore.shape, 6, 6, 0);
        }
        return MultipassContext.basic(metadata);
    }


	public BlockBedrockOreTE() {
		super(Material.ROCK);
		this.setBlockUnbreakable();
		this.setResistance(1_000_000);
	}

	public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {
		return false;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityBedrockOre();
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState meta, Random rand, int fortune) {
		return null;
	}

	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, EntityLivingBase entity, ItemStack stack) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		super.onBlockPlacedBy(world, new net.minecraft.util.math.BlockPos(x, y, z), com.hbm.world.WorldBlockState.read(world, x, y, z), entity, stack);
		com.hbm.world.WorldBlockState.markBlockForUpdate(world, x, y, z);
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float fx, float fy, float fz) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int i = nativeFacing != null ? nativeFacing.getIndex() : 0;


		ItemStack stack = com.hbm.util.NativeHeldItem.getMainHandNullable(player);
		if(stack == null) return false;
		if(!player.capabilities.isCreativeMode) return false;
		if(world.isRemote) return true;

		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(te instanceof TileEntityBedrockOre) {
			TileEntityBedrockOre ore = (TileEntityBedrockOre) te;

			if(stack.getItem() == ModItems.drillbit) {
				EnumDrillType type = EnumUtil.grabEnumSafely(EnumDrillType.class, stack.getItemDamage());
				ore.tier = type.tier;
			} else if(FluidContainerRegistry.getFluidType(stack) != Fluids.NONE) {
				FluidType type = FluidContainerRegistry.getFluidType(stack);
				int amount = FluidContainerRegistry.getFluidContent(stack, type);
				ore.acidRequirement = new FluidStack(type, amount);
			} else if(stack.getItem() instanceof IFillableItem) {
				IFillableItem item = (IFillableItem) stack.getItem();
				FluidType type = item.getFirstFluidType(stack);
				if(type != null) {
					ore.acidRequirement = new FluidStack(type, item.getFill(stack));
				}
			} else {
				ore.resource = stack.copy();
				ore.shape = world.rand.nextInt(10);
			}

			ore.markDirty();
		}

		com.hbm.world.WorldBlockState.markBlockForUpdate(world, x, y, z);

		return true;
	}



	@Override
	public int getPasses() {
		return 2;
	}










	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {

		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(!(te instanceof TileEntityBedrockOre))
			return;

		TileEntityBedrockOre ore = (TileEntityBedrockOre) te;

		List<String> text = new ArrayList();

		if(ore.resource != null) {
			text.add(ore.resource.getDisplayName());
		}

		text.add("Tier: " + ore.tier);

		if(ore.acidRequirement != null) {
			text.add("Requires: " + ore.acidRequirement.fill + "mB " + ore.acidRequirement.type.getLocalizedName());
		}

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}

	public static class TileEntityBedrockOre extends TileEntity {

		public ItemStack resource;
		public FluidStack acidRequirement;
		public int tier;
		public int color;
		public int shape;

		public TileEntityBedrockOre setStyle(int color, int shape) {
			this.color = color;
			this.shape = shape;
			return this;
		}


		@Override
		public void readFromNBT(NBTTagCompound nbt) {
			super.readFromNBT(nbt);

			Item resourceItem = null;

			// block/item ids shift between sessions; registry names written since the fix are preferred
			if(nbt.hasKey("0name", 8))
				resourceItem = Item.getByNameOrId(nbt.getString("0name"));
			else if(nbt.hasKey("0id", 99))
				resourceItem = Item.getItemById(nbt.getInteger("0id"));

			this.resource = new ItemStack(resourceItem, nbt.getByte("size"), nbt.getShort("meta"));

			if(this.resource.getItem() == null || this.resource.getItem() instanceof ItemAir) this.resource = new ItemStack(ModItems.powder_iron);

			FluidType type = Fluids.fromID(nbt.getInteger("fluid"));

			if(type != Fluids.NONE) {
				this.acidRequirement = new FluidStack(type, nbt.getInteger("amount"));
			}

			this.tier = nbt.getInteger("tier");
			this.color = nbt.getInteger("color");
			this.shape = nbt.getInteger("shape");
		}

		@Override
		public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
			super.writeToNBT(nbt);

			if(this.resource != null) {
				ResourceLocation resourceName = this.resource.getItem().getRegistryName();
				if(resourceName != null) nbt.setString("0name", resourceName.toString());
				nbt.setInteger("0id", Item.getIdFromItem(this.resource.getItem()));
				nbt.setByte("size", (byte) this.resource.getCount());
				nbt.setShort("meta", (short) this.resource.getItemDamage());
			}

			if(this.acidRequirement != null) {
				nbt.setInteger("fluid", this.acidRequirement.type.getID());
				nbt.setInteger("amount", this.acidRequirement.fill);
			}

			nbt.setInteger("tier", this.tier);
			nbt.setInteger("color", this.color);
			nbt.setInteger("shape", this.shape);
			return nbt;
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

			if(color == 0) {
				this.color = MainRegistry.proxy.getStackColor(resource, true);
			}

			com.hbm.world.WorldBlockState.markBlockForUpdate(this.world, this.pos.getX(), this.pos.getY(), this.pos.getZ());
		}
	}
}

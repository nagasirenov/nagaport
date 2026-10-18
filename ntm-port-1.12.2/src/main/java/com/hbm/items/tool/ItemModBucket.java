package com.hbm.items.tool;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

public class ItemModBucket extends ItemBucket {

	private static final int BUCKET_CAPACITY = 1000;

	protected int overrideFluidMeta = 0;
	protected Block containedFluid;
	private final Fluid bucketFluid;

	public ItemModBucket(Block fluid) {
		super(fluid);
		this.containedFluid = fluid;
		this.bucketFluid = null;
	}

	public ItemModBucket(Block fluid, Fluid bucketFluid) {
		super(fluid);
		this.containedFluid = fluid;
		this.bucketFluid = bucketFluid;
	}

	public ItemModBucket(Block fluid, int meta) {
		this(fluid, (Fluid) null);
		this.overrideFluidMeta = meta;
	}

	@Override
	public ICapabilityProvider initCapabilities(ItemStack stack, NBTTagCompound nbt) {
		return bucketFluid == null ? null : new BucketFluidHandler(stack);
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (bucketFluid != null) {
			IFluidHandlerItem handler = stack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
			FluidStack contained = handler == null ? null : handler.drain(BUCKET_CAPACITY, false);
			if (contained == null || contained.amount != BUCKET_CAPACITY) {
				return new ActionResult<>(EnumActionResult.FAIL, stack);
			}
		}

		return super.onItemRightClick(world, player, hand);
	}

	@Override
	public boolean tryPlaceContainedLiquid(EntityPlayer player, World world, BlockPos pos) {
		if (this.containedFluid == Blocks.AIR) {
			return false;
		} else {
			IBlockState state = world.getBlockState(pos);
			Material material = state.getMaterial();
			boolean flag = !material.isSolid();

			if (!world.isAirBlock(pos) && !flag) {
				return false;
			} else {
				if (world.provider.doesWaterVaporize() && this.containedFluid == Blocks.FLOWING_WATER) {
					world.playSound(player, pos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.BLOCKS, 0.5F,
							2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);

					for (int l = 0; l < 8; ++l) {
						world.spawnParticle(EnumParticleTypes.SMOKE_LARGE, pos.getX() + Math.random(), pos.getY() + Math.random(),
								pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);
					}
				} else {
					if (!world.isRemote && flag && !material.isLiquid()) {
						world.destroyBlock(pos, true);
					}

					world.setBlockState(pos, this.containedFluid.getStateFromMeta(overrideFluidMeta), 3);
				}

				return true;
			}
		}
	}

	private class BucketFluidHandler implements IFluidHandlerItem, ICapabilityProvider {

		private static final String FLUID_TAG = "Fluid";
		private static final String EMPTY_TAG = "EmptyBucket";
		private final ItemStack container;

		private BucketFluidHandler(ItemStack container) {
			this.container = container;
		}

		@Override
		public ItemStack getContainer() {
			if (isEmpty() || getFluid() == null) {
				return container.isEmpty() ? ItemStack.EMPTY : new ItemStack(Items.BUCKET, container.getCount());
			}
			return container;
		}

		@Override
		public IFluidTankProperties[] getTankProperties() {
			return new IFluidTankProperties[] { new FluidTankProperties(getFluid(), BUCKET_CAPACITY, true, true) };
		}

		@Override
		public int fill(FluidStack resource, boolean doFill) {
			if (resource == null || resource.amount < BUCKET_CAPACITY || resource.getFluid() != bucketFluid) {
				return 0;
			}

			FluidStack contained = getFluid();
			if (contained != null) {
				return 0;
			}

			if (doFill) {
				FluidStack filled = resource.copy();
				filled.amount = BUCKET_CAPACITY;
				NBTTagCompound tag = getOrCreateTag();
				tag.removeTag(EMPTY_TAG);
				if (filled.tag == null) {
					tag.removeTag(FLUID_TAG);
				} else {
					tag.setTag(FLUID_TAG, filled.writeToNBT(new NBTTagCompound()));
				}
			}

			return BUCKET_CAPACITY;
		}

		@Override
		public FluidStack drain(FluidStack resource, boolean doDrain) {
			FluidStack contained = getFluid();
			if (resource == null || resource.amount < BUCKET_CAPACITY || contained == null
					|| !contained.isFluidEqual(resource)) {
				return null;
			}
			return drain(BUCKET_CAPACITY, doDrain);
		}

		@Override
		public FluidStack drain(int maxDrain, boolean doDrain) {
			FluidStack contained = getFluid();
			if (contained == null || contained.amount != BUCKET_CAPACITY || maxDrain < BUCKET_CAPACITY) {
				return null;
			}

			FluidStack drained = contained.copy();
			drained.amount = BUCKET_CAPACITY;
			if (doDrain) {
				NBTTagCompound tag = getOrCreateTag();
				tag.removeTag(FLUID_TAG);
				tag.setBoolean(EMPTY_TAG, true);
			}
			return drained;
		}

		@Override
		public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
			return capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY;
		}

		@Override
		public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
			return hasCapability(capability, facing) ? CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.cast(this) : null;
		}

		private FluidStack getFluid() {
			if (isEmpty()) {
				return null;
			}

			NBTTagCompound tag = container.getTagCompound();
			if (tag != null && tag.hasKey(FLUID_TAG)) {
				FluidStack stored = FluidStack.loadFluidStackFromNBT(tag.getCompoundTag(FLUID_TAG));
				if (stored == null || stored.getFluid() != bucketFluid || stored.amount < BUCKET_CAPACITY) {
					return null;
				}
				if (stored.amount > BUCKET_CAPACITY) {
					stored.amount = BUCKET_CAPACITY;
				}
				return stored;
			}
			return new FluidStack(bucketFluid, BUCKET_CAPACITY);
		}

		private boolean isEmpty() {
			NBTTagCompound tag = container.getTagCompound();
			return tag != null && tag.getBoolean(EMPTY_TAG);
		}

		private NBTTagCompound getOrCreateTag() {
			if (!container.hasTagCompound()) {
				container.setTagCompound(new NBTTagCompound());
			}
			return container.getTagCompound();
		}
	}

}

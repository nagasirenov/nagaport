package com.hbm.blocks.network;

import api.hbm.block.IToolable;
import com.hbm.blocks.IBlockSideRotation;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.items.tool.ItemConveyorWand;
import com.hbm.items.tool.ItemTooling;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.network.TileEntityCraneBase;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

import java.util.List;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import com.hbm.blocks.SideRotationModelState;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import java.util.Random;

public abstract class BlockCraneBase extends BlockContainer implements IBlockSideRotation, IToolable, ITooltipProvider {

	protected ResourceLocation blockIcon;
	protected ResourceLocation iconSide;
	protected ResourceLocation iconIn;
	protected ResourceLocation iconSideIn;
	protected ResourceLocation iconOut;
	protected ResourceLocation iconSideOut;

	protected ResourceLocation iconDirectional;
	protected ResourceLocation iconDirectionalUp;
	protected ResourceLocation iconDirectionalDown;

	protected ResourceLocation iconDirectionalTurnLeft;
	protected ResourceLocation iconDirectionalTurnRight;
	protected ResourceLocation iconDirectionalSideLeftTurnUp;
	protected ResourceLocation iconDirectionalSideRightTurnUp;
	protected ResourceLocation iconDirectionalSideLeftTurnDown;
	protected ResourceLocation iconDirectionalSideRightTurnDown;
	protected ResourceLocation iconDirectionalSideUpTurnLeft;
	protected ResourceLocation iconDirectionalSideUpTurnRight;
	protected ResourceLocation iconDirectionalSideDownTurnLeft;
	protected ResourceLocation iconDirectionalSideDownTurnRight;

	public BlockCraneBase(Material mat) {
		super(mat);
		setDefaultState(blockState.getBaseState().withProperty(SideRotationModelState.METADATA, 0));
		initBaseModelTextures();
	}

	@Override
	public abstract TileEntityCraneBase createNewTileEntity(World p_149915_1_, int p_149915_2_);

	protected void initModelTextures() {
		initBaseModelTextures();
	}

	// Do not dispatch to subclass texture initialization from the base constructor.
	private void initBaseModelTextures() {
		this.blockIcon = new ResourceLocation(RefStrings.MODID, "blocks/crane_top");
		this.iconSide = new ResourceLocation(RefStrings.MODID, "blocks/crane_side");
		this.iconIn = new ResourceLocation(RefStrings.MODID, "blocks/crane_in");
		this.iconSideIn = new ResourceLocation(RefStrings.MODID, "blocks/crane_side_in");
		this.iconOut = new ResourceLocation(RefStrings.MODID, "blocks/crane_out");
		this.iconSideOut = new ResourceLocation(RefStrings.MODID, "blocks/crane_side_out");
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof ItemTooling) {
			return false;
		} else if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() instanceof ItemConveyorWand) {
			return false;
		} else if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
		// Preserve the legacy piston test (integer corner, float proximity, strict thresholds).
		// Native Y is feet-relative: legacy posY + 1.82D - yOffset becomes posY + 1.82D.
		int orientation;
		double eyeY = player.posY + 1.82D;
		if(MathHelper.abs((float) player.posX - (float) pos.getX()) < 2.0F
				&& MathHelper.abs((float) player.posZ - (float) pos.getZ()) < 2.0F
				&& eyeY - pos.getY() > 2.0D) {
			orientation = 1;
		} else if(MathHelper.abs((float) player.posX - (float) pos.getX()) < 2.0F
				&& MathHelper.abs((float) player.posZ - (float) pos.getZ()) < 2.0F
				&& pos.getY() - eyeY > 0.0D) {
			orientation = 0;
		} else {
			int yaw = MathHelper.floor((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
			orientation = yaw == 0 ? 2 : yaw == 1 ? 5 : yaw == 2 ? 3 : 4;
		}
		world.setBlockState(pos, getStateFromMeta(orientation), 2);
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		if (tool != ToolType.SCREWDRIVER) return false;

		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		if (!(te instanceof TileEntityCraneBase)) return false;

		TileEntityCraneBase craneTileEntity = (TileEntityCraneBase) te;

		ForgeDirection newDirection = ForgeDirection.getOrientation(side);

		if (player.isSneaking()) {
			craneTileEntity.setOutputOverride(newDirection);
		} else {
			craneTileEntity.setInput(newDirection);
		}

		return true;
	}

	public ForgeDirection getInputSide(IBlockAccess world, int x, int y, int z) {
		return ForgeDirection.getOrientation(getMetaFromState(world.getBlockState(new BlockPos(x, y, z))));
	}

	/**
	 * Returns the player-overridden output direction, or {@link ForgeDirection#UNKNOWN} if unset.
	 * A return value of {@link ForgeDirection#UNKNOWN} suggests use of default meta behavior.
	 * <i>Should</i> never return the current input direction.
	 */
	protected final ForgeDirection getOutputSideOverride(IBlockAccess world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		if (!(te instanceof TileEntityCraneBase)) return ForgeDirection.UNKNOWN;
		TileEntityCraneBase craneTileEntity = (TileEntityCraneBase) te;

		return craneTileEntity.getOutputOverride();
	}

	public ForgeDirection getOutputSide(IBlockAccess world, int x, int y, int z) {
		TileEntity te = world.getTileEntity(new BlockPos(x, y, z));
		if (!(te instanceof TileEntityCraneBase)) return ForgeDirection.UNKNOWN;
		TileEntityCraneBase craneTileEntity = (TileEntityCraneBase) te;

		return craneTileEntity.getOutputSide();
	}

	@Override
	public ResourceLocation getModelTexture(IBlockAccess world, int x, int y, int z, int side) {
		ForgeDirection inputSide = getInputSide(world, x, y, z);
		ForgeDirection outputOverride = getOutputSideOverride(world, x, y, z);
		boolean outputSideOverridden = outputOverride != ForgeDirection.UNKNOWN && outputOverride.getOpposite() != inputSide;
		ForgeDirection outputSide = outputSideOverridden ? outputOverride : inputSide.getOpposite();

		// take your left hand, make your thumb the input side and the index finger the output side
		// angle your middle finger to make your hand look like coordinate axes
		// the direction your middle finger is pointing towards will be the direction returned from this function
		ForgeDirection leftHandRotation = outputSide.getRotation(inputSide);

		if(side == 0 || side == 1) {
			if(side == outputSide.ordinal()) {
				return this.iconOut;
			}
			if(side == inputSide.ordinal()) {
				return this.iconIn;
			}

			if (side == 1) {
				if (outputSideOverridden) {
					if (leftHandRotation == ForgeDirection.UP) {
						return this.iconDirectionalTurnLeft;
					}
					if (leftHandRotation == ForgeDirection.DOWN) {
						return this.iconDirectionalTurnRight;
					}
				} else return iconDirectional;
			}

			return this.blockIcon;
		}

		if(side == outputSide.ordinal()) {
			return this.iconSideOut;
		}
		if(side == inputSide.ordinal()) {
			return this.iconSideIn;
		}

		if (outputSideOverridden) {
			if (leftHandRotation.ordinal() == side) {
				if (outputSide == ForgeDirection.UP)
					return this.iconDirectionalSideLeftTurnUp;
				if (outputSide == ForgeDirection.DOWN)
					return this.iconDirectionalSideRightTurnDown;
				if (inputSide == ForgeDirection.UP)
					return this.iconDirectionalSideUpTurnRight;
				if (inputSide == ForgeDirection.DOWN)
					return this.iconDirectionalSideDownTurnLeft;
			}
			if (leftHandRotation.getOpposite().ordinal() == side) {
				if (outputSide == ForgeDirection.UP)
					return this.iconDirectionalSideRightTurnUp;
				if (outputSide == ForgeDirection.DOWN)
					return this.iconDirectionalSideLeftTurnDown;
				if (inputSide == ForgeDirection.UP)
					return this.iconDirectionalSideUpTurnLeft;
				if (inputSide == ForgeDirection.DOWN)
					return this.iconDirectionalSideDownTurnRight;
			}
		} else {
			if(outputSide == ForgeDirection.UP) {
				return this.iconDirectionalUp;
			}
			if(outputSide == ForgeDirection.DOWN) {
				return this.iconDirectionalDown;
			}
		}

		return this.iconSide;
	}

	// kept for inventory rendering
	@Override
	public ResourceLocation getModelTexture(int side, int metadata) {

		if(side == 0 || side == 1) {
			if(side == metadata) {
				return this.iconOut;
			}
			if(side == ForgeDirection.getOrientation(metadata).getOpposite().ordinal()) {
				return this.iconIn;
			}

			return side == 1 ? this.iconDirectional : this.blockIcon;
		}

		if(side == metadata) {
			return this.iconSideOut;
		}
		if(side == ForgeDirection.getOrientation(metadata).getOpposite().ordinal()) {
			return this.iconSideIn;
		}

		if(metadata == 0) {
			return this.iconDirectionalUp;
		}
		if(metadata == 1) {
			return this.iconDirectionalDown;
		}

		return this.iconSide;
	}

	@Override
	public int getRotationFromSide(IBlockAccess world, int x, int y, int z, int side) {
		int meta = getMetaFromState(world.getBlockState(new BlockPos(x, y, z)));

		if(meta > 1 && side == 1) {
			if(meta == 2) return 3;
			if(meta == 3) return 0;
			if(meta == 4) return 1;
			if(meta == 5) return 2;
		}

		return 0;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return SideRotationModelState.create(this);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(SideRotationModelState.METADATA, meta & 15);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(SideRotationModelState.METADATA);
	}

	@Override
	public IBlockState getExtendedState(IBlockState state, IBlockAccess world, BlockPos pos) {
		return SideRotationModelState.extend(state, world, pos);
	}

	@Override
	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	@Override
	public Collection<ResourceLocation> getModelTextures() {
		LinkedHashSet<ResourceLocation> textures = new LinkedHashSet<>(Arrays.asList(
				blockIcon,
				iconSide,
				iconIn,
				iconSideIn,
				iconOut,
				iconSideOut,
				iconDirectional,
				iconDirectionalUp,
				iconDirectionalDown,
				iconDirectionalTurnLeft,
				iconDirectionalTurnRight,
				iconDirectionalSideLeftTurnUp,
				iconDirectionalSideRightTurnUp,
				iconDirectionalSideLeftTurnDown,
				iconDirectionalSideRightTurnDown,
				iconDirectionalSideUpTurnLeft,
				iconDirectionalSideUpTurnRight,
				iconDirectionalSideDownTurnLeft,
				iconDirectionalSideDownTurnRight));
		textures.remove(null);
		return Collections.unmodifiableSet(textures);
	}

	private final Random rand = new Random();
	public void dropContents(World world, int x, int y, int z, Block block, int meta, int start, int end) {
		ISidedInventory tileentityfurnace = (ISidedInventory) world.getTileEntity(new BlockPos(x, y, z));

		if(tileentityfurnace != null) {

			for(int i1 = start; i1 < end; ++i1) {
				ItemStack itemstack = tileentityfurnace.getStackInSlot(i1);

				if(itemstack != null && !itemstack.isEmpty()) {
					float f = this.rand.nextFloat() * 0.8F + 0.1F;
					float f1 = this.rand.nextFloat() * 0.8F + 0.1F;
					float f2 = this.rand.nextFloat() * 0.8F + 0.1F;

					while(itemstack.getCount() > 0) {
						int j1 = this.rand.nextInt(21) + 10;

						if(j1 > itemstack.getCount()) {
							j1 = itemstack.getCount();
						}

						// Capture before depletion: native empty stacks hide their item.
						net.minecraft.item.Item dropItem = itemstack.getItem();
						int dropMeta = itemstack.getItemDamage();
						NBTTagCompound dropTag = itemstack.getTagCompound();
						itemstack.shrink(j1);
						EntityItem entityitem = new EntityItem(world, x + f, y + f1, z + f2, new ItemStack(dropItem, j1, dropMeta));

						if(dropTag != null) {
							entityitem.getItem().setTagCompound(dropTag.copy());
						}

						float f3 = 0.05F;
						entityitem.motionX = (float) this.rand.nextGaussian() * f3;
						entityitem.motionY = (float) this.rand.nextGaussian() * f3 + 0.2F;
						entityitem.motionZ = (float) this.rand.nextGaussian() * f3;
						world.spawnEntity(entityitem);
					}
				}
			}

			world.updateComparatorOutputLevel(new BlockPos(x, y, z), block);
		}

		super.breakBlock(world, new BlockPos(x, y, z), block.getStateFromMeta(meta));
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}
}

package com.hbm.blocks.machine;

import java.util.List;
import java.util.Random;

import com.hbm.blocks.IPersistentInfoProvider;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.items.machine.IItemFluidIdentifier;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.machine.storage.TileEntityBarrel;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockFluidBarrel extends BlockContainer implements ITooltipProvider, IPersistentInfoProvider {

	public static final PropertyInteger METADATA = PropertyInteger.create("metadata", 0, 15);
	@Override protected BlockStateContainer createBlockState() { return new BlockStateContainer(this, METADATA); }
	@Override public IBlockState getStateFromMeta(int meta) { return getDefaultState().withProperty(METADATA, meta); }
	@Override public int getMetaFromState(IBlockState state) { return state.getValue(METADATA); }
	@Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
	@Override public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT_MIPPED; }
	@Override public boolean isOpaqueCube(IBlockState state) { return false; }
	@Override public boolean isFullCube(IBlockState state) { return false; }
	@Override public BlockFaceShape getBlockFaceShape(IBlockAccess world, IBlockState state, BlockPos pos, EnumFacing face) { return BlockFaceShape.UNDEFINED; }

	int capacity;

	public BlockFluidBarrel(Material p_i45386_1_, int capacity) {
		this(p_i45386_1_, capacity, net.minecraft.block.SoundType.STONE);
	}

	public BlockFluidBarrel(Material p_i45386_1_, int capacity, net.minecraft.block.SoundType sound) {
		super(p_i45386_1_);
		setDefaultState(blockState.getBaseState().withProperty(METADATA, 0));
		setSoundType(sound);
		this.capacity = capacity;
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityBarrel(capacity);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		ItemStack held = player.getHeldItem(hand);
		if(world.isRemote) {
			return true;
			
		} else if(!player.isSneaking()) {
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
			return true;
			
		} else if(player.isSneaking()){
			TileEntityBarrel mileEntity = (TileEntityBarrel) world.getTileEntity(pos);

			if(!held.isEmpty() && held.getItem() instanceof IItemFluidIdentifier) {
				FluidType type = ((IItemFluidIdentifier) held.getItem()).getType(world, x, y, z, held);

				mileEntity.tank.setTankType(type);
				mileEntity.markDirty();
				player.sendMessage(new TextComponentString("Changed type to ").setStyle(new Style().setColor(TextFormatting.YELLOW)).appendSibling(new TextComponentTranslation(type.getConditionalName())).appendSibling(new TextComponentString("!")));
				}
			return true;

		}else {
			return false;
		}
	}

	private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(0.125D, 0D, 0.125D, 0.875D, 1D, 0.875D);

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return BOUNDS; }

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return BOUNDS; }

	private final Random field_149933_a = new Random();
	public static boolean keepInventory;

	@Override
	public void breakBlock(World p_149749_1_, BlockPos pos, IBlockState state) {
		int p_149749_2_ = pos.getX(), p_149749_3_ = pos.getY(), p_149749_4_ = pos.getZ();
		if(!keepInventory) {
			ISidedInventory tileentityfurnace = (ISidedInventory) p_149749_1_.getTileEntity(pos);

			if(tileentityfurnace != null) {
				for(int i1 = 0; i1 < tileentityfurnace.getSizeInventory(); ++i1) {
					ItemStack itemstack = tileentityfurnace.getStackInSlot(i1);

					if(itemstack != null && !itemstack.isEmpty()) {
						float f = this.field_149933_a.nextFloat() * 0.8F + 0.1F;
						float f1 = this.field_149933_a.nextFloat() * 0.8F + 0.1F;
						float f2 = this.field_149933_a.nextFloat() * 0.8F + 0.1F;

						while(itemstack.getCount() > 0) {
							int j1 = this.field_149933_a.nextInt(21) + 10;

							if(j1 > itemstack.getCount()) {
								j1 = itemstack.getCount();
							}

							// Capture before depletion: native empty stacks hide their item.
							net.minecraft.item.Item dropItem = itemstack.getItem();
							int dropMeta = itemstack.getItemDamage();
							NBTTagCompound dropTag = itemstack.getTagCompound();
							itemstack.shrink(j1);
							EntityItem entityitem = new EntityItem(p_149749_1_, p_149749_2_ + f, p_149749_3_ + f1, p_149749_4_ + f2, new ItemStack(dropItem, j1, dropMeta));

							if(dropTag != null) {
								entityitem.getItem().setTagCompound(dropTag.copy());
							}

							float f3 = 0.05F;
							entityitem.motionX = (float) this.field_149933_a.nextGaussian() * f3;
							entityitem.motionY = (float) this.field_149933_a.nextGaussian() * f3 + 0.2F;
							entityitem.motionZ = (float) this.field_149933_a.nextGaussian() * f3;
							p_149749_1_.spawnEntity(entityitem);
						}
					}
				}

				p_149749_1_.updateComparatorOutputLevel(pos, state.getBlock());
			}
		}

		super.breakBlock(p_149749_1_, pos, state);
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack stack) {
		super.onBlockPlacedBy(world, pos, state, player, stack);
		IPersistentNBT.restoreData(world, pos.getX(), pos.getY(), pos.getZ(), stack);
	}
	
	@Override
	public void getDrops(NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
		if(world instanceof World) drops.addAll(IPersistentNBT.getDrops((World) world, pos.getX(), pos.getY(), pos.getZ(), this));
	}

	@Override
	public void onBlockHarvested(World world, BlockPos pos, IBlockState state, EntityPlayer player) {
		
		if(!player.capabilities.isCreativeMode) {
			harvesters.set(player);
			this.dropBlockAsItem(world, pos, state, 0);
			harvesters.set(null);
		}
	}
	
	@Override
	public void harvestBlock(World world, EntityPlayer player, BlockPos pos, IBlockState state, TileEntity tile, ItemStack tool) {
		player.addStat(StatList.getBlockStats(this), 1);
		player.addExhaustion(0.025F);
	}

	@Override
	public boolean hasComparatorInputOverride(IBlockState state) {
		return true;
	}

	@Override
	public int getComparatorInputOverride(IBlockState state, World world, BlockPos pos) {

		TileEntity te = world.getTileEntity(pos);

		if(!(te instanceof TileEntityBarrel))
			return 0;

		TileEntityBarrel barrel = (TileEntityBarrel) te;
		return barrel.getComparatorPower();
	}

	@Override
	public void addInformation(ItemStack stack, NBTTagCompound persistentTag, EntityPlayer player, List list, boolean ext) {
		FluidTank tank = new FluidTank(Fluids.NONE, 0);
		tank.readFromNBT(persistentTag, "tank");
		list.add(TextFormatting.YELLOW + "" + tank.getFill() + "/" + tank.getMaxFill() + "mB " + tank.getTankType().getLocalizedName());
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		
		if(this == ModBlocks.barrel_plastic) {
			list.add(TextFormatting.AQUA + "Capacity: 12,000mB");
			list.add(TextFormatting.YELLOW + "Cannot store hot fluids");
			list.add(TextFormatting.YELLOW + "Cannot store corrosive fluids");
			list.add(TextFormatting.YELLOW + "Cannot store antimatter");
		}
		
		if(this == ModBlocks.barrel_corroded) {
			list.add(TextFormatting.AQUA + "Capacity: 6,000mB");
			list.add(TextFormatting.GREEN + "Can store hot fluids");
			list.add(TextFormatting.GREEN + "Can store highly corrosive fluids");
			list.add(TextFormatting.YELLOW + "Cannot store antimatter");
			list.add(TextFormatting.RED + "Leaky");
		}
		
		if(this == ModBlocks.barrel_iron) {
			list.add(TextFormatting.AQUA + "Capacity: 8,000mB");
			list.add(TextFormatting.GREEN + "Can store hot fluids");
			list.add(TextFormatting.YELLOW + "Cannot store corrosive fluids properly");
			list.add(TextFormatting.YELLOW + "Cannot store antimatter");
		}
		
		if(this == ModBlocks.barrel_steel) {
			list.add(TextFormatting.AQUA + "Capacity: 16,000mB");
			list.add(TextFormatting.GREEN + "Can store hot fluids");
			list.add(TextFormatting.GREEN + "Can store corrosive fluids");
			list.add(TextFormatting.YELLOW + "Cannot store highly corrosive fluids properly");
			list.add(TextFormatting.YELLOW + "Cannot store antimatter");
		}
		
		if(this == ModBlocks.barrel_antimatter) {
			list.add(TextFormatting.AQUA + "Capacity: 16,000mB");
			list.add(TextFormatting.GREEN + "Can store hot fluids");
			list.add(TextFormatting.GREEN + "Can store highly corrosive fluids");
			list.add(TextFormatting.GREEN + "Can store antimatter");
		}
		
		if(this == ModBlocks.barrel_tcalloy) {
			list.add(TextFormatting.AQUA + "Capacity: 24,000mB");
			list.add(TextFormatting.GREEN + "Can store hot fluids");
			list.add(TextFormatting.GREEN + "Can store highly corrosive fluids");
			list.add(TextFormatting.YELLOW + "Cannot store antimatter");
		}
	}
}

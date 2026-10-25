package com.hbm.blocks.machine;

import com.hbm.blocks.ModBlocks;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.TileEntityDiFurnaceRTG;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import java.util.Random;

public class MachineDiFurnaceRTG extends BlockThermalFurnace {

	private final Random rand = new Random();
	private static boolean keepInventory;

	public MachineDiFurnaceRTG(boolean blockState) {
		super(Material.IRON, blockState);
	}


	@Override
	public Item getItemDropped(IBlockState state, Random p_149650_2_, int p_149650_3_) {
		return Item.getItemFromBlock(ModBlocks.machine_difurnace_rtg_off);
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
		this.setDefaultDirection(world, pos);
	}

	private void setDefaultDirection(World world, BlockPos pos) {
		if(!world.isRemote) {
			IBlockState block1 = world.getBlockState(pos.north());
			IBlockState block2 = world.getBlockState(pos.south());
			IBlockState block3 = world.getBlockState(pos.west());
			IBlockState block4 = world.getBlockState(pos.east());

			byte b0 = 3;

			if(block1.isFullBlock() && !block2.isFullBlock()) {
				b0 = 3;
			}
			if(block2.isFullBlock() && !block1.isFullBlock()) {
				b0 = 2;
			}
			if(block3.isFullBlock() && !block4.isFullBlock()) {
				b0 = 5;
			}
			if(block4.isFullBlock() && !block3.isFullBlock()) {
				b0 = 4;
			}

			world.setBlockState(pos, world.getBlockState(pos).withProperty(METADATA, (int) (b0)), 2);
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemStack) {
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		if(i == 0) {
			world.setBlockState(pos, world.getBlockState(pos).withProperty(METADATA, 2), 2);
		}
		if(i == 1) {
			world.setBlockState(pos, world.getBlockState(pos).withProperty(METADATA, 5), 2);
		}
		if(i == 2) {
			world.setBlockState(pos, world.getBlockState(pos).withProperty(METADATA, 3), 2);
		}
		if(i == 3) {
			world.setBlockState(pos, world.getBlockState(pos).withProperty(METADATA, 4), 2);
		}

		if(itemStack.hasDisplayName()) {
			((TileEntityDiFurnaceRTG) world.getTileEntity(pos)).setCustomName(itemStack.getDisplayName());
		}
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {

		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			TileEntityDiFurnaceRTG entity = (TileEntityDiFurnaceRTG) world.getTileEntity(pos);
			if(entity != null) {
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos.getX(), pos.getY(), pos.getZ());
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityDiFurnaceRTG();
	}

	public static void updateBlockState(boolean isProcessing, World world, int x, int y, int z) {
        BlockPos pos = new BlockPos(x, y, z);
		int i = world.getBlockState(pos).getValue(METADATA);
		TileEntity entity = world.getTileEntity(pos);
		keepInventory = true;

		if(isProcessing) {
			world.setBlockState(pos, ModBlocks.machine_difurnace_rtg_on.getDefaultState());
		} else {
			world.setBlockState(pos, ModBlocks.machine_difurnace_rtg_off.getDefaultState());
		}

		keepInventory = false;
		world.setBlockState(pos, world.getBlockState(pos).withProperty(METADATA, i), 2);

		if(entity != null) {
			entity.validate();
			world.setTileEntity(pos, entity);
		}
	}

	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
        Block block = state.getBlock();
		if(!keepInventory) {
			TileEntityDiFurnaceRTG tileentityfurnace = (TileEntityDiFurnaceRTG) world.getTileEntity(pos);

			if(tileentityfurnace != null) {
				for(int i1 = 0; i1 < tileentityfurnace.getSizeInventory(); ++i1) {
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
							EntityItem entityitem = new EntityItem(world, x + f, y + f1,
									z + f2,
									new ItemStack(dropItem, j1, dropMeta));

							if(dropTag != null) {
								entityitem.getItem()
										.setTagCompound(dropTag.copy());
							}

							float f3 = 0.05F;
							entityitem.motionX = (float) this.rand.nextGaussian() * f3;
							entityitem.motionY = (float) this.rand.nextGaussian() * f3 + 0.2F;
							entityitem.motionZ = (float) this.rand.nextGaussian() * f3;
							world.spawnEntity(entityitem);
						}
					}
				}

				world.updateComparatorOutputLevel(pos, block);
			}
		}

		super.breakBlock(world, pos, state);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {
        int x = pos.getX(), y = pos.getY(), z = pos.getZ();
		if(isActive) {
			int l = world.getBlockState(pos).getValue(METADATA);
			float f = x + 0.5F;
			float f1 = y + 0.25F + rand.nextFloat() * 6.0F / 16.0F;
			float f2 = z + 0.5F;
			float f3 = 0.52F;
			float f4 = rand.nextFloat() * 0.5F - 0.25F;
			float f5 = rand.nextFloat() * 0.75F + 0.125F;
			float f6 = rand.nextFloat() * 0.75F + 0.125F;

			if(l == 4) {
				world.spawnParticle(EnumParticleTypes.FLAME, f - f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + f5, (double) y + 1, z + f6, 0.0D, 0.0D, 0.0D);
			} else if(l == 5) {
				world.spawnParticle(EnumParticleTypes.FLAME, f + f3, f1, f2 + f4, 0.0D, 0.0D, 0.0D);
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + f5, (double) y + 1, z + f6, 0.0D, 0.0D, 0.0D);
			} else if(l == 2) {
				world.spawnParticle(EnumParticleTypes.FLAME, f + f4, f1, f2 - f3, 0.0D, 0.0D, 0.0D);
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + f5, (double) y + 1, z + f6, 0.0D, 0.0D, 0.0D);
			} else if(l == 3) {
				world.spawnParticle(EnumParticleTypes.FLAME, f + f4, f1, f2 + f3, 0.0D, 0.0D, 0.0D);
				world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, x + f5, (double) y + 1, z + f6, 0.0D, 0.0D, 0.0D);
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
		return new ItemStack(ModBlocks.machine_difurnace_rtg_off);
	}
}

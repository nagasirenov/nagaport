package com.hbm.blocks.bomb;

import java.util.Random;

import org.apache.logging.log4j.Level;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.BombConfig;
import com.hbm.config.GeneralConfig;
import com.hbm.entity.effect.EntityNukeTorex;
import com.hbm.entity.logic.EntityNukeExplosionMK5;
import com.hbm.interfaces.IBomb;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.bomb.TileEntityNukeBoy;
import com.hbm.util.TrackerUtil;

import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
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

public class NukeBoy extends BlockContainer implements IBomb {

	public TileEntityNukeBoy tetn = new TileEntityNukeBoy();

	private static boolean keepInventory = false;
	private final static Random field_149933_a = new Random();

	public NukeBoy(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityNukeBoy();
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		return Item.getItemFromBlock(ModBlocks.nuke_boy);
	}

	@Override
	public void breakBlock(World p_149749_1_, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState) {
		int p_149749_2_ = nativePos.getX(); int p_149749_3_ = nativePos.getY(); int p_149749_4_ = nativePos.getZ();
		Block p_149749_5_ = nativeState.getBlock();
		if(!keepInventory) {
			TileEntityNukeBoy tileentityfurnace = (TileEntityNukeBoy) p_149749_1_.getTileEntity(new net.minecraft.util.math.BlockPos(p_149749_2_, p_149749_3_, p_149749_4_));

			if(tileentityfurnace != null) {
				for(int i1 = 0; i1 < tileentityfurnace.getSizeInventory(); ++i1) {
					ItemStack itemstack = tileentityfurnace.getStackInSlot(i1);

					if(itemstack != null) {
						float f = NukeBoy.field_149933_a.nextFloat() * 0.8F + 0.1F;
						float f1 = NukeBoy.field_149933_a.nextFloat() * 0.8F + 0.1F;
						float f2 = NukeBoy.field_149933_a.nextFloat() * 0.8F + 0.1F;

						while(itemstack.getCount() > 0) {
							int j1 = NukeBoy.field_149933_a.nextInt(21) + 10;

							if(j1 > itemstack.getCount()) {
								j1 = itemstack.getCount();
							}

							// Capture identity before native depletion hides the item.
							net.minecraft.item.Item spillItem = itemstack.getItem();
							int spillMeta = itemstack.getItemDamage();
							NBTTagCompound spillTag = itemstack.getTagCompound();
							itemstack.shrink(j1);
							EntityItem entityitem = new EntityItem(p_149749_1_, p_149749_2_ + f, p_149749_3_ + f1, p_149749_4_ + f2, new ItemStack(spillItem, j1, spillMeta));

							if(spillTag != null) {
								entityitem.getItem().setTagCompound((NBTTagCompound) spillTag.copy());
							}

							float f3 = 0.05F;
							entityitem.motionX = (float) NukeBoy.field_149933_a.nextGaussian() * f3;
							entityitem.motionY = (float) NukeBoy.field_149933_a.nextGaussian() * f3 + 0.2F;
							entityitem.motionZ = (float) NukeBoy.field_149933_a.nextGaussian() * f3;
							p_149749_1_.spawnEntity(entityitem);
						}
					}
				}

				p_149749_1_.updateComparatorOutputLevel(nativePos, p_149749_5_);
			}
		}

		super.breakBlock(p_149749_1_, nativePos, nativeState);
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(); int y = nativePos.getY(); int z = nativePos.getZ();
		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {
			TileEntityNukeBoy entity = (TileEntityNukeBoy) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			if(entity != null) {
				FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, x, y, z);
			}
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void onNeighborBlockChange(World p_149695_1_, int x, int y, int z, Block p_149695_5_) {
		TileEntityNukeBoy entity = (TileEntityNukeBoy) p_149695_1_.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		if(p_149695_1_.isBlockPowered(new net.minecraft.util.math.BlockPos(x, y, z))) {
			if(entity.isReady() && !p_149695_1_.isRemote) {
				this.onBlockDestroyedByPlayer(p_149695_1_, x, y, z, 1);
				entity.clearSlots();
				com.hbm.world.WorldAirRemoval.remove(p_149695_1_, x, y, z);
				igniteTestBomb(p_149695_1_, x, y, z);
			}
		}
	}

	public boolean igniteTestBomb(World world, int x, int y, int z) {
		if(!world.isRemote) {

			tetn.clearSlots();
			com.hbm.sound.VanillaWorldSoundEffects.play(world, x, y, z, "random.explode", 1.0f, world.rand.nextFloat() * 0.1F + 0.9F);

			world.spawnEntityInWorld(EntityNukeExplosionMK5.statFac(world, BombConfig.boyRadius, x + 0.5, y + 0.5, z + 0.5));
			//world.spawnEntityInWorld(EntityNukeCloudSmall.statFac(world, x, y, z, BombConfig.boyRadius));
			
			EntityNukeTorex torex = new EntityNukeTorex(world);
			torex.setPositionAndRotation(x + 0.5, y + 1, z + 0.5, 0, 0);
			torex.getDataWatcher().updateObject(10, 1.5F);
			world.spawnEntityInWorld(torex);
			TrackerUtil.setTrackingRange(world, torex, 1000);
		}
		return false;
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
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack itemStack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		if(i == 0) {
			world.setBlockMetadataWithNotify(x, y, z, 5, 2);
		}
		if(i == 1) {
			world.setBlockMetadataWithNotify(x, y, z, 3, 2);
		}
		if(i == 2) {
			world.setBlockMetadataWithNotify(x, y, z, 4, 2);
		}
		if(i == 3) {
			world.setBlockMetadataWithNotify(x, y, z, 2, 2);
		}
		if(!world.isRemote) {
			if(GeneralConfig.enableExtendedLogging) {
				MainRegistry.logger.log(Level.INFO, "[BOMBPL]" + this.getLocalizedName() + " placed at " + x + " / " + y + " / " + z + "! " + "by "+ player.getCommandSenderName());
		}	
	}
}

	@Override
	public BombReturnCode explode(World world, int x, int y, int z) {

		if(!world.isRemote) {
			TileEntityNukeBoy entity = (TileEntityNukeBoy) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
			
			if(entity.isReady()) {
				this.onBlockDestroyedByPlayer(world, x, y, z, 1);
				entity.clearSlots();
				com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
				igniteTestBomb(world, x, y, z);
				return BombReturnCode.DETONATED;
			}
			
			return BombReturnCode.ERROR_MISSING_COMPONENT;
		}

		return BombReturnCode.UNDEFINED;
	}
}

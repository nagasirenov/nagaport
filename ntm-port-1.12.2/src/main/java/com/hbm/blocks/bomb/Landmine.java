package com.hbm.blocks.bomb;

import java.util.Random;

import org.apache.logging.log4j.Level;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.config.ServerConfig;
import com.hbm.explosion.ExplosionLarge;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.*;
import com.hbm.interfaces.IBomb;
import com.hbm.items.ModItems;
import com.hbm.main.MainRegistry;
import com.hbm.items.weapon.sedna.factory.XFactoryCatapult;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.toclient.AuxParticlePacketNT;
import com.hbm.tileentity.bomb.TileEntityLandmine;

import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.BlockFence;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class Landmine extends BlockContainer implements IBomb {

	public static boolean safeMode = false;

	public double range;
	public double height;

	public Landmine(Material mat, double range, double height) {
		super(mat);
		this.range = range;
		this.height = height;
	}

	@Override
	public TileEntity createNewTileEntity(World world, int i) {
		return new TileEntityLandmine();
	}

	@Override public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
	}
	@Override public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) { return false; }
	@Override public boolean isFullCube(net.minecraft.block.state.IBlockState state) { return false; }

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState i, Random rand, int j) {
		return null;
	}

	private static final AxisAlignedBB BOUNDS_AP = new AxisAlignedBB(5 * 0.0625F, 0.0F, 5 * 0.0625F, 11 * 0.0625F, 1 * 0.0625F, 11 * 0.0625F);
	private static final AxisAlignedBB BOUNDS_HE = new AxisAlignedBB(4 * 0.0625F, 0.0F, 4 * 0.0625F, 12 * 0.0625F, 2 * 0.0625F, 12 * 0.0625F);
	private static final AxisAlignedBB BOUNDS_SHRAP = new AxisAlignedBB(5 * 0.0625F, 0.0F, 5 * 0.0625F, 11 * 0.0625F, 1 * 0.0625F, 11 * 0.0625F);
	private static final AxisAlignedBB BOUNDS_FAT = new AxisAlignedBB(5 * 0.0625F, 0.0F, 4 * 0.0625F, 11 * 0.0625F, 6 * 0.0625F, 12 * 0.0625F);

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		if(this == ModBlocks.mine_ap) return BOUNDS_AP;
		if(this == ModBlocks.mine_he) return BOUNDS_HE;
		if(this == ModBlocks.mine_shrap) return BOUNDS_SHRAP;
		if(this == ModBlocks.mine_fat) return BOUNDS_FAT;
		return FULL_BLOCK_AABB;
	}

	public boolean canPlaceBlockAt(World world, net.minecraft.util.math.BlockPos nativePlacementPos) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		net.minecraft.block.state.IBlockState nativeSupportState = com.hbm.world.WorldBlockState.read(world, x, y - 1, z);
		if (nativeSupportState.getBlock().isSideSolid(nativeSupportState, world, new net.minecraft.util.math.BlockPos(x, y - 1, z), net.minecraft.util.EnumFacing.UP)) return true;
		Block nativeFenceBlock = com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock();
		return nativeFenceBlock == Blocks.OAK_FENCE || nativeFenceBlock == Blocks.NETHER_BRICK_FENCE;
	}

	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {

		if(world.isBlockPowered(new net.minecraft.util.math.BlockPos(x, y, z))) {
			explode(world, x, y, z);
		}

		if(!com.hbm.world.WorldBlockState.read(world, x, y - 1, z).isTopSolid() && !com.hbm.world.WorldBlockState.read(world, x, y - 1, z).isOpaqueCube()) {
			if(!safeMode) {
				explode(world, x, y, z);
			} else {
				com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			}
		}
	}

	@Override
	public void onPlayerDestroy(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		if(!safeMode) {
			explode(world, x, y, z);
		}

		super.onPlayerDestroy(world, pos, state);
	}

	public void onBlockDestroyedByPlayer(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, EntityPlayer player) {
		onPlayerDestroy(world, pos, state);
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float fx, float fy, float fz) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int i = nativeFacing != null ? nativeFacing.getIndex() : 0;

		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.defuser) {

			safeMode = true;
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);

			ItemStack itemstack = new ItemStack(this, 1);
			float f = world.rand.nextFloat() * 0.6F + 0.2F;
			float f1 = world.rand.nextFloat() * 0.2F;
			float f2 = world.rand.nextFloat() * 0.6F + 0.2F;

			EntityItem entityitem = new EntityItem(world, x + f, y + f1 + 1, z + f2, itemstack);

			float f3 = 0.05F;
			entityitem.motionX = (float) world.rand.nextGaussian() * f3;
			entityitem.motionY = (float) world.rand.nextGaussian() * f3 + 0.2F;
			entityitem.motionZ = (float) world.rand.nextGaussian() * f3;

			if(!world.isRemote)
				world.spawnEntity(entityitem);

			safeMode = false;
			return true;
		}

		return false;
	}

	public boolean isWaterAbove(World world, int x, int y, int z) {
		for(int xo = -1; xo <= 1; xo++) {
			for(int zo = -1; zo <= 1; zo++) {
				Block blockAbove = com.hbm.world.WorldBlockState.read(world, x + xo, y + 1, z + zo).getBlock();
				if(blockAbove == Blocks.WATER || blockAbove == Blocks.FLOWING_WATER) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public BombReturnCode explode(World world, int x, int y, int z) {

		if(!world.isRemote) {

			Landmine.safeMode = true;
			com.hbm.world.WorldBlockDestruction.destroy(world, x, y, z, false);
			Landmine.safeMode = false;

			if(this == ModBlocks.mine_ap) {
				ExplosionVNT vnt = new ExplosionVNT(world, x + 0.5, y + 0.5, z + 0.5, 3F);
				vnt.setEntityProcessor(new EntityProcessorCrossSmooth(0.5, ServerConfig.MINE_AP_DAMAGE.get()).setupPiercing(5F, 0.2F));
				vnt.setPlayerProcessor(new PlayerProcessorStandard());
				vnt.setSFX(new ExplosionEffectWeapon(5, 1F, 0.5F));
				vnt.explode();
			} else if(this == ModBlocks.mine_he) {
				ExplosionVNT vnt = new ExplosionVNT(world, x + 0.5, y + 0.5, z + 0.5, 4F);
				vnt.setBlockAllocator(new BlockAllocatorStandard());
				vnt.setBlockProcessor(new BlockProcessorStandard());
				vnt.setEntityProcessor(new EntityProcessorCrossSmooth(1, ServerConfig.MINE_HE_DAMAGE.get()).setupPiercing(15F, 0.2F));
				vnt.setPlayerProcessor(new PlayerProcessorStandard());
				vnt.setSFX(new ExplosionEffectWeapon(15, 3.5F, 1.25F));
				vnt.explode();
			} else if(this == ModBlocks.mine_shrap) {
				ExplosionVNT vnt = new ExplosionVNT(world, x + 0.5, y + 0.5, z + 0.5, 3F);
				vnt.setEntityProcessor(new EntityProcessorCrossSmooth(0.5, ServerConfig.MINE_SHRAP_DAMAGE.get()));
				vnt.setPlayerProcessor(new PlayerProcessorStandard());
				vnt.setSFX(new ExplosionEffectWeapon(5, 1F, 0.5F));
				vnt.explode();

				ExplosionLarge.spawnShrapnelShower(world, x + 0.5, y + 0.5, z + 0.5, 0, 1D, 0, 45, 0.2D);
				ExplosionLarge.spawnShrapnels(world, x + 0.5, y + 0.5, z + 0.5, 5);
			} else if(this == ModBlocks.mine_naval) {
				ExplosionVNT vnt = new ExplosionVNT(world, x + 5, y + 5, z + 5, 25F);
				vnt.setBlockAllocator(new BlockAllocatorWater(32));
				vnt.setBlockProcessor(new BlockProcessorStandard());
				vnt.setEntityProcessor(new EntityProcessorCrossSmooth(0.5, ServerConfig.MINE_NAVAL_DAMAGE.get()).setupPiercing(5F, 0.2F));
				vnt.setPlayerProcessor(new PlayerProcessorStandard());
				vnt.setSFX(new ExplosionEffectWeapon(10, 1F, 0.5F));
				vnt.explode();

				ExplosionLarge.spawnParticlesRadial(world, x + 0.5, y + 2, z + 0.5, 30);
				ExplosionLarge.spawnRubble(world,x + 0.5, y + 0.5, z + 0.5, 5 );

				// Only spawn water effects if there's water above the mine
				if (isWaterAbove(world, x, y, z)) {
					ExplosionLarge.spawnFoam(world, x + 0.5, y + 0.5, z + 0.5, 60);
				}

			} else if(this == ModBlocks.mine_fat) {

				ExplosionVNT vnt = new ExplosionVNT(world, x + 0.5, y + 0.5, z + 0.5, 10);
				vnt.setBlockAllocator(new BlockAllocatorStandard(64));
				vnt.setBlockProcessor(new BlockProcessorStandard());
				vnt.setEntityProcessor(new EntityProcessorCrossSmooth(2, ServerConfig.MINE_NUKE_DAMAGE.get()).withRangeMod(1.5F));
				vnt.setPlayerProcessor(new PlayerProcessorStandard());
				vnt.explode();

				XFactoryCatapult.incrementRad(world, x, y, z, 1.5F);
				NBTTagCompound data = new NBTTagCompound();
				data.setString("type", "muke");
				data.setBoolean("balefire", MainRegistry.polaroidID == 11 || world.rand.nextInt(100) == 0);
				PacketDispatcher.wrapper.sendToAllAround(new AuxParticlePacketNT(data, x + 0.5, y + 0.5, z + 0.5), new TargetPoint(world.provider.getDimension(), x + 0.5, y + 0.5, z + 0.5, 250));

				com.hbm.sound.WorldSoundEffects.play(world, x + 0.5, y + 0.5, z + 0.5, "hbm:weapon.mukeExplosion", 25.0F, 0.9F); // this has to be the single worst solution ever
			}
		}

		return BombReturnCode.DETONATED;
	}

	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack itemStack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
		if(!world.isRemote) {
			if(GeneralConfig.enableExtendedLogging) {
				MainRegistry.logger.log(Level.INFO, "[BOMBPL]" + this.getLocalizedName() + " placed at " + x + " / " + y + " / " + z + "! " + "by "+ player.getName());
			}
		}
	}

}

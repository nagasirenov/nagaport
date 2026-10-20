package com.hbm.blocks.bomb;

import static com.hbm.lib.ForgeDirection.DOWN;
import static com.hbm.lib.ForgeDirection.EAST;
import static com.hbm.lib.ForgeDirection.NORTH;
import static com.hbm.lib.ForgeDirection.SOUTH;
import static com.hbm.lib.ForgeDirection.UP;
import static com.hbm.lib.ForgeDirection.WEST;

import java.awt.Color;
import java.util.Random;

import com.hbm.blocks.ModBlocks;
import com.hbm.potion.HbmPotion;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.BlockFire;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class Balefire extends BlockFire {

	private IIcon icon;

	public Balefire() {
		super();
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		icon = register.registerIcon(this.getTextureName());
	}

	@SideOnly(Side.CLIENT)
	public IIcon getFireIcon(int i) {
		return icon;
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return icon;
	}

	@Override
	public void updateTick(World world, net.minecraft.util.math.BlockPos nativeTickPos, net.minecraft.block.state.IBlockState nativeTickState, Random rand) {
		int x = nativeTickPos.getX(), y = nativeTickPos.getY(), z = nativeTickPos.getZ();
		if(world.getGameRules().getGameRuleBooleanValue("doFireTick")) {

			if(!this.canPlaceBlockAt(world, x, y, z)) {
				com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			}

			int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );

			if(meta < 15) world.scheduleBlockUpdate(x, y, z, this, this.tickRate(world) + rand.nextInt(10));

			if(!this.canNeighborBurn(world, x, y, z) && !World.doesBlockHaveSolidTopSurface(world, x, y - 1, z)) {
				com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			} else {
				if(meta < 15) {
					this.tryCatchFire(world, x + 1, y, z, 500, rand, meta, WEST);
					this.tryCatchFire(world, x - 1, y, z, 500, rand, meta, EAST);
					this.tryCatchFire(world, x, y - 1, z, 300, rand, meta, UP);
					this.tryCatchFire(world, x, y + 1, z, 300, rand, meta, DOWN);
					this.tryCatchFire(world, x, y, z - 1, 500, rand, meta, SOUTH);
					this.tryCatchFire(world, x, y, z + 1, 500, rand, meta, NORTH);
					
					int h = 3;

					for(int ix = x - h; ix <= x + h; ++ix) {
						for(int iz = z - h; iz <= z + h; ++iz) {
							for(int iy = y - 1; iy <= y + 4; ++iy) {
								
								if(ix != x || iy != y || iz != z) {
									int fireLimit = 100;

									if(iy > y + 1) {
										fireLimit += (iy - (y + 1)) * 100;
									}
									
									if(com.hbm.world.WorldBlockState.read(world, ix, iy, iz).getBlock() == ModBlocks.balefire && com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, ix, iy, iz) ) > meta + 1) {
										world.setBlock(ix, iy, iz, this, meta + 1, 3);
										continue;
									}

									int neighborFireChance = this.getChanceOfNeighborsEncouragingFire(world, ix, iy, iz);

									if(neighborFireChance > 0) {
										int adjustedFireChance = (neighborFireChance + 40 + world.difficultySetting.getDifficultyId() * 7) / (meta + 30);

										if(adjustedFireChance > 0 && rand.nextInt(fireLimit) <= adjustedFireChance) {
											world.setBlock(ix, iy, iz, this, meta + 1, 3);
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private void tryCatchFire(World world, int x, int y, int z, int chance, Random rand, int fireMetadata, ForgeDirection face) {
		EnumFacing facing = face == null ? null : face.toEnumFacing();
		if (facing == null) return;
		BlockPos pos = new BlockPos(x, y, z);
		int flammability = world.getBlockState(pos).getBlock().getFlammability(world, pos, facing);

		if(rand.nextInt(chance) < flammability) {
			boolean flag = com.hbm.world.WorldBlockState.read(world, x, y, z).getBlock() == Blocks.TNT;

			world.setBlock(x, y, z, this, fireMetadata + 1, 3);

			if(flag) {
				Blocks.TNT.onBlockDestroyedByPlayer(world, x, y, z, 1);
			}
		}
	}

	private boolean canNeighborBurn(World world, int x, int y, int z) {
		return this.canCatchFire(world, new BlockPos(x + 1, y, z), EnumFacing.WEST)
				|| this.canCatchFire(world, new BlockPos(x - 1, y, z), EnumFacing.EAST)
				|| this.canCatchFire(world, new BlockPos(x, y - 1, z), EnumFacing.UP)
				|| this.canCatchFire(world, new BlockPos(x, y + 1, z), EnumFacing.DOWN)
				|| this.canCatchFire(world, new BlockPos(x, y, z - 1), EnumFacing.SOUTH)
				|| this.canCatchFire(world, new BlockPos(x, y, z + 1), EnumFacing.NORTH);
	}

	private int getChanceToEncourageFire(World world, int x, int y, int z, int spread, ForgeDirection face) {
		EnumFacing facing = face == null ? null : face.toEnumFacing();
		if (facing == null) return spread;
		BlockPos pos = new BlockPos(x, y, z);
		return Math.max(spread, world.getBlockState(pos).getBlock().getFireSpreadSpeed(world, pos, facing));
	}

	private int getChanceOfNeighborsEncouragingFire(World world, int x, int y, int z) {

		if(!world.isAirBlock(x, y, z)) {
			return 0;
		} else {
			int spread = 0;
			spread = this.getChanceToEncourageFire(world, x + 1, y, z, spread, WEST);
			spread = this.getChanceToEncourageFire(world, x - 1, y, z, spread, EAST);
			spread = this.getChanceToEncourageFire(world, x, y - 1, z, spread, UP);
			spread = this.getChanceToEncourageFire(world, x, y + 1, z, spread, DOWN);
			spread = this.getChanceToEncourageFire(world, x, y, z - 1, spread, SOUTH);
			spread = this.getChanceToEncourageFire(world, x, y, z + 1, spread, NORTH);
			return spread;
		}
	}

	@Override
	public boolean canCatchFire(IBlockAccess world, BlockPos pos, EnumFacing face) {
		return face != null && world.getBlockState(pos).getBlock().isFlammable(world, pos, face);
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		entity.setFire(10);

		if(entity instanceof EntityLivingBase) ((EntityLivingBase) entity).addPotionEffect(new PotionEffect(HbmPotion.radiation.id, 5 * 20, 9));
	}
	
	@SideOnly(Side.CLIENT)
	public int colorMultiplier(IBlockAccess world, int x, int y, int z) {
		int meta = world.getBlockMetadata(x, y, z);
		return Color.HSBtoRGB(0F, 0F, 1F - meta / 30F);
	}

	@Override
	public int getRenderType() {
		return 1;
	}
}

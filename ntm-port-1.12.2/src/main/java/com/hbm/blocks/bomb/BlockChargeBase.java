package com.hbm.blocks.bomb;

import static com.hbm.lib.ForgeDirection.DOWN;
import static com.hbm.lib.ForgeDirection.EAST;
import static com.hbm.lib.ForgeDirection.NORTH;
import static com.hbm.lib.ForgeDirection.SOUTH;
import static com.hbm.lib.ForgeDirection.UP;
import static com.hbm.lib.ForgeDirection.WEST;

import java.util.List;
import java.util.Random;

import com.hbm.config.GeneralConfig;
import com.hbm.blocks.BlockContainerBase;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.entity.item.EntityTNTPrimedBase;
import com.hbm.interfaces.IBomb;
import com.hbm.tileentity.bomb.TileEntityCharge;
import com.hbm.main.MainRegistry;

import api.hbm.block.IFuckingExplode;
import api.hbm.block.IToolable;
import net.minecraft.util.math.MathHelper;
import net.minecraft.block.Block;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public abstract class BlockChargeBase extends BlockContainerBase implements IBomb, IToolable, ITooltipProvider, IFuckingExplode {
	public static final PropertyInteger ORIENTATION = PropertyInteger.create("orientation", 0, 15);
	
	public static boolean safe = false;
	
	public BlockChargeBase() {
		super(Material.TNT);
		setDefaultState(blockState.getBaseState().withProperty(ORIENTATION, 0));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, ORIENTATION);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(ORIENTATION, Math.max(0, Math.min(15, meta)));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(ORIENTATION);
	}

	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY,
			float hitZ, int meta, EntityLivingBase placer, EnumHand hand) {
		return getStateFromMeta(facing.getIndex());
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		return new TileEntityCharge();
	}
	
	@Override
	public boolean isOpaqueCube() {
		return false;
	}
	
	@Override
	public boolean renderAsNormalBlock() {
		return false;
	}
	
	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float fX, float fY, float fZ, int meta) {
		return side;
	}
	
	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState i, Random rand, int j) {
		return null;
	}
	
	@Override
	public boolean canPlaceBlockOnSide(World world, int x, int y, int z, int side) {
		ForgeDirection dir = ForgeDirection.getOrientation(side);
		return	(dir == DOWN && com.hbm.lib.FacingCompat.isWorldSideSolid(world, x, y + 1, z, DOWN)) ||
				(dir == UP && com.hbm.lib.FacingCompat.isWorldSideSolid(world, x, y - 1, z, UP)) ||
				(dir == NORTH && com.hbm.lib.FacingCompat.isWorldSideSolid(world, x, y, z + 1, NORTH)) ||
				(dir == SOUTH && com.hbm.lib.FacingCompat.isWorldSideSolid(world, x, y, z - 1, SOUTH)) ||
				(dir == WEST && com.hbm.lib.FacingCompat.isWorldSideSolid(world, x + 1, y, z, WEST)) ||
				(dir == EAST && com.hbm.lib.FacingCompat.isWorldSideSolid(world, x - 1, y, z, EAST));
	}
	
	@Override
	public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
		
		ForgeDirection dir = ForgeDirection.getOrientation(com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) ));
		
		if(!com.hbm.lib.FacingCompat.isWorldSideSolid(world, x - dir.offsetX, y - dir.offsetY, z - dir.offsetZ, dir)) {
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			//this.explode(world, x, y, z);
		}
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {
		return null;
	}
	
	@Override
	public void setBlockBoundsBasedOnState(IBlockAccess world, int x, int y, int z) {
		
		float f = 0.0625F;
		
		switch(world.getBlockMetadata(x, y, z)) {
		case 0: this.setBlockBounds(0.0F, 10 * f, 0.0F, 1.0F, 1.0F, 1.0F); break;
		case 1: this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 6 * f, 1.0F); break;
		case 2: this.setBlockBounds(0.0F, 0.0F, 10 * f, 1.0F, 1.0F, 1.0F); break;
		case 3: this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 6 * f); break;
		case 4: this.setBlockBounds(10 * f, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F); break;
		case 5: this.setBlockBounds(0.0F, 0.0F, 0.0F, 6 * f, 1.0F, 1.0F); break;
		}
	}
	
	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		
		if(tool != ToolType.DEFUSER)
			return false;

		TileEntityCharge charge = (TileEntityCharge) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		
		if(charge.started) {
			charge.started = !charge.started;
			com.hbm.sound.WorldSoundEffects.play(world, x, y, z, "hbm:weapon.fstbmbStart", 1.0F, 1.0F);
			charge.markDirty();
		} else {
			safe = true;
			this.dismantle(world, x, y, z);
			safe = false;
		}
		
		return true;
	}
	
	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int i) {
		super.breakBlock(world, x, y, z, block, i);
		
		if(!safe)
			explode(world, x, y, z);
	}
	
	@Override
	public void onBlockDestroyedByExplosion(World world, int x, int y, int z, Explosion explosion) {
		if(!world.isRemote) {
			EntityTNTPrimedBase tntPrimed = new EntityTNTPrimedBase(world, x + 0.5D, y + 0.5D, z + 0.5D, explosion != null ? explosion.getExplosivePlacedBy() : null, this);
			tntPrimed.fuse = 0;
			tntPrimed.detonateOnCollision = false;
			world.spawnEntityInWorld(tntPrimed);
		}
	}

	@Override
	public void explodeEntity(World world, double x, double y, double z, EntityTNTPrimedBase entity) {
		explode(world, MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
	}
	
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add(TextFormatting.YELLOW + "Right-click to change timer.");
		list.add(TextFormatting.YELLOW + "Sneak-click to arm.");
		list.add(TextFormatting.RED + "Can only be disarmed and removed with defuser.");
	}
	
	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		} else {

			TileEntityCharge charge = (TileEntityCharge) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

			if(!charge.started) {
				
				if(player.isSneaking()) {
					
					if(charge.timer > 0) {
						charge.started = true;
						com.hbm.sound.WorldSoundEffects.play(world, x, y, z, "hbm:weapon.fstbmbStart", 1.0F, 1.0F);
					}
				} else {
					
					if(charge.timer == 0) { charge.timer = 100; }
					else if(charge.timer == 100) { charge.timer = 200; }
					else if(charge.timer == 200) { charge.timer = 300; }
					else if(charge.timer == 300) { charge.timer = 600; }
					else if(charge.timer == 600) { charge.timer = 1200; }
					else if(charge.timer == 1200) { charge.timer = 3600; }
					else if(charge.timer == 3600) { charge.timer = 6000; }
					else { charge.timer = 0; }
					
					com.hbm.sound.WorldSoundEffects.play(world, x, y, z, "hbm:item.techBoop", 1.0F, 1.0F);
				}
				
				charge.markDirty();
			}
			
			return false;
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {
		if(!world.isRemote) {
			if(GeneralConfig.enableExtendedLogging) {
				MainRegistry.logger.info("[BOMBPL]" + this.getLocalizedName() + " placed at " + x + " / " + y + " / " + z + "! " + "by "+ player.getCommandSenderName());
			}
		}
	}
	
}

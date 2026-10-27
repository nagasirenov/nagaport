package com.hbm.blocks.bomb;

import java.util.Random;

import com.hbm.entity.item.EntityTNTPrimedBase;
import com.hbm.util.ChatBuilder;

import api.hbm.block.IToolable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.util.text.TextFormatting;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public abstract class BlockTNTBase extends BlockDetonatable implements IToolable {

	@SideOnly(Side.CLIENT)
	private IIcon topIcon;
	@SideOnly(Side.CLIENT)
	private IIcon bottomIcon;

	public static final PropertyBool ACTIVE = PropertyBool.create("active");

	public BlockTNTBase() {
		super(Material.TNT, 15, 100, 20, false, false);
		this.setDefaultState(this.blockState.getBaseState().withProperty(ACTIVE, false));
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, ACTIVE);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(ACTIVE, meta == 1);
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(ACTIVE) ? 1 : 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return side == 0 ? this.bottomIcon : (side == 1 ? this.topIcon : this.blockIcon);
	}

	@Override
	public void onBlockAdded(World world, BlockPos pos, IBlockState state) {
		super.onBlockAdded(world, pos, state);
		int x = pos.getX(), y = pos.getY(), z = pos.getZ();

		if(world.isBlockPowered(new net.minecraft.util.math.BlockPos(x, y, z))) {
			this.onBlockDestroyedByPlayer(world, x, y, z, 1);
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
		} else {
			checkAndIgnite(world, x, y, z);
		}
	}

	@Override
	public void neighborChanged(net.minecraft.block.state.IBlockState nativeState, World world, net.minecraft.util.math.BlockPos nativePos, Block block, net.minecraft.util.math.BlockPos fromPos) {
		int x = nativePos.getX(); int y = nativePos.getY(); int z = nativePos.getZ();
		if(world.isBlockPowered(new net.minecraft.util.math.BlockPos(x, y, z))) {
			this.onBlockDestroyedByPlayer(world, x, y, z, 1);
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
		} else {
			checkAndIgnite(world, x, y, z);
		}
	}

	public void checkAndIgnite(World world, int x, int y, int z) {
		if (shouldIgnite(world, x, y, z)) {
			this.onBlockDestroyedByPlayer(world, x, y, z, 1);
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
		}
	}

	@Override
	public int quantityDropped(Random p_149745_1_) {
		return 1;
	}

	public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int meta) {
		this.prime(world, x, y, z, meta, (EntityLivingBase) null);
	}

	@Override
	public void onPlayerDestroy(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {
		this.prime(world, pos.getX(), pos.getY(), pos.getZ(), 1, (EntityLivingBase) null);
	}

	public void prime(World world, int x, int y, int z, int meta, EntityLivingBase living) {
		if(!world.isRemote) {
			if((meta & 1) == 1) {
				EntityTNTPrimedBase entitytntprimed = new EntityTNTPrimedBase(world, x + 0.5D, y + 0.5D, z + 0.5D, living, this);
				world.spawnEntity(entitytntprimed);
				world.playSound(null, entitytntprimed.posX, entitytntprimed.posY, entitytntprimed.posZ, com.hbm.main.ModSoundEvents.resolve("game.tnt.primed"), net.minecraft.util.SoundCategory.PLAYERS, 1.0F, 1.0F);
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;

		if(player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() == Items.FLINT_AND_STEEL) {
			this.prime(world, x, y, z, 1, player);
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			player.getHeldItemMainhand().damageItem(1, player);
			return true;
		} else {
			return super.onBlockActivated(world, nativePos, nativeState, player, nativeHand, nativeFacing, hitX, hitY, hitZ);
		}
	}


		@Override
	public void onEntityCollision(net.minecraft.world.World worldIn, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, net.minecraft.entity.Entity entityIn) {
		onEntityCollidedWithBlock(worldIn, pos.getX(), pos.getY(), pos.getZ(), entityIn);
	}

public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		if(entity instanceof EntityArrow && !world.isRemote) {
			EntityArrow entityarrow = (EntityArrow) entity;

			if(entityarrow.isBurning()) {
				this.prime(world, x, y, z, 1, entityarrow.shootingEntity instanceof EntityLivingBase ? (EntityLivingBase) entityarrow.shootingEntity : null);
				com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		// The original built side icons from the setBlockTextureName base.
		String base = this.getTextureName() != null ? this.getTextureName() : this.getRegistryName().toString();
		this.blockIcon = iconRegister.registerIcon(base + "_side");
		this.topIcon = iconRegister.registerIcon(base + "_top");
		this.bottomIcon = iconRegister.registerIcon(base + "_bottom");
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {

		if(tool == ToolType.DEFUSER) {
			if(!world.isRemote) {
				com.hbm.world.WorldBlockDestruction.destroy(world, x, y, z, false);
				this.dropBlockAsItem(world, new net.minecraft.util.math.BlockPos(x, y, z), world.getBlockState(new net.minecraft.util.math.BlockPos(x, y, z)), 0);
			}
			return true;
		}

		if(tool != ToolType.SCREWDRIVER)
			return false;

		if(!world.isRemote) {
			int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );

			if(meta == 0) {
				com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 1, 3);
				player.sendMessage(ChatBuilder.start("[ Ignite On Break: Enabled ]").color(TextFormatting.RED).flush());
			} else {
				com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 0, 3);
				player.sendMessage(ChatBuilder.start("[ Ignite On Break: Disabled ]").color(TextFormatting.GOLD).flush());
			}
		}

		return true;
	}
}

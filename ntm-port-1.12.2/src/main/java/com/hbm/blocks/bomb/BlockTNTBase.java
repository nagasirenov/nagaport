package com.hbm.blocks.bomb;

import java.util.Random;

import com.hbm.entity.item.EntityTNTPrimedBase;
import com.hbm.util.ChatBuilder;

import api.hbm.block.IToolable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
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

	public BlockTNTBase() {
		super(Material.TNT, 15, 100, 20, false, false);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return side == 0 ? this.bottomIcon : (side == 1 ? this.topIcon : this.blockIcon);
	}
	
	@Override
	public void onBlockAdded(World world, int x, int y, int z) {
		super.onBlockAdded(world, x, y, z);

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

	@Override
	public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int meta) {
		this.prime(world, x, y, z, meta, (EntityLivingBase) null);
	}

	public void prime(World world, int x, int y, int z, int meta, EntityLivingBase living) {
		if(!world.isRemote) {
			if((meta & 1) == 1) {
				EntityTNTPrimedBase entitytntprimed = new EntityTNTPrimedBase(world, x + 0.5D, y + 0.5D, z + 0.5D, living, this);
				world.spawnEntityInWorld(entitytntprimed);
				world.playSoundAtEntity(entitytntprimed, "game.tnt.primed", 1.0F, 1.0F);
			}
		}
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(player.getCurrentEquippedItem() != null && player.getCurrentEquippedItem().getItem() == Items.FLINT_AND_STEEL) {
			this.prime(world, x, y, z, 1, player);
			com.hbm.world.WorldAirRemoval.remove(world, x, y, z);
			player.getCurrentEquippedItem().damageItem(1, player);
			return true;
		} else {
			return super.onBlockActivated(world, x, y, z, player, side, hitX, hitY, hitZ);
		}
	}

	@Override
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
		this.blockIcon = iconRegister.registerIcon(this.getTextureName() + "_side");
		this.topIcon = iconRegister.registerIcon(this.getTextureName() + "_top");
		this.bottomIcon = iconRegister.registerIcon(this.getTextureName() + "_bottom");
	}
	
	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		
		if(tool == ToolType.DEFUSER) {
			if(!world.isRemote) {
				com.hbm.world.WorldBlockDestruction.destroy(world, x, y, z, false);
				this.dropBlockAsItem(world, x, y, z, 0, 0);
			}
			return true;
		}
		
		if(tool != ToolType.SCREWDRIVER)
			return false;

		if(!world.isRemote) {
			int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
			
			if(meta == 0) {
				world.setBlockMetadataWithNotify(x, y, z, 1, 3);
				player.sendMessage(ChatBuilder.start("[ Ignite On Break: Enabled ]").color(TextFormatting.RED).flush());
			} else {
				world.setBlockMetadataWithNotify(x, y, z, 0, 3);
				player.sendMessage(ChatBuilder.start("[ Ignite On Break: Disabled ]").color(TextFormatting.GOLD).flush());
			}
		}
		
		return true;
	}
}

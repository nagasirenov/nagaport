package com.hbm.blocks.bomb;

import java.util.Random;

import org.apache.logging.log4j.Level;

import com.hbm.blocks.ModBlocks;
import com.hbm.config.GeneralConfig;
import com.hbm.explosion.ExplosionThermo;
import com.hbm.interfaces.IBomb;
import com.hbm.lib.RefStrings;
import com.hbm.main.MainRegistry;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.World;

public class BombThermo extends Block implements IBomb {

	@SideOnly(Side.CLIENT)
	private IIcon iconTop;

	public BombThermo(Material p_i45394_1_) {
		super(p_i45394_1_);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.iconTop = iconRegister.registerIcon(RefStrings.MODID + ":therm_top");
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + (this == ModBlocks.therm_exo ? ":therm_exo" : ":therm_endo"));
	}

	@Override
	public Item getItemDropped(net.minecraft.block.state.IBlockState p_149650_1_, Random p_149650_2_, int p_149650_3_) {
		if(this == ModBlocks.therm_endo) {
			return Item.getItemFromBlock(ModBlocks.therm_endo);
		}

		return Item.getItemFromBlock(ModBlocks.therm_exo);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {
		return side == 1 ? this.iconTop : (side == 0 ? this.iconTop : this.blockIcon);
	}

	@Override
	public void neighborChanged(net.minecraft.block.state.IBlockState nativeState, World p_149695_1_, net.minecraft.util.math.BlockPos nativePos, Block p_149695_5_, net.minecraft.util.math.BlockPos fromPos) {
		int x = nativePos.getX(); int y = nativePos.getY(); int z = nativePos.getZ();
		if(p_149695_1_.isBlockPowered(new net.minecraft.util.math.BlockPos(x, y, z))) {
			{ net.minecraft.world.World nativePlacementWorld = p_149695_1_; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
			if(this == ModBlocks.therm_endo) {
				ExplosionThermo.freeze(p_149695_1_, x, y, z, 15);
				ExplosionThermo.freezer(p_149695_1_, x, y, z, 20);
			}

			if(this == ModBlocks.therm_exo) {
				ExplosionThermo.scorch(p_149695_1_, x, y, z, 15);
				ExplosionThermo.setEntitiesOnFire(p_149695_1_, x, y, z, 20);
			}

			p_149695_1_.createExplosion(null, x, y, z, 5.0F, true);
		}
	}

	@Override
	public BombReturnCode explode(World world, int x, int y, int z) {
		{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = x; int nativePlacementY = y; int nativePlacementZ = z; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		if(this == ModBlocks.therm_endo) {
			ExplosionThermo.freeze(world, x, y, z, 15);
			ExplosionThermo.freezer(world, x, y, z, 20);
		}

		if(this == ModBlocks.therm_exo) {
			ExplosionThermo.scorch(world, x, y, z, 15);
			ExplosionThermo.setEntitiesOnFire(world, x, y, z, 20);
		}

		world.createExplosion(null, x, y, z, 5.0F, true);
		return BombReturnCode.DETONATED;
	}
	@Override
	public void onBlockPlacedBy(World world, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase player, ItemStack itemStack) {
		int x = nativePlacementPos.getX(), y = nativePlacementPos.getY(), z = nativePlacementPos.getZ();
	if(!world.isRemote) {
			if(GeneralConfig.enableExtendedLogging) {
			MainRegistry.logger.log(Level.INFO, "[BOMBPL]" + this.getLocalizedName() + " placed at " + x + " / " + y + " / " + z + "! " + "by "+ player.getCommandSenderName());
		}	
	}
}
}

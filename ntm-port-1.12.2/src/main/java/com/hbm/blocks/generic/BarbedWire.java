package com.hbm.blocks.generic;

import com.hbm.blocks.ModBlocks;
import com.hbm.lib.ModDamageSource;
import com.hbm.potion.HbmPotion;
import com.hbm.render.icon.IIcon;
import com.hbm.render.icon.IIconRegister;
import com.hbm.util.ArmorUtil;
import com.hbm.world.gen.nbt.INBTBlockTransformable;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BarbedWire extends Block implements INBTBlockTransformable {

	public static final PropertyInteger ORIENTATION = PropertyInteger.create("orientation", 0, 5);
	@SideOnly(Side.CLIENT)
	private IIcon blockIcon;
	private String modelTexture;

	public BarbedWire(Material mat) {
		super(mat);
		this.setDefaultState(this.blockState.getBaseState().withProperty(ORIENTATION, 0));
	}

	public BarbedWire setModelTexture(String texture) {
		this.modelTexture = texture;
		return this;
	}

	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, ORIENTATION);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(ORIENTATION, MathHelper.clamp(meta, 0, 5));
	}

	@Override
	public int getMetaFromState(IBlockState state) {
		return state.getValue(ORIENTATION);
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister register) {
		blockIcon = register.registerIcon(modelTexture);
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		return blockIcon;
	}

	@Override
	public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity ent) {

		ent.setInWeb();

		ent.motionX *= 0.15D;
		ent.motionY *= 0.1D;
		ent.motionZ *= 0.15D;

		if(this == ModBlocks.barbed_wire) {
			ent.attackEntityFrom(DamageSource.CACTUS, 2.0F);
		}

		if(this == ModBlocks.barbed_wire_fire) {
			ent.attackEntityFrom(DamageSource.CACTUS, 2.0F);
			ent.setFire(1);
		}

		if(this == ModBlocks.barbed_wire_poison) {
			ent.attackEntityFrom(DamageSource.CACTUS, 2.0F);

			if(ent instanceof EntityLivingBase)
				((EntityLivingBase) ent).addPotionEffect(new PotionEffect(MobEffects.POISON, 5 * 20, 2));

		}

		if(this == ModBlocks.barbed_wire_acid) {
			ent.attackEntityFrom(DamageSource.CACTUS, 2.0F);

			if(ent instanceof EntityPlayer) {
				ArmorUtil.damageSuit((EntityPlayer) ent, 0, 1);
				ArmorUtil.damageSuit((EntityPlayer) ent, 1, 1);
				ArmorUtil.damageSuit((EntityPlayer) ent, 2, 1);
				ArmorUtil.damageSuit((EntityPlayer) ent, 3, 1);
			}
		}

		if(this == ModBlocks.barbed_wire_wither) {
			ent.attackEntityFrom(DamageSource.CACTUS, 2.0F);

			if(ent instanceof EntityLivingBase)
				((EntityLivingBase) ent).addPotionEffect(new PotionEffect(MobEffects.WITHER, 5 * 20, 4));
		}

		if(this == ModBlocks.barbed_wire_ultradeath) {
			ent.attackEntityFrom(ModDamageSource.pc, 5.0F);

			if(ent instanceof EntityLivingBase)
				((EntityLivingBase) ent).addPotionEffect(new PotionEffect(HbmPotion.radiation, 5 * 20, 9));
		}
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	@Override
	public boolean isFullCube(IBlockState state) {
		return false;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return null;
	}

	@Override
	public int damageDropped(IBlockState state) {
		return 0;
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemStack) {
		int quadrant = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
		int orientation;
		switch(quadrant) {
			case 0: orientation = 5; break;
			case 1: orientation = 3; break;
			case 2: orientation = 4; break;
			default: orientation = 2; break;
		}
		world.setBlockState(pos, state.withProperty(ORIENTATION, orientation), 2);
	}

	@Override
	public int transformMeta(int meta, int coordBaseMode) {
		return INBTBlockTransformable.transformMetaDeco(meta, coordBaseMode);
	}
}

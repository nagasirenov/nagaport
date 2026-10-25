package com.hbm.blocks.generic;
import com.hbm.render.icon.IIcon;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.deco.TileEntityDecoBlockAltF;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class DecoBlockAlt extends BlockContainer {

	protected IIcon blockIcon;

	public DecoBlockAlt(Material p_i45386_1_) {
		super(p_i45386_1_);
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		this.blockIcon = iconRegister.registerIcon(RefStrings.MODID + ":code");
	}

	/** Legacy icon view for the native fallback models. */
	@SideOnly(Side.CLIENT)
	public static IIcon getLegacyBlockIcon() {
		return com.hbm.render.icon.LegacyIconRegister.wrapSprite(net.minecraft.client.Minecraft.getMinecraft().getTextureMapBlocks().getTextureExtry(com.hbm.lib.RefStrings.MODID + ":blocks/code"));
	}

	@Override
	public TileEntity createNewTileEntity(World p_149915_1_, int p_149915_2_) {
		if(this == ModBlocks.statue_elb_f) return new TileEntityDecoBlockAltF();
		return null;
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
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 2, 2);
		}
		if(i == 1) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 5, 2);
		}
		if(i == 2) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 3, 2);
		}
		if(i == 3) {
			com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(world, x, y, z, 4, 2);
		}
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;

		if(world.isRemote) {
			return true;
		} else if(!player.isSneaking()) {

			if(player.getHeldItemMainhand() != null) {
				boolean cracked = com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.key_red_cracked;

				if((com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.key_red || cracked)) {
					if(cracked) com.hbm.util.NativeHeldItem.getMainHandNullable(player).shrink(1);
					com.hbm.world.WorldBlockDestruction.destroy(world, x, y, z, true);
					return true;
				}
			}
		}
		return false;
	}

	private static final AxisAlignedBB BOUNDS = new AxisAlignedBB(0.0F, 0.0F, 0.0F, 1.0F, 8 * 0.0625F, 1.0F);

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return BOUNDS;
	}
}

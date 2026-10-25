package com.hbm.blocks.bomb;

import com.hbm.entity.item.EntityTNTPrimedBase;
import com.hbm.explosion.vanillant.ExplosionVNT;
import com.hbm.explosion.vanillant.standard.BlockProcessorStandard;
import com.hbm.interfaces.IBomb;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockPlasticExplosive extends BlockDetonatable implements IBomb {

	@SideOnly(Side.CLIENT)
	private IIcon topIcon;

	public BlockPlasticExplosive(Material mat) {
		super(mat, 0, 0, 0, false, false);
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister p_149651_1_) {
		this.blockIcon = p_149651_1_.registerIcon(this.getRegistryName().toString());
		this.topIcon = p_149651_1_.registerIcon(this.getRegistryName().toString() + "_front");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		int k = getPistonOrientation(meta);
		return ForgeDirection.getOrientation(k).getOpposite().ordinal() == side ? this.topIcon : this.blockIcon;
	}

	public static int getPistonOrientation(int meta) {
		return meta & 7;
	}

	@Override
	public void onBlockPlacedBy(World p_149689_1_, net.minecraft.util.math.BlockPos nativePlacementPos, net.minecraft.block.state.IBlockState nativePlacementState, EntityLivingBase p_149689_5_, ItemStack p_149689_6_) {
		int p_149689_2_ = nativePlacementPos.getX(), p_149689_3_ = nativePlacementPos.getY(), p_149689_4_ = nativePlacementPos.getZ();
		int l = determineOrientation(p_149689_1_, p_149689_2_, p_149689_3_, p_149689_4_, p_149689_5_);
		com.hbm.world.WorldBlockState.setBlockMetadataWithNotify(p_149689_1_, p_149689_2_, p_149689_3_, p_149689_4_, l, 2);
	}

	public static int determineOrientation(World world, int x, int y, int z, EntityLivingBase player) {

		if(MathHelper.abs((float) player.posX - (float) x) < 2.0F && MathHelper.abs((float) player.posZ - (float) z) < 2.0F) {
			double d0 = player.posY + 1.82D - (double) player.getYOffset();

			if(d0 - (double) y > 2.0D) {
				return 0;
			}

			if((double) y - d0 > 0.0D) {
				return 1;
			}
		}

		int l = MathHelper.floor((double) (player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;

		return l == 0 ? 3 : (l == 1 ? 4 : (l == 2 ? 2 : (l == 3 ? 5 : 1)));
	}

	@Override
	public void neighborChanged(net.minecraft.block.state.IBlockState nativeState, World world, net.minecraft.util.math.BlockPos nativePos, Block block, net.minecraft.util.math.BlockPos fromPos) {
		int x = nativePos.getX(); int y = nativePos.getY(); int z = nativePos.getZ();
		if(world.isBlockPowered(new net.minecraft.util.math.BlockPos(x, y, z))) {
			this.explode(world, x, y, z);
		}
	}

	@Override
	public BombReturnCode explode(World world, int x, int y, int z) {

		if(!world.isRemote) {
			new ExplosionVNT(world, x + 0.5, y + 0.5, z + 0.5, 20).makeStandard().setBlockProcessor(new BlockProcessorStandard().setNoDrop()).explode();
		}

		return BombReturnCode.DETONATED;
	}

	@Override
	public void explodeEntity(World world, double x, double y, double z, EntityTNTPrimedBase entity) {
		explode(world, MathHelper.floor(x), MathHelper.floor(y), MathHelper.floor(z));
	}
}

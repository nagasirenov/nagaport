package com.hbm.blocks.network;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import com.hbm.lib.Library;
import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.LegacyBlockTextures;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.lib.Library;
import com.hbm.tileentity.network.TileEntityPipeAnchor;
import com.hbm.tileentity.network.TileEntityPipeBaseNT;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import com.hbm.lib.ForgeDirection;

public class FluidPipeAnchor extends FluidDuctBase implements ITooltipProvider, ILookOverlay {

	public FluidPipeAnchor withSoundType(net.minecraft.block.SoundType sound) {
		super.setSoundType(sound);
		return this;
	}

	public FluidPipeAnchor() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityPipeAnchor();
	}

	@Override public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
		return net.minecraft.util.EnumBlockRenderType.INVISIBLE;
	}
	@Override public boolean isOpaqueCube(net.minecraft.block.state.IBlockState state) { return false; }
	@Override public boolean isFullCube(net.minecraft.block.state.IBlockState state) { return false; }

	public int onBlockPlaced(World world, int x, int y, int z, int side, float fX, float fY, float fZ, int meta) {
		return side;
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		int meta = com.hbm.world.WorldBlockState.readMetadata(source, pos.getX(), pos.getY(), pos.getZ());
		double pixel = 0.0625D;
		double min = pixel * 4.0D;
		double max = pixel * 12.0D;

		ForgeDirection dir = ForgeDirection.getOrientation(meta).getOpposite();

		double minX = dir == Library.NEG_X ? 0.0D : min;
		double maxX = dir == Library.POS_X ? 1.0D : max;
		double minY = dir == Library.NEG_Y ? 0.0D : min;
		double maxY = dir == Library.POS_Y ? 1.0D : max;
		double minZ = dir == Library.NEG_Z ? 0.0D : min;
		double maxZ = dir == Library.POS_Z ? 1.0D : max;

		return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		list.add(TextFormatting.GOLD + "Connection Type: " + TextFormatting.YELLOW + "Single");
		list.add(TextFormatting.GOLD + "Connection Range: " + TextFormatting.YELLOW + "10m");
	}

	@Override // didn't think this was overridable, that makes everything so much easier. good job martin
	public void changeTypeRecursively(World world, int x, int y, int z, FluidType prevType, FluidType type, int loopsRemaining) {

		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(te instanceof TileEntityPipeAnchor) {
			TileEntityPipeAnchor pipe = (TileEntityPipeAnchor) te;

			if(pipe.getType() == prevType && pipe.getType() != type) {
				pipe.setType(type);

				if(loopsRemaining > 0) {
					ForgeDirection dir = ForgeDirection.getOrientation(pipe.getBlockMetadata()).getOpposite();
					Block b = com.hbm.world.WorldBlockState.read(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ).getBlock();

					if(b instanceof IBlockFluidDuct) ((IBlockFluidDuct) b).changeTypeRecursively(world, x + dir.offsetX, y + dir.offsetY, z + dir.offsetZ, prevType, type, loopsRemaining - 1);

					for(int[] pos : pipe.getConnected()) {
						Block c = com.hbm.world.WorldBlockState.read(world, pos[0], pos[1], pos[2]).getBlock();
						if(c instanceof IBlockFluidDuct) ((IBlockFluidDuct) c).changeTypeRecursively(world, pos[0], pos[1], pos[2], prevType, type, loopsRemaining - 1);
					}
				}
			}
		}
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {

		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));

		if(!(te instanceof TileEntityPipeBaseNT))
			return;

		TileEntityPipeBaseNT duct = (TileEntityPipeBaseNT) te;

		List<String> text = new ArrayList();
		text.add("&[" + duct.getType().getColor() + "&]" + duct.getType().getLocalizedName());
		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}
}

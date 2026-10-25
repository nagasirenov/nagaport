package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.inventory.fluid.FluidType;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.inventory.fluid.tank.FluidTank;
import com.hbm.inventory.fluid.trait.FT_Coolable;
import com.hbm.tileentity.IPersistentNBT;
import com.hbm.tileentity.IRepairable;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityChungus;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.block.IToolable;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import com.hbm.lib.ForgeDirection;

public class MachineChungus extends BlockDummyable implements ITooltipProvider, ILookOverlay, IToolable {

	public MachineChungus(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityChungus();
		if(meta >= 6) return new TileEntityProxyCombo(false, true, true);
		return null;
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;


		if(!player.isSneaking()) {

			int[] pos = this.findCore(world, x, y, z);

			if(pos == null)
				return true;

			TileEntityChungus entity = (TileEntityChungus) world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
			if(entity != null && !entity.damaged) {

				ForgeDirection dir = ForgeDirection.getOrientation(entity.getBlockMetadata() - offset);
				ForgeDirection turn = dir.getRotation(ForgeDirection.DOWN);

				int iX = entity.getPos().getX() + dir.offsetX + turn.offsetX * 2;
				int iX2 = entity.getPos().getX() + dir.offsetX * 2 + turn.offsetX * 2;
				int iZ = entity.getPos().getZ() + dir.offsetZ + turn.offsetZ * 2;
				int iZ2 = entity.getPos().getZ() + dir.offsetZ * 2 + turn.offsetZ * 2;

				if((x == iX || x == iX2) && (z == iZ || z == iZ2) && y < entity.getPos().getY() + 2) {
					com.hbm.sound.WorldSoundEffects.play(world, x + 0.5, y + 0.5, z + 0.5, "hbm:block.chungusLever", 1.5F, 1.0F);

					if(!world.isRemote) {
						FluidType type = entity.tanks[0].getTankType();

						if(type == Fluids.STEAM) {
							entity.tanks[0].setTankType(Fluids.HOTSTEAM);
							entity.tanks[1].setTankType(Fluids.STEAM);
							entity.tanks[0].setFill(entity.tanks[0].getFill() / 10);
							entity.tanks[1].setFill(0);
						} else if(type == Fluids.HOTSTEAM) {
							entity.tanks[0].setTankType(Fluids.SUPERHOTSTEAM);
							entity.tanks[1].setTankType(Fluids.HOTSTEAM);
							entity.tanks[0].setFill(entity.tanks[0].getFill() / 10);
							entity.tanks[1].setFill(0);
						} else if(type == Fluids.SUPERHOTSTEAM) {
							entity.tanks[0].setTankType(Fluids.ULTRAHOTSTEAM);
							entity.tanks[1].setTankType(Fluids.SUPERHOTSTEAM);
							entity.tanks[0].setFill(entity.tanks[0].getFill() / 10);
							entity.tanks[1].setFill(0);
						} else {
							entity.tanks[0].setTankType(Fluids.STEAM);
							entity.tanks[1].setTankType(Fluids.SPENTSTEAM);
							entity.tanks[0].setFill(Math.min(entity.tanks[0].getFill() * 1000, entity.tanks[0].getMaxFill()));
							entity.tanks[1].setFill(0);
						}
						entity.markDirty();
					}

					return true;
				}
			}
		}

		return false;
	}

	@Override
	public int[] getDimensions() {
		return new int[] { 4, 0, 10, 3, 2, 2 };
	}

	@Override
	public int getOffset() {
		return 3;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {3, 0, 0, 3, 2, 2}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {4, -4, 0, 3, 1, 1}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {3, 0, 6, -1, 1, 1}, this, dir);
		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {2, 0, 10, -7, 1, 1}, this, dir);
		com.hbm.world.WorldBlockState.writeLegacyBlock(world, x + dir.offsetX, y + 2, z + dir.offsetZ, this, dir.ordinal(), 3);

		this.makeExtra(world, x + dir.offsetX, y + 2, z + dir.offsetZ); //front connector
		this.makeExtra(world, x + dir.offsetX * (o - 10), y, z + dir.offsetZ * (o - 10)); //back connector
		ForgeDirection side = dir.getRotation(ForgeDirection.UP);
		this.makeExtra(world, x + dir.offsetX * o + side.offsetX * 2 , y, z + dir.offsetZ * o + side.offsetZ * 2); //side connectors
		this.makeExtra(world, x + dir.offsetX * o - side.offsetX * 2 , y, z + dir.offsetZ * o - side.offsetZ * 2);
	}

	@Override
	protected boolean checkRequirement(World world, int x, int y, int z, ForgeDirection dir, int o) {

		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {3, 0, 0, 3, 2, 2}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {4, -4, 0, 3, 1, 1}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {3, 0, 6, -1, 1, 1}, x, y, z, dir)) return false;
		if(!MultiblockHandlerXR.checkSpace(world, x + dir.offsetX * o , y + dir.offsetY * o, z + dir.offsetZ * o, new int[] {2, 0, 10, -7, 1, 1}, x, y, z, dir)) return false;
		if(!com.hbm.world.WorldBlockState.read(world, x + dir.offsetX, y + 2, z + dir.offsetZ).getBlock().canPlaceBlockAt(world, new net.minecraft.util.math.BlockPos(x + dir.offsetX, y + 2, z + dir.offsetZ))) return false;

		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return;

		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
		if(!(te instanceof TileEntityChungus)) return;

		TileEntityChungus chungus = (TileEntityChungus) te;
		if(chungus.damaged) {
			IRepairable.addGenericOverlay(event, world, x, y, z, this);
			return;
		}

		List<String> text = new ArrayList<>();

		FluidTank tankInput = chungus.tanks[0];
		FluidTank tankOutput = chungus.tanks[1];

		FluidType inputType = tankInput.getTankType();
		FluidType outputType = Fluids.NONE;

		if(inputType.hasTrait(FT_Coolable.class)) {
			outputType = inputType.getTrait(FT_Coolable.class).coolsTo;
		}

		text.add(TextFormatting.GREEN + "-> " + TextFormatting.RESET + inputType.getLocalizedName() + ": " + String.format(Locale.US, "%,d", tankInput.getFill()) + "/" + String.format(Locale.US, "%,d", tankInput.getMaxFill()) + "mB");
		text.add(TextFormatting.RED + "<- " + TextFormatting.RESET + outputType.getLocalizedName() + ": " + String.format(Locale.US, "%,d", tankOutput.getFill()) + "/" + String.format(Locale.US, "%,d", tankOutput.getMaxFill()) + "mB");
		text.add(TextFormatting.RED + "<- " + TextFormatting.RESET + BobMathUtil.getShortNumber(chungus.powerBuffer) + "HE");


		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		if(tool != ToolType.TORCH) return false;
		return IRepairable.tryRepairMultiblock(world, x, y, z, this, player);
	}


		@Override
	public void getDrops(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> drops, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, int fortune) {
		int meta = state.getBlock().getMetaFromState(state);
		net.minecraft.world.World w = world instanceof net.minecraft.world.World ? (net.minecraft.world.World) world : null;
		java.util.ArrayList<net.minecraft.item.ItemStack> ret = getDrops(w, pos.getX(), pos.getY(), pos.getZ(), meta, fortune);
		if(ret != null) drops.addAll(ret);
	}

public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		return IPersistentNBT.getDrops(world, x, y, z, this);
	}

	@Override
	public boolean canDropFromExplosion(Explosion explosion) {
		return false;
	}

	@Override
	public void onBlockExploded(World world, net.minecraft.util.math.BlockPos nativePos, Explosion explosion) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();

		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return;
		TileEntity core = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
		if(!(core instanceof TileEntityChungus)) return;

		TileEntityChungus tank = (TileEntityChungus) core;
		if(tank.lastExplosion == explosion) return;
		tank.lastExplosion = explosion;

		if(!tank.damaged) {
			tank.damaged = true;
			tank.markDirty();
		} else {
			{ net.minecraft.world.World nativePlacementWorld = world; int nativePlacementX = pos[0]; int nativePlacementY = pos[1]; int nativePlacementZ = pos[2]; net.minecraft.block.Block nativePlacementBlock = Blocks.AIR; int nativePlacementMeta = 0; int nativePlacementFlags = 3; if (nativePlacementWorld == null) throw new NullPointerException(); if (nativePlacementX >= -30000000 && nativePlacementX < 30000000 && nativePlacementZ >= -30000000 && nativePlacementZ < 30000000 && nativePlacementY >= 0 && nativePlacementY < 256) nativePlacementWorld.setBlockState(new net.minecraft.util.math.BlockPos(nativePlacementX, nativePlacementY, nativePlacementZ), nativePlacementBlock.getStateFromMeta(nativePlacementMeta), nativePlacementFlags); }
		}
	}

}

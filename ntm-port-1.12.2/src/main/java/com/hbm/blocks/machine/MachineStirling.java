package com.hbm.blocks.machine;

import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.IBlockMulti;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityStirling;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import com.hbm.lib.ForgeDirection;

public class MachineStirling extends BlockDummyable implements ILookOverlay, ITooltipProvider, IBlockMulti {

	public MachineStirling() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {

		if(meta >= 12)
			return new TileEntityStirling();

		if(meta >= extra)
			return new TileEntityProxyCombo().power();

		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {1, 0, 1, 1, 1, 1};
	}

	@Override
	public int getOffset() {
		return 1;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		super.fillSpace(world, x, y, z, dir, o);

		x = x + dir.offsetX * o;
		z = z + dir.offsetZ * o;

		this.makeExtra(world, x + 1, y, z);
		this.makeExtra(world, x - 1, y, z);
		this.makeExtra(world, x, y, z + 1);
		this.makeExtra(world, x, y, z - 1);
	}

	@Override
	public boolean onBlockActivated(World world, net.minecraft.util.math.BlockPos nativePos, net.minecraft.block.state.IBlockState nativeState, EntityPlayer player, net.minecraft.util.EnumHand nativeHand, net.minecraft.util.EnumFacing nativeFacing, float hitX, float hitY, float hitZ) {
		int x = nativePos.getX(), y = nativePos.getY(), z = nativePos.getZ();
		int side = nativeFacing != null ? nativeFacing.getIndex() : 0;


		if(world.isRemote) {
			return true;

		} else if(!player.isSneaking()) {
			int[] pos = this.findCore(world, x, y, z);

			if(pos == null)
				return false;

			TileEntityStirling stirling = (TileEntityStirling)world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
			int meta = stirling.getGeatMeta();

			if(!stirling.hasCog && com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() == ModItems.gear_large && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItemDamage() == meta) {
				com.hbm.util.NativeHeldItem.getMainHandNullable(player).shrink(1);
				stirling.hasCog = true;
				stirling.markDirty();
				world.playSound(null, x + 0.5, y + 0.5, z + 0.5, com.hbm.main.ModSoundEvents.resolve("hbm:item.upgradePlug"), net.minecraft.util.SoundCategory.BLOCKS, 1.5F, 0.75F);
				return true;
			}
		}

		return false;
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack itemStack) {
		super.onBlockPlacedBy(world, new net.minecraft.util.math.BlockPos(x, y, z), com.hbm.world.WorldBlockState.read(world, x, y, z), player, itemStack);

		if(itemStack.getItemDamage() == 1) {

			int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;
			int o = -getOffset();

			ForgeDirection dir = ForgeDirection.NORTH;
			if(i == 0) dir = ForgeDirection.getOrientation(2);
			if(i == 1) dir = ForgeDirection.getOrientation(5);
			if(i == 2) dir = ForgeDirection.getOrientation(3);
			if(i == 3) dir = ForgeDirection.getOrientation(4);

			dir = getDirModified(dir);

			TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o));

			if(te instanceof TileEntityStirling) {
				((TileEntityStirling) te).hasCog = false;
			}
		}
	}


		@Override
	public void getDrops(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> drops, net.minecraft.world.IBlockAccess world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, int fortune) {
		int meta = state.getBlock().getMetaFromState(state);
		net.minecraft.world.World w = world instanceof net.minecraft.world.World ? (net.minecraft.world.World) world : null;
		java.util.ArrayList<net.minecraft.item.ItemStack> ret = getDrops(w, pos.getX(), pos.getY(), pos.getZ(), meta, fortune);
		if(ret != null) drops.addAll(ret);
	}

public ArrayList<ItemStack> getDrops(World world, int x, int y, int z, int metadata, int fortune) {
		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();

		net.minecraft.block.state.IBlockState state = this.getStateFromMeta(metadata);
		int count = quantityDropped(state, fortune, world.rand);
		int dmg = 0;

		int[] pos = this.findCore(world, x, y, z);

		if(pos != null) {
			TileEntityStirling stirling = (TileEntityStirling)world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
			if(!stirling.hasCog) {
				dmg = 1;
			}
		}

		for(int i = 0; i < count; i++) {
			Item item = getItemDropped(state, world.rand, fortune);
			if(item != null) {
				ret.add(new ItemStack(item, 1, dmg));
			}
		}
		return ret;
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {

		int[] pos = this.findCore(world, x, y, z);

		if(pos == null)
			return;

		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));

		if(!(te instanceof TileEntityStirling))
			return;

		TileEntityStirling stirling = (TileEntityStirling) te;

		List<String> text = new ArrayList();
		text.add(stirling.heat + "TU/t");
		text.add((stirling.hasCog ? stirling.powerBuffer : 0) + "HE/t");

		if(this != ModBlocks.machine_stirling_creative) {
			int maxHeat = stirling.maxHeat();
			double percent = (double) stirling.heat / (double) maxHeat;
			int color = ((int) (0xFF - 0xFF * percent)) << 16 | ((int)(0xFF * percent) << 8);

			if(percent > 1D)
				color = 0xff0000;

			text.add("&[" + color + "&]" + ((stirling.heat * 1000 / maxHeat) / 10D) + "%");

			if(stirling.heat > maxHeat) {
				text.add("&[" + (BobMathUtil.getBlink() ? 0xff0000 : 0xffff00) + "&]! ! ! OVERSPEED ! ! !");
			}

			if(!stirling.hasCog) {
				text.add("&[" + 0xff0000 + "&]Gear missing!");
			}
		}

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}

	@Override
	public int getSubCount() {
		return 0;
	}
}

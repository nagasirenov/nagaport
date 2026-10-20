package com.hbm.blocks.machine;

import api.hbm.block.IToolable;
import api.hbm.conveyor.IConveyorBelt;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.items.machine.ItemStamp;
import com.hbm.tileentity.machine.TileEntityConveyorPress;
import com.hbm.util.BobMathUtil;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.material.Material;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import com.hbm.lib.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class MachineConveyorPress extends BlockDummyable implements IConveyorBelt, ILookOverlay, IToolable, ITooltipProvider {

	public MachineConveyorPress(Material mat) {
		super(mat);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		if(meta >= 12) return new TileEntityConveyorPress();
		return null;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {2, 0, 0, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ) {
		if(world.isRemote) {
			return true;
		} else {

			int[] pos = this.findCore(world, x, y, z);

			if(pos == null)
				return false;

			TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));

			if(!(te instanceof TileEntityConveyorPress))
				return false;

			TileEntityConveyorPress press = (TileEntityConveyorPress) te;

			if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() instanceof ItemStamp && press.slots[0] == null) {
				press.slots[0] = com.hbm.util.NativeHeldItem.getMainHandNullable(player).copy();
				press.slots[0].setCount(1);
				com.hbm.util.NativeHeldItem.getMainHandNullable(player).stackSize--;
				world.playSoundEffect(x + 0.5, y + 0.5, z + 0.5, "hbm:item.upgradePlug", 1.0F, 1.0F);
				press.markChanged();
				world.markBlockForUpdate(x, y, z);
				return true;
			}
		}

		return false;
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {

		if(tool != ToolType.SCREWDRIVER) return false;

		int[] pos = this.findCore(world, x, y, z);

		if(pos == null)
			return false;

		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));

		if(!(te instanceof TileEntityConveyorPress))
			return false;

		TileEntityConveyorPress press = (TileEntityConveyorPress) te;

		if(press.slots[0] == null) return false;

		if(!player.inventory.addItemStackToInventory(press.slots[0].copy())) {
			EntityItem item = new EntityItem(world, x + 0.5, y + this.maxY, z + 0.5, press.slots[0].copy());
			world.spawnEntity(item);
		} else {
			player.inventoryContainer.detectAndSendChanges();
		}

		press.slots[0] = null;
		press.markChanged();

		return true;
	}

	@Override
	public Vec3d getTravelLocation(World world, int x, int y, int z, Vec3d itemPos, double speed) {
		ForgeDirection dir = this.getTravelDirection(world, x, y, z, itemPos);
		Vec3d snap = this.getClosestSnappingPosition(world, x, y, z, itemPos);
		itemPos = new Vec3d(MathHelper.clamp(itemPos.x, (double) (x), (double) (x + 1)), itemPos.y, MathHelper.clamp(itemPos.z, (double) (z), (double) (z + 1)));
		Vec3d dest = new Vec3d(snap.x - dir.offsetX * speed, snap.y - dir.offsetY * speed, snap.z - dir.offsetZ * speed);
		Vec3d motion = new Vec3d((dest.x - itemPos.x), (dest.y - itemPos.y), (dest.z - itemPos.z));
		double len = motion.length();
		Vec3d ret = new Vec3d(itemPos.x + motion.x / len * speed, itemPos.y + motion.y / len * speed, itemPos.z + motion.z / len * speed);
		return ret;
	}

	public ForgeDirection getTravelDirection(World world, int x, int y, int z, Vec3d itemPos) {
		int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y - 1, z) ) - offset;
		return ForgeDirection.getOrientation(meta).getRotation(ForgeDirection.UP);
	}

	@Override
	public Vec3d getClosestSnappingPosition(World world, int x, int y, int z, Vec3d itemPos) {

		ForgeDirection dir = this.getTravelDirection(world, x, y, z, itemPos);
		double itemX = MathHelper.clamp(itemPos.x, (double) (x), (double) (x + 1));
		double itemZ = MathHelper.clamp(itemPos.z, (double) (z), (double) (z + 1));
		double posX = x + 0.5;
		double posZ = z + 0.5;
		if(dir.offsetX != 0) posX = itemX;
		if(dir.offsetZ != 0) posZ = itemZ;
		return new Vec3d(posX, y + 0.25, posZ);
	}

	@Override
	public boolean canItemStay(World world, int x, int y, int z, Vec3d itemPos) {
		return com.hbm.world.WorldBlockState.read(world, x, y - 1, z).getBlock() == this && com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y - 1, z) ) >= 12;
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		int[] pos = this.findCore(world, x, y, z);

		if(pos == null)
			return;

		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));

		if(!(te instanceof TileEntityConveyorPress))
			return;

		TileEntityConveyorPress press = (TileEntityConveyorPress) te;
		List<String> text = new ArrayList();

		text.add(BobMathUtil.getShortNumber(press.power) + "HE / " + BobMathUtil.getShortNumber(press.maxPower) + "HE");
		text.add("Installed stamp: " + ((press.syncStack == null || press.syncStack.getItem() == null) ? (TextFormatting.RED + "NONE") : press.syncStack.getDisplayName()));

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}

	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}
}

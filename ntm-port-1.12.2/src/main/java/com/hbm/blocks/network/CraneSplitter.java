package com.hbm.blocks.network;

import api.hbm.block.IToolable;
import api.hbm.conveyor.IConveyorBelt;
import api.hbm.conveyor.IConveyorItem;
import api.hbm.conveyor.IConveyorPackage;
import api.hbm.conveyor.IEnterableBlock;
import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ITooltipProvider;
import com.hbm.entity.item.EntityMovingItem;
import com.hbm.tileentity.network.TileEntityCraneSplitter;
import com.hbm.util.i18n.I18nUtil;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import com.hbm.lib.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class CraneSplitter extends BlockDummyable implements IConveyorBelt, IEnterableBlock, ITooltipProvider, IToolable, ILookOverlay {


	public CraneSplitter() {
		super(Material.IRON);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityCraneSplitter();
	}

	@Override
	public int[] getDimensions() {
		return new int[] {0, 0, 0, 0, 0, 1};
	}

	@Override
	public int getOffset() {
		return 0;
	}

    /** Exact original registry owner, not a class-wide renderer override for foreign subclasses. */
    public static boolean isNativeModelOwner(net.minecraft.block.Block block) {
        return block != null && block.getClass() == CraneSplitter.class
                && new net.minecraft.util.ResourceLocation("hbm", "tile.crane_splitter").equals(block.getRegistryName());
    }
    @Override public net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state) {
        return net.minecraft.util.EnumBlockRenderType.MODEL;
    }
    @Override public net.minecraft.util.BlockRenderLayer getRenderLayer() { return net.minecraft.util.BlockRenderLayer.CUTOUT_MIPPED; }
    @Override public boolean hasCustomBreakingProgress(net.minecraft.block.state.IBlockState state) { return isNativeModelOwner(this); }

	@Override public boolean canItemEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorItem entity) { return getTravelDirection(world, x, y, z, null) == dir; }
	@Override public boolean canPackageEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorPackage entity) { return false; }
	@Override public void onPackageEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorPackage entity) { }

	@Override
	public void onItemEnter(World world, int x, int y, int z, ForgeDirection dir, IConveyorItem entity) {
		int[] core = this.findCore(world, x, y, z);
		if(core == null) return;
		x = core[0];
		y = core[1];
		z = core[2];
		TileEntity tile = world.getTileEntity(new BlockPos(x, y, z));
		if(!(tile instanceof TileEntityCraneSplitter)) return;
		TileEntityCraneSplitter splitter = (TileEntityCraneSplitter) tile;
		ForgeDirection rot = ForgeDirection.getOrientation(splitter.getBlockMetadata() - offset).getRotation(ForgeDirection.DOWN);

		ItemStack[] splits = splitter.splitStack(entity.getItemStack());

		spawnMovingItem(world, x, y, z, splits[0]);
		spawnMovingItem(world, x + rot.offsetX, y, z + rot.offsetZ, splits[1]);
	}

	private void spawnMovingItem(World world, int x, int y, int z, ItemStack stack) {
		if(stack == null || stack.isEmpty()) return;
		EntityMovingItem moving = new EntityMovingItem(world);
		Vec3d pos = new Vec3d(x + 0.5, y + 0.5, z + 0.5);
		Vec3d snap = this.getClosestSnappingPosition(world, x, y, z, pos);
		moving.setPosition(snap.x, snap.y, snap.z);
		moving.setItemStack(stack);
		world.spawnEntity(moving);
	}

	@Override
	public boolean canItemStay(World world, int x, int y, int z, Vec3d itemPos) {
		return true;
	}

	@Override
	public Vec3d getTravelLocation(World world, int x, int y, int z, Vec3d itemPos, double speed) {
		ForgeDirection dir = this.getTravelDirection(world, x, y, z, itemPos);
		Vec3d snap = this.getClosestSnappingPosition(world, x, y, z, itemPos);
		// The original mutable Vec3 was clamped by getClosestSnappingPosition before this arithmetic.
		itemPos = new Vec3d(MathHelper.clamp(itemPos.x, x, x + 1), itemPos.y, MathHelper.clamp(itemPos.z, z, z + 1));
		Vec3d dest = new Vec3d(snap.x - dir.offsetX * speed, snap.y - dir.offsetY * speed, snap.z - dir.offsetZ * speed);
		Vec3d motion = new Vec3d((dest.x - itemPos.x), (dest.y - itemPos.y), (dest.z - itemPos.z));
		double len = motion.length();
		Vec3d ret = new Vec3d(itemPos.x + motion.x / len * speed, itemPos.y + motion.y / len * speed, itemPos.z + motion.z / len * speed);
		return ret;
	}

	@Override
	public Vec3d getClosestSnappingPosition(World world, int x, int y, int z, Vec3d itemPos) {
		ForgeDirection dir = this.getTravelDirection(world, x, y, z, itemPos);
		double itemX = MathHelper.clamp(itemPos.x, x, x + 1);
		double itemZ = MathHelper.clamp(itemPos.z, z, z + 1);
		double posX = x + 0.5;
		double posZ = z + 0.5;
		if(dir.offsetX != 0) posX = itemX;
		if(dir.offsetZ != 0) posZ = itemZ;
		return new Vec3d(posX, y + 0.25, posZ);
	}

	public ForgeDirection getTravelDirection(World world, int x, int y, int z, Vec3d itemPos) {
		int meta = getMetaFromState(world.getBlockState(new BlockPos(x, y, z)));
		if(meta >= 12) return ForgeDirection.getOrientation(meta - offset);
		return ForgeDirection.getOrientation(meta).getRotation(ForgeDirection.UP);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean ext) {
		this.addStandardInfo(stack, player, list, ext);
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		if(world.isRemote) return true;
		if(tool != ToolType.SCREWDRIVER) return false;

		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return false;

		TileEntity te = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
		if(!(te instanceof TileEntityCraneSplitter)) return false;

		TileEntityCraneSplitter crane = (TileEntityCraneSplitter) te;

		// The core of the dummy is always the left hand block
		boolean isLeft = x == pos[0] && y == pos[1] && z == pos[2];
		int adjust = player.isSneaking() ? -1 : 1;

		if(isLeft) {
			crane.leftRatio = (byte)MathHelper.clamp(crane.leftRatio + adjust, 1, 16);
		} else {
			crane.rightRatio = (byte)MathHelper.clamp(crane.rightRatio + adjust, 1, 16);
		}

		return true;
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return;

		TileEntity te = world.getTileEntity(new BlockPos(pos[0], pos[1], pos[2]));
		if(!(te instanceof TileEntityCraneSplitter)) return;

		TileEntityCraneSplitter crane = (TileEntityCraneSplitter) te;

		List<String> text = new ArrayList<>();
		text.add("Splitter ratio: " + crane.leftRatio + ":" + crane.rightRatio);

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
	}
}

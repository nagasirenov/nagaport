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
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.network.TileEntityCraneSplitter;
import com.hbm.util.i18n.I18nUtil;

import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import com.hbm.render.icon.IIcon;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import com.hbm.lib.ForgeDirection;

import java.util.ArrayList;
import java.util.List;

public class CraneSplitter extends BlockDummyable implements IConveyorBelt, IEnterableBlock, ITooltipProvider, IToolable, ILookOverlay {

	@SideOnly(Side.CLIENT) public IIcon iconTopLeft;
	@SideOnly(Side.CLIENT) public IIcon iconTopRight;
	@SideOnly(Side.CLIENT) public IIcon iconFrontLeft;
	@SideOnly(Side.CLIENT) public IIcon iconFrontRight;
	@SideOnly(Side.CLIENT) public IIcon iconBackLeft;
	@SideOnly(Side.CLIENT) public IIcon iconBackRight;
	@SideOnly(Side.CLIENT) public IIcon iconLeft;
	@SideOnly(Side.CLIENT) public IIcon iconRight;
	@SideOnly(Side.CLIENT) public IIcon iconBelt;
	@SideOnly(Side.CLIENT) public IIcon iconInner;
	@SideOnly(Side.CLIENT) public IIcon iconInnerSide;

	public CraneSplitter() {
		super(Material.iron);
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

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		super.registerBlockIcons(iconRegister);
		this.iconTopLeft = iconRegister.registerIcon(RefStrings.MODID + ":crane_splitter_top_left");
		this.iconTopRight = iconRegister.registerIcon(RefStrings.MODID + ":crane_splitter_top_right");
		this.iconFrontLeft = iconRegister.registerIcon(RefStrings.MODID + ":crane_splitter_front_left");
		this.iconFrontRight = iconRegister.registerIcon(RefStrings.MODID + ":crane_splitter_front_right");
		this.iconBackLeft = iconRegister.registerIcon(RefStrings.MODID + ":crane_splitter_back_left");
		this.iconBackRight = iconRegister.registerIcon(RefStrings.MODID + ":crane_splitter_back_right");
		this.iconLeft = iconRegister.registerIcon(RefStrings.MODID + ":crane_splitter_left");
		this.iconRight = iconRegister.registerIcon(RefStrings.MODID + ":crane_splitter_right");
		this.iconBelt = iconRegister.registerIcon(RefStrings.MODID + ":crane_splitter_belt");
		this.iconInner = iconRegister.registerIcon(RefStrings.MODID + ":crane_splitter_inner");
		this.iconInnerSide = iconRegister.registerIcon(RefStrings.MODID + ":crane_splitter_inner_side");
	}

	public static int renderID = RenderingRegistry.getNextAvailableRenderId();

	@Override
	public int getRenderType() {
		return renderID;
	}

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
		TileEntity tile = world.getTileEntity(x, y, z);
		if(!(tile instanceof TileEntityCraneSplitter)) return;
		TileEntityCraneSplitter splitter = (TileEntityCraneSplitter) tile;
		ForgeDirection rot = ForgeDirection.getOrientation(splitter.getBlockMetadata() - offset).getRotation(ForgeDirection.DOWN);

		ItemStack[] splits = splitter.splitStack(entity.getItemStack());

		spawnMovingItem(world, x, y, z, splits[0]);
		spawnMovingItem(world, x + rot.offsetX, y, z + rot.offsetZ, splits[1]);
	}

	private void spawnMovingItem(World world, int x, int y, int z, ItemStack stack) {
		if(stack == null || stack.stackSize <= 0) return;
		EntityMovingItem moving = new EntityMovingItem(world);
		Vec3d pos = new Vec3d(x + 0.5, y + 0.5, z + 0.5);
		Vec3d snap = this.getClosestSnappingPosition(world, x, y, z, pos);
		moving.setPosition(snap.x, snap.y, snap.z);
		moving.setItemStack(stack);
		world.spawnEntityInWorld(moving);
	}

	@Override
	public boolean canItemStay(World world, int x, int y, int z, Vec3d itemPos) {
		return true;
	}

	@Override
	public Vec3d getTravelLocation(World world, int x, int y, int z, Vec3d itemPos, double speed) {
		ForgeDirection dir = this.getTravelDirection(world, x, y, z, itemPos);
		Vec3d snap = this.getClosestSnappingPosition(world, x, y, z, itemPos);
		itemPos = new Vec3d(MathHelper.clamp_double(itemPos.x, x, x + 1), itemPos.y, MathHelper.clamp_double(itemPos.z, z, z + 1));
		Vec3d dest = new Vec3d(snap.x - dir.offsetX * speed, snap.y - dir.offsetY * speed, snap.z - dir.offsetZ * speed);
		Vec3d motion = new Vec3d((dest.x - itemPos.x), (dest.y - itemPos.y), (dest.z - itemPos.z));
		double len = motion.length();
		Vec3d ret = new Vec3d(itemPos.x + motion.x / len * speed, itemPos.y + motion.y / len * speed, itemPos.z + motion.z / len * speed);
		return ret;
	}

	@Override
	public Vec3d getClosestSnappingPosition(World world, int x, int y, int z, Vec3d itemPos) {
		ForgeDirection dir = this.getTravelDirection(world, x, y, z, itemPos);
		double itemX = MathHelper.clamp_double(itemPos.x, x, x + 1);
		double itemZ = MathHelper.clamp_double(itemPos.z, z, z + 1);
		double posX = x + 0.5;
		double posZ = z + 0.5;
		if(dir.offsetX != 0) posX = itemX;
		if(dir.offsetZ != 0) posZ = itemZ;
		return new Vec3d(posX, y + 0.25, posZ);
	}

	public ForgeDirection getTravelDirection(World world, int x, int y, int z, Vec3d itemPos) {
		int meta = world.getBlockMetadata(x, y, z);
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

		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);
		if(!(te instanceof TileEntityCraneSplitter)) return false;

		TileEntityCraneSplitter crane = (TileEntityCraneSplitter) te;

		// The core of the dummy is always the left hand block
		boolean isLeft = x == pos[0] && y == pos[1] && z == pos[2];
		int adjust = player.isSneaking() ? -1 : 1;

		if(isLeft) {
			crane.leftRatio = (byte)MathHelper.clamp_int(crane.leftRatio + adjust, 1, 16);
		} else {
			crane.rightRatio = (byte)MathHelper.clamp_int(crane.rightRatio + adjust, 1, 16);
		}

		return true;
	}

	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return;

		TileEntity te = world.getTileEntity(pos[0], pos[1], pos[2]);
		if(!(te instanceof TileEntityCraneSplitter)) return;

		TileEntityCraneSplitter crane = (TileEntityCraneSplitter) te;

		List<String> text = new ArrayList<>();
		text.add("Splitter ratio: " + crane.leftRatio + ":" + crane.rightRatio);

		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getUnlocalizedName() + ".name"), 0xffff00, 0x404000, text);
	}
}

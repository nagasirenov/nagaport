package com.hbm.blocks.machine.rbmk;

import com.hbm.blocks.BlockDummyable;
import com.hbm.blocks.ILookOverlay;
import com.hbm.blocks.ModBlocks;
import com.hbm.handler.MultiblockHandlerXR;
import com.hbm.handler.neutron.NeutronNodeWorld;
import com.hbm.handler.neutron.RBMKNeutronHandler.RBMKNeutronNode;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemRBMKLid;
import com.hbm.main.MainRegistry;
import com.hbm.tileentity.machine.rbmk.RBMKDials;
import com.hbm.tileentity.machine.rbmk.TileEntityRBMKBase;

import api.hbm.block.IToolable;
import com.hbm.util.fauxpointtwelve.BlockPos;
import net.minecraftforge.fml.common.network.internal.FMLNetworkHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.properties.IProperty;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.BlockRenderLayer;
import net.minecraftforge.common.property.ExtendedBlockState;
import net.minecraftforge.common.property.IExtendedBlockState;
import net.minecraftforge.common.property.IUnlistedProperty;
import net.minecraft.block.material.Material;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import com.hbm.render.icon.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import com.hbm.lib.ForgeDirection;

public abstract class RBMKBase extends BlockDummyable implements IToolable, ILookOverlay {

	public static boolean dropLids = true;
	public static boolean digamma = false;

	public static final int LID_NONE = 0;
	public static final int LID_STANDARD = 1;
	public static final int LID_GLASS = 2;
	public static int renderLid = LID_NONE;
	public static boolean overrideOnlyRenderSides = false;

	public IIcon coverTextureTop;
	public IIcon coverTextureSide;
	public IIcon glassTextureTop;
	public IIcon glassTextureSide;
	public IIcon textureTop;

	protected RBMKBase() {
		super(Material.IRON);
		this.setHardness(3F);
		this.setResistance(30F);
	}

	/** Native rendering boundary only; core, inventory, lid installation and neutron mechanics remain unchanged. */
	@Override protected BlockStateContainer createBlockState() {
		return new ExtendedBlockState(this, new IProperty<?>[] {METADATA}, new IUnlistedProperty<?>[] {RBMKRenderState.RENDER});
	}
	@Override public EnumBlockRenderType getRenderType(IBlockState state) { return EnumBlockRenderType.MODEL; }
	@Override @SideOnly(Side.CLIENT) public BlockRenderLayer getRenderLayer() { return BlockRenderLayer.CUTOUT_MIPPED; }
	@Override public IBlockState getExtendedState(IBlockState state, IBlockAccess world, net.minecraft.util.math.BlockPos pos) {
		int meta = getMetaFromState(state), lid = LID_NONE;
		if(meta >= 6 && meta < 12 && !hasOwnLid()) {
			int[] core = findCore(world, pos.getX(), pos.getY(), pos.getZ());
			if(core != null) {
				IBlockState coreState = world.getBlockState(new net.minecraft.util.math.BlockPos(core[0], core[1], core[2]));
				lid = metaToLid(coreState.getBlock().getMetaFromState(coreState));
			}
		}
		return ((IExtendedBlockState)state).withProperty(RBMKRenderState.RENDER, new RBMKRenderState.Snapshot(state, world, pos, meta, lid));
	}
	protected IIcon blockIcon;
	protected String getRBMKTexture() {
		RBMKRenderState.Profile profile = RBMKRenderState.profile(this);
		if(profile == null) throw new IllegalStateException("Unspecified RBMK texture owner " + getRegistryName());
		return profile.texture.replace(":blocks/", ":");
	}

	@Override public boolean isOpaqueCube() { return true; }
	@Override public boolean renderAsNormalBlock() { return true; }
	
	public boolean hasOwnLid() {
		return this == ModBlocks.rbmk_control || this == ModBlocks.rbmk_control_auto || this == ModBlocks.rbmk_control_mod ||
				this == ModBlocks.rbmk_control_reasim || this == ModBlocks.rbmk_control_reasim_auto;
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister reg) {
		this.blockIcon = reg.registerIcon(this.getRBMKTexture() + "_side");
		this.textureTop = reg.registerIcon(this.getRBMKTexture() + "_top");
		if(hasOwnLid()) return;
		this.coverTextureTop = reg.registerIcon(this.getRBMKTexture() + "_cover_top");
		this.coverTextureSide = reg.registerIcon(this.getRBMKTexture() + "_cover_side");
		this.glassTextureTop = reg.registerIcon(this.getRBMKTexture() + "_glass_top");
		this.glassTextureSide = reg.registerIcon(this.getRBMKTexture() + "_glass_side");
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int meta) {
		if(!hasOwnLid()) {
			if(renderLid == LID_STANDARD) return side == 0 || side == 1 ? coverTextureTop : coverTextureSide;
			if(renderLid == LID_GLASS) return side == 0 || side == 1 ? glassTextureTop : glassTextureSide;
		}
		return side == 0 || side == 1 ? textureTop : blockIcon;
	}

	@Override
	public int[] getDimensions() {
		return new int[] {3, 0, 0, 0, 0, 0};
	}

	@Override
	public int getOffset() {
		return 0;
	}

	public boolean openInv(World world, int x, int y, int z, EntityPlayer player) {

		if(world.isRemote) return true;

		int[] pos = this.findCore(world, x, y, z);
		if(pos == null) return false;

		TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));
		if(!(te instanceof TileEntityRBMKBase)) return false;

		TileEntityRBMKBase rbmk = (TileEntityRBMKBase) te;

		if(com.hbm.util.NativeHeldItem.getMainHandNullable(player) != null && com.hbm.util.NativeHeldItem.getMainHandNullable(player).getItem() instanceof ItemRBMKLid) {
			if(!rbmk.hasLid()) return false;
		}

		if(!player.isSneaking())
			FMLNetworkHandler.openGui(player, MainRegistry.instance, 0, world, pos[0], pos[1], pos[2]);
		
		return true;
	}

	@Override
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int x, int y, int z) {

		float height = 0.0F;

		int[] pos = this.findCore(world, x, y, z);

		if(pos != null) {
			TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));

			if(te instanceof TileEntityRBMKBase) {

				TileEntityRBMKBase rbmk = (TileEntityRBMKBase) te;

				if(rbmk.hasLid()) {
					height += 0.25F;
				}
			}
		}

		return AxisAlignedBB.getBoundingBox(x + this.minX, y + this.minY, z + this.minZ, x + this.maxX, y + this.maxY + height, z + this.maxZ);
	}

	/*
	 * NORTH: no cover
	 * EAST: concrete cover
	 * SOUTH: lead glass cover
	 * WEST: UNUSED
	 */

	public static final ForgeDirection DIR_NO_LID = ForgeDirection.NORTH;
	public static final ForgeDirection DIR_NORMAL_LID = ForgeDirection.EAST;
	public static final ForgeDirection DIR_GLASS_LID = ForgeDirection.SOUTH;
	
	public static int metaToLid(int meta) {
		if(meta - 10 == DIR_NORMAL_LID.ordinal()) return LID_STANDARD;
		if(meta - 10 == DIR_GLASS_LID.ordinal()) return LID_GLASS;
		return LID_NONE;
	}

	@Override
	public void fillSpace(World world, int x, int y, int z, ForgeDirection dir, int o) {
		MultiblockHandlerXR.fillSpace(world, x + dir.offsetX * o, y + dir.offsetY * o, z + dir.offsetZ * o, getDimensions(world), this, dir);
		this.makeExtra(world, x, y + RBMKDials.getColumnHeight(world), z);
	}

	@Override
	protected ForgeDirection getDirModified(ForgeDirection dir) {
		return DIR_NO_LID;
	}

	public int[] getDimensions(World world) {
		return new int[] {RBMKDials.getColumnHeight(world), 0, 0, 0, 0, 0};
	}

	@Override
	public void breakBlock(World world, int x, int y, int z, Block b, int i) {

		if(!world.isRemote && dropLids) {

			if(i == DIR_NORMAL_LID.ordinal() + offset) {
				world.spawnEntity(new EntityItem(world, x + 0.5, y + 0.5 + RBMKDials.getColumnHeight(world), z + 0.5, new ItemStack(ModItems.rbmk_lid)));
			}
			if(i == DIR_GLASS_LID.ordinal() + offset) {
				world.spawnEntity(new EntityItem(world, x + 0.5, y + 0.5 + RBMKDials.getColumnHeight(world), z + 0.5, new ItemStack(ModItems.rbmk_lid_glass)));
			}
		}

		super.breakBlock(world, x, y, z, b, i);
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {

		if(tool != ToolType.SCREWDRIVER)
			return false;

		int[] pos = this.findCore(world, x, y, z);

		if(pos != null) {
			TileEntity te = world.getTileEntity(new net.minecraft.util.math.BlockPos(pos[0], pos[1], pos[2]));

			if(te instanceof TileEntityRBMKBase) {

				TileEntityRBMKBase rbmk = (TileEntityRBMKBase) te;
				int i = rbmk.getBlockMetadata();

				if(rbmk.hasLid() && rbmk.isLidRemovable()) {

					RBMKNeutronNode node = (RBMKNeutronNode) NeutronNodeWorld.getNode(world, new BlockPos(te));
					if(node != null)
						node.removeLid();

					if(!world.isRemote) {
						if(i == DIR_NORMAL_LID.ordinal() + offset) {
							world.spawnEntity(new EntityItem(world, pos[0] + 0.5, pos[1] + 0.5 + RBMKDials.getColumnHeight(world), pos[2] + 0.5, new ItemStack(ModItems.rbmk_lid)));
						}
						if(i == DIR_GLASS_LID.ordinal() + offset) {
							world.spawnEntity(new EntityItem(world, pos[0] + 0.5, pos[1] + 0.5 + RBMKDials.getColumnHeight(world), pos[2] + 0.5, new ItemStack(ModItems.rbmk_lid_glass)));
						}

						rbmk.explodeOnBroken = false;
						world.setBlockMetadataWithNotify(pos[0], pos[1], pos[2], DIR_NO_LID.ordinal() + offset, 3);
						rbmk.explodeOnBroken = true;
					}

					return true;
				}
			}
		}

		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void printHook(Pre event, World world, int x, int y, int z) {
		TileEntityRBMKBase.diagnosticPrintHook(event, world, x, y, z);
	}

	// Retained NTM family tags for existing descendant hooks, not native engine render IDs.
	public static final int renderIDRods = 1;
	public static final int renderIDPassive = 2;
	public static final int renderIDControl = 3;

	@Override
	public int transformMeta(int meta, int coordBaseMode) {
		return meta;
	}
}

package com.hbm.blocks.machine;

import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.block.state.IBlockState;
import java.util.ArrayList;
import java.util.List;

import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.material.Mats.MaterialStack;
import com.hbm.items.ModItems;
import com.hbm.items.machine.ItemScraps;
import com.hbm.tileentity.machine.TileEntityFoundryOutlet;
import com.hbm.util.i18n.I18nUtil;

import api.hbm.block.ICrucibleAcceptor;
import api.hbm.block.IToolable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import com.hbm.lib.ForgeDirection;

public class FoundryOutlet extends FoundryBlock implements ICrucibleAcceptor, ILookOverlay, IToolable {


	public FoundryOutlet() {
		super();
	}
	



	
	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase player, ItemStack itemStack) {
		int x=pos.getX(), y=pos.getY(), z=pos.getZ();
		int i = MathHelper.floor(player.rotationYaw * 4.0F / 360.0F + 0.5D) & 3;

		if(i == 0) world.setBlockState(pos, getStateFromMeta(2), 2);
		if(i == 1) world.setBlockState(pos, getStateFromMeta(5), 2);
		if(i == 2) world.setBlockState(pos, getStateFromMeta(3), 2);
		if(i == 3) world.setBlockState(pos, getStateFromMeta(4), 2);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		return new TileEntityFoundryOutlet();
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBounding, List<AxisAlignedBB> list, Entity entity, boolean actualState) {
		int x=pos.getX(), y=pos.getY(), z=pos.getZ();
		
		AxisAlignedBB aabb = null;
		int meta = getMetaFromState(world.getBlockState(new BlockPos(x, y, z)));

		if(meta == 4) aabb = new AxisAlignedBB(x + 0.625D, y, z + 0.3125D, x + 1D, y + 0.5D, z + 0.6875D);
		if(meta == 5) aabb = new AxisAlignedBB(x + 0D, y, z + 0.3125D, x + 0.375D, y + 0.5D, z + 0.6875D);
		if(meta == 2) aabb = new AxisAlignedBB(x + 0.3125D, y, z + 0.625D, x + 0.6875D, y + 0.5D, z + 1D);
		if(meta == 3) aabb = new AxisAlignedBB(x + 0.3125D, y, z + 0D, x + 0.6875D, y + 0.5D, z + 0.375D);
		
		if(aabb != null && entityBounding.intersects(aabb)) {
			list.add(aabb);
		}
	}






	
	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		int x=pos.getX(), y=pos.getY(), z=pos.getZ();
		if(world.isRemote) {
			return true;
		}
		
		if(!player.isSneaking()) {
			TileEntityFoundryOutlet tile = (TileEntityFoundryOutlet) world.getTileEntity(new BlockPos(x, y, z));
			
			if(!player.getHeldItem(hand).isEmpty() && player.getHeldItem(hand).getItem() == ModItems.scraps) {
				MaterialStack mat = ItemScraps.getMats(player.getHeldItem(hand));
				if(mat != null) {
					tile.filter = mat.material;
				}
			} else {
				tile.invertRedstone = !tile.invertRedstone;
			}
			tile.markDirty();
			notifyChange(world, new BlockPos(x, y, z));
		}
		
		return true;
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		
		if(tool == ToolType.SCREWDRIVER) {
			if(world.isRemote) return true;

			TileEntityFoundryOutlet tile = (TileEntityFoundryOutlet) world.getTileEntity(new BlockPos(x, y, z));
			tile.filter = null;
			tile.invertFilter = false;
			tile.markDirty();
			notifyChange(world, new BlockPos(x, y, z));
		}
		
		if(tool == ToolType.HAND_DRILL) {
			if(world.isRemote) return true;

			TileEntityFoundryOutlet tile = (TileEntityFoundryOutlet) world.getTileEntity(new BlockPos(x, y, z));
			tile.invertFilter = !tile.invertFilter;
			tile.markDirty();
			notifyChange(world, new BlockPos(x, y, z));
		}
		
		return false;
	}

	@Override public boolean canAcceptPartialPour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) { return false; }
	@Override public MaterialStack pour(World world, int x, int y, int z, double dX, double dY, double dZ, ForgeDirection side, MaterialStack stack) { return stack; }

	@Override
	public boolean canAcceptPartialFlow(World world, int x, int y, int z, ForgeDirection side, MaterialStack stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(new BlockPos(x, y, z))).canAcceptPartialFlow(world, x, y, z, side, stack);
	}
	
	@Override
	public MaterialStack flow(World world, int x, int y, int z, ForgeDirection side, MaterialStack stack) {
		return ((ICrucibleAcceptor) world.getTileEntity(new BlockPos(x, y, z))).flow(world, x, y, z, side, stack);
	}



	

	


	@Override
	public void printHook(Pre event, World world, int x, int y, int z) {
		TileEntityFoundryOutlet outlet = (TileEntityFoundryOutlet) world.getTileEntity(new BlockPos(x, y, z));
		List<String> text = new ArrayList();
		
		if(outlet.filter != null) {
			text.add(TextFormatting.YELLOW + I18nUtil.resolveKey("foundry.filter", outlet.filter.names[0]));
		}
		if(outlet.invertFilter) {
			text.add(TextFormatting.YELLOW + I18nUtil.resolveKey("foundry.invertFilter"));
		}
		if(outlet.invertRedstone) {
			text.add(TextFormatting.DARK_RED + I18nUtil.resolveKey("foundry.inverted"));
		}
		
		ILookOverlay.printGeneric(event, I18nUtil.resolveKey(this.getTranslationKey() + ".name"), 0xFF4000, 0x401000, text);
	}

	@Override public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
        switch(getMetaFromState(state)) {
        case 4:return new AxisAlignedBB(.625,0,.3125,1,.5,.6875);
        case 5:return new AxisAlignedBB(0,0,.3125,.375,.5,.6875);
        case 2:return new AxisAlignedBB(.3125,0,.625,.6875,.5,1);
        case 3:return new AxisAlignedBB(.3125,0,0,.6875,.5,.375);
        default:return FULL_BLOCK_AABB;
        }
    }
    @Override public AxisAlignedBB getCollisionBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) { return getBoundingBox(state,world,pos); }
}

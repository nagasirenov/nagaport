package com.hbm.blocks.machine.pile;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.pile.TileEntityPileFuel;

import api.hbm.block.IBlowable;
import api.hbm.block.IToolable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import com.hbm.render.icon.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockGraphiteFuel extends BlockGraphiteDrilledTE implements IToolable, IBlowable {

	@Override
	public TileEntity createNewTileEntity(World world, int meta) {
		TileEntityPileFuel pile = new TileEntityPileFuel();
		if((meta & 8) != 0)
			pile.progress = pile.maxProgress - 1000; // pu239 rods cringe :(
		
		return pile;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		super.registerBlockIcons(iconRegister);
		this.blockIconAluminum = iconRegister.registerIcon(RefStrings.MODID + ":block_graphite_fuel_aluminum");
	}
	
	@Override
	public boolean hasComparatorInputOverride() {
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
		TileEntityPileFuel pile = (TileEntityPileFuel)world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		return MathHelper.clamp((pile.progress * 15) / (pile.maxProgress - 1000), 0, 15);
	}
	
	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		
		if(!world.isRemote) {
			
			int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
			
			if(tool == ToolType.SCREWDRIVER) {
				
				int cfg = meta & 3;
				
				if(side == cfg * 2 || side == cfg * 2 + 1) {
					world.setBlock(x, y, z, ModBlocks.block_graphite_drilled, meta & 7, 3);
					this.ejectItem(world, x, y, z, ForgeDirection.getOrientation(side), new ItemStack(getInsertedItem(meta)));
				}
			}
			
			if(tool == ToolType.HAND_DRILL) {
				TileEntityPileFuel pile = (TileEntityPileFuel) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
				player.sendMessage(new TextComponentString("CP1 FUEL ASSEMBLY " + x + " " + y + " " + z).setStyle(new Style().setColor(TextFormatting.GOLD)));
				player.sendMessage(new TextComponentString("HEAT: " + pile.heat + "/" + pile.maxHeat).setStyle(new Style().setColor(TextFormatting.YELLOW)));
				player.sendMessage(new TextComponentString("DEPLETION: " + pile.progress + "/" + pile.maxProgress).setStyle(new Style().setColor(TextFormatting.YELLOW)));
				player.sendMessage(new TextComponentString("FLUX: " + pile.lastNeutrons).setStyle(new Style().setColor(TextFormatting.YELLOW)));
				if((meta & 8) == 8)
					player.sendMessage(new TextComponentString("PU-239 RICH").setStyle(new Style().setColor(TextFormatting.DARK_GREEN)));
			}
		}
		
		return true;
	}
	
	@Override
	protected Item getInsertedItem(int meta) {
		return (meta & 8) == 8 ? ModItems.pile_rod_pu239 : ModItems.pile_rod_uranium;
	}
	
	@Override
	public void applyFan(World world, int x, int y, int z, ForgeDirection dir, int dist) {
		TileEntityPileFuel pile = (TileEntityPileFuel) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
		pile.heat -= pile.heat * 0.025;
	}
}

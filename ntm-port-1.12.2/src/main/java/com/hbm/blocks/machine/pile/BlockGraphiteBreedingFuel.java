package com.hbm.blocks.machine.pile;

import com.hbm.blocks.ModBlocks;
import com.hbm.items.ModItems;
import com.hbm.lib.RefStrings;
import com.hbm.tileentity.machine.pile.TileEntityPileBreedingFuel;

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
import net.minecraft.world.World;
import com.hbm.lib.ForgeDirection;

public class BlockGraphiteBreedingFuel extends BlockGraphiteDrilledTE implements IToolable {
	
	@Override
	public TileEntity createNewTileEntity(World world, int mets) {
		return new TileEntityPileBreedingFuel();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister iconRegister) {
		super.registerBlockIcons(iconRegister);
		this.blockIconAluminum = iconRegister.registerIcon(RefStrings.MODID + ":block_graphite_lithium_aluminum");
	}

	@Override
	public boolean onScrew(World world, EntityPlayer player, int x, int y, int z, int side, float fX, float fY, float fZ, ToolType tool) {
		
		if(!world.isRemote) {
			
			if(tool == ToolType.SCREWDRIVER) {
	
				int meta = com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(world, x, y, z) );
				int cfg = meta & 3;
				
				if(side == cfg * 2 || side == cfg * 2 + 1) {
					world.setBlock(x, y, z, ModBlocks.block_graphite_drilled, meta, 3);
					this.ejectItem(world, x, y, z, ForgeDirection.getOrientation(side), new ItemStack(ModItems.pile_rod_lithium));
				}
			}
			
			if(tool == ToolType.HAND_DRILL) {
				TileEntityPileBreedingFuel pile = (TileEntityPileBreedingFuel) world.getTileEntity(new net.minecraft.util.math.BlockPos(x, y, z));
				player.sendMessage(new TextComponentString("CP1 FUEL ASSEMBLY " + x + " " + y + " " + z).setStyle(new Style().setColor(TextFormatting.GOLD)));
				player.sendMessage(new TextComponentString("DEPLETION: " + pile.progress + "/" + pile.maxProgress).setStyle(new Style().setColor(TextFormatting.YELLOW)));
				player.sendMessage(new TextComponentString("FLUX: " + pile.lastNeutrons).setStyle(new Style().setColor(TextFormatting.YELLOW)));
			}
		}
		
		return true;
	}
	
	@Override
	protected Item getInsertedItem() {
		return ModItems.pile_rod_lithium;
	}
	
}

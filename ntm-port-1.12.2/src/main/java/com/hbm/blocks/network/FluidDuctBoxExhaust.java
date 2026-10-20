package com.hbm.blocks.network;

import com.hbm.blocks.ILookOverlay;
import com.hbm.inventory.fluid.Fluids;
import com.hbm.lib.ForgeDirection;
import com.hbm.lib.Library;
import com.hbm.tileentity.network.TileEntityPipeExhaust;
import com.hbm.util.i18n.I18nUtil;
import net.minecraft.block.material.Material;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.NonNullList;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Pre;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

public class FluidDuctBoxExhaust extends FluidDuctBox {

    public FluidDuctBoxExhaust(Material material) {
        super(material);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int metadata) {
        return new TileEntityPipeExhaust();
    }

    @Override
    public boolean canConnectTo(IBlockAccess world, int x, int y, int z, ForgeDirection direction, TileEntity tile) {
        return Library.canConnectFluid(world, x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ, direction, Fluids.SMOKE) ||
                Library.canConnectFluid(world, x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ, direction, Fluids.SMOKE_LEADED) ||
                Library.canConnectFluid(world, x + direction.offsetX, y + direction.offsetY, z + direction.offsetZ, direction, Fluids.SMOKE_POISON);
    }

    @Override
    public int getSubCount() {
        return 1;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubBlocks(CreativeTabs tab, NonNullList<ItemStack> items) {
        for(int metadata = 0; metadata < 15; metadata += 3) {
            items.add(new ItemStack(this, 1, metadata));
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void printHook(Pre event, World world, int x, int y, int z) {
        List<String> text = new ArrayList<>();
        text.add(Fluids.SMOKE.getLocalizedName());
        text.add(Fluids.SMOKE_LEADED.getLocalizedName());
        text.add(Fluids.SMOKE_POISON.getLocalizedName());
        ILookOverlay.printGeneric(event, I18nUtil.resolveKey(getTranslationKey() + ".name"), 0xffff00, 0x404000, text);
    }
}

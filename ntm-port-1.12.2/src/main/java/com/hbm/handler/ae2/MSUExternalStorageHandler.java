package com.hbm.handler.ae2;

import com.hbm.tileentity.machine.storage.TileEntityMassStorage;
import com.hbm.util.ItemStackUtil;

import appeng.api.networking.security.IActionSource;
import appeng.api.AEApi;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.me.storage.MEMonitorIInventory;
import appeng.util.inv.IMEAdaptor;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class MSUExternalStorageHandler {

    public MSUExternalStorageHandler() {}

    public boolean canHandle(TileEntity te, EnumFacing d, IStorageChannel<?> channel, IActionSource mySrc) {
        return channel == AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class) && te instanceof TileEntityMassStorage;
    }

    public IMEMonitor<IAEItemStack> getInventory(TileEntity te, EnumFacing d, IStorageChannel<?> channel, IActionSource src) {
        if (!canHandle(te, d, channel, src))
            return null;
        
        // Note: apparently I need this, though I'm not sure why. Storage drawers does it.
        // Here's a relevant discussion, if anyone wants to dive into that rabbit hole:
        // https://github.com/AppliedEnergistics/Applied-Energistics-2/issues/418
        return new MEMonitorIInventory(new IMEAdaptor(new MassStorageMEInventory((TileEntityMassStorage)te), src)) {
            @Override
            public boolean isPrioritized(IAEItemStack stack) {
                ItemStack type = ((TileEntityMassStorage)te).getType();

                return type != null && ItemStackUtil.areStacksCompatible(stack.createItemStack(), type);
            }
        };
    }
    
}

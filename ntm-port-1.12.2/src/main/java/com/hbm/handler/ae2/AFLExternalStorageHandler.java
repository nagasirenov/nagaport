package com.hbm.handler.ae2;

import com.hbm.tileentity.machine.TileEntityMachineArcFurnaceLarge;
import com.hbm.tileentity.TileEntityProxyCombo;

import appeng.api.networking.security.IActionSource;
import appeng.api.AEApi;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.me.storage.MEMonitorIInventory;
import appeng.util.inv.IMEAdaptor;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

public class AFLExternalStorageHandler {

    public AFLExternalStorageHandler() {}

    public boolean canHandle(TileEntity te, EnumFacing d, IStorageChannel<?> channel, IActionSource mySrc) {
		boolean coreProxy = te instanceof TileEntityProxyCombo && ((TileEntityProxyCombo) te).getTile() instanceof TileEntityMachineArcFurnaceLarge;
        return channel == AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class) && (te instanceof TileEntityMachineArcFurnaceLarge || coreProxy);
    }

    public IMEMonitor<IAEItemStack> getInventory(TileEntity te, EnumFacing d, IStorageChannel<?> channel, IActionSource src) {
        if (!canHandle(te, d, channel, src)) return null;
		if (te instanceof TileEntityProxyCombo) return new MEMonitorIInventory(new IMEAdaptor(new ArcFurnaceLargeMEInventory((TileEntityMachineArcFurnaceLarge) ((TileEntityProxyCombo)te).getTile()), src)) {};
		return new MEMonitorIInventory(new IMEAdaptor(new ArcFurnaceLargeMEInventory((TileEntityMachineArcFurnaceLarge) te), src)) {};
    }

}

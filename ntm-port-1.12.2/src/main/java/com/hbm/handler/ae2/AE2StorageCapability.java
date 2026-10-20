package com.hbm.handler.ae2;

import javax.annotation.Nullable;

import com.hbm.tileentity.TileEntityProxyCombo;
import com.hbm.tileentity.machine.TileEntityMachineArcFurnaceLarge;
import com.hbm.tileentity.machine.storage.TileEntityMassStorage;

import appeng.api.AEApi;
import appeng.capabilities.Capabilities;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.IStorageMonitorable;
import appeng.api.storage.IStorageMonitorableAccessor;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/**
 * Native rv6 storage-bus entry point. This class is loaded only by the guarded
 * AE2 initializer, so normal tiles never acquire hard AE2 field/interface types.
 */
public final class AE2StorageCapability implements ICapabilityProvider {
    private final TileEntity tile;
    private TileEntity resolvedCore;
    private IStorageMonitorableAccessor accessor;

    public AE2StorageCapability(TileEntity tile) {
        this.tile = tile;
    }

    @Nullable
    private IStorageMonitorableAccessor resolveAccessor() {
        TileEntity core = tile;
        if (core instanceof TileEntityProxyCombo) {
            core = ((TileEntityProxyCombo) core).getTile();
            // The old external handler accepted AFL proxies, not arbitrary MSU proxies.
            if (!(core instanceof TileEntityMachineArcFurnaceLarge)) core = null;
        } else if (!(core instanceof TileEntityMassStorage) && !(core instanceof TileEntityMachineArcFurnaceLarge)) {
            core = null;
        }
        if (core != null && core.isInvalid()) core = null;
        if (core != resolvedCore) {
            resolvedCore = core;
            accessor = core == null ? null : new Accessor(core);
        }
        return accessor;
    }

    @Override
    public boolean hasCapability(Capability<?> capability, @Nullable EnumFacing facing) {
        return Capabilities.STORAGE_MONITORABLE_ACCESSOR != null && capability == Capabilities.STORAGE_MONITORABLE_ACCESSOR && resolveAccessor() != null;
    }

    @Override
    @Nullable
    public <T> T getCapability(Capability<T> capability, @Nullable EnumFacing facing) {
        if (Capabilities.STORAGE_MONITORABLE_ACCESSOR == null || capability != Capabilities.STORAGE_MONITORABLE_ACCESSOR) return null;
        return Capabilities.STORAGE_MONITORABLE_ACCESSOR.cast(resolveAccessor());
    }

    private static final class Accessor implements IStorageMonitorableAccessor {
        private final TileEntity core;

        private Accessor(TileEntity core) {
            this.core = core;
        }

        @Override
        public IStorageMonitorable getInventory(IActionSource source) {
            final IItemStorageChannel items = AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
            // A separate monitor per bus/source retains polling and change notifications.
            final IMEMonitor<IAEItemStack> monitor = core instanceof TileEntityMassStorage
                    ? new MSUExternalStorageHandler().getInventory(core, null, items, source)
                    : new AFLExternalStorageHandler().getInventory(core, null, items, source);
            return new IStorageMonitorable() {
                @Override
                @SuppressWarnings("unchecked")
                public <T extends IAEStack<T>> IMEMonitor<T> getInventory(IStorageChannel<T> channel) {
                    return channel == items ? (IMEMonitor<T>) (IMEMonitor<?>) monitor : null;
                }
            };
        }
    }

    public static final class AttachHandler {
        @SubscribeEvent
        public void attach(AttachCapabilitiesEvent<TileEntity> event) {
            TileEntity tile = event.getObject();
            // Proxies are attached before their core/world is ready; resolve lazily on access.
            if (tile instanceof TileEntityMassStorage || tile instanceof TileEntityMachineArcFurnaceLarge
                    || tile instanceof TileEntityProxyCombo) {
                event.addCapability(new ResourceLocation("hbm", "ae2_storage"), new AE2StorageCapability(tile));
            }
        }
    }
}

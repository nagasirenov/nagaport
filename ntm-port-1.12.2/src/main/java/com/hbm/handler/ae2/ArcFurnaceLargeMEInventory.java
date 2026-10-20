package com.hbm.handler.ae2;

import com.hbm.tileentity.machine.TileEntityMachineArcFurnaceLarge;

import net.minecraftforge.fml.common.Optional;

import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.IStorageChannel;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import net.minecraft.item.ItemStack;

@Optional.InterfaceList({ @Optional.Interface(iface = "appeng.api.storage.IMEInventory", modid = "appliedenergistics2") })
public class ArcFurnaceLargeMEInventory implements IMEInventory<IAEItemStack> {

	private TileEntityMachineArcFurnaceLarge afl;

	public ArcFurnaceLargeMEInventory(TileEntityMachineArcFurnaceLarge afl) {
		this.afl = afl;
	}

	@Override
	public IAEItemStack injectItems(IAEItemStack input, Actionable type, IActionSource src) {
		ItemStack is = input.createItemStack();
		is = afl.distributeInput(is, type == Actionable.MODULATE);

		if(is == null) return null;
		return getChannel().createStack(is);
	}

	@Override
	public IAEItemStack extractItems(IAEItemStack request, Actionable mode, IActionSource src) {
		ItemStack is = request.createItemStack();
		is = afl.collectRequested(is, mode == Actionable.MODULATE);

		if(is == null) return null;
		return getChannel().createStack(is);
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems(IItemList<IAEItemStack> out) {
		ItemStack is;
		for(int i = 0; i < 25; i++) {
			is = afl.getAvailableItemFromSlot(i);
			if(is != null)
				out.add(getChannel().createStack(is));
		}

		return out;
	}

	@Override
	public IStorageChannel<IAEItemStack> getChannel() {
		return AEApi.instance().storage().getStorageChannel(IItemStorageChannel.class);
	}
}

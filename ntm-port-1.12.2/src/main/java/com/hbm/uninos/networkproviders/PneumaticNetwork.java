package com.hbm.uninos.networkproviders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;

import com.hbm.tileentity.machine.TileEntityMachineAutocrafter;
import com.hbm.tileentity.network.TileEntityPneumoTube;
import com.hbm.uninos.NodeNet;
import com.hbm.util.BobMathUtil;
import com.hbm.util.ItemStackUtil;
import com.hbm.util.Tuple.Triplet;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.MathHelper;
import com.hbm.lib.ForgeDirection;

public class PneumaticNetwork extends NodeNet {

	public static final byte SEND_FIRST = 0;
	public static final byte SEND_LAST = 1;
	public static final byte SEND_RANDOM = 2;
	public static final byte RECEIVE_ROBIN = 0;
	public static final byte RECEIVE_RANDOM = 1;
	
	public Random rand = new Random();

	protected static final int timeout = 1_000;
	public static final int ITEMS_PER_TRANSFER = 64;

	// while the system has parts that expects IInventires to be TileEntities to work properly (mostly range checks),
	// it can actually handle non-TileEntities just fine.
	public HashMap<IInventory, Triplet<ForgeDirection, Long, TileEntityPneumoTube>> receivers = new HashMap();

	public void addReceiver(IInventory inventory, ForgeDirection pipeDir, TileEntityPneumoTube endpoint) {
		receivers.put(inventory, new Triplet(pipeDir, System.currentTimeMillis(), endpoint));
	}

	@Override public void update() {

		// weeds out invalid targets
		// technically not necessary since that step is taken during the send operation,
		// but we still want to reap garbage data that would otherwise accumulate
		long timestamp = System.currentTimeMillis();
		receivers.entrySet().removeIf(x -> { return (timestamp - x.getValue().getY() > timeout) || NodeNet.isBadLink(x.getKey()); });
	}

	public boolean send(IInventory source, TileEntityPneumoTube tube, ForgeDirection accessDir, int sendOrder, int receiveOrder, int maxRange, int nextReceiver) {

		// turns out there may be a short time window where the cleanup hasn't happened yet, but chunkloading has already caused tiles to go invalid
		// so we just run it again here, just to be sure.
		long timestamp = System.currentTimeMillis();
		receivers.entrySet().removeIf(x -> { return (timestamp - x.getValue().getY() > timeout) || NodeNet.isBadLink(x.getKey()); });
		
		if(receivers.isEmpty()) return false;

		int sourceSide = accessDir.ordinal();
		int[] sourceSlotAccess = getSlotAccess(source, sourceSide);

		if(sendOrder == SEND_LAST) BobMathUtil.reverseIntArray(sourceSlotAccess);
		if(sendOrder == SEND_RANDOM) BobMathUtil.shuffleIntArray(sourceSlotAccess);

		ISidedInventory sidedSource = source instanceof ISidedInventory ? (ISidedInventory) source : null;
		boolean hasItem = false;

		for(int i : sourceSlotAccess) {
			ItemStack stack = source.getStackInSlot(i);
			if(stack != null && !stack.isEmpty()) {
				if(sidedSource != null && !sidedSource.canExtractItem(i, stack, com.hbm.lib.ForgeDirection.getOrientation(sourceSide).toEnumFacing())) continue;
				boolean match = tube.matchesFilter(stack);
				if((match && !tube.whitelist) || (!match && tube.whitelist)) continue;
				hasItem = true;
				break;
			}
		}

		// return early if there arent any items in the source inventory, saves on some cpu usage for idle networks
		if(!hasItem) return false;

		// for round robin, receivers are ordered by proximity to the source
		ReceiverComparator comparator = new ReceiverComparator(tube);
		List<Entry<IInventory, Triplet<ForgeDirection, Long, TileEntityPneumoTube>>> receiverList = new ArrayList(receivers.size());
		receiverList.addAll(receivers.entrySet());

		if(receiveOrder == RECEIVE_ROBIN) receiverList.sort(comparator);
		if(receiveOrder == RECEIVE_RANDOM) Collections.shuffle(receiverList);

		TileEntity tile1 = source instanceof TileEntity ? (TileEntity) source : null;

		int attempts = 0;
		int maxAttempts = receiverList.size();

		// try all receivers for both modes, in an attempts based system.
		// instead of bailing out of trying after the first failure (which means you have to wait 0.25 seconds), we just try the next one.
		while(attempts < maxAttempts) {
			int index = (receiveOrder == RECEIVE_ROBIN) ? (nextReceiver + attempts) % receiverList.size() : attempts;

			Entry<IInventory, Triplet<ForgeDirection, Long, TileEntityPneumoTube>> candidate = receiverList.get(index);

			if(NodeNet.isBadLink(candidate.getKey())) {
				receivers.remove(candidate.getKey());
				attempts++;
				continue;
			}

			IInventory dest = candidate.getKey();
			TileEntityPneumoTube endpointTile = candidate.getValue().getZ();
			TileEntity tile2 = dest instanceof TileEntity ? (TileEntity) dest : null;

			// range check for our compression level, skip if either source or dest aren't tile entities
			if(tile1 != null && tile2 != null) {
				int sq = (tile1.getPos().getX() - tile2.getPos().getX()) * (tile1.getPos().getX() - tile2.getPos().getX()) + (tile1.getPos().getY() - tile2.getPos().getY()) * (tile1.getPos().getY() - tile2.getPos().getY()) + (tile1.getPos().getZ() - tile2.getPos().getZ()) * (tile1.getPos().getZ() - tile2.getPos().getZ());
				if(sq > maxRange * maxRange) {
					attempts++;
					continue;
				}
			}

			ISidedInventory sidedDest = dest instanceof ISidedInventory ? (ISidedInventory) dest : null;
			int destSide = candidate.getValue().getX().getOpposite().ordinal();
			int[] destSlotAccess = getSlotAccess(dest, destSide);
			int itemsLeftToSend = ITEMS_PER_TRANSFER; // not actually individual items, but rather the total "mass", based on max stack size
			int itemHardCap = dest instanceof TileEntityMachineAutocrafter ? 1 : ITEMS_PER_TRANSFER;
			boolean didSomething = false;

			for(int sourceIndex : sourceSlotAccess) {
				ItemStack sourceStack = source.getStackInSlot(sourceIndex);
				if(sourceStack == null || sourceStack.isEmpty()) continue;
				if(sidedSource != null && !sidedSource.canExtractItem(sourceIndex, sourceStack, com.hbm.lib.ForgeDirection.getOrientation(sourceSide).toEnumFacing())) continue;
				// filter of the source
				boolean match = tube.matchesFilter(sourceStack);
				if((match && !tube.whitelist) || (!match && tube.whitelist)) continue;
				// filter of the receiver, only if the sender and receiver aren't the same block
				if(endpointTile != null && endpointTile != tube) {
					match = endpointTile.matchesFilter(sourceStack);
					if((match && !endpointTile.whitelist) || (!match && endpointTile.whitelist)) continue;
				}
				// the "mass" of an item. something that only stacks to 4 has a "mass" of 16. max transfer mass is 64, i.e. one standard stack, or one single unstackable item
				int proportionalValue = MathHelper.clamp(64 / sourceStack.getMaxStackSize(), 1, 64);

				// try to fill partial stacks first
				for(int destIndex : destSlotAccess) {
					ItemStack destStack = dest.getStackInSlot(destIndex);
					if(destStack == null || destStack.isEmpty()) continue;
					if(!ItemStackUtil.areStacksCompatible(sourceStack, destStack)) continue;
					int toMove = BobMathUtil.min(sourceStack.getCount(), destStack.getMaxStackSize() - destStack.getCount(), dest.getInventoryStackLimit() - destStack.getCount(), itemsLeftToSend / proportionalValue, itemHardCap);
					if(toMove <= 0) continue;

					ItemStack checkStack = destStack.copy();
					checkStack.grow(toMove);
					if(!dest.isItemValidForSlot(destIndex, checkStack)) continue;
					if(sidedDest != null && !sidedDest.canInsertItem(destIndex, checkStack, com.hbm.lib.ForgeDirection.getOrientation(destSide).toEnumFacing())) continue;

					sourceStack.shrink(toMove);
					if(sourceStack.getCount() <= 0) source.setInventorySlotContents(sourceIndex, ItemStack.EMPTY);
					destStack.grow(toMove);
					itemsLeftToSend -= toMove * proportionalValue;
					didSomething = true;
					if(itemsLeftToSend <= 0) break;
				}

				// if there's stuff left to send, occupy empty slots
				if(itemsLeftToSend > 0 && sourceStack.getCount() > 0) for(int destIndex : destSlotAccess) {
					ItemStack destStack = dest.getStackInSlot(destIndex);
					if(destStack != null && !destStack.isEmpty()) continue;
					int toMove = BobMathUtil.min(sourceStack.getCount(), dest.getInventoryStackLimit(), itemsLeftToSend / proportionalValue, itemHardCap);
					if(toMove <= 0) continue;

					ItemStack checkStack = sourceStack.copy();
					checkStack.setCount(toMove);
					if(!dest.isItemValidForSlot(destIndex, checkStack)) continue;
					if(sidedDest != null && !sidedDest.canInsertItem(destIndex, checkStack, com.hbm.lib.ForgeDirection.getOrientation(destSide).toEnumFacing())) continue;

					ItemStack newStack = sourceStack.copy();
					newStack.setCount(toMove);
					sourceStack.shrink(toMove);
					if(sourceStack.getCount() <= 0) source.setInventorySlotContents(sourceIndex, ItemStack.EMPTY);
					dest.setInventorySlotContents(destIndex, newStack);
					itemsLeftToSend -= toMove * proportionalValue;
					didSomething = true;
					if(itemsLeftToSend <= 0) break;
				}

				if(itemsLeftToSend <= 0) break;
			}

			// make sure both parties are saved to disk and increment the counter for round robin
			if(didSomething) {
				source.markDirty();
				dest.markDirty();
				return true;
			}

			attempts++;
		}
		return false;
	}

	/** Returns an array of accessible slots from the given side of an IInventory. If it's an ISidedInventory, uses the sided restrictions instead. */
	public static int[] getSlotAccess(IInventory inventory, int dir) {

		if(inventory instanceof ISidedInventory) {
			int[] slotAccess = ((ISidedInventory) inventory).getSlotsForFace(com.hbm.lib.ForgeDirection.getOrientation(dir).toEnumFacing());
			return Arrays.copyOf(slotAccess, slotAccess.length); //we mess with the order, so better not use the original array
		} else {
			int[] slotAccess = new int[inventory.getSizeInventory()];
			for(int i = 0; i < inventory.getSizeInventory(); i++) slotAccess[i] = i;
			return slotAccess;
		}
	}

	/** Compares IInventory by distance, going off the assumption that they are TileEntities. Uses positional data for tie-breaking if the distance is the same. */
	public static class ReceiverComparator implements Comparator<Entry<IInventory, Triplet<ForgeDirection, Long, TileEntityPneumoTube>>> {

		private TileEntityPneumoTube origin;

		public ReceiverComparator(TileEntityPneumoTube origin) {
			this.origin = origin;
		}

		@Override
		public int compare(Entry<IInventory, Triplet<ForgeDirection, Long, TileEntityPneumoTube>> o1, Entry<IInventory, Triplet<ForgeDirection, Long, TileEntityPneumoTube>> o2) {

			TileEntity tile1 = o1.getKey() instanceof TileEntity ? (TileEntity) o1.getKey() : null;
			TileEntity tile2 = o2.getKey() instanceof TileEntity ? (TileEntity) o2.getKey() : null;

			// prioritize actual TileEntities
			if(tile1 == null && tile2 != null) return 1;
			if(tile1 != null && tile2 == null) return -1;
			if(tile1 == null && tile2 == null) return 0;

			// calculate distances from origin
			int dist1 = (tile1.getPos().getX() - origin.getPos().getX()) * (tile1.getPos().getX() - origin.getPos().getX()) + (tile1.getPos().getY() - origin.getPos().getY()) * (tile1.getPos().getY() - origin.getPos().getY()) + (tile1.getPos().getZ() - origin.getPos().getZ()) * (tile1.getPos().getZ() - origin.getPos().getZ());
			int dist2 = (tile2.getPos().getX() - origin.getPos().getX()) * (tile2.getPos().getX() - origin.getPos().getX()) + (tile2.getPos().getY() - origin.getPos().getY()) * (tile2.getPos().getY() - origin.getPos().getY()) + (tile2.getPos().getZ() - origin.getPos().getZ()) * (tile2.getPos().getZ() - origin.getPos().getZ());

			// tier-breaker: use hash value instead
			if(dist1 == dist2) {
				return TileEntityPneumoTube.getIdentifier(tile1.getPos().getX(), tile1.getPos().getY(), tile1.getPos().getZ()) - TileEntityPneumoTube.getIdentifier(tile2.getPos().getX(), tile2.getPos().getY(), tile2.getPos().getZ());
			}
			
			// no tie? return difference of the distances
			return dist1 - dist2;
		}
	}
}

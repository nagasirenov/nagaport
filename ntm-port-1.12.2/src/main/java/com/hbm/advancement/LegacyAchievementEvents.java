package com.hbm.advancement;

import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;

/** Restores only the vanished vanilla event predicates used by NTM.
 * The inventory-key C2S path separately awards OPEN_INVENTORY to its authenticated sender.
 * These are post-pickup/crafting/player-extraction events, never inventory possession checks.
 */
public final class LegacyAchievementEvents {
    @SubscribeEvent
    public void pickedUp(PlayerEvent.ItemPickupEvent event) {
        // The entity's stack may already be empty. Forge captured the actual picked-up stack.
        Item item = event.getStack().getItem();
        if (item == Item.getItemFromBlock(Blocks.LOG) || item == Item.getItemFromBlock(Blocks.LOG2)) {
            NtmAdvancements.award(event.player, NtmAdvancements.MINE_WOOD);
        }
    }

    @SubscribeEvent
    public void crafted(PlayerEvent.ItemCraftedEvent event) {
        Item item = outputItem(event.crafting);
        boolean shiftTransfer = LegacyCraftingCallbacks.isVanillaShiftTransfer();
        if (item == Item.getItemFromBlock(Blocks.CRAFTING_TABLE)) {
            NtmAdvancements.award(event.player, NtmAdvancements.BUILD_WORKBENCH);
            if (shiftTransfer) NtmAdvancements.award(event.player, NtmAdvancements.BUILD_WORKBENCH);
        }
        if (item instanceof ItemPickaxe) {
            NtmAdvancements.award(event.player, NtmAdvancements.BUILD_PICKAXE);
            if (shiftTransfer) NtmAdvancements.award(event.player, NtmAdvancements.BUILD_PICKAXE);
        }
        if (item == Item.getItemFromBlock(Blocks.FURNACE)) {
            NtmAdvancements.award(event.player, NtmAdvancements.BUILD_FURNACE);
            if (shiftTransfer) NtmAdvancements.award(event.player, NtmAdvancements.BUILD_FURNACE);
        }
    }

    @SubscribeEvent
    public void smelted(PlayerEvent.ItemSmeltedEvent event) {
        // Native SlotFurnace and NTM SlotSmelting fire this after each extraction callback.
        // SlotSmelting must not also make a second explicit iron/fish award attempt.
        Item item = outputItem(event.smelting);
        if (item == Items.IRON_INGOT) {
            NtmAdvancements.award(event.player, NtmAdvancements.ACQUIRE_IRON);
        }
        if (item == Items.COOKED_FISH) {
            NtmAdvancements.award(event.player, NtmAdvancements.COOK_FISH);
        }
    }
    /** Full shift-click's onTake receives a zero-count original stack in 1.12.
     * In 1.7 getItem still exposed that stack's identity, so both callbacks attempted
     * the achievement. Native copy retains the raw item; normalize only the copy.
     */
    private static Item outputItem(ItemStack output) {
        if (!output.isEmpty()) return output.getItem();
        ItemStack identity = output.copy();
        identity.setCount(1);
        return identity.getItem();
    }
}

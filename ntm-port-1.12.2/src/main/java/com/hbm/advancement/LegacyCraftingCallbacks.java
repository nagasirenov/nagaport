package com.hbm.advancement;

/** Narrow correction for the removed vanilla achievement callbacks, not NTM craft events.
 * 1.7 SlotCrafting checked vanilla achievements in both shift-transfer onSlotChange and
 * onPickupFromSlot. 1.12 emits only one FML crafting event for that pair. Match the exact
 * native workbench/player-container chain; no player history or generic event doubling.
 */
final class LegacyCraftingCallbacks {
    static boolean isVanillaShiftTransfer() {
        return isVanillaShiftTransfer(Thread.currentThread().getStackTrace());
    }

    static boolean isVanillaShiftTransfer(StackTraceElement[] frames) {
        int eventPosts = 0;
        for (int i = 0; i < frames.length; i++) {
            StackTraceElement frame = frames[i];
            if (frame.getClassName().equals("net.minecraftforge.fml.common.eventhandler.EventBus")
                    && frame.getMethodName().equals("post")) eventPosts++;
            if (frame.getClassName().equals("net.minecraftforge.fml.common.FMLCommonHandler")
                    && frame.getMethodName().equals("firePlayerCraftingEvent")) {
                // The nearest event must originate in this precise native callback chain.
                // Nested direct posts and programmatically fired events must not inherit it.
                if (eventPosts != 1 || i + 4 >= frames.length) return false;
                return matches(frames[i + 1], "SlotCrafting", "onCrafting", "func_75208_c")
                        && matches(frames[i + 2], "SlotCrafting", "onCrafting", "func_75210_a")
                        && matches(frames[i + 3], "Slot", "onSlotChange", "func_75220_a")
                        && (matches(frames[i + 4], "ContainerWorkbench", "transferStackInSlot", "func_82846_b")
                        || matches(frames[i + 4], "ContainerPlayer", "transferStackInSlot", "func_82846_b"));
            }
        }
        return false;
    }

    private static boolean matches(StackTraceElement frame, String type, String mcp, String srg) {
        return frame.getClassName().equals("net.minecraft.inventory." + type)
                && (frame.getMethodName().equals(mcp) || frame.getMethodName().equals(srg));
    }
    private LegacyCraftingCallbacks() { }
}

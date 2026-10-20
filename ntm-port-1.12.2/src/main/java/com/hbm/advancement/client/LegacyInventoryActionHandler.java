package com.hbm.advancement.client;

import java.util.Objects;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/** Restores the old inventory-key attempt, not arbitrary GUI opening.
 * Native processKeyBinds invokes displayGuiScreen before GuiOpenEvent cancellation
 * or creative-screen redirection. Adjacent frames reject nested/programmatic opens.
 * Unknown transformed caller paths are deliberately not assumed to be key actions.
 */
@SideOnly(Side.CLIENT)
public final class LegacyInventoryActionHandler {
    private final Runnable sendInventoryAction;
    public LegacyInventoryActionHandler(Runnable sendInventoryAction) {
        this.sendInventoryAction = Objects.requireNonNull(sendInventoryAction);
    }
    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public void onGuiOpen(GuiOpenEvent event) {
        if(event.getGui() != null && event.getGui().getClass() == GuiInventory.class
                && isNativeInventoryAttempt(Thread.currentThread().getStackTrace())) {
            sendInventoryAction.run();
        }
    }
    public static boolean isNativeInventoryAttempt(StackTraceElement[] trace) {
        String owner = Minecraft.class.getName();
        for(int i = 0; i + 1 < trace.length; i++) {
            StackTraceElement display = trace[i], caller = trace[i + 1];
            if(owner.equals(display.getClassName())
                    && ("displayGuiScreen".equals(display.getMethodName()) || "func_147108_a".equals(display.getMethodName()))) {
                return owner.equals(caller.getClassName())
                        && ("processKeyBinds".equals(caller.getMethodName()) || "func_184117_aA".equals(caller.getMethodName()));
            }
        }
        return false;
    }
}

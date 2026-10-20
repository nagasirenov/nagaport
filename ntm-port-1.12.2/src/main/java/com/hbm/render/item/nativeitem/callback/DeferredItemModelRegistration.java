package com.hbm.render.item.nativeitem.callback;

import java.util.Objects;
import java.util.function.Consumer;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** Resolve item identities only after Forge has flushed block and item registries.
 * Subscribers installed by the initializer must explicitly process this event;
 * they are not assumed to receive an event already being dispatched.
 */
public final class DeferredItemModelRegistration {
    private final Consumer<ModelRegistryEvent> initializer;
    private boolean initialized;
    public DeferredItemModelRegistration(Consumer<ModelRegistryEvent> initializer) {
        this.initializer=Objects.requireNonNull(initializer,"initializer");
    }
    @SubscribeEvent public void register(ModelRegistryEvent event) {
        if(initialized) return;
        // Mark before invoking to prevent recursive dispatch/double publication.
        // Initialization failure remains a startup failure, never partial retry.
        initialized=true;
        initializer.accept(event);
    }
}

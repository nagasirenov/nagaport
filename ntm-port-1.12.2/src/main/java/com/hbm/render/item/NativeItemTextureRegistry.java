package com.hbm.render.item;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

import com.hbm.render.LegacyTexturePath;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;

/**
 * Common-side item asset declarations. This is not a legacy Item API: each
 * declaration records the original texture independently of registry/translation
 * names, for the native client model provider. In particular, enum items retain
 * their separate, polymorphic subtype icon configuration.
 */
public final class NativeItemTextureRegistry {
    private static final Map<Item, String> TEXTURES = new IdentityHashMap<>();

    private NativeItemTextureRegistry() { }

    /** Preserve fluent evaluation order, concrete type and object identity. */
    public static <T extends Item> T texture(T item, String texture) {
        Objects.requireNonNull(item, "item");
        Objects.requireNonNull(texture, "texture");
        if(texture.trim().isEmpty()) throw new IllegalArgumentException("Item texture cannot be empty");
        TEXTURES.put(item, texture);
        return item;
    }

    public static String texture(Item item) { return TEXTURES.get(item); }

    /** Snapshot: client reloads must not mutate common-side configuration. */
    public static Map<Item, String> textures() {
        return Collections.unmodifiableMap(new IdentityHashMap<>(TEXTURES));
    }

    /** Old item atlas names are relative to textures/items, not textures/. */
    public static ResourceLocation atlasLocation(String texture) {
        return LegacyTexturePath.itemAtlasLocation(texture);
    }
}

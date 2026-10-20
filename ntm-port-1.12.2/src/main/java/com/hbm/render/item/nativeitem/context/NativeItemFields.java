package com.hbm.render.item.nativeitem.context;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/** Exact native declaration seam; descriptors survive MCP/SRG field renaming.
 * The native-jar ownership audit fixes each supported owner's unique field type. */
final class NativeItemFields {
    private NativeItemFields() { }
    static Field unique(Class<?> owner, Class<?> type) {
        Field found = null;
        for(Field field : owner.getDeclaredFields()) {
            if(!Modifier.isStatic(field.getModifiers()) && field.getType() == type) {
                if(found != null) throw new IllegalStateException("Ambiguous native field: " + owner.getName() + " / " + type.getName());
                field.setAccessible(true);
                found = field;
            }
        }
        if(found == null) throw new IllegalStateException("Missing native field: " + owner.getName() + " / " + type.getName());
        return found;
    }
    static Object get(Field field, Object owner) {
        try { return field.get(owner); }
        catch(IllegalAccessException failure) { throw new IllegalStateException(failure); }
    }
    static void set(Field field, Object owner, Object value) {
        try { field.set(owner, value); }
        catch(IllegalAccessException failure) { throw new IllegalStateException(failure); }
    }
}

package com.hbm.crafting.nativeapi;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

/** Registration of NTM-owned table recipes only, after item identities exist. */
public final class NtmCraftingRegistry {
    private static final Map<String, Integer> NEXT_DUPLICATE = new HashMap<>();
    private static final Map<String, byte[]> DEFINITIONS = new HashMap<>();
    private NtmCraftingRegistry() { }

    public static NtmCraftingRecipe shaped(String site, boolean ore, ItemStack output, Object... definition) {
        return register(site, true, ore, output, definition, NtmCraftingRecipe.shaped(output, ore, definition));
    }
    public static NtmCraftingRecipe shapeless(String site, boolean ore, ItemStack output, Object... definition) {
        return register(site, false, ore, output, definition, NtmCraftingRecipe.shapeless(output, ore, definition));
    }

    /** Fixed explicit identity for an NTM custom handler whose algorithm is not a table definition. */
    public static void special(String site, IRecipe recipe) {
        ResourceLocation name = new ResourceLocation("hbm", "crafting/" + site);
        if (ForgeRegistries.RECIPES.containsKey(name)) throw new IllegalStateException("Duplicate NTM special recipe identity: " + name);
        recipe.setRegistryName(name);
        ForgeRegistries.RECIPES.register(recipe);
    }

    private static NtmCraftingRecipe register(String site, boolean shaped, boolean ore, ItemStack output,
            Object[] definition, NtmCraftingRecipe recipe) {
        byte[] canonical = canonical(shaped, ore, output, definition);
        String key = site + "/" + digest(canonical);
        byte[] prior = DEFINITIONS.get(key);
        if (prior != null && !Arrays.equals(prior, canonical)) throw new IllegalStateException("NTM recipe definition digest collision: " + key);
        int duplicate = NEXT_DUPLICATE.containsKey(key) ? NEXT_DUPLICATE.get(key) : 0;
        ResourceLocation name = new ResourceLocation("hbm", "crafting/" + key + "/" + duplicate);
        if (ForgeRegistries.RECIPES.containsKey(name)) throw new IllegalStateException("Duplicate NTM recipe identity: " + name);
        recipe.setRegistryName(name);
        ForgeRegistries.RECIPES.register(recipe);
        DEFINITIONS.put(key, canonical);
        NEXT_DUPLICATE.put(key, duplicate + 1);
        return recipe;
    }

    private static byte[] canonical(boolean shaped, boolean ore, ItemStack output, Object[] definition) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            DataOutputStream out = new DataOutputStream(bytes);
            out.writeInt(1); // Definition encoding version, not process/config order.
            out.writeBoolean(shaped); out.writeBoolean(ore);
            value(out, output); value(out, definition); out.flush();
            return bytes.toByteArray();
        } catch (IOException impossible) { throw new IllegalStateException(impossible); }
    }

    private static void value(DataOutputStream out, Object value) throws IOException {
        if (value instanceof ItemStack) {
            ItemStack stack = (ItemStack) value;
            out.writeByte(1); name(out, stack.getItem().getRegistryName());
            out.writeInt(stack.getCount()); out.writeInt(stack.getItemDamage());
            nbt(out, stack.serializeNBT());
        } else if (value instanceof Item) { out.writeByte(2); name(out, ((Item) value).getRegistryName()); }
        else if (value instanceof Block) { out.writeByte(3); name(out, ((Block) value).getRegistryName()); }
        else if (value instanceof String) { out.writeByte(4); string(out, (String) value); }
        else if (value instanceof Character) { out.writeByte(5); out.writeChar((Character) value); }
        else if (value instanceof Object[]) {
            out.writeByte(6); Object[] array = (Object[]) value; out.writeInt(array.length);
            for (Object entry : array) value(out, entry);
        } else throw new IllegalArgumentException("Unsupported NTM recipe identity value: " + value);
    }

    private static void name(DataOutputStream out, ResourceLocation name) throws IOException {
        if (name == null) throw new IllegalArgumentException("Recipe identity requested before item/block registration");
        string(out, name.toString());
    }
    private static void string(DataOutputStream out, String value) throws IOException {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8); out.writeInt(bytes.length); out.write(bytes);
    }
    private static void nbt(DataOutputStream out, NBTBase tag) throws IOException {
        out.writeByte(tag.getId());
        if (tag instanceof NBTTagCompound) {
            NBTTagCompound compound = (NBTTagCompound) tag;
            TreeSet<String> keys = new TreeSet<>(compound.getKeySet()); out.writeInt(keys.size());
            for (String key : keys) { string(out, key); nbt(out, compound.getTag(key)); }
        } else if (tag instanceof NBTTagList) {
            NBTTagList list = (NBTTagList) tag; out.writeByte(list.getTagType()); out.writeInt(list.tagCount());
            for (NBTBase entry : list) nbt(out, entry);
        } else {
            // A one-entry compound has no ordering ambiguity; native binary encoding
            // retains numeric types and complete byte/int/long arrays without reflection.
            NBTTagCompound leaf = new NBTTagCompound(); leaf.setTag("v", tag);
            CompressedStreamTools.write(leaf, out);
        }
    }
    private static String digest(byte[] definition) {
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(definition);
            StringBuilder result = new StringBuilder(64);
            for (byte b : bytes) result.append(Character.forDigit((b >>> 4) & 15, 16)).append(Character.forDigit(b & 15, 16));
            return result.toString();
        } catch (NoSuchAlgorithmException impossible) { throw new IllegalStateException(impossible); }
    }
}

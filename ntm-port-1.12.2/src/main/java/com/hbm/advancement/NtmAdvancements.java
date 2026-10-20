package com.hbm.advancement;

import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.Nullable;
import net.minecraft.advancements.Advancement;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatBasic;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.util.FakePlayer;

/** Native advancement completion with the original direct-parent event gates.
 * No delayed awards, inventory backfill, legacy save conversion or cached Advancement objects.
 */
public final class NtmAdvancements {
    public static final ResourceLocation ROOT = new ResourceLocation("hbm", "ntm/root");
    public static final String CRITERION = "earned";
    public static final ResourceLocation OPEN_INVENTORY = id("openInventory");
    public static final ResourceLocation MINE_WOOD = id("mineWood");
    public static final ResourceLocation BUILD_WORKBENCH = id("buildWorkBench");
    public static final ResourceLocation BUILD_PICKAXE = id("buildPickaxe");
    public static final ResourceLocation BUILD_FURNACE = id("buildFurnace");
    public static final ResourceLocation ACQUIRE_IRON = id("acquireIron");
    public static final ResourceLocation COOK_FISH = id("cookFish");
    private static final Map<ResourceLocation, StatBase> COUNTERS = new LinkedHashMap<>();

    public static ResourceLocation id(String legacyName) { return AchievementDefinitions.id(legacyName); }

    /** Called during common initialization, before any player can earn an achievement.
     * Counters supplement real advancements; they are not a replacement for them.
     */
    public static void registerCounters() {
        if (!COUNTERS.isEmpty()) return;
        for (AchievementDefinitions.Definition definition : AchievementDefinitions.all().values()) {
            COUNTERS.put(definition.id, new StatBasic("stat.hbm.achievement." + definition.legacyName,
                    new TextComponentTranslation("achievement." + definition.legacyName))
                    .initIndependentStat().registerStat());
        }
    }

    public static StatBase counter(ResourceLocation id) {
        StatBase counter = COUNTERS.get(id);
        if (counter == null) throw new IllegalStateException("Unregistered NTM achievement counter " + id);
        return counter;
    }

    /** True only for a first native completion. Repeated qualified attempts still increment.
     * Null preserves the deliberately unregistered legacy rotConsum no-op.
     */
    public static boolean award(EntityPlayer player, @Nullable ResourceLocation id) {
        if (id == null || !(player instanceof EntityPlayerMP) || player instanceof FakePlayer
                || player.world.isRemote) return false;
        EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        AchievementDefinitions.Definition definition = definition(id);
        if (definition.parent != null && !has(serverPlayer, definition.parent)) return false;
        Advancement advancement = resolve(serverPlayer, id);
        Advancement root = resolve(serverPlayer, ROOT);
        // Match the old increment for every qualified event, not only grantCriterion == true.
        serverPlayer.addStat(counter(id), 1);
        serverPlayer.getAdvancements().grantCriterion(root, CRITERION);
        return serverPlayer.getAdvancements().grantCriterion(advancement, CRITERION);
    }

    public static boolean has(EntityPlayer player, @Nullable ResourceLocation id) {
        if (id == null || !(player instanceof EntityPlayerMP) || player instanceof FakePlayer
                || player.world.isRemote) return false;
        EntityPlayerMP serverPlayer = (EntityPlayerMP) player;
        definition(id);
        return serverPlayer.getAdvancements().getProgress(resolve(serverPlayer, id)).isDone();
    }

    /** Bobmazon's existing icon presentation, resolved only after native item registration. */
    public static ItemStack icon(ResourceLocation id) {
        AchievementDefinitions.Definition definition = definition(id);
        if (!Item.REGISTRY.containsKey(definition.icon)) {
            throw new IllegalStateException("Unregistered NTM achievement icon " + definition.icon);
        }
        return new ItemStack(Item.REGISTRY.getObject(definition.icon), 1, definition.metadata);
    }

    private static AchievementDefinitions.Definition definition(ResourceLocation id) {
        AchievementDefinitions.Definition definition = AchievementDefinitions.all().get(id);
        if (definition == null) throw new IllegalArgumentException("Unknown NTM achievement " + id);
        return definition;
    }

    private static Advancement resolve(EntityPlayerMP player, ResourceLocation id) {
        Advancement advancement = player.server.getAdvancementManager().getAdvancement(id);
        if (advancement == null) throw new IllegalStateException("Missing native NTM advancement " + id);
        return advancement;
    }

    private NtmAdvancements() { }
}

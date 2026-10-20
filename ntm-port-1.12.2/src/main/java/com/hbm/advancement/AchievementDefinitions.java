package com.hbm.advancement;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.util.ResourceLocation;

/** Exact old-ID mapping and direct gameplay parents. Visual JSON parents are not gates. */
public final class AchievementDefinitions {
    public static final class Definition {
        public final ResourceLocation id;
        public final String legacyName;
        public final ResourceLocation parent;
        public final ResourceLocation icon;
        public final int metadata;
        private Definition(String legacyName, String id, String parent, String icon, int metadata) {
            this.legacyName = legacyName;
            this.id = new ResourceLocation(id);
            this.parent = parent == null ? null : new ResourceLocation(parent);
            this.icon = new ResourceLocation(icon);
            this.metadata = metadata;
        }
    }
    private static final Map<ResourceLocation, Definition> DEFINITIONS = new LinkedHashMap<>();
    private static final Map<String, ResourceLocation> LEGACY_IDS = new LinkedHashMap<>();
    static {
        add("sacrifice", "hbm:ntm/sacrifice", null, "hbm:item.burnt_bark", 0);
        add("impossible", "hbm:ntm/impossible", null, "hbm:item.nothing", 0);
        add("tasteofblood", "hbm:ntm/tasteofblood", null, "hbm:item.fluid_icon", 34);
        add("goFish", "hbm:ntm/gofish", null, "hbm:item.achievement_icon", 0);
        add("potato", "hbm:ntm/potato", null, "hbm:item.battery_potatos", 0);
        add("c20_5", "hbm:ntm/c20_5", null, "hbm:item.achievement_icon", 9);
        add("fiend", "hbm:ntm/fiend", null, "hbm:item.shimmer_sledge", 0);
        add("fiend2", "hbm:ntm/fiend2", null, "hbm:item.shimmer_axe", 0);
        add("stratum", "hbm:ntm/stratum", null, "hbm:tile.stone_gneiss", 0);
        add("omega12", "hbm:ntm/omega12", null, "hbm:item.particle_digamma", 0);
        add("no9", "hbm:ntm/no9", null, "hbm:item.no9", 0);
        add("slimeball", "hbm:ntm/slimeball", null, "hbm:item.achievement_icon", 1);
        add("sulfuric", "hbm:ntm/sulfuric", "hbm:ntm/slimeball", "hbm:item.achievement_icon", 2);
        add("inferno", "hbm:ntm/inferno", null, "hbm:item.canister_napalm", 0);
        add("redRoom", "hbm:ntm/redroom", null, "hbm:item.key_red", 0);
        add("hidden", "hbm:ntm/hidden", null, "hbm:item.achievement_icon", 9);
        add("horizonsStart", "hbm:ntm/horizonsstart", null, "hbm:item.sat_gerald", 0);
        add("horizonsEnd", "hbm:ntm/horizonsend", "hbm:ntm/horizonsstart", "hbm:item.sat_gerald", 0);
        add("horizonsBonus", "hbm:ntm/horizonsbonus", "hbm:ntm/horizonsend", "hbm:item.sat_gerald", 0);
        add("bossCreeper", "hbm:ntm/bosscreeper", null, "hbm:item.coin_creeper", 0);
        add("bossMeltdown", "hbm:ntm/bossmeltdown", "hbm:ntm/bosscreeper", "hbm:item.coin_radiation", 0);
        add("bossMaskman", "hbm:ntm/bossmaskman", "hbm:ntm/bosscreeper", "hbm:item.coin_maskman", 0);
        add("bossWorm", "hbm:ntm/bossworm", "hbm:ntm/bossmaskman", "hbm:item.coin_worm", 0);
        add("bossUFO", "hbm:ntm/bossufo", "hbm:ntm/bossworm", "hbm:item.coin_ufo", 0);
        add("radPoison", "hbm:ntm/radpoison", null, "hbm:item.geiger_counter", 0);
        add("radDeath", "hbm:ntm/raddeath", "hbm:ntm/radpoison", "minecraft:skull", 0);
        add("someWounds", "hbm:ntm/somewounds", null, "hbm:item.injector_knife", 0);
        add("digammaSee", "hbm:ntm/digammasee", null, "hbm:item.achievement_icon", 3);
        add("digammaFeel", "hbm:ntm/digammafeel", "hbm:ntm/digammasee", "hbm:item.achievement_icon", 4);
        add("digammaKnow", "hbm:ntm/digammaknow", "hbm:ntm/digammafeel", "hbm:item.achievement_icon", 5);
        add("digammaKauaiMoho", "hbm:ntm/digammakauaimoho", "hbm:ntm/digammaknow", "hbm:item.achievement_icon", 6);
        add("digammaUpOnTop", "hbm:ntm/digammaupontop", "hbm:ntm/digammakauaimoho", "hbm:item.achievement_icon", 7);
        add("burnerPress", "hbm:ntm/burnerpress", null, "hbm:tile.machine_press", 0);
        add("blastFurnace", "hbm:ntm/blastfurnace", "hbm:ntm/burnerpress", "hbm:tile.machine_difurnace_off", 0);
        add("assembly", "hbm:ntm/assembly", "hbm:ntm/burnerpress", "hbm:tile.machine_assembly_machine", 0);
        add("selenium", "hbm:ntm/selenium", "hbm:ntm/burnerpress", "hbm:item.ingot_starmetal", 0);
        add("chemplant", "hbm:ntm/chemplant", "hbm:ntm/assembly", "hbm:tile.machine_chemical_plant", 0);
        add("concrete", "hbm:ntm/concrete", "hbm:ntm/chemplant", "hbm:tile.concrete", 0);
        add("polymer", "hbm:ntm/polymer", "hbm:ntm/chemplant", "hbm:item.ingot_polymer", 0);
        add("desh", "hbm:ntm/desh", "hbm:ntm/chemplant", "hbm:item.ingot_desh", 0);
        add("tantalum", "hbm:ntm/tantalum", "hbm:ntm/chemplant", "hbm:item.gem_tantalium", 0);
        add("gasCent", "hbm:ntm/gascent", "hbm:ntm/desh", "hbm:item.ingot_uranium_fuel", 0);
        add("centrifuge", "hbm:ntm/centrifuge", "hbm:ntm/polymer", "hbm:tile.machine_centrifuge", 0);
        add("driveFail", "hbm:ntm/drivefail", "hbm:ntm/polymer", "hbm:item.hard_drive_full", 0);
        add("FOEQ", "hbm:ntm/foeq", "hbm:ntm/desh", "hbm:item.sat_foeq", 0);
        add("soyuz", "hbm:ntm/soyuz", "hbm:ntm/desh", "minecraft:baked_potato", 0);
        add("space", "hbm:ntm/space", "hbm:ntm/desh", "hbm:item.missile_soyuz", 0);
        add("schrab", "hbm:ntm/schrab", "hbm:ntm/desh", "hbm:item.ingot_schrabidium", 0);
        add("acidizer", "hbm:ntm/acidizer", "hbm:ntm/desh", "hbm:tile.machine_crystallizer", 0);
        add("radium", "hbm:ntm/radium", "hbm:ntm/centrifuge", "hbm:item.coffee_radium", 0);
        add("technetium", "hbm:ntm/technetium", "hbm:ntm/centrifuge", "hbm:item.ingot_tcalloy", 0);
        add("ZIRNOXBoom", "hbm:ntm/zirnoxboom", "hbm:ntm/centrifuge", "hbm:item.debris_element", 0);
        add("chicagoPile", "hbm:ntm/chicagopile", "hbm:ntm/centrifuge", "hbm:item.pile_rod_plutonium", 0);
        add("SILEX", "hbm:ntm/silex", "hbm:ntm/acidizer", "hbm:tile.machine_silex", 0);
        add("watz", "hbm:ntm/watz", "hbm:ntm/schrab", "hbm:item.watz_pellet", 0);
        add("watzBoom", "hbm:ntm/watzboom", "hbm:ntm/watz", "hbm:item.bucket_mud", 0);
        add("RBMK", "hbm:ntm/rbmk", "hbm:ntm/concrete", "hbm:item.rbmk_fuel_ueu", 0);
        add("RBMKBoom", "hbm:ntm/rbmkboom", "hbm:ntm/rbmk", "hbm:item.debris_fuel", 0);
        add("bismuth", "hbm:ntm/bismuth", "hbm:ntm/rbmk", "hbm:item.ingot_bismuth", 0);
        add("breeding", "hbm:ntm/breeding", "hbm:ntm/rbmk", "hbm:item.ingot_am_mix", 0);
        add("fusion", "hbm:ntm/fusion", "hbm:ntm/bismuth", "hbm:tile.iter", 0);
        add("redBalloons", "hbm:ntm/redballoons", "hbm:ntm/polymer", "hbm:item.missile_nuclear", 0);
        add("manhattan", "hbm:ntm/manhattan", "hbm:ntm/polymer", "hbm:tile.nuke_boy", 0);
        add("openInventory", "hbm:legacy/openinventory", null, "minecraft:book", 0);
        add("mineWood", "hbm:legacy/minewood", "hbm:legacy/openinventory", "minecraft:log", 0);
        add("buildWorkBench", "hbm:legacy/buildworkbench", "hbm:legacy/minewood", "minecraft:crafting_table", 0);
        add("buildPickaxe", "hbm:legacy/buildpickaxe", "hbm:legacy/buildworkbench", "minecraft:wooden_pickaxe", 0);
        add("buildFurnace", "hbm:legacy/buildfurnace", "hbm:legacy/buildpickaxe", "minecraft:furnace", 0);
        add("acquireIron", "hbm:legacy/acquireiron", "hbm:legacy/buildfurnace", "minecraft:iron_ingot", 0);
        add("cookFish", "hbm:legacy/cookfish", "hbm:legacy/buildfurnace", "minecraft:cooked_fish", 0);
    }
    private static void add(String name, String id, String parent, String icon, int metadata) {
        Definition definition = new Definition(name, id, parent, icon, metadata);
        if (DEFINITIONS.put(definition.id, definition) != null || LEGACY_IDS.put(name, definition.id) != null) {
            throw new IllegalStateException("Duplicate NTM achievement " + name);
        }
    }
    public static Map<ResourceLocation, Definition> all() { return Collections.unmodifiableMap(DEFINITIONS); }
    public static ResourceLocation id(String legacyName) {
        ResourceLocation id = LEGACY_IDS.get(legacyName);
        if (id == null) throw new IllegalArgumentException("Unknown NTM achievement " + legacyName);
        return id;
    }
    private AchievementDefinitions() { }
}

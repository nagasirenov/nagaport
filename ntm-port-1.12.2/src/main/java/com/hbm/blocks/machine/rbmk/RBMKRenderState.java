package com.hbm.blocks.machine.rbmk;

import java.util.*;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.property.*;

/** Render-only identity and immutable neighborhood data for the original eighteen column blocks. */
public final class RBMKRenderState {
    private RBMKRenderState() { }
    public enum Family { ROD, PASSIVE, CONTROL }
    public static final class Profile {
        public final String name, ownerClass, texture;
        public final Family family;
        public final boolean ownLid, specialBottom;
        private Profile(String name, String ownerClass, String texture, Family family, boolean ownLid, boolean specialBottom) {
            this.name = "tile." + name;
            this.ownerClass = "com.hbm.blocks.machine.rbmk." + ownerClass;
            this.texture = "hbm:blocks/rbmk/" + texture;
            this.family = family; this.ownLid = ownLid; this.specialBottom = specialBottom;
        }
    }
    private static final Map<String, Profile> PROFILES;
    static {
        Map<String, Profile> map = new LinkedHashMap<>();
        add(map, "rbmk_rod", "RBMKRod", "rbmk_element", Family.ROD, false, false);
        add(map, "rbmk_rod_mod", "RBMKRod", "rbmk_element_mod", Family.ROD, false, false);
        add(map, "rbmk_rod_reasim", "RBMKRodReaSim", "rbmk_element_reasim", Family.ROD, false, false);
        add(map, "rbmk_rod_reasim_mod", "RBMKRodReaSim", "rbmk_element_reasim_mod", Family.ROD, false, false);
        add(map, "rbmk_control", "RBMKControl", "rbmk_control", Family.CONTROL, true, false);
        add(map, "rbmk_control_mod", "RBMKControl", "rbmk_control_mod", Family.CONTROL, true, false);
        add(map, "rbmk_control_auto", "RBMKControlAuto", "rbmk_control_auto", Family.CONTROL, true, false);
        add(map, "rbmk_control_reasim", "RBMKControl", "rbmk_control_reasim", Family.CONTROL, true, true);
        add(map, "rbmk_control_reasim_auto", "RBMKControlAuto", "rbmk_control_reasim_auto", Family.CONTROL, true, true);
        add(map, "rbmk_boiler", "RBMKBoiler", "rbmk_boiler", Family.CONTROL, false, false);
        add(map, "rbmk_heater", "RBMKHeater", "rbmk_heater", Family.CONTROL, false, false);
        for(String suffix : new String[] {"blank", "reflector", "absorber", "moderator", "outgasser", "storage", "cooler"})
            add(map, "rbmk_" + suffix, "RBMK" + Character.toUpperCase(suffix.charAt(0)) + suffix.substring(1), "rbmk_" + suffix, Family.PASSIVE, false, false);
        PROFILES = Collections.unmodifiableMap(map);
    }
    private static void add(Map<String, Profile> map, String name, String type, String texture, Family family, boolean own, boolean bottom) {
        Profile profile = new Profile(name, type, texture, family, own, bottom); map.put(profile.name, profile);
    }
    public static Collection<Profile> profiles() { return PROFILES.values(); }
    public static Profile profile(String name) { return PROFILES.get(name); }
    public static Profile profile(Block block) {
        ResourceLocation name = block.getRegistryName();
        if(name == null || !"hbm".equals(name.getNamespace())) return null;
        Profile profile = PROFILES.get(name.getPath());
        return profile != null && block.getClass().getName().equals(profile.ownerClass) ? profile : null;
    }
    public static final IUnlistedProperty<Snapshot> RENDER = new IUnlistedProperty<Snapshot>() {
        @Override public String getName() { return "rbmk_render"; }
        @Override public boolean isValid(Snapshot value) { return value != null; }
        @Override public Class<Snapshot> getType() { return Snapshot.class; }
        @Override public String valueToString(Snapshot value) { return value.pos.toString(); }
    };
    public static Snapshot snapshot(IBlockState state) {
        if(!(state instanceof IExtendedBlockState)) throw new IllegalArgumentException("RBMK world model requires extended render state");
        Snapshot snapshot = ((IExtendedBlockState)state).getValue(RENDER);
        if(snapshot == null) throw new IllegalArgumentException("RBMK world model requires its render snapshot");
        return snapshot;
    }
    public static final class Snapshot {
        public final IBlockAccess world;
        public final IBlockState state;
        public final BlockPos pos;
        public final int metadata, lid, bodyFaces, upperFaces, packedLight;
        public Snapshot(IBlockState state, IBlockAccess world, BlockPos pos, int metadata, int lid) {
            if(metadata < 0 || metadata > 15 || lid < 0 || lid > 2) throw new IllegalArgumentException("Invalid RBMK render variant");
            this.state = state instanceof IExtendedBlockState ? ((IExtendedBlockState)state).getClean() : state;
            this.world = world; this.pos = pos.toImmutable(); this.metadata = metadata; this.lid = lid;
            int body = 0, upper = 0;
            for(EnumFacing side : EnumFacing.values()) {
                // Original shouldSideBeRendered uses the owner's full bounds, not pipe/lid render bounds.
                if(!world.getBlockState(pos.offset(side)).isOpaqueCube()) body |= 1 << side.getIndex();
                if(lid != 0 && side.getAxis() != EnumFacing.Axis.Y || !world.getBlockState(pos.up().offset(side)).isOpaqueCube()) upper |= 1 << side.getIndex();
            }
            bodyFaces = body; upperFaces = upper;
            packedLight = state.getPackedLightmapCoords(world, pos);
        }
        public boolean extra() { return metadata >= 6 && metadata < 12; }
    }
}

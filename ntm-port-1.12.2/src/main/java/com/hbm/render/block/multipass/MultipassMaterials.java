package com.hbm.render.block.multipass;

import java.util.*;
import com.hbm.blocks.MultipassContext;
import net.minecraft.util.ResourceLocation;

/** Original twelve families' side/meta/pass material decisions, independent of native light solvers. */
public final class MultipassMaterials {
    public enum Kind { CABLE, OC_CABLE, FLUID, EXHAUST, PNEUMATIC, CABLE_GAUGE, FLUID_GAUGE, ROUTER, FISSURE, ORE, BEDROCK_ORE, SELLAFIELD }
    public final Kind kind;
    public final ResourceLocation base;
    private final List<ResourceLocation> stones;
    public MultipassMaterials(Kind kind,String base,List<ResourceLocation> stones) {
        this.kind=Objects.requireNonNull(kind); this.base=texture(base);
        this.stones=Collections.unmodifiableList(new ArrayList<>(stones));
        if(kind==Kind.ORE && stones.isEmpty()) throw new IllegalArgumentException("Ore stone palette is required");
    }
    public static ResourceLocation texture(String name) {
        ResourceLocation id=new ResourceLocation(name.indexOf(':')<0?"hbm:"+name:name);
        return new ResourceLocation(id.getNamespace(),id.getPath().startsWith("blocks/")?id.getPath():"blocks/"+id.getPath());
    }
    public int passes(boolean item) { return item && kind!=Kind.ORE && kind!=Kind.SELLAFIELD && kind!=Kind.FISSURE ? 1 : kind==Kind.ROUTER ? 7 : kind==Kind.OC_CABLE ? 3 : 2; }
    public boolean visible(int pass,int side,boolean item) { return item || kind!=Kind.ROUTER || pass==0 || pass-1==side; }
    public boolean camouflage(MultipassContext c,int pass,boolean item) {
        if(item || pass!=0 || c.paint==null) return false;
        return kind==Kind.CABLE || kind==Kind.OC_CABLE || kind==Kind.FLUID || kind==Kind.EXHAUST || kind==Kind.PNEUMATIC;
    }
    public ResourceLocation material(MultipassContext c,int pass,int side,boolean item) {
        switch(kind) {
            case CABLE: return pass==0?base:texture("red_cable_overlay");
            case OC_CABLE: return pass==0?base:texture(pass==1?"oc_cable_overlay":"oc_cable_color");
            case FLUID: return pass==0?base:texture(c.paint==null?"fluid_duct_paintable_color":"fluid_duct_paintable_overlay");
            case EXHAUST: return pass==0 || c.paint==null?base:texture("fluid_duct_paintable_overlay");
            case PNEUMATIC: return pass==0 || !c.tile?base:texture(side==c.ejection?"pneumatic_tube_paintable_overlay_in":side==c.insertion?"pneumatic_tube_paintable_overlay_out":"pneumatic_tube_paintable_overlay");
            case CABLE_GAUGE: return pass==0 || side!=c.metadata?base:texture("cable_gauge");
            case FLUID_GAUGE: return pass==0?base:texture(side==c.metadata?"pipe_gauge":"fluid_duct_paintable_overlay");
            case ROUTER: return pass==0?base:texture("crane_router_overlay");
            case FISSURE: return pass==0?base:texture("molten_overlay");
            case ORE: return pass==0?stones.get(c.metadata % stones.size()):base;
            case BEDROCK_ORE: return pass==0 || (!item && !c.tile)?base:texture("ore_random_"+((item?c.metadata:c.shape)%10+1));
            case SELLAFIELD: return pass==1?base:texture("sellafield_slaked"+(item || c.variation==0?"":"_"+c.variation));
            default: throw new AssertionError(kind);
        }
    }
    public int color(MultipassContext c,int pass,boolean item) {
        // The original inventory loop never calls colorMultiplier or changes GL color.
        if(item) return 0xffffff;
        switch(kind) {
            case OC_CABLE: return pass==2?c.color:0xffffff;
            case FLUID: return pass==1 && c.tile && c.paint==null?c.color:0xffffff;
            case BEDROCK_ORE: return pass==1 && c.tile?c.color:0xffffff;
            case SELLAFIELD: return pass==0?c.color:0xffffff;
            case ROUTER: return pass==0?0xffffff:new int[]{0xff0000,0xff8000,0xffff00,0x00ff00,0x0080ff,0x8000ff}[pass-1];
            default:return 0xffffff;
        }
    }
    public Collection<ResourceLocation> textures() {
        Set<ResourceLocation> result=new LinkedHashSet<>(); result.add(base); result.addAll(stones);
        for(int metadata=0;metadata<16;metadata++) for(int shape=0;shape<10;shape++) for(int variant=0;variant<4;variant++) {
            MultipassContext c=MultipassContext.of(metadata,null,true,0xffffff,shape,2,3,variant);
            for(int pass=0;pass<passes(false);pass++) for(int side=0;side<6;side++) result.add(material(c,pass,side,false));
        }
        if(kind==Kind.FLUID || kind==Kind.EXHAUST) result.add(texture("fluid_duct_paintable_overlay"));
        return Collections.unmodifiableSet(result);
    }
}

import net.minecraft.util.ResourceLocation;
public final class ResourceNames { public static void main(String[] args){for(String s:args){ResourceLocation r=new ResourceLocation(s);System.out.println(s+"\t"+r.toString().replace(":","\t"));}} }

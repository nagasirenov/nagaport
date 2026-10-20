import java.lang.reflect.*;import net.minecraft.init.Bootstrap;
/** Explicit excluded pre-initialization cases, not an equivalence test. */
public class NullBlockLimits extends OwnedWriteTest {
 public static void main(String[]a)throws Exception{
  Bootstrap.register();try(Old o=new Old(a[0],a[1],a[2])){
   for(String scenario:new String[]{"stone","air","empty section"}){
    o.seed(0,64,0);Object sections=field(o.chunk,"u").get(o.current[0]);Object section=Array.get(sections,4);
    if(scenario.equals("air"))o.storage.getMethod("a",int.class,int.class,int.class,o.block).invoke(section,0,0,0,o.block.getMethod("e",int.class).invoke(null,0));
    if(scenario.equals("empty section"))Array.set(sections,4,null);
    ((int[])field(o.chunk,"b").get(o.current[0]))[0]=64;
    int beforeId=(Integer)o.block.getMethod("b",o.block).invoke(null,o.read.invoke(o.w,0,64,0));o.calls=0;
    try{o.write.invoke(o.w,0,64,0,null,6,3);throw new AssertionError("expected original NPE");}catch(InvocationTargetException e){if(!(e.getCause() instanceof NullPointerException))throw e;}
    int writeRequests=o.calls;Object afterSection=Array.get(sections,4);int afterId=(Integer)o.block.getMethod("b",o.block).invoke(null,o.read.invoke(o.w,0,64,0));
    System.out.println("Original in-domain null target, "+scenario+": block ID "+beforeId+" -> "+afterId+", metadata="+o.meta(0,64,0)+", write chunk requests="+writeRequests+", height-cache 64 -> "+((int[])field(o.chunk,"b").get(o.current[0]))[0]+", allocated section="+(afterSection!=null)+", non-air count="+(afterSection==null?"absent":field(o.storage,"b").getInt(afterSection)));
    if(beforeId!=afterId)throw new AssertionError("unexpected raw owner mutation");
   }
  }
  System.out.println("LIMIT: native conversion of null occurs before chunk access. Selected write equivalence requires the initialized, nonnull constructor identities. Pre-init/reflection-replaced null fields inside the domain are not equivalent; outside-domain null is independently preserved.");
 }
}

package nameproof;
import java.lang.reflect.*;
import java.util.*;
import com.mojang.authlib.GameProfile;
/** Executes the original signed, unmodified engine classes through reflection. */
public final class LegacyPlayerNameTest {
 public static void main(String[] args)throws Exception {
  Field uf=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);sun.misc.Unsafe u=(sun.misc.Unsafe)uf.get(null);
  Class<?> player=Class.forName("yz");Field profile=player.getDeclaredField("i");profile.setAccessible(true);Method name=player.getMethod("b_");int checks=0;
  for(String type:new String[]{"mw","blk"})for(String s:new String[]{null,"","Target Practice","HbMinecraft","alice","ALICE","\u00a7cRaw Name","\u03b1\u03b2\u03b3"}) {
   Object p=u.allocateInstance(Class.forName(type));GameProfile g=new GameProfile(new UUID(1,2),s);profile.set(p,g);
   if(name.invoke(p)!=s)throw new AssertionError(type+" must return raw GameProfile name by identity");checks++;
  }
  System.out.println("PASS "+checks+" actual original player raw-profile identity assertions, including null/empty/case/Unicode/formatting codes. No original player constructor or game lifecycle claim.");
 }
}

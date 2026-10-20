/** Directly compiled original World subclass; startup constructor is never invoked. Only observation/lighting/scheduler seams override methods. */
public class OriginalFlagWorld extends bjf {
 public StringBuilder events;public int scheduled;public aji expected;
 public OriginalFlagWorld(){super(null,null,0,null,null);}
 @Override public boolean t(int x,int y,int z){return true;}
 @Override public void g(int x,int y,int z){events.append('u');}
 @Override public void c(int x,int y,int z,aji old){events.append('n');if(old!=ajn.b)throw new AssertionError("wrong original old owner");}
 @Override public void f(int x,int y,int z,aji b){events.append('c');}
 @Override public void a(int x,int y,int z,aji b,int ticks){scheduled++;if(x!=0||y!=64||z!=0||b!=expected||ticks!=2)throw new AssertionError("wrong original falling scheduled callback");}
}

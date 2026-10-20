/** Runs against the actual supplied obfuscated 1.7.10 client, not a model. */
public class LegacyInventoryNbtTest {
    static int checks;
    static void check(boolean ok,String message) { if(!ok) throw new AssertionError(message); checks++; }
    static dh data(int id,int count,int damage) { dh n=new dh();n.a("id",(short)id);n.a("Count",(byte)count);n.a("Damage",(short)damage);return n; }
    public static void main(String[] args) {
        kl.b();
        check(add.a(new dh())==null,"missing ID null");
        check(add.a(data(32000,1,0))==null,"unregistered ID null");
        check(add.a(data(0,1,0))==null,"zero ID null");
        for(int count:new int[]{-128,-1,0,1,64,127}) for(int damage:new int[]{-32768,-1,0,12,32767}) {
            dh n=data(265,count,damage),tag=new dh();tag.a("unknown","retained");n.a("tag",tag);
            add stack=add.a(n);
            check(stack!=null,"registered count remains non-null");
            check(stack.b==count,"signed count retained");
            check(stack.k()==Math.max(0,damage),"negative damage clamps zero");
            check(stack.d==tag,"tag alias retained");
        }
        System.out.println("PASS actual original 1.7.10 loader: "+checks+" checks");
    }
}

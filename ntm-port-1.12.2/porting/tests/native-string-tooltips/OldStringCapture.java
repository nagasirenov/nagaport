import java.util.List;
/** Subclass probe only; bdw and its String wrapper are actual original client classes. */
public class OldStringCapture extends bdw {
 public List seen; public int x,y,calls;
 protected void a(List list,int x,int y){seen=list;this.x=x;this.y=y;calls++;}
 public void invoke(String text,int x,int y){super.a(text,x,y);}
}

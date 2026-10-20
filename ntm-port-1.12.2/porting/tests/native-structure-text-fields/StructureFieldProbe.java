import java.lang.reflect.*;
import java.util.*;
import java.io.File;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import sun.misc.Unsafe;
public class StructureFieldProbe {
 static int checks;
 static void eq(Object a,Object b,String reason){checks++;if(!Objects.equals(a,b))throw new AssertionError(reason+": "+a+" != "+b);}
 static void put(Object value,Class<?> c,String name,Object v)throws Exception{Field f=c.getDeclaredField(name);f.setAccessible(true);f.set(value,v);}
 static GuiTextField widget(Object screen,String name)throws Exception{Field f=screen.getClass().getDeclaredField(name);f.setAccessible(true);return (GuiTextField)f.get(screen);}
 static Object read(Object o,Class<?> c,String n)throws Exception{Field f=c.getDeclaredField(n);f.setAccessible(true);return f.get(o);}
 public static void main(String[] args)throws Exception{
  Field uf=Unsafe.class.getDeclaredField("theUnsafe");uf.setAccessible(true);Unsafe u=(Unsafe)uf.get(null);
  FontRenderer font=(FontRenderer)u.allocateInstance(FontRenderer.class);int[] widths=new int[256];Arrays.fill(widths,6);put(font,FontRenderer.class,"charWidth",widths);put(font,FontRenderer.class,"glyphWidth",new byte[65536]);
  Minecraft mc=(Minecraft)u.allocateInstance(Minecraft.class);put(null,Minecraft.class,"instance",mc);
  String[] names={"textName","textSizeX","textSizeY","textSizeZ"};String[] values={"demo","12","34","56"};
  for(int width:new int[]{320,640,801}){
   StructureDispatchFixture.Save save=new StructureDispatchFixture.Save();save.width=width;put(save,GuiScreen.class,"fontRenderer",font);mc.currentScreen=save;save.initialize();
   for(int id=0;id<4;id++){
    GuiTextField f=widget(save,names[id]);eq(f.getId(),id,"save ID");eq(f.getText(),values[id],"save initial text");eq(f.x,width/2-150+(id>0?(id-1)*50:0),"save x");eq(f.y,id==0?50:100,"save y");eq(f.width,id==0?300:50,"save width");eq(f.height,20,"save height");eq(read(f,GuiTextField.class,"fontRenderer"),font,"font identity");eq(read(f,GuiTextField.class,"guiResponder"),null,"no responder");eq(read(f,GuiTextField.class,"canLoseFocus"),true,"default lose focus");
    save.dispatchMouse(f.x+1,f.y+1,0);
    for(int other=0;other<4;other++)eq(widget(save,names[other]).isFocused(),other==id,"actual ordered click dispatch focuses only selected widget");
    save.dispatchKey('Q',16);eq(f.getText(),"Q"+values[id],"actual ordered key dispatch changes selected widget only");f.setText(values[id]);
   }
   save.dispatchMouse(width+50,300,0);save.dispatchKey('Q',16);for(int id=0;id<4;id++)eq(widget(save,names[id]).getText(),values[id],"outside click does not edit any widget");
   GuiTextField old=widget(save,"textName");save.initialize();eq(old==widget(save,"textName"),false,"reinit creates fresh widget");for(int id=0;id<4;id++)eq(widget(save,names[id]).getId(),id,"reinit stable IDs");
   StructureDispatchFixture.Load load=new StructureDispatchFixture.Load();load.width=width;put(load,GuiScreen.class,"fontRenderer",font);mc.currentScreen=load;load.initialize();GuiTextField f=widget(load,"textName");eq(f.getId(),0,"independent load owner ID");eq(f.getText(),"demo","load initial text");eq(f.x,width/2-150,"load x");eq(f.y,50,"load y");eq(f.width,300,"load width");eq(f.height,20,"load height");eq(load.nameFilter,"demo","initial name filter");eq(read(f,GuiTextField.class,"guiResponder"),null,"load no responder");
   load.selectFile(new File("selected.nbt"));eq(f.getText(),"selected","actual file callback strips extension and sets text");eq(f.getId(),0,"file callback does not change ID");
   load.dispatchMouse(f.x+1,f.y+1,0);load.dispatchKey('Q',16);eq(f.getText(),"Qselected","load widget key dispatch");load.dispatchMouse(width+5,300,0);eq(f.isFocused(),false,"load default outside unfocus");
  }
  System.out.println("PASS "+checks+" native nested-owner constructor/configuration/ordered dispatch/file-selection assertions; reused prior old/native widget lifecycle proof, not rerun.");
 }
}

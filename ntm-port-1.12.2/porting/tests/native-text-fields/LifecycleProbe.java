import java.lang.reflect.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.*;
import sun.misc.Unsafe;

/** Bounded real old/native widgets: no replacement Minecraft classes, no GL renderer. */
public final class LifecycleProbe {
    static int checks;
    static final Unsafe U;
    static {
        try { Field f=Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);U=(Unsafe)f.get(null); }
        catch(Exception e) { throw new RuntimeException(e); }
    }
    static void eq(Object a,Object b,String why) { checks++;if(!Objects.equals(a,b))throw new AssertionError(why+": "+a+" != "+b); }
    static Object field(Object target,String name)throws Exception { Field f=target.getClass().getDeclaredField(name);f.setAccessible(true);return f.get(target); }
    static void put(Object target,Class<?> owner,String name,Object value)throws Exception { Field f=owner.getDeclaredField(name);f.setAccessible(true);f.set(target,value); }
    static Object old(bcd target,String name,Class<?>[] types,Object...args)throws Exception { return bcd.class.getMethod(name,types).invoke(target,args); }
    static void compare(GuiTextField n,bcd o,String stage)throws Exception {
        String[][] mapping={{"text","j"},{"maxStringLength","k"},{"cursorCounter","l"},{"enableBackgroundDrawing","m"},{"canLoseFocus","n"},{"isFocused","o"},{"isEnabled","p"},{"lineScrollOffset","q"},{"cursorPosition","r"},{"selectionEnd","s"},{"enabledColor","t"},{"disabledColor","u"},{"visible","v"}};
        for(String[] pair:mapping)eq(field(n,pair[0]),field(o,pair[1]),stage+" "+pair[0]);
        eq(n.getText(),o.b(),stage+" text getter");eq(n.getSelectedText(),o.c(),stage+" selection getter");eq(n.getWidth(),o.o(),stage+" width getter");
    }
    static void lifecycle(GuiTextField n,bcd o,GuiScreen screen)throws Exception {
        compare(n,o,"defaults");
        n.setMaxStringLength(12);o.f(12);n.setTextColor(0x00ff00);o.g(0x00ff00);n.setDisabledTextColour(-1);o.h(-1);
        n.setEnableBackgroundDrawing(false);o.a(false);n.setText("0123456789abcdef");o.a("0123456789abcdef");compare(n,o,"configured truncation");
        n.setCursorPosition(3);o.e(3);n.setSelectionPos(7);o.i(7);n.writeText("A\nB\u00a7C");o.b("A\nB\u00a7C");compare(n,o,"filtered replacement");
        n.deleteFromCursor(-2);o.b(-2);compare(n,o,"backspace");
        n.setText("one two tri");o.a("one two tri");n.deleteWords(-1);o.a(-1);compare(n,o,"word delete");
        n.setCursorPosition(-99);o.e(-99);n.setSelectionPos(999);o.i(999);n.writeText("new");o.b("new");compare(n,o,"clamped selection replacement");
        n.updateCursorCounter();o.a();n.setFocused(true);o.b(true);eq(screen.isFocused(),true,"native screen receives focus");compare(n,o,"focus reset");
        n.updateCursorCounter();o.a();n.setFocused(true);o.b(true);compare(n,o,"repeated focus retains blink");
        eq(n.textboxKeyTyped('Q',16),o.a('Q',16),"ordinary typed character consumed");compare(n,o,"typed character");
        eq(n.textboxKeyTyped('\n',16),o.a('\n',16),"invalid typed character rejected");compare(n,o,"invalid character");
        n.mouseClicked(n.x+n.width,n.y,0);o.a(n.x+n.width,n.y,0);eq(screen.isFocused(),false,"native screen loses focus");compare(n,o,"exclusive right edge");
        n.mouseClicked(n.x,n.y,1);o.a(n.x,n.y,1);compare(n,o,"right click only focuses");
        n.setCanLoseFocus(false);o.d(false);n.mouseClicked(n.x+1,n.y+1,0);o.a(n.x+1,n.y+1,0);compare(n,o,"locked focus inside");
        n.setCanLoseFocus(true);o.d(true);n.mouseClicked(n.x+6,n.y+1,0);o.a(n.x+6,n.y+1,0);compare(n,o,"mouse cursor");
        n.setEnabled(false);o.c(false);n.setVisible(false);o.e(false);compare(n,o,"disabled and hidden state");
        eq(n.textboxKeyTyped('Q',16),o.a('Q',16),"disabled typed character consumed without change");compare(n,o,"disabled key");
        n.setFocused(false);o.b(false);eq(n.textboxKeyTyped('Q',16),false,"native unfocused key ignored");eq(o.a('Q',16),false,"original unfocused key ignored");
    }
    static void callbacks(GuiTextField n)throws Exception {
        final List<String> events=new ArrayList<>();
        GuiPageButtonList.GuiResponder responder=new GuiPageButtonList.GuiResponder(){
            public void setEntryValue(int id,boolean value){throw new AssertionError("boolean callback");}
            public void setEntryValue(int id,float value){throw new AssertionError("float callback");}
            public void setEntryValue(int id,String value){events.add(id+":"+value);}
        };
        eq(field(n,"guiResponder"),null,"default responder is null");
        n.setGuiResponder(responder);n.setMaxStringLength(32);n.setText("ab");eq(events.size(),0,"programmatic setText has no callback");
        n.writeText("c");eq(events.get(0),n.getId()+":abc","write callback routes constructor id");
        n.deleteFromCursor(-1);eq(events.get(1),n.getId()+":ab","delete callback routes constructor id");
        n.setValidator(s -> !s.contains("x"));n.writeText("x");eq(events.size(),2,"rejected edit has no callback");eq(n.getText(),"ab","rejected edit retained text");
        n.setGuiResponder(null);n.writeText("z");eq(events.size(),2,"detached responder not called");
    }
    public static void main(String[] args)throws Exception {
        FontRenderer font=(FontRenderer)U.allocateInstance(FontRenderer.class);bbu legacyFont=(bbu)U.allocateInstance(bbu.class);
        int[] widths=new int[256];Arrays.fill(widths,6);put(font,FontRenderer.class,"charWidth",widths);put(legacyFont,bbu.class,"d",widths.clone());
        put(font,FontRenderer.class,"glyphWidth",new byte[65536]);put(legacyFont,bbu.class,"e",new byte[65536]);
        Minecraft mc=(Minecraft)U.allocateInstance(Minecraft.class);put(null,Minecraft.class,"instance",mc);GuiScreen screen=new GuiScreen() {};mc.currentScreen=screen;
        ConstructorFixture fixture=new ConstructorFixture();fixture.fontRenderer=font;fixture.legacyFontRenderer=legacyFont;
        int count=0;
        for(int layout=0;layout<3;layout++) {
            fixture.guiLeft=layout*41-9;fixture.guiTop=layout*23-5;fixture.width=640+layout*80;fixture.x=layout*7;fixture.y=layout*11;fixture.oX=4;fixture.oY=4;fixture.in=layout*3;
            for(int site=0;site<ConstructorFixture.COUNTS.length;site++)for(int i=0;i<ConstructorFixture.COUNTS[site];i++) {
                GuiTextField n=(GuiTextField)ConstructorFixture.class.getMethod("native"+site,int.class).invoke(fixture,i);
                bcd o=(bcd)ConstructorFixture.class.getMethod("legacy"+site,int.class).invoke(fixture,i);
                eq(n.x,field(o,"f"),"constructor x");eq(n.y,field(o,"g"),"constructor y");eq(n.width,field(o,"h"),"constructor width");eq(n.height,field(o,"i"),"constructor height");
                eq(field(n,"fontRenderer"),font,"native font identity");eq(field(o,"a"),legacyFont,"original font identity");
                eq(n.getId(),ConstructorFixture.id(site,i),"stable constructor id");lifecycle(n,o,screen);callbacks(n);count++;
            }
        }
        // Deliberately retain an engine-difference control. This click is not dispatched by
        // GUICalculator, the only scoped owner with canLoseFocus=false.
        GuiTextField locked=new GuiTextField(0,font,10,20,80,12);bcd oldLocked=new bcd(legacyFont,10,20,80,12);
        locked.setText("abc");oldLocked.a("abc");locked.setFocused(true);oldLocked.b(true);locked.setCanLoseFocus(false);oldLocked.d(false);
        locked.mouseClicked(9,19,0);oldLocked.a(9,19,0);eq(locked.getCursorPosition(),3,"native rejects out-of-bounds locked-focus click");eq(oldLocked.h(),0,"original moves cursor for out-of-bounds locked-focus click");
        // Constructor defaults also accept a null font; no replacement renderer or hidden global dependency.
        GuiTextField nullFont=new GuiTextField(-7,null,1,2,3,4);eq(nullFont.getId(),-7,"native id unconstrained");eq(field(nullFont,"fontRenderer"),null,"null font stored exactly");
        System.out.println("PASS "+checks+" actual old/native widget assertions across "+count+" production constructor instances; headless constructor/edit/focus/callback scope, not GUI rendering or whole-mod compilation.");
    }
}

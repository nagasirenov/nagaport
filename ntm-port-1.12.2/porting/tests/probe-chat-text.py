#!/usr/bin/env python3
"""Real target API probe: full ChatBuilder + extracted packet codec/message expressions.
Does not compile full packet handler, blocks, GUI or the whole mod; no Minecraft stubs.
"""
import argparse, pathlib, re, subprocess, tempfile
p=argparse.ArgumentParser(); p.add_argument('--classpath',required=True); p.add_argument('--java-home',type=pathlib.Path,required=True); a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[2]
src=root/'src/main/java/com/hbm'
def read(path): return (src/path).read_text()
packet=read('packet/toclient/PlayerInformPacket.java')
# Keep production fields, constructors and both complete codecs verbatim. Only
# change the class name, remove the unrelated client handler/imports.
packet=packet[packet.index('public class PlayerInformPacket'):packet.index('\tpublic static class Handler')]
packet=packet.replace('PlayerInformPacket','PacketCodecProbe')+'}\n'
packet='import net.minecraftforge.fml.common.network.simpleimpl.IMessage;\nimport net.minecraftforge.fml.common.network.ByteBufUtils;\nimport io.netty.buffer.ByteBuf;\nimport net.minecraft.util.text.ITextComponent;\n'+packet
pump=read('blocks/network/FluidPump.java')
message=re.search(r'player\.sendMessage\((new TextComponentString\("Changed type to "\).*?)\);',pump).group(1)
# This one parameter replaces the block's fluid-type dependency, not Minecraft.
message=message.replace('type.getConditionalName()', 'key')
wand=read('blocks/generic/BlockWandStructure.java')
click=wand[wand.index('TextComponentString fileText ='):wand.index('\n\t\t}',wand.index('TextComponentString fileText ='))]
click=click.replace('player.sendMessage(', 'return (')
fire=read('blocks/bomb/BlockFireworks.java')
charges=re.search(r'player\.sendMessage\((new TextComponentTranslation\(this.getUnlocalizedName\(\) \+ "\.charges".*?)\);',fire).group(1)
charges=charges.replace('this.getUnlocalizedName()', 'key').replace('te.charges','count')
java=r'''
import java.io.File;
import java.lang.reflect.Field;
import io.netty.buffer.*;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import com.hbm.util.ChatBuilder;
public class ChatTextProbe {
 static void check(boolean b,String s){if(!b)throw new AssertionError(s);}
 static ITextComponent pump(String key){return PUMP;}
 static ITextComponent charges(String key,int count){return CHARGES;}
 static ITextComponent saved(File file){CLICK}
 static Object field(Object p,String n)throws Exception{Field f=p.getClass().getDeclaredField(n); f.setAccessible(true);return f.get(p);}
 public static void main(String[] ignored)throws Exception {
  ITextComponent p=pump("test.gas");
  check(p.getUnformattedText().equals("Changed type to test.gas!"),"wording/sibling order");
  check(p.getStyle().getColor()==TextFormatting.YELLOW,"root yellow");
  check(p.getSiblings().size()==2,"two siblings");
  for(ITextComponent c:p.getSiblings())check(c.getStyle().getColor()==TextFormatting.YELLOW,"inherited yellow");
  ITextComponent c=charges("tile.test",17);
  check(((TextComponentTranslation)c).getKey().equals("tile.test.charges"),"translation key");
  check(((TextComponentTranslation)c).getFormatArgs()[0].equals(17),"numeric translation arg");
  check(c.getStyle().getColor()==TextFormatting.YELLOW,"translation style");
  ITextComponent s=saved(new File("/tmp/structures/probe.nbt"));
  check(s.getUnformattedText().equals("Saved structure as probe.nbt"),"saved-file wording");
  Style st=s.getSiblings().get(0).getStyle();
  check(st.getUnderlined() && st.getClickEvent().getAction()==ClickEvent.Action.OPEN_FILE,"file style/action");
  check(st.getClickEvent().getValue().equals("/tmp/structures"),"file destination");
  ITextComponent b=ChatBuilder.start("first").color(TextFormatting.GOLD).next("second").color(TextFormatting.RED).nextTranslation("missing.key",4).flush();
  check(b.getSiblings().size()==1 && b.getSiblings().get(0).getSiblings().size()==1,"builder preserves nested sibling shape");
  check(b.getSiblings().get(0).getStyle().getColor()==TextFormatting.RED,"explicit child override");
  check(b.getSiblings().get(0).getSiblings().get(0).getStyle().getColor()==TextFormatting.RED,"nested inheritance");
  for(int millis:new int[]{0,3000}) for(boolean fancy:new boolean[]{false,true}) {
   PacketCodecProbe in=fancy ? new PacketCodecProbe(p,91,millis) : new PacketCodecProbe("literal \u2622 text",91,millis);
   ByteBuf buf=Unpooled.buffer();in.toBytes(buf);
   check(buf.getInt(0)==91 && buf.getInt(4)==millis && buf.getBoolean(8)==fancy,"wire header order");
   PacketCodecProbe out=new PacketCodecProbe();out.fromBytes(buf);
   check(!buf.isReadable(),"codec consumes complete payload");
   check(field(out,"id").equals(91)&&field(out,"millis").equals(millis)&&field(out,"fancy").equals(fancy),"metadata roundtrip");
   if(fancy) check(ITextComponent.Serializer.componentToJson((ITextComponent)field(out,"component")).equals(ITextComponent.Serializer.componentToJson(p)),"text JSON roundtrip");
   else check(field(out,"dmesg").equals("literal \u2622 text"),"UTF8 plain roundtrip");
   buf.release();
  }
  check(net.minecraft.entity.player.EntityPlayer.class.getMethod("sendMessage",ITextComponent.class)!=null,"real inherited sendMessage signature");
  System.out.println("PASS: full production ChatBuilder; extracted production packet codec (4 roundtrips), pump/firework/file messages; real text styles, siblings, click events and delivery signature. No full block, packet-handler or gameplay test.");
 }
}
'''.replace('PUMP',message).replace('CHARGES',charges).replace('CLICK',click)
with tempfile.TemporaryDirectory(prefix='ntm-chat-') as tmp:
 d=pathlib.Path(tmp); (d/'PacketCodecProbe.java').write_text(packet); (d/'ChatTextProbe.java').write_text(java)
 subprocess.run([str(a.java_home/'bin/javac'),'-cp',a.classpath,'-d',tmp,str(d/'PacketCodecProbe.java'),str(d/'ChatTextProbe.java'),str(src/'util/ChatBuilder.java')],check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',tmp+':'+a.classpath,'ChatTextProbe'],check=True)

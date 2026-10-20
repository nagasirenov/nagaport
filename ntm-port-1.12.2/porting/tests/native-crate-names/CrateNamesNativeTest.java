import java.util.*;
import net.minecraft.inventory.*;
import net.minecraft.init.*;
import net.minecraft.item.*;
import net.minecraft.nbt.*;
import net.minecraft.util.text.*;
import net.minecraft.util.text.translation.LanguageMap;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.server.SPacketOpenWindow;
import io.netty.buffer.Unpooled;

/** Exact source method fixtures with real native title, translation and packet APIs. */
public final class CrateNamesNativeTest {
 static int checks;
 static void check(boolean b,String message){checks++;if(!b)throw new AssertionError(message);}
 static ITextComponent roundtrip(IInventory inventory)throws Exception {
  SPacketOpenWindow sent=new SPacketOpenWindow(7,"minecraft:container",inventory.getDisplayName(),inventory.getSizeInventory());
  PacketBuffer buffer=new PacketBuffer(Unpooled.buffer());try{
   sent.writePacketData(buffer);SPacketOpenWindow received=new SPacketOpenWindow();received.readPacketData(buffer);
   check(received.getWindowId()==7&&received.getSlotCount()==1,"native packet identity/slot count");return received.getWindowTitle();
  }finally{buffer.release();}
 }
 static void title(IInventory inv,String name,boolean custom,String rendered)throws Exception {
  check(inv.getName().equals(name),"native virtual name");check(inv.hasCustomName()==custom,"native custom predicate");
  ITextComponent component=inv.getDisplayName();check(custom?component instanceof TextComponentString:component instanceof TextComponentTranslation,"literal versus translated component");
  check(component.getUnformattedText().equals(rendered),"resolved title");
  check(roundtrip(inv).getUnformattedText().equals(rendered),"actual native window packet preserves title");
  if(!custom)check(((TextComponentTranslation)component).getKey().equals(name),"translation key preserved");
 }
 public static void main(String[] args)throws Exception {
  Bootstrap.register();Map<String,String> language=new HashMap<>();String[] keys={"crateIron","crateDesh","crateSteel","crateTungsten","crateTemplate","safe","fileCabinet","massStorage"};
  for(String key:keys)language.put("container."+key,"Localized "+key);language.put("literal.key","must not translate custom name");LanguageMap.replaceWith(language);
  CrateNameBase[] crates={new CrateIron(),new CrateDesh(),new CrateSteel(),new CrateTungsten(),new CrateTemplate(),new Safe(),new FileCabinet(),new MassStorage()};
  for(int i=0;i<crates.length;i++){
   CrateNameBase crate=crates[i];String key="container."+keys[i];
   for(String empty:new String[]{null,""}){crate.customName=empty;title(crate,key,false,"Localized "+keys[i]);}
   for(String custom:new String[]{"literal.key"," ","Δ custom §a100%"}){
    crate.customName=custom;String expected=i<6?custom:key;
    // FileCabinet/MassStorage deliberately retain original fixed-key + custom-flag quirk.
    title(crate,expected,true,expected);
   }
  }
  // A descendant overriding getName remains visible through the shared display root.
  CrateIron descendant=new CrateIron(){@Override public String getName(){return "literal.key";}};
  descendant.customName="custom flag";title(descendant,"literal.key",true,"literal.key");
  // Item-crate delegation runs the exact getter/predicate/display methods. The
  // registry-specific factory and markDirty side effects are explicit exclusions.
  for(int i=0;i<6;i++){
   ItemCrateName item=new ItemCrateName();item.target=new ItemStack(Items.STICK);item.selected=crates[i];item.selected.customName=null;
   title(item,"container."+keys[i],false,"Localized "+keys[i]);
   item.target.setStackDisplayName("literal.key");title(item,"literal.key",true,"literal.key");check(item.selected.customName.equals("literal.key"),"custom item title forwarded to tile");
   item.target.setStackDisplayName("");title(item,"container."+keys[i],true,"container."+keys[i]);check(item.selected.customName.equals(""),"empty item name retains original differing item/tile flags");
  }
  System.out.println("PASS CrateNamesNativeTest: "+checks+" assertions; eight exact descendant getters, original null/empty/custom rules, inherited virtual display, actual literal/translation components and native window-packet roundtrip. Item factory and markDirty are bounded collaborators; no whole machine or rendering claim.");
 }
}

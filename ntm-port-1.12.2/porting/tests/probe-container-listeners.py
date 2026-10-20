#!/usr/bin/env python3
"""Exercise extracted production synchronization callbacks on real Container APIs.
Not a full container/tile compile, GUI test or network serialization test.
"""
from pathlib import Path
import argparse, re, subprocess, tempfile, os
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);a=p.parse_args()
root=Path(__file__).resolve().parents[2]
base=root.parent/'ntm-original-pristine/src/main/java/com/hbm/inventory/container'
folder=root/'src/main/java/com/hbm/inventory/container'
replacements={'ICrafting':'IContainerListener','addCraftingToCrafters':'addListener','sendProgressBarUpdate':'sendWindowProperty','crafters':'listeners'}
def migrate(s):
 for old,new in replacements.items():s=re.sub(r'\b'+old+r'\b',new,s)
 return s

def method(s,name):
 m=re.search(r'public void '+name+r'\([^)]*\)\s*\{',s)
 if not m:return None
 end=m.end();depth=1
 while depth:
  depth+=(s[end]=='{')-(s[end]=='}');end+=1
 return s[m.start():end]

def norm(s):return re.sub(r'\s+','',s)
count=0
for f in folder.glob('*.java'):
 old=(base/f.name).read_text() if (base/f.name).exists() else ''
 if not re.search(r'\bICrafting\b',old):continue
 new=f.read_text();assert not re.search(r'\bICrafting\b',new),f
 for legacy,target in [('addCraftingToCrafters','addListener'),('detectAndSendChanges','detectAndSendChanges'),('updateProgressBar','updateProgressBar')]:
  before=method(old,legacy);after=method(new,target)
  assert (before is None and after is None) or norm(migrate(before))==norm(after),(f,legacy)
 count+=1
assert count==23,count
specs=[('ContainerMachineShredder','diFurnace',['progress'],['progress'],[1]),('ContainerMachineTurbofan','diFurnace',['afterburner'],['afterburner'],[1]),('ContainerMachineRTG','testNuke',['heat'],['heat'],[0]),('ContainerRtgFurnace','diFurnace',['dualCookTime'],['dualCookTime'],[0]),('ContainerMachineTeleporter','diFurnace',['targetX','targetY','targetZ'],['x','y','z'],[1,2,3])]
classes=[]
for name,tile,fields,cached,ids in specs:
 source=(folder/(name+'.java')).read_text()
 callbacks='\n'.join(method(source,m) for m in ['addListener','detectAndSendChanges','updateProgressBar'])
 setters=' '.join('case %d: %s.%s=value; break;'%(i,tile,f) for i,f in enumerate(fields))
 getters=' '.join('case %d: return %s.%s;'%(i,tile,f) for i,f in enumerate(fields))
 classes.append('''static final class %s extends CheckedContainer {
 static final class Data { %s }
 private final Data %s = new Data();
 %s
 %s
 void set(int index,int value) { switch(index) { %s default: throw new AssertionError(); } }
 int get(int index) { switch(index) { %s default: throw new AssertionError(); } }
 int[] ids() { return new int[] {%s}; }
}'''%(name,' '.join('int '+f+';' for f in fields),tile,' '.join('private int '+f+';' for f in cached),callbacks,setters,getters,','.join(map(str,ids))))
java='''import java.util.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
public final class ContainerListenerProbe {
 static void check(boolean b,String msg) { if(!b) throw new AssertionError(msg); }
 static abstract class CheckedContainer extends Container {
  abstract void set(int index,int value); abstract int get(int index); abstract int[] ids();
  public boolean canInteractWith(EntityPlayer player) { return true; }
 }
 static final class Listener implements IContainerListener {
  int fullContents, updates; Map<Integer,Integer> values=new HashMap<>();
  public void sendAllContents(Container c,NonNullList<ItemStack> stacks) { fullContents++; check(stacks.isEmpty(),"zero-slot fixture"); }
  public void sendSlotContents(Container c,int slot,ItemStack stack) { throw new AssertionError("unexpected slot update"); }
  public void sendWindowProperty(Container c,int id,int value) { updates++; values.put(id,value); }
  public void sendAllWindowProperties(Container c,IInventory inv) { throw new AssertionError("unexpected whole-inventory properties"); }
 }
 %s
 public static void main(String[] args) {
  CheckedContainer[] containers={%s};
  for(CheckedContainer c:containers) {
   int[] ids=c.ids(); Listener first=new Listener(), second=new Listener();
   for(int i=0;i<ids.length;i++) c.set(i,10+i);
   c.addListener(first); c.addListener(second);
   check(first.fullContents==1 && second.fullContents==1,"initial inventory broadcast");
   for(int i=0;i<ids.length;i++) {
    check(first.values.get(ids[i])==10+i && second.values.get(ids[i])==10+i,"initial properties to both listeners");
   }
   int oldFirst=first.updates,oldSecond=second.updates;
   c.detectAndSendChanges();
   check(first.updates==oldFirst && second.updates==oldSecond,"no unchanged property updates");
   for(int value:new int[] {-32768,-1,0,32767,32768}) {
    for(int i=0;i<ids.length;i++) c.set(i,value+i);
    oldFirst=first.updates;oldSecond=second.updates;c.detectAndSendChanges();
    check(first.updates-oldFirst==ids.length && second.updates-oldSecond==ids.length,"all changed properties once per listener");
    for(int i=0;i<ids.length;i++) check(first.values.get(ids[i])==value+i && second.values.get(ids[i])==value+i,"changed property ids and values");
   }
   c.removeListener(first); oldFirst=first.updates;
   for(int i=0;i<ids.length;i++) c.set(i,100+i);
   c.detectAndSendChanges();check(first.updates==oldFirst,"removed listener stops receiving");
   Listener reopened=new Listener();c.addListener(reopened);
   for(int i=0;i<ids.length;i++) check(reopened.values.get(ids[i])==100+i,"reopened listener receives current values");
   for(int i=0;i<ids.length;i++) { c.updateProgressBar(ids[i],-100-i);check(c.get(i)==-100-i,"client property callback"); }
   c.updateProgressBar(999,42);
   for(int i=0;i<ids.length;i++) check(c.get(i)==-100-i,"unknown property ignored");
  }
  System.out.println("PASS: extracted production callbacks for five containers/seven properties on real Container/IContainerListener; initial sync, two listeners, unchanged suppression, changed values, remove/reopen and client update dispatch.");
  System.out.println("SCOPE: tile fields are explicit test data; complete containers/tiles, GUI lifecycle and wire-width serialization are NOT tested.");
 }
}'''%('\n'.join(classes),','.join('new '+s[0]+'()' for s in specs))
with tempfile.TemporaryDirectory(prefix='hbm-container-listener-') as tmp:
 file=Path(tmp)/'ContainerListenerProbe.java';file.write_text(java)
 subprocess.run([str(a.java_home/'bin/javac'),'-cp',a.classpath,'-d',tmp,str(file)],check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',tmp+os.pathsep+a.classpath,'ContainerListenerProbe'],check=True,cwd=tmp)
print('PASS: all 23 legacy listener consumers and synchronization callback bodies match pristine after only four API-name replacements.')

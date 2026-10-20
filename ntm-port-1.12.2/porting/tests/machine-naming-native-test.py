#!/usr/bin/env python3
"""Execute extracted production naming methods against real 1.12.2 IInventory/text components.
Unrelated storage/lifecycle uses native InventoryBasic fixture context; NOT whole-machine compilation.
All 161 production default hooks are compiled, including original null/config branches.
"""
import argparse
import importlib.util
import json
import os
from pathlib import Path
import re
import subprocess
import tempfile
ROOT = Path(__file__).resolve().parents[2]
spec=importlib.util.spec_from_file_location('names',ROOT/'porting/tests/check-machine-naming-source.py')
names=importlib.util.module_from_spec(spec);spec.loader.exec_module(names)

def method(source, name):
    match=re.search(r'public\s+(?:[\w.]+)\s+'+name+r'\s*\([^)]*\)\s*\{', source)
    assert match, name
    start=match.start();brace=source.index('{',start);end=brace+1;depth=1
    while depth:
        depth+=(source[end]=='{')-(source[end]=='}');end+=1
    return source[start:end]
def source(path): return (ROOT/path).read_bytes().decode('utf-8')
def selected(path, methods): return '\n'.join(method(source(path),m) for m in methods)
parser=argparse.ArgumentParser(description=__doc__)
parser.add_argument('--java-home',type=Path,required=True)
parser.add_argument('--classpath',required=True)
args=parser.parse_args()
names.main()
base='src/main/java/com/hbm/tileentity/TileEntityMachineBase.java'
parts=['''import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.util.text.*;
import java.util.Objects;
public class MachineNamingNativeTest {
static int checks;
static void eq(Object a,Object b){checks++;if(!Objects.equals(a,b))throw new AssertionError("Expected "+b+", got "+a);}
static void yes(boolean b){checks++;if(!b)throw new AssertionError();}
static abstract class Base extends InventoryBasic {
private String customName;
int dirty;
Base(){super("fixture",false,0);}
@Override public void markDirty(){dirty++;}
public abstract String getDefaultName();
'''+selected(base,['getName','hasCustomName','getDisplayName','setCustomName','getInventoryName','hasCustomInventoryName'])+'\n}\n']
checks=[]
for i,p in enumerate(names.MANIFEST['descendant_default_paths']):
    src=source(p); hook=method(src,'getDefaultName'); old=names.reverse_source(p,src)
    oldhook=method(old,'getName')
    assert hook.replace('getDefaultName','getName',1)==oldhook, p
    extra=''
    if 'config != null' in hook: extra='static class Config {String localizedName;} Config config;'
    parts.append('static class Hook'+str(i)+' extends Base { '+extra+hook+' }')
    if 'config != null' in hook:
        checks.append('Hook%d c=new Hook%d(); exercise(c,"INVALID"); c.config=new Hook%d.Config(); c.config.localizedName="configured.name"; exercise(c,"configured.name"); c.config.localizedName=null; exercise(c,null);'%(i,i,i))
    else:
        expected=re.search(r'return\s+("[^"\n]*"|null)\s*;',oldhook)[1]
        checks.append('exercise(new Hook%d(),%s);'%(i,expected))
for short,tail,field in [('Battery','machine/storage/TileEntityMachineBattery.java','customName'),('RTG','machine/TileEntityDiFurnaceRTG.java','name')]:
    p='src/main/java/com/hbm/tileentity/'+tail
    parts.append('static class '+short+' extends Base { private String '+field+';'+selected(p,['getDefaultName','getName','hasCustomName','setCustomName'])+'}')
# The real FENSU inherits Battery naming without overriding it.
fensu=source('src/main/java/com/hbm/tileentity/machine/storage/TileEntityMachineFENSU.java')
assert 'extends TileEntityMachineBattery' in fensu and not re.search(r'public\s+(?:String|boolean)\s+(?:getName|getDefaultName|hasCustomName|getInventoryName|hasCustomInventoryName)\s*\(',fensu)
parts.append('static class FENSU extends Battery {}')
parts.append('''static void exercise(Base b,String expected){
IInventory inv=b;
eq(inv.getName(),expected);yes(!inv.hasCustomName());eq(b.getInventoryName(),expected);yes(!b.hasCustomInventoryName());
ITextComponent component=inv.getDisplayName();yes(component instanceof TextComponentTranslation);eq(((TextComponentTranslation)component).getKey(),expected);
for(String value:new String[]{"Custom name", "   ", "§a名前"}){
int dirty=b.dirty;b.setCustomName(value);eq(b.dirty,dirty+1);yes(inv.hasCustomName());eq(inv.getName(),value);eq(b.getInventoryName(),value);yes(b.hasCustomInventoryName());
component=inv.getDisplayName();yes(component instanceof TextComponentString);eq(component.getUnformattedText(),value);
}
for(String value:new String[]{"",null}){int dirty=b.dirty;b.setCustomName(value);eq(b.dirty,dirty+1);yes(!inv.hasCustomName());eq(inv.getName(),expected);yes(inv.getDisplayName() instanceof TextComponentTranslation);}
}
public static void main(String[] args)throws Exception {
'''+ '\n'.join(checks)+'''
exercise(new Battery(),"container.battery");exercise(new RTG(),"container.diFurnaceRTG");
exercise(new FENSU(),"container.battery");
exercise(new Battery(){@Override public String getDefaultName(){return "subclass.battery";}},"subclass.battery");
exercise(new RTG(){@Override public String getDefaultName(){return "unused.subclass.rtg";}},"container.diFurnaceRTG");
// Owners have independent fields. Setting the root field must not change either child's naming.
java.lang.reflect.Field root=Base.class.getDeclaredField("customName");root.setAccessible(true);
for(Base b:new Base[]{new Battery(),new RTG()}){root.set(b,"root NBT value");yes(!b.hasCustomName());b.setCustomName("owned");eq(root.get(b),"root NBT value");eq(b.getName(),"owned");b.setCustomName(null);yes(!b.hasCustomName());}
System.out.println("PASS: "+checks+" native naming assertions; 161 exact extracted hooks, config/null/empty/custom/clear/owner/subclass/display dispatch. NOT full machines/gameplay.");
}}
''')
with tempfile.TemporaryDirectory(prefix='hbm-machine-naming-') as temp:
    out=Path(temp);test=out/'MachineNamingNativeTest.java';test.write_text('\n'.join(parts),encoding='utf-8')
    exe='.exe' if os.name=='nt' else ''
    subprocess.run([str(args.java_home/'bin'/('javac'+exe)),'-source','8','-target','8','-encoding','UTF-8','-cp',args.classpath,'-d',temp,str(test)],check=True)
    subprocess.run([str(args.java_home/'bin'/('java'+exe)),'-cp',os.pathsep.join([temp,args.classpath]),'MachineNamingNativeTest'],cwd=temp,check=True)

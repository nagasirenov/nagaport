#!/usr/bin/env python3
"""Native/1.7 full-method fixtures with genuine Block/Item registries and dispatch.
Only two complete production owner classes are claimed; other fixtures are bounded.
"""
import argparse,hashlib,json,re,subprocess,tempfile,zipfile,importlib.util
from pathlib import Path
H=Path(__file__).resolve().parent;M=H.parents[2];S=M/'src/main/java'
spec=importlib.util.spec_from_file_location('proof',H/'source-proof.py');proof=importlib.util.module_from_spec(spec);spec.loader.exec_module(proof)

def generate(t):
 d=json.loads((H/'selection.json').read_text());originals=json.loads((H/'original-methods.json').read_text());native=[];legacy=[];allparts=[];cases=[]
 def out(path,body,old=False):
  p=t/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(body);(legacy if old else native).append(p)
 normalize=lambda s:re.sub(r'\b(Items|Blocks)\.(\w+)',lambda m:m[1]+'.'+m[2].upper(),s)
 def to_old(s):
  s=s.replace('Item.getItemFromBlock(', 'adb.a(').replace('getItemDropped(', 'a(').replace('quantityDroppedWithBonus(', 'a(').replace('quantityDropped(', 'a(')
  for old,new in [('ModBlocks','LegacyModBlocks'),('ModItems','LegacyModItems'),('Items','LegacyItems'),('Blocks','LegacyBlocks'),('Item','adb'),('Block','aji')]:s=re.sub(r'\b'+old+r'\b',new,s)
  return s
 with zipfile.ZipFile(H/'baseline.zip') as z:
  outgas=z.read('com/hbm/blocks/generic/BlockOutgas.java').decode();oldq=proof.method(outgas,'public int quantityDroppedWithBonus(');newq=proof.method((S/'com/hbm/blocks/generic/BlockOutgas.java').read_text(),'public int quantityDroppedWithBonus(')
  out('NativeOutgasSlice.java','import java.util.Random;import net.minecraft.block.Block;import net.minecraft.block.material.Material;import net.minecraft.item.Item;public class NativeOutgasSlice extends Block {public NativeOutgasSlice(){super(Material.ROCK);}\n@Override '+newq+'\n}')
  out('LegacyOutgasSlice.java','import java.util.Random;public class LegacyOutgasSlice extends aji {public LegacyOutgasSlice(){super(awt.e);}\n@Override '+to_old(oldq)+'\n}',True)
  for i,e in enumerate(d['selected']):
   cur=(S/e['path']).read_text();before=z.read(e['path']).decode();nparts=[proof.method(cur,'public Item getItemDropped(')];oparts=[normalize(originals[e['path']])]
   if e['class'] in ['TritiumLamp','ReinforcedLamp']:
    helper=proof.method(before,'protected Block getOff(');nparts.append(helper);oparts.append(helper)
   if e['class'] in ['BlockSellafieldOre','BlockStepEffect']:
    for sig in ['public int quantityDropped(Random','public int quantityDroppedWithBonus(']:nparts.append(proof.method(cur,sig));oparts.append(proof.method(before,sig))
   fields='public Block single; public Block drops; public boolean allowFortune=true;'
   parent='NativeOutgasSlice' if e['class']=='BlockNetherCoal' else 'Block';oldparent='LegacyOutgasSlice' if e['class']=='BlockNetherCoal' else 'aji'
   ctor='' if parent=='NativeOutgasSlice' else 'super(Material.ROCK);';oldctor='' if oldparent=='LegacyOutgasSlice' else 'super(awt.e);'
   name='NativeCase'+str(i);oldname='LegacyCase'+str(i);nb='\n'.join(nparts);ob='\n'.join(oparts);allparts+=nparts+oparts
   out(name+'.java','import java.util.Random;import net.minecraft.block.Block;import net.minecraft.block.material.Material;import net.minecraft.item.Item;import net.minecraft.init.Items;import net.minecraft.init.Blocks;public class '+name+' extends '+parent+' {public '+name+'(){'+ctor+'}'+fields+'\n@Override '+nb+'\n}')
   out(oldname+'.java','import java.util.Random;public class '+oldname+' extends '+oldparent+' {public '+oldname+'(){'+oldctor+'}'+to_old(fields)+'\n@Override '+to_old(ob)+'\n}',True)
   cases.append({'native':name,'legacy':oldname,'owner':e['class'],'identity_fields':sorted(set(re.findall(r'ModBlocks\.(\w+)',nb))),'quantity':e['class'] in ['BlockStepEffect','BlockSellafieldOre','BlockNetherCoal']})
 alltext='\n'.join(allparts);blocks=sorted(set(re.findall(r'ModBlocks\.(\w+)',alltext)));items=sorted(set(re.findall(r'ModItems\.(\w+)',alltext)));vanillai=sorted(set(re.findall(r'\bItems\.(\w+)',alltext)));vanillab=sorted(set(re.findall(r'\bBlocks\.(\w+)',alltext)))
 for cls,typ,names in [('ModBlocks','net.minecraft.block.Block',blocks),('ModItems','net.minecraft.item.Item',items)]:out(cls+'.java','public class '+cls+'{public static '+typ+' '+','.join(names)+';}')
 for cls,typ,names in [('LegacyModBlocks','aji',blocks),('LegacyModItems','adb',items),('LegacyItems','adb',vanillai),('LegacyBlocks','aji',vanillab)]:out(cls+'.java','public class '+cls+'{public static '+typ+' '+','.join(names)+';}',True)
 out('LegacyTarget.java','public class LegacyTarget extends aji {public LegacyTarget(){super(awt.e);}}',True)
 initializers='\n'.join('runCase('+str(i)+',"'+c['owner']+'",new String[]{'+','.join('"'+n+'"' for n in c['identity_fields'])+'});' for i,c in enumerate(cases))
 out('DropNativeTest.java',(H/'DropNativeTest.java.in').read_text().replace('RUN_CASES',initializers))
 production=['com/hbm/blocks/generic/BlockNoDrop.java','com/hbm/blocks/generic/BlockGenericDrop.java','com/hbm/blocks/generic/BlockGeneric.java','com/hbm/blocks/BlockBase.java','com/hbm/lib/RefStrings.java','com/hbm/render/icon/IIcon.java','com/hbm/render/icon/IIconRegister.java']
 native += [S/p for p in production]
 return native,legacy,cases

def main():
 p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-client',type=Path,required=True);p.add_argument('--keep',type=Path);p.add_argument('--mutants',action='store_true');a=p.parse_args();proof.audit();assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce';cp=a.classpath_file.read_text().strip()
 with tempfile.TemporaryDirectory(prefix='native-drops-') as td:
  t=a.keep or Path(td);t.mkdir(exist_ok=True,parents=True);n,o,cases=generate(t);old=t/'old-classes';new=t/'native-classes';old.mkdir(exist_ok=True);new.mkdir(exist_ok=True)
  subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-sourcepath',str(t),'-cp',str(a.legacy_client)+':'+cp,'-d',str(old)]+list(map(str,o)),check=True)
  subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-sourcepath',str(t),'-cp',cp,'-d',str(new)]+list(map(str,n)),check=True)
  subprocess.run([str(a.java_home/'bin/java'),'-Xmx400m','-cp',str(new)+':'+cp,'DropNativeTest',str(a.legacy_client),str(old)],check=True,cwd=t)
  dis=subprocess.check_output([str(a.java_home/'bin/javap'),'-p','-c','-classpath',str(new)]+['NativeCase'+str(i) for i in range(83)]+['NativeOutgasSlice','com.hbm.blocks.generic.BlockNoDrop','com.hbm.blocks.generic.BlockGenericDrop'],text=True)
  (H/'native-attribution.log').write_text(dis)
  if a.mutants:
   case={c['owner']:'NativeCase'+str(i)+'.java' for i,c in enumerate(cases)}
   mutations=[(case['BlockStepEffect'],'rand.nextInt(10)','rand.nextInt(5)','changed random bound'),('NativeOutgasSlice.java','this.getItemDropped(getStateFromMeta(0), rand, fortune)','super.getItemDropped(getStateFromMeta(0), rand, fortune)','bypassed virtual dispatch'),('NativeOutgasSlice.java','public int quantityDroppedWithBonus(int fortune, Random rand) {','public int quantityDroppedWithBonus(int fortune, Random rand) { this.getItemDropped(getStateFromMeta(0), rand, fortune);','duplicate virtual dispatch'),(case['BlockNoDrop'],'return null;','return Item.getItemFromBlock(this);','lost null return')]
   for file,before,after,label in mutations:
    path=t/file;original=path.read_text();assert original.count(before)==1;path.write_text(original.replace(before,after))
    subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-sourcepath',str(t),'-cp',str(new)+':'+cp,'-d',str(new),str(path)],check=True,capture_output=True)
    result=subprocess.run([str(a.java_home/'bin/java'),'-Xmx400m','-cp',str(new)+':'+cp,'DropNativeTest',str(a.legacy_client),str(old)],cwd=t,capture_output=True,text=True)
    assert result.returncode!=0 and 'java.lang.AssertionError:' in result.stderr,(label,result.stdout,result.stderr)
    print('PASS runtime mutant rejected:',label,next(x for x in result.stderr.splitlines() if 'AssertionError:' in x))
    path.write_text(original);subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-sourcepath',str(t),'-cp',str(new)+':'+cp,'-d',str(new),str(path)],check=True,capture_output=True)
   path=t/case['BlockNoDrop'];original=path.read_text();path.write_text(original.replace('net.minecraft.block.state.IBlockState','int'));result=subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-sourcepath',str(t),'-cp',str(new)+':'+cp,'-d',str(new),str(path)],capture_output=True,text=True);assert result.returncode!=0 and 'does not override' in result.stderr;path.write_text(original);print('PASS attribution mutant rejected: legacy scalar callback is not a native override')
if __name__=='__main__':main()

#!/usr/bin/env python3
"""Full callback slices, not complete owner compilation. Real engine/World/Chunk API."""
import argparse,hashlib,json,re,subprocess,tempfile
from pathlib import Path
H=Path(__file__).resolve().parent; M=H.parents[2]; S=M/'src/main/java'; D=json.loads((H/'selection.json').read_text())
def lex(s):return re.sub(r'//[^\r\n]*|/\*.*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'',lambda m:''.join('\n' if c=='\n' else ' ' for c in m[0]),s,flags=re.S)
def method(s,sig):
 a=s.index(sig);code=lex(s);i=code.index('{',a)+1;n=1
 while n:n+=(code[i]=='{')-(code[i]=='}');i+=1
 return s[a:i]
def audit():
 owners={};calls=[];selected={'com.hbm.blocks.machine.'+e['name'] for e in D['entries']}
 for p in S.rglob('*.java'):
  s=p.read_text();code=lex(s);pkg=re.search(r'package\s+([\w.]+)',code)
  if not pkg:continue
  imports={x[1].split('.')[-1]:x[1] for x in re.finditer(r'import\s+([\w.]+)\s*;',code)}
  depths=[];dep=0
  for c in code:depths.append(dep);dep+=(c=='{')-(c=='}')
  for x in re.finditer(r'\bclass\s+(\w+)\s*(?:extends\s+([\w.]+))?',code):
   if depths[x.start()]!=0:continue
   parent=x[2];owners[pkg[1]+'.'+x[1]]=None if not parent else parent if '.' in parent else imports.get(parent,pkg[1]+'.'+parent)
  for x in re.finditer(r'\bbreakBlock\s*\(',code):
   start=code.rfind('\n',0,x.start())+1;end=code.find('\n',x.start());calls.append({'path':str(p.relative_to(S)),'line':code.count('\n',0,x.start())+1,'scope_depth':depths[x.start()],'text':s[start:end].strip()})
  for name in selected:
   short=name.split('.')[-1]
   assert not re.search(r'new\s+'+short+r'\s*\([^;]*?\)\s*\{',code),('anonymous descendant',p,short)
 for owner,parent in owners.items():
  visited=set()
  while parent in owners:
   assert parent not in visited;visited.add(parent)
   assert parent not in selected,('unaudited descendant',owner,parent)
   parent=owners[parent]
 for e in D['entries']:
  p=S/e['path'];current=p.read_text();assert hashlib.sha256(current.encode()).hexdigest()==e['after_sha256'];assert current.count(e['new'])==1
  inverse=current.replace(e['new'],e['old']);assert hashlib.sha256(inverse.encode()).hexdigest()==e['before_sha256'];assert method(current,'public void breakBlock(')==e['new']
  assert owners['com.hbm.blocks.machine.'+e['name']]=='net.minecraft.block.BlockContainer'
  # Metadata was used solely in the old super call. Never synthesize metadata.
  body=e['old'].split('{',1)[1];w,x,y,z,b,meta=e['params'];body=body.replace('super.breakBlock('+', '.join(e['params'])+');','');assert not re.search(r'\b'+meta+r'\b',lex(body))
  assert 'getMetaFromState' not in e['new'];assert 'state.getBlock()' in e['new'];assert e['new'].count('super.breakBlock(')==1
 (H/'scope-audit.json').write_text(json.dumps({'base':D['base'],'selected':sorted(selected),'descendants':[],'callback_occurrences':calls},indent=2)+'\n')
 print('PASS six whole-file inverse hashes; unused metadata; exact direct native parent; no named or anonymous descendants; callback receiver/scope inventory recorded')
def main():
 p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-client',type=Path,required=True);p.add_argument('--keep',type=Path);p.add_argument('--mutants',action='store_true');a=p.parse_args();audit();assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()==json.loads((H/'original-audit.json').read_text())['legacy_client_sha256'];cp=a.classpath_file.read_text().strip()
 with tempfile.TemporaryDirectory(prefix='removal-native-') as td:
  t=a.keep or Path(td);t.mkdir(exist_ok=True,parents=True);out=t/'classes';out.mkdir(exist_ok=True);files=[]
  imports='import java.util.*;import net.minecraft.block.*;import net.minecraft.block.material.Material;import net.minecraft.block.state.IBlockState;import net.minecraft.util.math.BlockPos;import net.minecraft.world.World;import net.minecraft.tileentity.TileEntity;import net.minecraft.inventory.*;import net.minecraft.item.*;import net.minecraft.entity.item.EntityItem;import net.minecraft.nbt.NBTTagCompound;'
  for i,e in enumerate(D['entries']):
   f=t/('RemovalCase'+str(i)+'.java');f.write_text(imports+'public class RemovalCase'+str(i)+' extends BlockContainer { public static boolean keepInventory; public Random field_149933_a=new Random(0); public RemovalCase'+str(i)+'(){super(Material.IRON);} public TileEntity createNewTileEntity(World world,int meta){return new RemovalNativeTest.InventoryTile();}\n@Override '+method((S/e['path']).read_text(),'public void breakBlock(')+'\n}');files.append(f)
  helper=method((S/'com/hbm/util/ItemStackUtil.java').read_text(),'public static void spillItems(');assert hashlib.sha256(helper.encode()).hexdigest()==json.loads((H/'original-audit.json').read_text())['production_spill_helper_sha256'];f=t/'ProductionSpill.java';f.write_text(imports+'public class ProductionSpill {'+helper+'}');files.append(f)
  f=t/'RemovalNativeTest.java';f.write_text((H/'RemovalNativeTest.java.in').read_text().replace('CASES',','.join('new RemovalCase'+str(i)+'()' for i in range(6))));files.append(f)
  subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-sourcepath',str(t),'-cp',cp,'-d',str(out)]+list(map(str,files)),check=True)
  subprocess.run([str(a.java_home/'bin/java'),'-Xmx400m','-cp',str(out)+':'+cp,'RemovalNativeTest'],check=True,cwd=t)
  if a.mutants:
   f=t/'RemovalCase0.java';original=f.read_text()
   for old,new,label in [
    ('super.breakBlock(world, pos, state);','', 'lost super removal'),
    ('Block block = state.getBlock();','Block block = world.getBlockState(pos).getBlock();','new owner instead of old owner'),
    ('if(!(te instanceof ISidedInventory)) return;','', 'lost wrong-tile early return'),
    ('world.spawnEntity(entityitem);','world.spawnEntity(entityitem); world.spawnEntity(entityitem);','duplicate inventory drops')]:
    assert original.count(old)==1;f.write_text(original.replace(old,new))
    subprocess.run([str(a.java_home/'bin/javac'),'-cp',str(out)+':'+cp,'-d',str(out),str(f)],check=True,capture_output=True)
    result=subprocess.run([str(a.java_home/'bin/java'),'-Xmx400m','-cp',str(out)+':'+cp,'RemovalNativeTest'],cwd=t,capture_output=True,text=True)
    assert result.returncode!=0,(label,result.stdout);print('PASS rejected runtime mutant:',label)
   f.write_text(original);subprocess.run([str(a.java_home/'bin/javac'),'-cp',str(out)+':'+cp,'-d',str(out),str(f)],check=True,capture_output=True)
  dis=subprocess.check_output([str(a.java_home/'bin/javap'),'-p','-c','-classpath',str(out)+':'+cp]+['RemovalCase'+str(i) for i in range(6)]+['net.minecraft.block.BlockContainer'],text=True);(H/'native-attribution.log').write_text(dis)
  old=subprocess.check_output([str(a.java_home/'bin/javap'),'-p','-c','-classpath',str(a.legacy_client),'ajc'],text=True);(H/'legacy-container-attribution.log').write_text(old)
  oldchunk=subprocess.check_output([str(a.java_home/'bin/javap'),'-p','-c','-classpath',str(a.legacy_client),'apx'],text=True);idx=oldchunk.index('Method aji.a:(Lahb;IIILaji;I)V');(H/'legacy-chunk-attribution.log').write_text(oldchunk[oldchunk.rfind('\n',0,idx-1000)+1:oldchunk.find('\n',idx+500)])
  assert 'Method aj' in old and 'ahb.p:(III)V' in old
  chunk=subprocess.check_output([str(a.java_home/'bin/javap'),'-p','-c','-classpath',cp,'net.minecraft.world.chunk.Chunk'],text=True);idx=chunk.index('Method net/minecraft/block/Block.breakBlock:');(H/'native-chunk-attribution.log').write_text(chunk[chunk.rfind('\n',0,idx-1100)+1:chunk.find('\n',idx+2500)]);assert 'invokevirtual' in chunk[idx-60:idx]
if __name__=='__main__':main()

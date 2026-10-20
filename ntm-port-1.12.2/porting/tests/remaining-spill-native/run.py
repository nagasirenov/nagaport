import argparse,hashlib,json,re,subprocess,tempfile
from pathlib import Path
H=Path(__file__).resolve().parent;S=H.parents[2]/'src/main/java';D=json.loads((H/'selection.json').read_text())
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--mutants',action='store_true');a=p.parse_args();cp=a.classpath_file.read_text().strip()
for e in D['entries']:
 raw=(S/e['path']).read_bytes();assert hashlib.sha256(raw).hexdigest()==e['after_sha256'];s=raw.decode();assert s.count(e['new'])==1;assert hashlib.sha256(s.replace(e['new'],e['old']).encode()).hexdigest()==e['before_sha256'];assert 'extends BlockContainer' in s
for e in D['entries']:
 assert re.sub(r'getTileEntity\(new net.minecraft.util.math.BlockPos\((.*?)\)\)',r'getTileEntity(\1)',e['old'],flags=re.S)==e['original_method']
 assert hashlib.sha256(e['original_method'].encode()).hexdigest()==e['original_method_sha256']
print('PASS twelve whole-file inverses, original method parity, preserved non-callback bytes')
with tempfile.TemporaryDirectory(prefix='remaining-spill-') as td:
 t=Path(td);out=t/'classes';out.mkdir();files=[]
 imports='import java.util.*;import net.minecraft.block.*;import net.minecraft.block.material.Material;import net.minecraft.block.state.IBlockState;import net.minecraft.util.math.BlockPos;import net.minecraft.world.World;import net.minecraft.tileentity.TileEntity;import net.minecraft.inventory.*;import net.minecraft.item.*;import net.minecraft.entity.item.EntityItem;import net.minecraft.nbt.NBTTagCompound;'
 for i,e in enumerate(D['entries']):
  m=e['new'];m=re.sub(r'\bTileEntity(?:Nuke\w+|BombMulti|MachineShredder)\b','RemovalNativeTest.InventoryTile',m);m=m.replace(e['name']+'.field_149933_a','RemovalCase'+str(i)+'.field_149933_a')
  f=t/('RemovalCase'+str(i)+'.java');f.write_text(imports+'public class RemovalCase'+str(i)+' extends BlockContainer { public static boolean keepInventory; public static Random field_149933_a=new Random(0); public RemovalCase'+str(i)+'(){super(Material.IRON);} public TileEntity createNewTileEntity(World world,int meta){return new RemovalNativeTest.InventoryTile();}\n@Override '+m+'\n}');files.append(f)
 f=t/'RemovalNativeTest.java';f.write_text((H/'RemovalNativeTest.java.in').read_text().replace('CASES',','.join('new RemovalCase'+str(i)+'()' for i in range(12))));files += [f,H/'OriginalSpillReference.java']
 javac=str(a.java_home/'bin/javac');java=str(a.java_home/'bin/java')
 subprocess.run([javac,'-encoding','UTF-8','-cp',cp,'-d',str(out)]+list(map(str,files)),check=True)
 def run():return subprocess.run([java,'-Xmx300m','-cp',str(out)+':'+cp,'RemovalNativeTest'],cwd=t,capture_output=True,text=True)
 result=run();print(result.stdout);print(result.stderr);assert result.returncode==0
 if a.mutants:
  f=files[0];original=f.read_text()
  for old,new,label in [
   ('new ItemStack(spillItem, j1, spillMeta)','new ItemStack(itemstack.getItem(), j1, itemstack.getItemDamage())','identity read after depletion'),
   ('spillTag.copy()','spillTag','NBT alias instead of deep copy'),
   ('if(itemstack != null)','if(itemstack != null && !itemstack.isEmpty())','lost empty-stack RNG draws'),
   ('super.breakBlock(p_149749_1_, nativePos, nativeState);','','lost real super removal'),
   ('spawnEntity(entityitem);','spawnEntity(entityitem); p_149749_1_.spawnEntity(entityitem);','duplicate drop')]:
   assert original.count(old)==1,(old,original.count(old));f.write_text(original.replace(old,new));subprocess.run([javac,'-cp',str(out)+':'+cp,'-d',str(out),str(f)],check=True,capture_output=True);r=run();assert r.returncode!=0,label;print('PASS rejected runtime mutant:',label)

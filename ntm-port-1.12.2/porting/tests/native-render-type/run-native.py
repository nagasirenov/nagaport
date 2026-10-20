import argparse,json,subprocess,tempfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--root',type=Path,required=True);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-jar',type=Path,required=True);p.add_argument('--mutant',choices=['old-signature','model','state-dependent']);a=p.parse_args();E=Path(__file__).resolve().parent;cp=a.classpath_file.read_text().strip();rows=json.loads((E/'sites.json').read_text())
with tempfile.TemporaryDirectory() as d:
 t=Path(d);bank=[]
 for i,r in enumerate(rows):
  s=(a.root/r['path']).read_bytes().decode();assert s.count(r['after'])==1;method=r['after']
  if a.mutant=='old-signature':method=method.replace('net.minecraft.util.EnumBlockRenderType getRenderType(net.minecraft.block.state.IBlockState state)','int getRenderType()').replace('net.minecraft.util.EnumBlockRenderType.INVISIBLE','-1')
  if a.mutant=='model':method=method.replace('EnumBlockRenderType.INVISIBLE','EnumBlockRenderType.MODEL')
  if a.mutant=='state-dependent':method=method.replace('return net.minecraft.util.EnumBlockRenderType.INVISIBLE;','return state.getValue(META)==0 ? net.minecraft.util.EnumBlockRenderType.INVISIBLE : net.minecraft.util.EnumBlockRenderType.MODEL;')
  bank.append('static class C%d extends Base { @Override %s }'%(i,method))
 (t/'CallbackBank.java').write_text('import net.minecraft.block.*;import net.minecraft.block.state.*;import net.minecraft.block.properties.*;import net.minecraft.block.material.*;public class CallbackBank { static final PropertyInteger META=PropertyInteger.create("meta",0,15); static class Base extends Block { Base(){super(Material.ROCK);} protected BlockStateContainer createBlockState(){return new BlockStateContainer(this,META);} }'+''.join(bank)+'static Block[] blocks(){return new Block[]{'+','.join('new C%d(),new C%d(){}'%(i,i) for i in range(len(rows)))+'};}}')
 (t/'OldRenderTypeFixture.java').write_text('public class OldRenderTypeFixture extends aji { public OldRenderTypeFixture(){super(awt.e);} public int b(){return -1;} }')
 subprocess.run([str(a.java_home/'bin/javac'),'-cp',str(a.legacy_jar),'-d',d,str(t/'OldRenderTypeFixture.java')],check=True)
 subprocess.run([str(a.java_home/'bin/javac'),'-cp',cp,'-sourcepath',str(t/'empty'),'-d',d,str(t/'CallbackBank.java'),str(E/'RenderTypeNativeTest.java'),str(E/'ForgeEventNativeLauncher.java'),str(E/'LegacyFixtureRelocator.java')],check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',d+':'+cp,'LegacyFixtureRelocator',d],check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-cp',d+':'+cp,'ForgeEventNativeLauncher','RenderTypeNativeTest',str(a.legacy_jar),d],cwd=d,check=True)

#!/usr/bin/env python3
import importlib.util,subprocess,tempfile,os,zipfile,sys
from pathlib import Path
H=Path(__file__).resolve().parent;R=H.parents[2];S=R/'src/main/java';J=Path('/workspace/shared/nagaport-toolchain/java8/bin');CP=Path('/tmp/nagaport-ae2-native-classpath.txt').read_text().strip()
spec=importlib.util.spec_from_file_location('owners',H.parent/'barbed-wire-placement-native/owners.py');o=importlib.util.module_from_spec(spec);spec.loader.exec_module(o)
mutant=sys.argv[1] if len(sys.argv)>1 else None
with tempfile.TemporaryDirectory(prefix='barbed-resource-native-') as tmp:
 t=Path(tmp);files=o.generate(t);files=[p for p in files if p.name!='BarbedNativeTest.java']
 p=t/'BarbedConstruction.java';s=p.read_text();s=s[:s.index('public static void initialize()')]+'''public static void initialize(){String[] names={"barbed_wire","barbed_wire_fire","barbed_wire_poison","barbed_wire_acid","barbed_wire_wither","barbed_wire_ultradeath"};for(int i=0;i<names.length;i++){Block b=createBarbedWire(names[i]);try{ModBlocks.class.getField(names[i]).set(null,b);}catch(Exception e){throw new RuntimeException(e);}net.minecraftforge.fml.common.registry.ForgeRegistries.BLOCKS.register(b);net.minecraftforge.fml.common.registry.ForgeRegistries.ITEMS.register(new net.minecraft.item.ItemBlock(b).setRegistryName(b.getRegistryName()));}}}''';p.write_text(s)
 for f in ['com/hbm/main/BarbedWireObjModelHandler.java','com/hbm/render/icon/LegacyIconRegister.java','com/hbm/render/icon/LegacyTextureAtlasSprite.java','com/hbm/render/item/NativeItemTextureRegistry.java','com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java']:files.append(S/f)
 files += [H/'BarbedResourceTest.java',H.parent/'MaterialTextureAtlasSpriteNativeTest.java']
 resources=t/'resources';assets=['models/blocks/barbed_wire.obj']
 for suffix in ['', '_fire','_poison','_acid','_wither','_ultradeath']:
  name='barbed_wire'+suffix;assets+=['textures/blocks/'+name+'_model.png','blockstates/tile.'+name+'.json']
 for f in assets:
  rel='src/main/resources/assets/hbm/'+f;p=resources/'assets/hbm'/f;p.parent.mkdir(parents=True,exist_ok=True)
  source=R/rel;p.write_bytes(source.read_bytes() if source.exists() else subprocess.check_output(['git','show','HEAD:ntm-port-1.12.2/'+rel],cwd=R))
 if mutant=='state-route':
  for p in (resources/'assets/hbm/blockstates').glob('*.json'):p.rename(p.with_name(p.name.replace('tile.','')))
 if mutant=='texture':p=t/'BarbedConstruction.java';p.write_text(p.read_text().replace('":blocks/" + registryName','":blocks/tile." + registryName'))
 if mutant in ['obj-route','loader-route']:
  src=S/'com/hbm/main/BarbedWireObjModelHandler.java';p=t/'com/hbm/main/BarbedWireObjModelHandler.java';p.parent.mkdir(parents=True,exist_ok=True);text=src.read_text();text=text.replace('"blocks/barbed_wire.obj"','"models/blocks/barbed_wire.obj"') if mutant=='obj-route' else text.replace('"models/block/barbed_wire"','"models/block/not_barbed_wire"');p.write_text(text);files=[p if f==src else f for f in files]
 with zipfile.ZipFile(t/'resources.jar','w') as z:
  for p in resources.rglob('*'):
   if p.is_file():z.write(p,p.relative_to(resources).as_posix())
 for jar in CP.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for n in z.namelist():
     if n.endswith('.so') and '/' not in n:(t/n).write_bytes(z.read(n))
 subprocess.run([str(J/'javac'),'-source','8','-target','8','-encoding','UTF-8','-cp',CP,'-d',tmp]+list(map(str,files)),check=True)
 result=subprocess.run([str(J/'java'),'-Djava.awt.headless=true','-Djava.library.path='+tmp,'-cp',tmp+os.pathsep+CP,'BarbedResourceTest',str(resources),str(t/'resources.jar')],cwd=tmp,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=False);sys.exit(result.returncode)

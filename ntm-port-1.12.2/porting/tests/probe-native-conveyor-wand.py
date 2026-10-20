#!/usr/bin/env python3
"""Whole native wand/ownership/model draw proof, with existing complete machine regressions."""
import argparse,ast,hashlib,importlib.util,json,os,pathlib,subprocess,tempfile,zipfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
r=pathlib.Path(__file__).resolve().parents[2];t=r/'porting/tests';s=r/'src/main/java'
# Preserve every old machine branch: only the explicit native frame-model contract/dispatch is new.
machine=s/'com/hbm/render/util/NativeMachineItemRenderer.java';old=machine.read_text()
for chunk in ['''
    /** A known native item model may retain its own original block-helper frame draw. */
    public interface FrameItemModel {
        void renderFrame(EntityItem entity, double x, double y, double z, float yaw, float partialTicks, Display display);
    }''','''
        if(model instanceof FrameItemModel) {
            ((FrameItemModel)model).renderFrame(entity, x, y, z, yaw, partialTicks, display);
            return;
        }''']:
 assert old.count(chunk)==1;old=old.replace(chunk,'')
assert hashlib.sha256(old.encode()).hexdigest()==json.loads((t/'native-machine-item-scope.json').read_text())['production']['src/main/java/com/hbm/render/util/NativeMachineItemRenderer.java']
print('PASS every preexisting machine branch byte-exact after removing the two explicit frame-contract additions',flush=True)
with tempfile.TemporaryDirectory(prefix='native-conveyor-wand-') as d:
 out=pathlib.Path(d);files=[s/('com/hbm/render/util/'+n+'.java') for n in ['NativeItemBaseModel','NativeMachineItemRenderer','NativeGuiItemDraw','NativeIconExtrusion','NativeItemQuadRenderer','RenderDecoItem','RenderItemStack']]
 files += [s/'com/hbm/render/item/nativeitem/MaterialTextureAtlasSprite.java']
 files += [t/(n+'.java') for n in ['NativeMachineItemTest','NativeMachineDrawTest','NativeMachineConsumerTest','NativeBaseModelOwnershipTest','MaterialTextureAtlasSpriteNativeTest','ForgeEventNativeLauncher','NativeConveyorWandTest','StaticItemEntityOwnershipTest']]
 spec=importlib.util.spec_from_file_location('fixtures',t/'machine-renderer-fixtures.py');f=importlib.util.module_from_spec(spec);spec.loader.exec_module(f);f.add(r,out,files)
 # Reuse the independently declared NTM boundaries; all conveyor production classes remain complete.
 tree=ast.parse((t/'probe-native-conveyor-owners.py').read_text());collaborators=None
 for node in tree.body:
  if isinstance(node,ast.Assign) and any(isinstance(x,ast.Name) and x.id=='collaborators' for x in node.targets):collaborators=ast.literal_eval(node.value)
 assert collaborators
 for path,body in collaborators.items():
  existing=out/'fixtures'/path
  if existing.exists():
   text=existing.read_text()
   if path.endswith('ModBlocks.java'):text=text[:-1]+'public static net.minecraft.block.Block conveyor,conveyor_lift,conveyor_chute;}'
   elif path.endswith('ModItems.java'):text=text[:-1]+'public static net.minecraft.item.Item conveyor_wand;}'
   else:raise AssertionError(path)
   existing.write_text(text)
  else:
   target=out/'fixtures'/path;target.parent.mkdir(parents=True,exist_ok=True);target.write_text(body);files.append(target)
 names=['Base','Bendable','','Double','Triple','Express','Lift','Chute','Classic']
 paths=['com/hbm/blocks/network/BlockConveyor'+n+'.java' for n in names]+['com/hbm/blocks/network/ConveyorModelState.java','com/hbm/blocks/ITooltipProvider.java','com/hbm/lib/ForgeDirection.java','api/hbm/block/IToolable.java']+['api/hbm/conveyor/'+n+'.java' for n in ['IConveyorBelt','IEnterableBlock','IConveyorItem','IConveyorPackage']]
 paths+=['com/hbm/render/block/'+n+'.java' for n in ['RenderConveyor','RenderConveyorLift','RenderConveyorChute']]+['com/hbm/render/block/nativeblock/'+n+'.java' for n in ['ConveyorBakedModel','ConveyorCuboidGeometry','ConveyorMaterials','ThermalModelTransforms']]+['com/hbm/main/ConveyorBlockModelHandler.java']
 paths+=['com/hbm/render/item/block/'+n+'.java' for n in ['ItemRenderBlock','BlockItemModel','BlockItemPerspectives','BlockItemRegistration']]+['com/hbm/render/item/nativeitem/staticfamily/'+n+'.java' for n in ['SingleMeshEntityItemRenderer','StaticItemEntityOwnership']]
 files += [s/path for path in paths]
 for jar in a.classpath.split(os.pathsep):
  if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
   with zipfile.ZipFile(jar) as z:
    for name in z.namelist():
     if name.endswith('.so') and '/' not in name:(out/name).write_bytes(z.read(name))
 assert (out/'liblwjgl64.so').exists()
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out/'fixtures'),'-cp',a.classpath,'-d',d]+list(map(str,files)),check=True)
 print('PASS whole wand/seven-file native root, complete nine conveyor owners/loader/models, 14 machine renderers and real helper classes compile',flush=True)
 archive=out/"wand-resource-proof.jar"
 with zipfile.ZipFile(archive,"w") as jar:
  for path in ["assets/hbm/models/item/item.conveyor_wand.json","assets/hbm/textures/items/wand_s.png"]:jar.write(r/"src/main/resources"/path,path)
 cp=d+os.pathsep+a.classpath
 for main,launch in [('NativeConveyorWandTest',True),('NativeMachineItemTest',False),('com.hbm.render.item.nativeitem.staticfamily.StaticItemEntityOwnershipTest',False)]:
  subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+d,'-cp',cp]+(['ForgeEventNativeLauncher'] if launch else [])+[main]+([str(r/"src/main/resources"),str(archive)] if main=="NativeConveyorWandTest" else []),cwd=d,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)

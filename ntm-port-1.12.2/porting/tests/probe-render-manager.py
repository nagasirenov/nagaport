#!/usr/bin/env python3
"""Native constructor/factory probe. Compile complete production RenderEmpty and transparently
constructor-only excerpts of the other 51 renderers. The omitted rendering implementations are
not claimed to compile, and no custom entity fields or Minecraft substitutes are introduced.
"""
import argparse,json,pathlib,re,subprocess,tempfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[2]
subprocess.run(['python3',str(root/'porting/tests/check-render-manager-root.py')],check=True)
manifest=json.loads((root/'porting/tests/render-manager-root-manifest.json').read_text())
def constructors(text,name):
    result=[]
    for m in re.finditer(r'public\s+'+name+r'\([^)]*\)\s*\{',text):
        pos=m.end();depth=1
        while depth:
            if text[pos]=='{':depth+=1
            if text[pos]=='}':depth-=1
            pos+=1
        result.append(text[m.start():pos])
    return result
with tempfile.TemporaryDirectory(prefix='render-manager-probe-') as tmp:
    out=pathlib.Path(tmp);generated=out/'constructorprobe';generated.mkdir()
    files=[root/'src/main/java/com/hbm/render/entity/RenderEmpty.java',root/'porting/tests/RenderManagerFactoryTest.java']
    cases=[]
    for entry in manifest['renderers']:
        path=root/entry['path'];name=path.stem
        if name=='RenderEmpty':continue
        text=path.read_text();cons=constructors(text,name);assert cons
        fields=''
        if name=='MultiCloudRenderer':
            # Exact real production fields assigned by the two extracted constructors.
            fields='\n'.join(re.findall(r'private (?:Item\[\]|int) field_\w+;',text))
            assert fields.count(';')==2
            cases.append('constructorprobe.MultiCloudRenderer chain = new constructorprobe.MultiCloudRenderer(manager, items); check(chain.getRenderManager()==manager,"MultiCloud delegated manager");')
            cases.append('java.lang.reflect.Field arrayField=chain.getClass().getDeclaredField("field_94151_a");arrayField.setAccessible(true);check(arrayField.get(chain)==items,"MultiCloud exact array identity");')
            cases.append('java.lang.reflect.Field damageField=chain.getClass().getDeclaredField("field_94150_f");damageField.setAccessible(true);check(damageField.getInt(chain)==0,"MultiCloud default damage");')
            cases.append('constructorprobe.MultiCloudRenderer explicit = new constructorprobe.MultiCloudRenderer(manager,items,9);check(damageField.getInt(explicit)==9 && explicit.getRenderManager()==manager,"MultiCloud explicit damage/manager");')
        else:cases.append('check(new constructorprobe.'+name+'(manager).getRenderManager()==manager,"'+name+' manager");')
        # Explicit test-only shell: constructors are verbatim; rendering is not implemented/tested.
        # The sole required abstract texture method throws if accidentally called.
        code='package constructorprobe;\nimport net.minecraft.client.renderer.entity.Render;\nimport net.minecraft.client.renderer.entity.RenderManager;\nimport net.minecraft.item.Item;\n'
        code+='public class '+name+' extends Render {\n'+fields+'\n'+'\n'.join(cons)+'\n'
        code+='protected net.minecraft.util.ResourceLocation getEntityTexture(net.minecraft.entity.Entity entity) { throw new UnsupportedOperationException("Constructor-only probe: rendering omitted"); }\n}\n'
        f=generated/(name+'.java');f.write_text(code);files.append(f)
    # Compile the native IRenderFactory lambda shape for every migrated registration. Custom entity
    # keys remain verified by the source audit, not replaced with fake entity definitions.
    for i,entry in enumerate(manifest['registrations']):
        name=entry['renderer']
        owner='com.hbm.render.entity.' if name=='RenderEmpty' else 'constructorprobe.'
        args='rm, items' if name=='MultiCloudRenderer' else 'rm'
        cases.append('net.minecraftforge.fml.client.registry.IRenderFactory<net.minecraft.entity.Entity> factory'+str(i)+' = rm -> new '+owner+name+'('+args+');')
        cases.append('check(factory'+str(i)+'.createRenderFor(manager).getRenderManager()==manager,"native factory signature '+str(i)+'");')
    # Real supplied-manager object, no GL services are touched by these constructor-only cases.
    harness='''import java.lang.reflect.Field;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.item.Item;
public class RenderConstructorProbe {
 static int checks;static void check(boolean value,String label){checks++;if(!value)throw new AssertionError(label);}
 public static void main(String[] args)throws Exception {
  Field f=sun.misc.Unsafe.class.getDeclaredField("theUnsafe");f.setAccessible(true);
  RenderManager manager=(RenderManager)((sun.misc.Unsafe)f.get(null)).allocateInstance(RenderManager.class);
  Item[] items=new Item[8];
  CASES
  System.out.println("PASS "+checks+" constructor-only assertions: 51 exact production constructor excerpts compiled against real Render/RenderManager, including overloaded MultiCloud delegation.");
  System.out.println("LIMIT: excerpt classes omit rendering implementations/unused fields; not full production renderer compilation.");
 }
}'''.replace('CASES','\n'.join(cases))
    f=out/'RenderConstructorProbe.java';f.write_text(harness);files.append(f)
    subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-encoding','UTF-8','-cp',a.classpath,'-d',str(out)]+list(map(str,files)),check=True)
    for main in ['RenderManagerFactoryTest','RenderConstructorProbe']:
        subprocess.run([str(a.java_home/'bin/java'),'-cp',str(out)+':'+a.classpath,main],cwd=out,check=True)

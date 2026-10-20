#!/usr/bin/env python3
import argparse,hashlib,json,os,re,subprocess,tempfile,zipfile
from pathlib import Path
import migrate
HERE=Path(__file__).resolve().parent
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-jar',type=Path,default=Path('/tmp/nagaport-legacy-client.jar'));a=p.parse_args()
m=migrate.verify();files=m['files'];creations=[r for f in files for r in f['creations']];repairs=[r for f in files for r in f['repairs']]
cp=a.classpath_file.read_text().strip()
items=sorted({r['item'] for r in repairs})
# Exact production statements, retaining source order and separated original startup phases.
text='import net.minecraft.item.*;import net.minecraft.item.ItemArmor.ArmorMaterial;import net.minecraft.init.SoundEvents;import net.minecraftforge.common.util.EnumHelper;import net.minecraftforge.oredict.OreDictionary;\nfinal class ProductionMaterialSlice {\n'
text+='static final String[] MAIN_NAMES={'+','.join('"'+r['variable']+'"' for r in files[0]['creations'])+'};\n'
text+='static class ModItems {\n'+''.join('static final Item '+item+'=new Item();\n' for item in items)+'}\n'
# Holder fields only for deferred MainRegistry repairs. Creation expressions stay exact.
text+='\n'.join('static ArmorMaterial '+r['variable']+';' for r in files[0]['creations'])+'\n'
events=[]
for f,method in zip(files,['createMain','createArmor','createLiquidator']):
 text+='static void '+method+'(){\n'
 for r in f['creations']:
  if method=='createMain':text+='{'+r['after']+'ProductionMaterialSlice.'+r['variable']+'='+r['variable']+';}'
  else:text+=r['after']
  text+='\nNativeArmorMaterialTest.created("'+r['variable']+'",'+r['variable']+');\n';events.append('create:'+r['variable'])
  if method!='createMain':
   repair=next(x for x in f['repairs'] if x['variable']==r['variable'])
   text+=repair['after']+'\nNativeArmorMaterialTest.repaired("'+r['variable']+'", ModItems.'+repair['item']+');\n';events.append('repair:'+r['variable'])
 text+='}\n'
text+='static void repairMain(){\n'
for r in files[0]['repairs']:
 text+=r['after']+'\nNativeArmorMaterialTest.repaired("'+r['variable']+'", ModItems.'+r['item']+');\n';events.append('repair:'+r['variable'])
text+='}\n}\n'
(HERE/'ProductionMaterialSlice.java').write_text(text)
scope={'boundary':'Exact production material/repair statement slices, not full registration owners. Repair fields are distinct genuine native Item fixtures. Original material and ItemArmor constructors and RenderBiped fallback execute unmodified original-client bytecode. No NTM armor gameplay or Forge1614 repair-field bytecode is simulated.','creations':creations,'repairs':repairs,'events':events,'fixture_items':items}
(HERE/'native-scope.json').write_text(json.dumps(scope,indent=2)+'\n')
inputs={str(f.name):migrate.sha(f.read_bytes()) for f in [HERE/'NativeArmorMaterialTest.java',HERE/'ProductionMaterialSlice.java',HERE/'run.py',HERE/'migrate.py',HERE/'audit.py',a.legacy_jar]};assert inputs[a.legacy_jar.name]=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
inputs['classpath_file_sha256']=migrate.sha(a.classpath_file.read_bytes());inputs['classpath_jars']={x:migrate.sha(Path(x).read_bytes()) for x in cp.split(os.pathsep) if x.endswith('.jar') and Path(x).is_file()}
(HERE/'runtime-inputs.json').write_text(json.dumps(inputs,indent=2)+'\n')
with tempfile.TemporaryDirectory(prefix='native-armor-materials-') as d:
 before=text
 for r in creations+repairs:
  assert before.count(r['after'])==1
  before=before.replace(r['after'],r['before'])
 before_dir=Path(d)/'before';before_dir.mkdir();(before_dir/'ProductionMaterialSlice.java').write_text(before)
 command=[str(a.java_home/'bin/javac'),'-encoding','UTF-8','-Xmaxerrs','1000','-source','8','-target','8','-sourcepath',str(before_dir),'-cp',cp,'-d',str(before_dir),str(HERE/'NativeArmorMaterialTest.java'),str(before_dir/'ProductionMaterialSlice.java')]
 result=subprocess.run(command,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
 (HERE/'before-native-compile.log').write_text(result.stdout.replace(str(before_dir),'[temporary-before-slice]'))
 assert result.returncode!=0 and len(re.findall(r'error:',result.stdout))==61,result.stdout[-3000:]
 print('PASS bounded native compile delta: original 31 EnumHelper overload + 30 customCraftingMaterial field errors; migrated exact slices compile below.')
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',d,'-cp',cp,'-d',d,str(HERE/'NativeArmorMaterialTest.java'),str(HERE/'ProductionMaterialSlice.java')],check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Xmx600m','-Djava.awt.headless=true','-cp',d+os.pathsep+cp,'NativeArmorMaterialTest',str(HERE/'native-scope.json'),str(a.legacy_jar),str(HERE/'repair-engine-boundaries.tsv')],cwd=d,check=True)

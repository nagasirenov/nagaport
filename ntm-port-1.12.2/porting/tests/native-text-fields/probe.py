#!/usr/bin/env python3
import argparse,json,subprocess,tempfile,os,zipfile,hashlib
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath-file',type=Path,required=True);p.add_argument('--legacy-client',type=Path,required=True);p.add_argument('--native-library-dir',type=Path,required=True);p.add_argument('--mutants',action='store_true');a=p.parse_args()
e=Path(__file__).resolve().parent;root=e.parents[2];manifest=json.loads((e/'manifest.json').read_text());records=[r for r in manifest['records'] if r['selected']]
with tempfile.TemporaryDirectory(prefix='native-text-field-probe-') as work:
 d=Path(work);lines=['import net.minecraft.client.gui.*;','public class ConstructorFixture {','public FontRenderer fontRenderer; public bbu legacyFontRenderer; public int guiLeft,guiTop,width,x,y,oX,oY,in;', 'public static final int[] COUNTS={'+','.join(str(r['instances']) for r in records)+'};','public static int id(int site,int i){switch(site){']
 for i,r in enumerate(records):lines.append('case '+str(i)+': return '+r['id']+';')
 lines.extend(['default:throw new AssertionError(site);}}'])
 for i,r in enumerate(records):
  current=(root/r['file']).read_text();assert current.count(r['after'])==1,(r['file'],r['assignment'])
  lines.append('public GuiTextField native'+str(i)+'(int i){return '+r['after']+';}')
  lines.append('public bcd legacy'+str(i)+'(int i){return '+r['constructor'].replace('new GuiTextField(', 'new bcd(').replace('this.fontRenderer','this.legacyFontRenderer').replace('(fontRenderer,','(legacyFontRenderer,')+';}')
 lines.append('}');(d/'ConstructorFixture.java').write_text('\n'.join(lines)+'\n')
 # Default-package test harnesses require a metadata-unsigned copy of the signed original jar.
 # Every class and resource byte remains identical; only JAR signature metadata is removed.
 unsigned=d/'legacy-test-only.jar'
 with zipfile.ZipFile(a.legacy_client) as original,zipfile.ZipFile(unsigned,'w',zipfile.ZIP_DEFLATED) as out:
  for info in original.infolist():
   if not info.filename.upper().startswith('META-INF/'):
    data=original.read(info.filename);out.writestr(info,data)
 with zipfile.ZipFile(a.legacy_client) as original,zipfile.ZipFile(unsigned) as copy:
  assert all(original.read(name)==copy.read(name) for name in copy.namelist())
 print('Original client SHA256 '+hashlib.sha256(a.legacy_client.read_bytes()).hexdigest(),flush=True)
 cp=a.classpath_file.read_text().strip()+os.pathsep+str(unsigned)
 subprocess.run([str(a.java_home/'bin/javac'),'-cp',cp,'-d',str(d),str(d/'ConstructorFixture.java'),str(e/'LifecycleProbe.java')],check=True)
 command=[str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+str(a.native_library_dir),'-cp',str(d)+os.pathsep+cp,'LifecycleProbe']
 subprocess.run(command,check=True)
 if a.mutants:
  original=(d/'ConstructorFixture.java').read_text();r=records[0];args=[r['id']]+r['arguments'];mutations={}
  wrong=args.copy();wrong[0]='999';mutations['constructor id']=wrong
  wrong=args.copy();wrong[2],wrong[3]=wrong[3],wrong[2];mutations['constructor x']=wrong
  wrong=args.copy();wrong[1]='null';mutations['native font identity']=wrong
  wrong=args.copy();wrong[4],wrong[5]=wrong[5],wrong[4];mutations['constructor width']=wrong
  for reason,args in mutations.items():
   altered=original.replace(r['after'],'new GuiTextField('+', '.join(args)+')',1)
   assert altered!=original
   (d/'ConstructorFixture.java').write_text(altered)
   subprocess.run([str(a.java_home/'bin/javac'),'-cp',str(d)+os.pathsep+cp,'-d',str(d),str(d/'ConstructorFixture.java')],check=True)
   result=subprocess.run(command,text=True,capture_output=True)
   assert result.returncode!=0 and reason in result.stderr,(reason,result.returncode,result.stderr)
   print('REJECTED runtime mutant: '+reason,flush=True)
  (d/'ConstructorFixture.java').write_text(original)

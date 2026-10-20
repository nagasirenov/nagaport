#!/usr/bin/env python3
"""Actual original/native String wrappers plus exact complete caller if-subtrees."""
import argparse,hashlib,json,subprocess,zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--project',type=Path,required=True);p.add_argument('--out',type=Path,required=True);p.add_argument('--classpath-file',default='/tmp/nagaport-ae2-native-classpath.txt');p.add_argument('--java-home',default='/workspace/shared/nagaport-toolchain/java8');p.add_argument('--legacy',default='/tmp/nagaport-legacy-client.jar');p.add_argument('--unsigned-legacy',default='/tmp/attribute-legacy-unsigned.jar');a=p.parse_args();e=Path(__file__).resolve().parent;a.out.mkdir(parents=True,exist_ok=True);j=Path(a.java_home)/'bin';cp=Path(a.classpath_file).read_text().strip();m=json.loads((e/'manifest.json').read_text());h=lambda b:hashlib.sha256(b).hexdigest()
# Reuse the existing metadata-only unsigned archive; never modify original class bytes.
with zipfile.ZipFile(a.legacy) as old,zipfile.ZipFile(a.unsigned_legacy) as clean:
 names=[s for s in old.namelist() if not s.startswith('META-INF/')];assert set(names)==set(s for s in clean.namelist() if not s.startswith('META-INF/'));assert all(old.read(s)==clean.read(s) for s in names);old_hash=h(old.read('bdw.class'))
records=[r for r in m['records'] if r['file'].endswith(('GUICombustionEngine.java','GUIScreenRecipeSelector.java'))];assert len(records)==4
branches=[r['branch'].replace('drawCreativeTabHoveringText','drawHoveringText') for r in records]
for r,s in zip(records,branches):assert s in (a.project/r['file']).read_text()
comb=[s for r,s in zip(records,branches) if r['file'].endswith('GUICombustionEngine.java')];selector=[s for r,s in zip(records,branches) if r['file'].endswith('GUIScreenRecipeSelector.java')];assert len(comb)==1 and len(selector)==3
body='''import net.minecraft.util.math.MathHelper;import net.minecraft.util.text.TextFormatting;
public class RepresentativeStringBranches extends StringTooltipProbe.Screen {
 public void combustion(int x,int y,int guiLeft,int guiTop,boolean isMouseLocked,int setting){
'''+comb[0]+'''\n }
 public void selector(int mouseX,int mouseY,int guiLeft,int guiTop){
'''+ '\n'.join(selector)+'''\n }
}\n'''
f=a.out/'RepresentativeStringBranches.java';f.write_text(body);(a.out/'representative-branches.json').write_text(json.dumps([{'file':r['file'],'exact_branch':s} for r,s in zip(records,branches)],indent=2)+'\n');launcher=e.parent/'native-gui-sounds/ForgeEventNativeLauncher.java'
inputs=[e/'StringTooltipProbe.java',e/'OldStringCapture.java',launcher,f,Path(a.legacy),Path(a.unsigned_legacy)];(a.out/'input-hashes.json').write_text(json.dumps({'inputs':{str(p):h(p.read_bytes()) for p in inputs},'verified_original_and_unsigned_non_metadata_entries':len(names),'original_bdw.class':old_hash},indent=2)+'\n')
def compile(out,source):
 out.mkdir(exist_ok=True)
 with (out/'compile.log').open('w') as log:subprocess.run([str(j/'javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out/'empty'),'-cp',a.legacy+':'+cp,'-d',str(out),str(e/'StringTooltipProbe.java'),str(e/'OldStringCapture.java'),str(launcher),str(source)],stdout=log,stderr=subprocess.STDOUT,check=True)
def run(out,ok):
 with (out/'runtime.log').open('w') as log:r=subprocess.run([str(j/'java'),'-Xmx400m','-Djava.awt.headless=true','-cp',str(out)+':'+a.unsigned_legacy+':'+cp,'ForgeEventNativeLauncher','StringTooltipProbe'],stdout=log,stderr=subprocess.STDOUT,cwd=out)
 text=(out/'runtime.log').read_text();assert (r.returncode==0)==ok,text
 if not ok:assert 'exact caller coordinates' in text,text
 return text
compile(a.out,f);print(run(a.out,True));mut=a.out/'clamp-mutant';mut.mkdir(exist_ok=True);mf=mut/'RepresentativeStringBranches.java';bad=body.replace('MathHelper.clamp(x, guiLeft + 80, guiLeft + 114)','x');assert bad!=body;mf.write_text(bad);compile(mut,mf);run(mut,False);print('PASS compiled clamp-removal mutant rejected by genuine native event observation')

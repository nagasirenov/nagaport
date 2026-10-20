#!/usr/bin/env python3
"""Compile/run actual native path and exact complete representative production methods."""
import argparse, hashlib, json, subprocess
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--project',type=Path,required=True);p.add_argument('--out',type=Path,required=True);p.add_argument('--classpath-file',default='/tmp/nagaport-ae2-native-classpath.txt');p.add_argument('--java-home',default='/workspace/shared/nagaport-toolchain/java8');a=p.parse_args();e=Path(__file__).resolve().parent;a.out.mkdir(parents=True,exist_ok=True);j=Path(a.java_home)/'bin';cp=Path(a.classpath_file).read_text().strip();m=json.loads((e/'manifest.json').read_text());methods=[]
for owner in ['GUIScreenBobmazon','GUIScreenDesignator','GUIScreenTemplateFolder','GUIRBMKConsole']:
 rows=[r for r in m['records'] if r['file'].endswith(owner+'.java') and ('public void drawCustomInfoStat' if owner=='GUIRBMKConsole' else 'public void drawString') in r['method']];assert len(rows)==1
 method=rows[0]['method'].replace('func_146283_a','drawHoveringText');assert method in (a.project/rows[0]['file']).read_text();methods.append((owner,method))
header='import java.util.*; import net.minecraft.item.ItemStack;\n'
body=header+'\n'.join('class '+o+' extends TooltipProbe.ProbeScreen {\n'+(s if o=='GUIRBMKConsole' else 'class FolderButton { String info; ItemStack stack;\n'+s+'\n}')+'\n}' for o,s in methods)+'\n'
f=a.out/'RepresentativeTooltipMethods.java';f.write_text(body);(a.out/'representative-methods.json').write_text(json.dumps(methods,indent=2)+'\n')
launcher=e.parent/'native-gui-sounds/ForgeEventNativeLauncher.java';inputs=[e/'TooltipProbe.java',launcher,f]
(a.out/'input-hashes.json').write_text(json.dumps({str(x):hashlib.sha256(x.read_bytes()).hexdigest() for x in inputs},indent=2)+'\n')
def compile(out,source):
 out.mkdir(exist_ok=True);cmd=[str(j/'javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(out/'empty'),'-cp',cp,'-d',str(out),str(e/'TooltipProbe.java'),str(launcher),str(source)]
 with (out/'compile.log').open('w') as log: subprocess.run(cmd,stdout=log,stderr=subprocess.STDOUT,check=True)
def run(out,ok):
 with (out/'runtime.log').open('w') as log:r=subprocess.run([str(j/'java'),'-Xmx400m','-Djava.awt.headless=true','-cp',str(out)+':'+cp,'ForgeEventNativeLauncher','TooltipProbe'],stdout=log,stderr=subprocess.STDOUT,cwd=out)
 text=(out/'runtime.log').read_text();assert (r.returncode==0)==ok,text
 if not ok: assert 'original coordinates order' in text,text
 return text
compile(a.out,f);print(run(a.out,True))
mut=a.out/'coordinate-mutant';mut.mkdir(exist_ok=True);mf=mut/'RepresentativeTooltipMethods.java';changed=body.replace('Arrays.asList(text), tPosX, tPosY','Arrays.asList(text), tPosY, tPosX');assert changed!=body;mf.write_text(changed);compile(mut,mf);run(mut,False);print('PASS compiled coordinate-order mutant rejected by actual native event assertion')

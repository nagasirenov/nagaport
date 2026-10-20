#!/usr/bin/env python3
import argparse,hashlib,json,os,re,subprocess,tempfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--project',type=Path,required=True);p.add_argument('--resources',type=Path,required=True);p.add_argument('--legacy',default='/tmp/nagaport-legacy-client.jar');p.add_argument('--classpath-file',default='/tmp/nagaport-ae2-native-classpath.txt');p.add_argument('--java-home',default='/workspace/shared/nagaport-toolchain/java8');p.add_argument('--out',type=Path,required=True);a=p.parse_args();e=Path(__file__).resolve().parent;a.out.mkdir(parents=True,exist_ok=True);j=Path(a.java_home)/'bin';cp=Path(a.classpath_file).read_text().strip();src=a.project/'src/main/java';m=json.loads((e/'manifest.json').read_text())
def unescape(s):return re.sub(r'\\([\\trn])',lambda q:{'\\':'\\','t':'\t','r':'\r','n':'\n'}[q[1]],s)
names={'com/hbm/inventory/gui/GUIScreenBobble.java':'initGui()','com/hbm/inventory/gui/GUIScreenRecipeSelector.java':'click()','com/hbm/items/weapon/ItemCrucible.java':'playSwing(float)'}
methods=[]
for path,name in names.items():
 row=next(x for x in m['calls'] if x['path']==path and x['enclosing_method_signature']==name);original=unescape(row['enclosing_method_source']);method=original.replace(m['old_selector'],m['new_selector']);assert method in (src/path).read_text();methods.append(method)
body='''import net.minecraft.client.Minecraft;import net.minecraft.client.gui.GuiScreen;import net.minecraft.util.ResourceLocation;import net.minecraftforge.fml.relauncher.Side;import net.minecraftforge.fml.relauncher.SideOnly;
public final class RepresentativeMethods extends GuiScreen {\n'''+ '\n'.join(methods)+'''\npublic static void invoke(Minecraft mc){RepresentativeMethods r=new RepresentativeMethods();r.mc=mc;r.initGui();r.click();r.playSwing(.83F);}}\n'''
f=a.out/'RepresentativeMethods.java';f.write_text(body);(a.out/'representative-inputs.json').write_text(json.dumps({'methods':names,'full_extracted_methods':methods},indent=2)+'\n')
files=[src/'com/hbm/sound/GuiSound.java',src/'com/hbm/sound/SoundCategoryResolver.java',e/'GuiSoundProbe.java',e/'ForgeEventNativeLauncher.java',f]
(a.out/'input-hashes.json').write_text(json.dumps({str(x):hashlib.sha256(x.read_bytes()).hexdigest() for x in files+[Path(a.legacy),a.resources/'assets/hbm/sounds.json']},indent=2)+'\n')
with (a.out/'compile.log').open('w') as log:subprocess.run([str(j/'javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(a.out/'empty'),'-cp',cp,'-d',str(a.out)]+list(map(str,files)),stdout=log,stderr=subprocess.STDOUT,check=True)
with (a.out/'runtime.log').open('w') as log:subprocess.run([str(j/'java'),'-Xmx400m','-Djava.awt.headless=true','-cp',str(a.out)+':'+cp,'ForgeEventNativeLauncher','GuiSoundProbe',a.legacy,str(a.resources)],stdout=log,stderr=subprocess.STDOUT,cwd=a.out,check=True)
print((a.out/'runtime.log').read_text())

#!/usr/bin/env python3
"""Native Forge side-transform probe plus exact annotation-only armor source audit."""
import argparse, os, subprocess, tempfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--java-home',required=True,type=Path);p.add_argument('--classpath',required=True)
a=p.parse_args()
annotation=b'\t@net.minecraftforge.fml.relauncher.SideOnly(net.minecraftforge.fml.relauncher.Side.CLIENT)'
paths=[ROOT/'src/main/java/com/hbm/render/tileentity/IItemRendererProvider.java']
paths += [q for q in (ROOT/'src/main/java/com/hbm/items/armor').glob('*.java') if b'public ItemRenderBase getRenderer()' in q.read_bytes()]
assert len(paths)==16
for path in paths:
    current=path.read_bytes(); newline=b'\r\n' if b'\r\n' in current else b'\n'
    before=subprocess.check_output(['git','show','f45ef1d63a0d8cd793b90d207d6ea7362793d442:ntm-port-1.12.2/'+path.relative_to(ROOT).as_posix()],cwd=ROOT)
    assert current.count(annotation)==1 and current.replace(annotation+newline,b'')==before,path
print('PASS 16 exact annotation-only complete-source reversals',flush=True)
with tempfile.TemporaryDirectory(prefix='hbm-provider-side-') as out:
    files=[ROOT/'src/main/java/com/hbm/render/item/ItemRenderBase.java', paths[0], ROOT/'porting/tests/ItemProviderSideNativeTest.java']
    subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-cp',a.classpath,'-d',out]+list(map(str,files)),check=True)
    subprocess.run([str(a.java_home/'bin/java'),'-cp',out+os.pathsep+a.classpath,'ItemProviderSideNativeTest',out+'/com/hbm/render/tileentity/IItemRendererProvider.class'],check=True,cwd=out)

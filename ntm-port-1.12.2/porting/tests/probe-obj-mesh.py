#!/usr/bin/env python3
"""Compile complete production core and ModelWormHead against actual pinned target classes.
Old flat/inset provenance: MinecraftForge/MinecraftForge commit
9274e4fe435cb415099a8216c1b42235f185443e, src/main/java/net/minecraftforge/client/model/obj/Face.java
SHA256 afd6fc808708765039ff83b0873a81cbc2f95d7411ef87f7db818f2ed5c88ce6.
WavefrontObject.java SHA256 9d55d712b66254eda54b69b077a2bf041d9da6e0d5847091f98776325afb95dd.
Source retrieved read-only from official raw.githubusercontent.com at that immutable revision.
No downloaded Forge source is copied into production or substituted for Minecraft classes.
"""
import argparse, pathlib, subprocess, tempfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[2]
src=root/'src/main/java/com/hbm'; assets=root/'src/main/resources/assets/hbm/models'
files=sorted((src/'render/mesh').glob('*.java'))+[src/'render/model/ModelWormHead.java',src/'lib/RefStrings.java',root/'porting/tests/ObjMeshMinecraftTest.java']
counts=[]
for name in ['core_receiver.obj','fluidtank.obj','weapons/tom_flame.hmf']:
    counts.append(str(sum(line.startswith('f ') for line in (assets/name).read_text().splitlines())))
with tempfile.TemporaryDirectory(prefix='obj-native-probe-') as tmp:
    subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-encoding','UTF-8','-cp',a.classpath,'-d',tmp]+list(map(str,files)),check=True)
    subprocess.run([str(a.java_home/'bin/java'),'-cp',tmp+':'+a.classpath,'ObjMeshMinecraftTest',str(assets)]+counts,cwd=tmp,check=True)

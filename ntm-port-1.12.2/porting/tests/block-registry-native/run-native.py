#!/usr/bin/env python3
"""Genuine 1.12 Forge APIs, full production owners/queue/atlas handler and exact table slices.
No full ModBlocks compile claim: other unported owners remain out of this bounded probe.
The only NTM fixture is MainRegistry's logger and Random, not any platform/content class.
"""
import argparse, hashlib, os, re, subprocess, tempfile, zipfile
from pathlib import Path
HERE=Path(__file__).resolve().parent
ROOT=HERE.parents[2]
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--java-home',type=Path,required=True)
p.add_argument('--classpath-file',type=Path,required=True)
p.add_argument('--legacy-jar',type=Path,required=True)
a=p.parse_args();cp=a.classpath_file.read_text().strip()
assert hashlib.sha256(a.legacy_jar.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
subprocess.run(['python',str(HERE/'check-source.py')],check=True)
names=['structure_anchor','crystal_hardened','absorber','absorber_red','absorber_green','absorber_pink','pink_planks']
source=(ROOT/'src/main/java/com/hbm/blocks/ModBlocks.java').read_text()
statements=[];registrations=[]
for name in names:
    construction=re.findall(r'^\s*'+name+r' = .*;$',source,re.M)
    registration=re.findall(r'^\s*GameRegistry.registerBlock\('+name+r', .*;$',source,re.M)
    assert len(construction)==len(registration)==1,name
    statements.append(construction[0]);registrations.append(registration[0])
materials=[]
for name in ['thick_foliage','fluidmud','fluidacid','fluidtoxic','fluidschrabidic','fluidcorium']:
    start=source.index('public static final Material '+name+' = ');depth=0
    for end in range(start,len(source)):
        ch=source[end]
        if ch=='{':depth+=1
        elif ch=='}':depth-=1
        elif ch==';' and depth==0:break
    materials.append(source[start:end+1])
slice='''import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.*;
import com.hbm.blocks.BlockBase;
import com.hbm.blocks.generic.BlockGeneric;
import com.hbm.lib.RefStrings;
import com.hbm.registry.GameRegistry;
public final class ModBlocksConstructionSlice {
'''+ '\n'.join(materials)+'\npublic static Block '+', '.join(names)+';\npublic static void initialize() {\n'+'\n'.join(statements)+'\n}\npublic static void register() {\n'+'\n'.join(registrations)+'\n}\npublic static Block[] blocks() { return new Block[]{'+','.join(names)+'}; }\n}\n'
production=['com/hbm/blocks/BlockBase.java','com/hbm/blocks/generic/BlockGeneric.java','com/hbm/registry/GameRegistry.java','com/hbm/lib/RefStrings.java','com/hbm/render/icon/IIcon.java','com/hbm/render/icon/IIconRegister.java','com/hbm/render/icon/LegacyIconRegister.java','com/hbm/render/icon/LegacyTextureAtlasSprite.java','com/hbm/main/LegacyIconRegistrationHandler.java','com/hbm/blocks/ModSoundType.java','com/hbm/blocks/ModSoundTypes.java','com/hbm/main/ModSoundEvents.java']
print('SCOPE: twelve complete production files; seven verbatim construction and seven registry statements; six verbatim material fields.',flush=True)
print('BOUNDARY: MainRegistry logger/random fixture only. Full ModBlocks, other owners, whole client and dedicated server are NOT compiled or validated here.',flush=True)
with tempfile.TemporaryDirectory(prefix='hbm-block-registry-native-') as directory:
    tmp=Path(directory); generated=tmp/'ModBlocksConstructionSlice.java';generated.write_text(slice)
    fixture=tmp/'com/hbm/main/MainRegistry.java';fixture.parent.mkdir(parents=True)
    fixture.write_text('package com.hbm.main; public final class MainRegistry { public static final MainRegistry instance=new MainRegistry(); public final java.util.Random rand=new java.util.Random(); public static final org.apache.logging.log4j.Logger logger=org.apache.logging.log4j.LogManager.getLogger("NativeRegistryProbe"); }\n')
    files=[ROOT/'src/main/java'/f for f in production]+[generated,fixture,HERE/'NativeAtlasEnvironment.java',HERE/'BlockRegistryNativeTest.java',HERE.parent/'ForgeEventNativeLauncher.java']
    for f in files:print('COMPILE',f.name,hashlib.sha256(f.read_bytes()).hexdigest(),flush=True)
    for jar in cp.split(os.pathsep):
        if 'lwjgl-platform' in jar and jar.endswith('natives-linux.jar'):
            with zipfile.ZipFile(jar) as z:
                for n in z.namelist():
                    if n.endswith('.so') and '/' not in n:(tmp/n).write_bytes(z.read(n))
    assert (tmp/'liblwjgl64.so').is_file()
    subprocess.run([str(a.java_home/'bin/javac'),'-cp',str(a.java_home/'lib/tools.jar'),'-d',directory,str(HERE/'ParseProduction.java')],check=True)
    subprocess.run([str(a.java_home/'bin/java'),'-cp',directory+os.pathsep+str(a.java_home/'lib/tools.jar'),'ParseProduction',str(ROOT/'src/main/java/com/hbm/blocks/ModBlocks.java'),str(ROOT/'src/main/java/com/hbm/blocks/BlockBase.java')],check=True)
    subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',str(tmp/'empty-sourcepath'),'-cp',cp,'-d',directory]+list(map(str,files)),check=True)
    subprocess.run([str(a.java_home/'bin/java'),'-Djava.awt.headless=true','-Djava.library.path='+directory,'-cp',directory+os.pathsep+cp,'ForgeEventNativeLauncher','BlockRegistryNativeTest',str(ROOT/'src/main/resources'),str(a.legacy_jar)],cwd=directory,env=dict(os.environ,MESA_SHADER_CACHE_DISABLE='true'),check=True)

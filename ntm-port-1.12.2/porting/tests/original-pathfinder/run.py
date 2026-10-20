#!/usr/bin/env python3
"""Portable, offline native/actual-original pathfinding differential."""
import argparse, hashlib, importlib.util, json, os, re, subprocess, tempfile, zipfile
from pathlib import Path
HERE=Path(__file__).resolve().parent
CLIENT_SHA='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
def legacy_wrapper(source):
    text=source.read_text()
    # Only API spellings are changed; execute original wrapper control flow and
    # mutable Vec3 arithmetic on actual 1.7 classes, not a copied native model.
    text=re.sub(r'^package .*;\n|^import .*;\n','',text,flags=re.M)
    names={'PathFinderUtils':'LegacyPathFinderUtils','PathEntity':'ayf','PathFinder':'ayg','Entity':'sa','World':'ahb','ChunkCache':'ahr','MathHelper':'qh','Vec3':'azw'}
    for old,new in names.items():text=re.sub(r'\b'+old+r'\b',new,text)
    names={'theProfiler':'C','startSection':'a','endSection':'b','floor_double':'c','posX':'s','posY':'t','posZ':'u','createVectorHelper':'a','normalize':'a','xCoord':'a','yCoord':'b','zCoord':'c','getBlock':'a','getMaterial':'o','blocksMovement':'c','isNormalCube':'r','createEntityPathTo':'a'}
    for old,new in names.items():text=re.sub(r'\b'+old+r'\b',new,text)
    return text

def main():
    p=argparse.ArgumentParser();p.add_argument('--module',type=Path,default=HERE.parents[2]);p.add_argument('--original-root',type=Path);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',type=Path,required=True);p.add_argument('--forge-patches',type=Path,required=True);a=p.parse_args()
    a.module=a.module.resolve();original=a.original_root or a.module.parent/'ntm-original-pristine'
    spec=importlib.util.spec_from_file_location('original_path_mapping',HERE/'check-mapping.py');guard=importlib.util.module_from_spec(spec);spec.loader.exec_module(guard);guard.check(a.module)
    assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()==CLIENT_SHA,'Wrong original client'
    assert hashlib.sha256(a.forge_patches.read_bytes()).hexdigest()=='5cb7f1cd1c4faf0c435548c610c3322c15ae3524cbb4926f272ec057dce83bd4','Wrong original Forge1614 patches'
    wrapper=original/'src/main/java/com/hbm/entity/pathfinder/PathFinderUtils.java'
    assert hashlib.sha256(wrapper.read_bytes()).hexdigest()=='454472071fabb21c2764a0904d4ffd9f111d90ccd99567c62354e4572175bf8f','Wrong pristine wrapper'
    print('PASS pinned original client, Forge1614 patches and pristine wrapper',flush=True)
    with zipfile.ZipFile(a.forge_patches) as z:
        assert not any('PathFinder' in n or 'PathPoint' in n or 'PathEntity' in n or n.endswith('/Path.java.patch') for n in z.namelist()),'Unreviewed original path patch'
    src=a.module/'src/main/java'
    calls=[]
    for file in src.rglob('*.java'):
        text=file.read_text(encoding='utf-8')
        count=len(re.findall(r'PathFinderUtils\.getPathEntityTo(?:Entity|Coord)Partial\(',text))
        if count:calls.append((file.relative_to(src).as_posix(),count))
    assert sorted(calls)==[('com/hbm/entity/mob/EntityFBI.java',1),('com/hbm/entity/mob/glyphid/EntityGlyphid.java',4)],('Caller census changed',calls)
    production=['com/hbm/entity/pathfinder/NTMPathFinder.java','com/hbm/entity/pathfinder/PathFinderUtils.java','com/hbm/blocks/generic/BlockMetalFence.java','com/hbm/blocks/IBlockMulti.java']
    suffix='.exe' if os.name=='nt' else '';javac=str(a.java_home/'bin'/('javac'+suffix));java=str(a.java_home/'bin'/('java'+suffix))
    with tempfile.TemporaryDirectory(prefix='ntm-original-path-') as tmp:
        out=Path(tmp);fixture=out/'com/hbm/blocks/ModBlocks.java';fixture.parent.mkdir(parents=True);fixture.write_text('package com.hbm.blocks; public final class ModBlocks { public static net.minecraft.block.Block fence_gate; }\n')
        fbi=(src/'com/hbm/entity/mob/EntityFBI.java').read_text()
        glyphid=(src/'com/hbm/entity/mob/glyphid/EntityGlyphid.java').read_text()
        statement=re.findall(r'this\.getNavigator\(\)\.setPath\(PathFinderUtils\.getPathEntityToEntityPartial\([^\n]+;',fbi)
        target=re.findall(r'PathFinderUtils\.getPathEntityToEntityPartial\([^;]+?\)',glyphid)
        coords=re.findall(r'PathFinderUtils\.getPathEntityToCoordPartial\([^;]+?\)',glyphid)
        assert len(statement)==1 and len(target)==1 and len(coords)==3,'Five-caller scope changed'
        owners=(HERE/'OwnerCallsTest.java.in').read_text().replace('@FBI_STATEMENT@',statement[0]).replace('@GLYPHID_TARGET@',target[0])
        for i,expression in enumerate(coords):owners=owners.replace('@GLYPHID_COORD_'+str(i)+'@',expression)
        owner=out/'OwnerCallsTest.java';owner.write_text(owners)
        old=out/'LegacyPathFinderUtils.java';old.write_text(legacy_wrapper(original/'src/main/java/com/hbm/entity/pathfinder/PathFinderUtils.java'))
        for path in production:print('PRODUCTION',path,hashlib.sha256((src/path).read_bytes()).hexdigest(),flush=True)
        # Original default-package classes and controlled subclasses must share
        # a package. Copy class bytes unchanged into a test-only unsigned JAR;
        # do not redistribute this file or modify the supplied client.
        oracle=out/'original-classes.jar'
        with zipfile.ZipFile(a.legacy_client) as source,zipfile.ZipFile(oracle,'w') as target:
            for name in source.namelist():
                if not name.startswith('META-INF/'):
                    target.writestr(name,source.read(name))
        cp=os.pathsep.join([a.classpath,str(oracle)])
        subprocess.run([javac,'-source','8','-target','8','-encoding','UTF-8','-sourcepath',tmp,'-cp',cp,'-d',tmp]+[str(src/f) for f in production]+[str(fixture),str(old),str(owner),str(HERE/'NativePathFinderTest.java')],check=True)
        bytecode=subprocess.check_output([str(a.java_home/'bin'/('javap'+suffix)),'-c','-p','-classpath',tmp,'com.hbm.entity.pathfinder.NTMPathFinder','com.hbm.entity.pathfinder.PathFinderUtils'],universal_newlines=True)
        for forbidden in ['net/minecraft/pathfinding/PathEntity','net/minecraft/pathfinding/PathFinder','net/minecraft/pathfinding/WalkNodeProcessor','worldObj','floor_double','getBlockMetadata','costMalus']:
            assert forbidden not in bytecode,'Obsolete or changed native policy in helper bytecode: '+forbidden
        print('PASS complete production helpers contain only native APIs, no native altered search/cost policy',flush=True)
        subprocess.run([java,'-Xmx1G','-cp',tmp+os.pathsep+cp,'NativePathFinderTest'],cwd=tmp,check=True)
if __name__=='__main__':main()

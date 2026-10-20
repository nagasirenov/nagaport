#!/usr/bin/env python3
"""Compile whole production ILoadedTile with EXTRACTED actual Compat lookup only.
Not complete Compat, energy network, LoadedBase, packet, furnace or gameplay proof.
"""
import argparse, hashlib, json, os, subprocess, tempfile
from pathlib import Path
ROOT = Path(__file__).resolve().parents[2]

def method(s, signature):
    start=s.index(signature); brace=s.index('{',start); end=brace+1; depth=1
    while depth:
        depth += (s[end]=='{')-(s[end]=='}'); end+=1
    return s[start:end]

def parity():
    manifest=json.loads(Path(__file__).with_name('tile-access-cache-manifest.json').read_text(encoding='utf-8'))
    for path, digest in manifest['before_sha256'].items():
        current=(ROOT/path).read_text(encoding='utf-8')
        if 'ILoadedTile' in path:
            old='world.provider.dimensionId';new='world.provider.getDimension()'
        else:
            old='if(!world.getChunkProvider().chunkExists(x >> 4, z >> 4)) return null;\n\t\treturn world.getTileEntity(x, y, z);'
            new='net.minecraft.util.math.BlockPos pos = new net.minecraft.util.math.BlockPos(x, y, z);\n\t\tif(!world.isBlockLoaded(pos, false)) return null;\n\t\treturn world.getTileEntity(pos);'
        assert current.count(new)==1, path+' expected exactly one native substitution'
        restored=current.replace(new,old)
        assert hashlib.sha256(restored.encode('utf-8')).hexdigest()==digest, path+' changed beyond reviewed migration'
    print('PASS: exact two-file parity via normalized baseline hashes; no Git objects required',flush=True)

if __name__=='__main__':
    p=argparse.ArgumentParser(description=__doc__);p.add_argument('--classpath');p.add_argument('--java-home',type=Path);p.add_argument('--parity-only',action='store_true');a=p.parse_args();parity()
    if a.parity_only: raise SystemExit(0)
    if not a.classpath or not a.java_home: p.error('--classpath and --java-home required')
    with tempfile.TemporaryDirectory(prefix='hbm-cache-native-') as tmp:
        tmp=Path(tmp)
        s=(ROOT/'src/main/java/com/hbm/util/Compat.java').read_text(encoding='utf-8')
        fixture=tmp/'Compat.java';fixture.write_text('package com.hbm.util; import net.minecraft.world.World; import net.minecraft.tileentity.TileEntity; public class Compat {\n'+method(s,'public static TileEntity getTileStandard(')+'\n}',encoding='utf-8')
        sources=[ROOT/'src/main/java'/x for x in ['api/hbm/tile/ILoadedTile.java','com/hbm/util/Tuple.java','com/hbm/interfaces/Spaghetti.java']]+[fixture,ROOT/'porting/tests/TileAccessCacheNativeTest.java']
        subprocess.run([str(a.java_home/'bin'/('javac.exe' if os.name=='nt' else 'javac')),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',str(tmp)]+list(map(str,sources)),check=True)
        subprocess.run([str(a.java_home/'bin'/('java.exe' if os.name=='nt' else 'java')),'-cp',str(tmp)+os.pathsep+a.classpath,'TileAccessCacheNativeTest'],check=True)
    print('PASS: whole ILoadedTile linked to extracted actual Compat method; NOT whole Compat/network/gameplay',flush=True)

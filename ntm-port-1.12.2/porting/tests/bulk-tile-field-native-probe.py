#!/usr/bin/env python3
"""Execute actual mapped native field semantics and exact production expression samples.
No substitute Minecraft/Forge classes. Not complete consumer compilation/gameplay.
"""
import argparse, hashlib, importlib.util, os, re, subprocess, tempfile
from pathlib import Path
p=argparse.ArgumentParser(description=__doc__)
p.add_argument('--java-home',type=Path,required=True)
p.add_argument('--classpath',required=True)
p.add_argument('--root',type=Path,default=Path(__file__).resolve().parents[2])
a=p.parse_args();root=a.root.resolve();exe='.exe' if os.name=='nt' else ''
if not (a.java_home/'bin'/('javac'+exe)).is_file() and (a.java_home.parent/'bin'/('javac'+exe)).is_file():
    a.java_home=a.java_home.parent
spec=importlib.util.spec_from_file_location('bulk_field_audit',Path(__file__).with_name('bulk-tile-field-migration.py'))
audit=importlib.util.module_from_spec(spec);spec.loader.exec_module(audit)
manifest=audit.read_manifest(root/'porting/tests/bulk-tile-field-source-manifest.json')
audit.verify(root=root,manifest=manifest)
def extract(relative,pattern):
    text=(root/'src/main/java/com/hbm'/relative).read_bytes().decode('utf-8')
    matches=re.findall(pattern,text)
    assert len(matches)==1,(relative,pattern,len(matches))
    expression=matches[0]
    print('EXACT PRODUCTION EXPRESSION',relative,hashlib.sha256(expression.encode('utf-8')).hexdigest(),expression,flush=True)
    return expression
geyser='blocks/BlockGeysierDCM.java'
expressions={
    '@AIR_PUMP_WORLD@':extract('tileentity/machine/TileEntityAirPump.java',r'public World getWorld\(\)\s*\{\s*return ([^;]+);'),
    '@GEYSER_SERVER@':extract(geyser,r'if\((!this\.world\.isRemote)\)'),
    '@GEYSER_X@':extract(geyser,r'data\.setDouble\("posX", ([^;]+)\);'),
    '@GEYSER_Y@':extract(geyser,r'data\.setDouble\("posY", ([^;]+)\);'),
    '@GEYSER_Z@':extract(geyser,r'data\.setDouble\("posZ", ([^;]+)\);'),
    '@GEYSER_TOP@':extract(geyser,r'(this\.pos\.getY\(\) \+ 60)'),
    '@VOLCANO_X@':extract('blocks/BlockVolcanoV2.java',r'double x = ([^;]+);'),
}
text=Path(__file__).with_name('bulk-tile-field-native-test.java.in').read_text(encoding='utf-8')
for token,expression in expressions.items():
    assert text.count(token)==1,token
    text=text.replace(token,expression)
assert not re.search(r'@[A-Z_]+@',text)
production=root/'src/main/java/com/hbm/lib/ForgeDirection.java'
print('COMPLETE PRODUCTION INPUT',hashlib.sha256(production.read_bytes()).hexdigest(),production.relative_to(root),flush=True)
with tempfile.TemporaryDirectory(prefix='hbm-bulk-native-fields-') as temporary:
    out=Path(temporary);source=out/'BulkTileFieldNativeTest.java';source.write_text(text,encoding='utf-8')
    subprocess.run([str(a.java_home/'bin'/('javac'+exe)),'-source','8','-target','8','-encoding','UTF-8','-cp',a.classpath,'-d',str(out),str(production),str(source)],check=True)
    subprocess.run([str(a.java_home/'bin'/('java'+exe)),'-Djava.awt.headless=true','-cp',os.pathsep.join([str(out),a.classpath]),'BulkTileFieldNativeTest'],cwd=str(out),check=True)

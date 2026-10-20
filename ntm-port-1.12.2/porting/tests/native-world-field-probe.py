#!/usr/bin/env python3
"""Compile native API fixtures and verbatim production expressions, not whole tiles."""
import argparse, os,importlib.util,pathlib,re,subprocess,tempfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
root=pathlib.Path(__file__).resolve().parents[2]
s=importlib.util.spec_from_file_location('world_field_audit',root/'porting/tests/native-world-field-audit.py');audit=importlib.util.module_from_spec(s);s.loader.exec_module(audit);audit.verify()
def extract(rel,pattern):
    text=(root/'src/main/java/com/hbm'/rel).read_bytes().decode('utf-8');matches=re.findall(pattern,text)
    assert matches and len(set(matches))==1,(rel,matches)
    return matches[0]
expressions={
 '@SERVER@':extract('tileentity/machine/TileEntityFurnaceSpace.java',r'if\((!this\.world\.isRemote)\)'),
 '@GUARD@':extract('tileentity/TileEntityLoadedBase.java',r'if\((preBuf\.equals\(lastPackedBuf\) && this\.world\.getTotalWorldTime\(\) % 20 != 0)\) return;'),
 '@PITCH@':extract('tileentity/machine/storage/TileEntityFileCabinet.java',r'(this\.world\.rand\.nextFloat\(\) \* 0\.1F \+ 0\.7F)'),
 '@FORWARD@':extract('tileentity/machine/TileEntityFurnaceBrick.java',r'MachineBrickFurnace\.updateBlockState\(this\.burnTime > 0, (this\.world),')}
with tempfile.TemporaryDirectory(prefix='native-world-field-') as directory:
    out=pathlib.Path(directory);text=(root/'porting/tests/native-world-field-test.java.in').read_text()
    for token,expression in expressions.items():text=text.replace(token,expression)
    assert not re.search(r'@[A-Z]+@',text)
    java=out/'NativeWorldFieldTest.java';java.write_text(text)
    subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-encoding','UTF-8','-cp',a.classpath,'-d',str(out),str(java)],check=True)
    for main in ['NativeWorldFieldTest']:
        subprocess.run([str(a.java_home/'bin/java'),'-cp',str(out)+os.pathsep+a.classpath,main],cwd=out,check=True)

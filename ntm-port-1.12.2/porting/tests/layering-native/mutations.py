#!/usr/bin/env python3
"""Execute semantic mutants against the real native owner test, no source-only kills."""
import os,pathlib,subprocess,sys
root,out=map(pathlib.Path,sys.argv[1:]);java=pathlib.Path(os.environ['JAVA_HOME'])/'bin'
cp=pathlib.Path(os.environ['NATIVE_CLASSPATH_FILE']).read_text().strip()
source=(root/'ntm-port-1.12.2/src/main/java/com/hbm/blocks/generic/BlockLayering.java').read_text()
mutants={
 'collapse-high-bit':('withProperty(METADATA, meta & 15)','withProperty(METADATA, meta & 7)'),
 'collision-extra-layer':('(getMetaFromState(state) & 7) / 8.0D','((getMetaFromState(state) & 7) + 1) / 8.0D'),
 'mask-replacement':('return meta < 7 && material.isReplaceable();','return (meta & 7) < 7 && material.isReplaceable();'),
 'old-null-drop-sentinel':('return Items.AIR;','return null;'),
 'omit-harvest-super':('super.harvestBlock(world, player, pos, state, tile, tool);','/* omitted native superclass harvest */'),
 'retain-unsupported':('if (!canPlaceBlockAt(world, pos)) {','if (false) {'),
 'solid-faces':('return BlockFaceShape.UNDEFINED;','return BlockFaceShape.SOLID;'),
 'wrong-quantity':('return (getMetaFromState(state) & 7) + 1;','return (getMetaFromState(state) & 7);'),
}
for name,(before,after) in mutants.items():
 assert source.count(before)==1,(name,source.count(before))
 d=out/'mutants'/name;d.mkdir(parents=True,exist_ok=True);p=d/'BlockLayering.java';p.write_text(source.replace(before,after));classes=d/'classes';classes.mkdir(exist_ok=True)
 build=subprocess.run([str(java/'javac'),'-source','8','-target','8','-cp',str(out/'classes')+':'+cp,'-d',str(classes),str(p)],capture_output=True,text=True)
 (d/'compile.log').write_text(build.stdout+build.stderr);assert build.returncode==0,(name,build.stderr)
 run=subprocess.run([str(java/'java'),'-cp',str(classes)+':'+str(out/'classes')+':'+cp,'LayeringNativeTest'],capture_output=True,text=True)
 log=run.stdout+run.stderr;(d/'test.log').write_text(log)
 assert run.returncode!=0 and 'java.lang.AssertionError:' in log,(name,log)
 print('PASS mutant rejected:',name,log.split('java.lang.AssertionError:')[1].splitlines()[0])
print('PASS 8 semantic runtime mutants rejected')

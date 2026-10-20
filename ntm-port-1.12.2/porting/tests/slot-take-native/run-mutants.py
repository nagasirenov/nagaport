#!/usr/bin/env python3
"""Reject callback behavior regressions in isolated generated probes, never production sources.
Usage: run-mutants.py JAVA_HOME CLASSPATH_FILE NATIVE_OUTPUT_DIR
"""
from pathlib import Path
import sys,subprocess,os,json
HERE=Path(__file__).resolve().parent;JAVA,CPF,OUT=map(lambda s:Path(s).resolve(),sys.argv[1:]);CP=CPF.read_text().strip();src=OUT/'src';base=OUT/'classes'
root='com/hbm/inventory/SlotCraftingOutput.java';anvil='com/hbm/inventory/container/ContainerAnvil.java';weapon='com/hbm/inventory/container/ContainerWeaponTable.java'
spec=[('missing-native-dirty',root,'return super.onTake(player, stack);','return stack;'),('lost-anvil-return-identity',anvil,'return stack;','return ItemStack.EMPTY;'),('lost-weapon-index-reset',weapon,'ContainerWeaponTable.this.index = 0;','/* omitted reset */'),('crafting-after-dirty',root,'this.onCrafting(stack);\n\t\treturn super.onTake(player, stack);','ItemStack result = super.onTake(player, stack);\n\t\tthis.onCrafting(stack);\n\t\treturn result;')]
for name,path,old,new in spec:
 text=(src/path).read_text();assert old in text;mutated=text.replace(old,new,1);out=OUT/'mutants'/name;file=out/path;file.parent.mkdir(parents=True,exist_ok=True);file.write_text(mutated);classes=out/'classes';classes.mkdir(exist_ok=True)
 p=subprocess.run([str(JAVA/'bin/javac'),'-encoding','UTF-8','-cp',str(base)+os.pathsep+CP,'-d',str(classes),str(file)],capture_output=True,text=True)
 assert p.returncode==0,p.stdout+p.stderr
 p=subprocess.run([str(JAVA/'bin/java'),'-cp',str(classes)+os.pathsep+str(base)+os.pathsep+CP,'SlotTakeNativeTest'],capture_output=True,text=True,cwd=out)
 assert p.returncode!=0 and 'AssertionError' in p.stderr,name+' escaped'
 (out/'result.log').write_text(p.stdout+p.stderr);print('PASS rejected runtime mutant: '+name)
# Restoring the exact old root callback cannot compile against the genuine 1.12 Slot parent.
m=json.loads((HERE/'manifest.json').read_text());record=next(f for f in m['files'] if f['path'].endswith('/SlotCraftingOutput.java'));edit=next(e for e in record['edits'] if e['kind']=='callback');text=(src/root).read_text();assert edit['new'] in text;text=text.replace(edit['new'],edit['old']);out=OUT/'mutants/legacy-native-negative';file=out/root;file.parent.mkdir(parents=True,exist_ok=True);file.write_text(text)
p=subprocess.run([str(JAVA/'bin/javac'),'-encoding','UTF-8','-cp',str(base)+os.pathsep+CP,'-d',str(out),str(file)],capture_output=True,text=True)
assert p.returncode!=0 and 'onPickupFromSlot' in p.stderr and 'does not override' in p.stderr;pfile=out/'result.log';pfile.write_text(p.stdout+p.stderr)
print('PASS original root signature rejected by genuine native Slot: missing callback plus invalid @Override')

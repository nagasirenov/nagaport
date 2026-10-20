#!/usr/bin/env python3
"""Native extracted consumption-block test; not full machine execution."""
import hashlib, json, pathlib, subprocess, tempfile
root=pathlib.Path(__file__).resolve().parents[3]
here=pathlib.Path(__file__).resolve().parent
m=json.loads((here/'source.json').read_text())
s=(root/m['path']).read_text()
assert hashlib.sha256(s.encode()).hexdigest()==m['after'], 'complete source changed'
before=s.replace(m['new'],m['old'])
assert hashlib.sha256(before.encode()).hexdigest()==m['before'], 'source reversal failed'
assert s.count(m['new'])==1
# Exactly the current real consumption block. Quantity syntax is adapted ONLY
# in this test for positive-entry native stacks, not installed in production.
start=s.index('\t\t\t\tif(this.slots[2] != null) {')
end=s.index('\n\t\t\t}\n\n\t\t\tif(canProcess())',start)
block=s[start:end]
assert block.count('stackSize--')==1 and block.count('stackSize == 0')==1
block=block.replace('this.slots[2].stackSize--;','this.slots[2].shrink(1);').replace('this.slots[2].stackSize == 0','this.slots[2].getCount() == 0')
template=(here/'FuelReceiverTest.java.in').read_text()
java=template.replace('/* ACTUAL_BLOCK */',block)
j='/workspace/shared/nagaport-toolchain/java8/bin/'
cp=pathlib.Path('/tmp/nagaport-ae2-native-classpath.txt').read_text().strip()
with tempfile.TemporaryDirectory(prefix='difurnace-fuel-') as out:
 p=pathlib.Path(out)/'FuelReceiverTest.java';p.write_text(java)
 subprocess.run([j+'javac','-cp',cp,'-d',out,str(p)],check=True)
 subprocess.run([j+'java','-cp',out+':'+cp,'FuelReceiverTest'],check=True)
 for label, bad in [('depleted receiver', java.replace('fuelItem.getContainerItem(this.slots[2])', 'this.slots[2].getItem().getContainerItem(this.slots[2])')), ('absent residue', java.replace('residue == ItemStack.EMPTY ? null : residue', 'residue'))]:
  p.write_text(bad)
  subprocess.run([j+'javac','-cp',cp,'-d',out,str(p)],check=True)
  result=subprocess.run([j+'java','-cp',out+':'+cp,'FuelReceiverTest'],stdout=subprocess.PIPE,stderr=subprocess.PIPE)
  assert result.returncode != 0, label + ' mutation survived'
  print('PASS rejected mutation: '+label)
print('PASS complete-source reverse proof; native extracted block; full machine compilation NOT claimed')

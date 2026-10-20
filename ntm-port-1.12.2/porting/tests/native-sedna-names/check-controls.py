from pathlib import Path
import subprocess,tempfile,json
h=Path(__file__).resolve().parent
lines=(h/'after-controls.tsv').read_text().splitlines();r=[x.split('\t') for x in lines if '\tControls.java\t' in x];yes=[x for x in r if x[0]=='YES'];assert len(yes)==1 and yes[0][-1]=='"yes"';assert len([x for x in r if x[0]=='NO'])==3
# Dynamic, factory and subclass syntax are outside exact-fresh-owner selector and have no YES record.
assert all(not any(label in '\t'.join(x) for label in ['no-dynamic','no-factory','no-subclass','no-anonymous','no-other-builder']) for x in yes)
owner=h.parents[2]/'src/main/java/com/hbm/items/weapon/sedna/ItemGunBaseNT.java';s=owner.read_text();results=[]
for old,new in [('return this;','return null;'),('return setDefaultAmmo(ammo, amount);','return null;'),('this.LAMBDA_NAME_MUTATOR = lambda;','this.LAMBDA_NAME_MUTATOR = null;')]:
 # Restrict first mutation to the proved default-ammo builder, not an unrelated method.
 start=s.index('public ItemGunBaseNT setDefaultAmmo(');i=s.index(old,start);mut=s[:i]+new+s[i+len(old):]
 with tempfile.TemporaryDirectory() as tmp:
  p=Path(tmp)/'ItemGunBaseNT.java';p.write_text(mut);q=subprocess.run(['/workspace/shared/nagaport-toolchain/java8/bin/java','-cp','/tmp/sedna-name-scanner:/workspace/shared/nagaport-toolchain/java8/lib/tools.jar','ReturnProof',str(p)],capture_output=True,text=True);assert q.returncode!=0;results.append({'mutation':old+' => '+new,'rejected':True})
(h/'return-mutants.json').write_text(json.dumps(results,indent=2)+'\n');print('PASS one positive and six excluded selector controls; three return/body proof mutants rejected')

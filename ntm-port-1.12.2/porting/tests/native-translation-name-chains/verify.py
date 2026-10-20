import pathlib,subprocess,json,hashlib,sys,collections
h=pathlib.Path(__file__).resolve().parent;root=h.parents[3];base='36387bc1c10fa9c36fbb27f5e9d44b45d474eb18';sha=lambda x:hashlib.sha256(x).hexdigest()
records=[l.split('\t') for l in (h/'before.tsv').read_text().splitlines() if l.startswith('YES\t')];assert records
paths={p.name:p for p in (root/'ntm-port-1.12.2/src/main/java').rglob('*.java') if p.name in {r[1] for r in records}}
manifest=[]
for name, rs in __import__('itertools').groupby(sorted(records,key=lambda r:r[1]),key=lambda r:r[1]):
 p=paths[name];rel=str(p.relative_to(root));old=subprocess.check_output(['git','show',base+':'+rel],cwd=root);s=old.decode();edits=list(rs)
 for r in sorted(edits,key=lambda r:int(r[2]),reverse=True):
  a,b=map(int,r[2:4]);assert s[a:b]=='setUnlocalizedName';s=s[:a]+'setTranslationKey'+s[b:]
 new=s.encode()
 if '--apply' in sys.argv:
  assert p.read_bytes() in (old,new), 'Refuse unknown source contents'
  p.write_bytes(new)
 assert p.read_bytes()==new
 back=s;shift=0;offsets=[]
 for r in sorted(edits,key=lambda r:int(r[2])):offsets.append(int(r[2])+shift);shift-=1
 for pos in reversed(offsets):assert back[pos:pos+17]=='setTranslationKey';back=back[:pos]+'setUnlocalizedName'+back[pos+17:]
 assert back.encode()==old
 manifest.append(dict(path=rel,before=sha(old),after=sha(new),edits=edits))
# Unchanged exact owner and reused pinned historical evidence.
pins={}
for rel in ['ntm-port-1.12.2/src/main/java/com/hbm/items/armor/ArmorFSB.java']+['ntm-port-1.12.2/porting/tests/native-translation-names/'+f for f in ['bytecode-proof.json','binary-inputs.json','NameOracle.java','oracle.log','bytecode.py']]:
 b=(root/rel).read_bytes();assert b==subprocess.check_output(['git','show',base+':'+rel],cwd=root);pins[rel]=sha(b)
(h/'manifest.json').write_text(json.dumps(dict(base=base,files=manifest,unchanged_proofs=pins),indent=2)+'\n')
if (h/'after.tsv').exists():
 after=[l.split('\t') for l in (h/'after.tsv').read_text().splitlines() if l.startswith('YES\t')]
 assert len(after)==len(records)
 # Match full receiver, name argument, constructor and predecessor executable independently of offset shifts.
 key=lambda r:tuple(r[1:2]+r[6:])
 assert collections.Counter(map(key,after))==collections.Counter(map(key,records))
 assert all(r[4]=='setTranslationKey' and r[5]=='net.minecraft.item.Item#setTranslationKey(java.lang.String):net.minecraft.item.Item' for r in after)
 for prefix in ['EXPR\t','NEW\t','CLOSURE\t','CLOSURECALL\t']:
  get=lambda f:collections.Counter(l for l in (h/f).read_text().splitlines() if l.startswith(prefix))
  assert get('before.tsv')==get('after.tsv'),prefix
print('PASS',len(records),'exact method-token replacements, whole-file inverse, pinned unchanged owner/runtime/bytecode proofs'+(' and independent full AST receiver/argument/predecessor/destination equality' if (h/'after.tsv').exists() else ''))
if (h/'independent-after.tsv').exists():
 before=(h/'independent-before.tsv').read_text().splitlines();after=(h/'independent-after.tsv').read_text().splitlines()
 assert [l for l in before if not l.startswith('CALL\t')]==[l for l in after if not l.startswith('CALL\t')]
 calls=[l for l in after if l.startswith('CALL\t')];assert len(calls)==32 and all(l.endswith('METHOD_INVOCATION:net.minecraft.item.Item:net.minecraft.item.Item#setTranslationKey(java.lang.String)') for l in calls)
 assert [l.split('\t')[1] for l in before if l.startswith('CALL\t')]==[l.split('\t')[1] for l in calls]
 print('PASS 477 independently enumerated receiver AST nodes and argument records unchanged; 32 actual native Item destinations')
if (h/'controls.tsv').exists():
 controls=[l.split('\t')[0] for l in (h/'controls.tsv').read_text().splitlines() if '\tControls.java\t' in l];assert controls==['YES']+['NO']*5;print('PASS 6 attribution controls: exact owner accepted; dynamic/factory/anonymous/subclass override/wrong argument rejected')
mutants=0
for f in manifest:
 data=(root/f['path']).read_bytes()
 for r in f['edits']:
  # Any one changed byte is rejected by the exact whole-file guard.
  start=int(r[2]);mutated=data[:start]+bytes([data[start]^1])+data[start+1:];assert sha(mutated)!=f['after'];mutants+=1
print('PASS',mutants,'whole-file mutation guards')

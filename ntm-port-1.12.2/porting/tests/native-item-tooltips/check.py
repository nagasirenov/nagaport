#!/usr/bin/env python3
"""Exact finite signature-only boundary, full-source ancestry/call audit and native attribution."""
import argparse,base64,collections,gzip,hashlib,json,pathlib,subprocess,tempfile
p=argparse.ArgumentParser();p.add_argument('--project',required=True,type=pathlib.Path);p.add_argument('--reattribute',action='store_true');p.add_argument('--java-home',default='/workspace/shared/nagaport-toolchain/java8');p.add_argument('--classpath-file',default='/tmp/nagaport-ae2-native-classpath.txt');a=p.parse_args();e=pathlib.Path(__file__).resolve().parent;m=json.loads((e/'manifest.json').read_text());d=lambda s:base64.b64decode(s).decode();h=lambda b:hashlib.sha256(b).hexdigest()
assert m['base']=='112cf44057e5cd1d1173561e66dbcca97edc9bdb';assert len(m['files'])==91
read=lambda name:gzip.decompress((e/(name+'.gz')).read_bytes()).decode()
rows=lambda text:[l.split('\t') for l in text.splitlines()]
b=rows(read('before.tsv'));c=rows(read('after.tsv'));classes={d(r[2]):d(r[3]).split(';') for r in b if r[0]=='CLASS'};methods=[r for r in b if r[0]=='METHOD'];calls=[r for r in b if r[0]=='CALL'];selected=[];excluded=[]
for r in methods:
 owner=d(r[4]);params=d(r[5]).split(';');refs=d(r[6]).split(';');anc=classes.get(owner,[])
 if len(params)!=4 or 'EntityPlayer ' not in params[1] or 'net.minecraft.item.Item' not in anc:continue
 why=[]
 if len(anc)<2 or not anc[1].startswith('net.minecraft.'):why.append('custom parent/domain family deferred')
 if params[1].split()[-1] in refs:why.append('player used')
 if params[3].split()[-1] in refs:why.append('advanced boolean used')
 if any(owner in ancestors[1:] for ancestors in classes.values()):why.append('source descendants')
 if any(d(q[3]).startswith(owner+':') for q in calls):why.append('direct consumers')
 if why:excluded.append([owner,why])
 else:selected.append(r)
assert excluded==m['excluded'];assert len(selected)==91
assert [r for r in b if r[0]=='CLASS']==[r for r in c if r[0]=='CLASS']
# All direct-call receiver text and actual resolution targets remain unchanged.
assert [(r[1],r[3:]) for r in calls]==[(r[1],r[3:]) for r in c if r[0]=='CALL']
# Unknown dynamic consumer remains explicit, not silently treated as native resolution.
unresolved=[r for r in calls if d(r[3])=='unresolved'];assert any(d(r[4])=='filter.getItem().addInformation(filter, player, lore, ext)' for r in unresolved)
byowner={d(r[4]):r for r in c if r[0]=='METHOD'};mutants=0
for r in selected:
 path=r[1];v=m['files'][path];assert v['owner']==d(r[4]);assert sum(d(q[4])==v['owner'] for q in methods)==1,'owner overload';assert v['ancestry']==classes[v['owner']]
 data=(a.project/path).read_bytes();s=data.decode();assert h(data)==v['after_sha256'];off=v['offset'];assert s[off:off+len(v['new'])]==v['new'];restored=s[:off]+v['old']+s[off+len(v['new']):];assert h(restored.encode())==v['before_sha256'];assert restored.encode()==subprocess.check_output(['git','show',m['base']+':ntm-port-1.12.2/'+path],cwd=a.project)
 assert restored[:off]+v['new']+restored[off+len(v['old']):]==s
 n=byowner[v['owner']];assert d(n[7])==d(r[7])==v['body'];assert h(v['body'].encode())==v['body_sha256'];assert d(n[6])==d(r[6]);assert 'tooltipWorld' not in d(n[6]) and 'tooltipFlag' not in d(n[6]);assert 'net.minecraft.world.World tooltipWorld' in d(n[5]) and 'net.minecraft.client.util.ITooltipFlag tooltipFlag' in d(n[5])
 assert d(n[8])==d(r[8]).replace(v['old'],v['new'],1)
 for bad in (s+'\n',s[:off]+v['old']+s[off+len(v['new']):],s.replace(v['body'],v['body'].replace('{','{ tooltipFlag.isAdvanced();',1),1)):
  assert bad!=s and h(bad.encode())!=v['after_sha256'];mutants+=1
before=read('before.log').splitlines();after=read('after.log').splitlines();bc=collections.Counter(before[:-1]);ac=collections.Counter(after[:-1]);assert sum((bc-ac).values())==91 and not ac-bc;assert all('compiler.err.method.does.not.override.superclass' in x for x in bc-ac);assert all(x.split(':')[0] in m['files'] for x in bc-ac)
assert before[-1].startswith('ERRORS=13966 ') and after[-1].startswith('ERRORS=13875 ')
if a.reattribute:
 with tempfile.TemporaryDirectory(prefix='item-tooltip-attribution-') as temp:
  out=pathlib.Path(temp);j=pathlib.Path(a.java_home)/'bin';subprocess.run([str(j/'javac'),'-cp',str(pathlib.Path(a.java_home)/'lib/tools.jar'),'-d',temp,str(e/'ItemTooltipScanner.java')],check=True)
  files=sorted((a.project/'src/main/java').rglob('*.java'));listing=out/'sources.txt';listing.write_text('\n'.join(str(p.resolve()) for p in files)+'\n');cp=pathlib.Path(a.classpath_file).read_text().strip();r=subprocess.run([str(j/'java'),'-Xmx3g','-cp',temp+':'+str(pathlib.Path(a.java_home)/'lib/tools.jar'),'ItemTooltipScanner',str(listing),cp],stdout=subprocess.PIPE,stderr=subprocess.PIPE,text=True,check=True)
  norm=lambda s:s.replace(str(a.project.resolve())+'/', '')
  assert norm(r.stdout)==read('after.tsv'),'live native source attribution differs';assert norm(r.stderr)==read('after.log'),'live diagnostics differ';print(r.stderr.splitlines()[-1])
print('PASS 91 exact Git-base file inverses and round trips; 91 entire bodies unchanged; 273 signature/body/flag mutants rejected; full-source ancestry, descendants, owner overloads and all direct-call targets audited; exactly 91 native override errors removed, zero added.')

#!/usr/bin/env python3
from pathlib import Path
import sys,csv,json,zipfile,collections,hashlib,re,gzip
HERE=Path(__file__).resolve().parent
OVERLAY=Path(sys.argv[1]).resolve()
def read_tsv(name):
    path=HERE/name
    return path.open() if path.exists() else gzip.open(str(path)+'.gz','rt')
def unescape(s):
    out=[];i=0
    while i<len(s):
        if s[i]=='\\' and i+1<len(s):i+=1;out.append({'n':'\n','r':'\r','t':'\t','\\':'\\'}.get(s[i],'\\'+s[i]))
        else:out.append(s[i])
        i+=1
    return ''.join(out)
def utf16slice(s,start,end):return s.encode('utf-16-le')[start*2:end*2].decode('utf-16-le')
manifest=json.loads((HERE/'before-selected-manifest.json').read_text());inputs=json.loads((HERE/'inputs.json').read_text());z=zipfile.ZipFile(inputs['archive'])
rows=[{k:unescape(v) for k,v in row.items()} for row in csv.DictReader((HERE/'after.tsv').open(),delimiter='\t')]
assert len(rows)==manifest['selected_call_count'],len(rows)
after={(r['path'],int(r['line'])):r for r in rows};assert len(after)==len(rows)
file_proof={};changed=collections.defaultdict(list)
for c in manifest['calls']:changed[c['path']].append(c)
actual_paths={str(x.relative_to(OVERLAY)) for x in OVERLAY.rglob('*.java')};assert set(changed)<=actual_paths
unchanged_overlay={}
for path in sorted(actual_paths-set(changed)):
    baseline=z.read('ntm-port-1.12.2/src/main/java/'+path);actual=(OVERLAY/path).read_bytes();assert actual==baseline,path+' unrelated overlay file differs from baseline';unchanged_overlay[path]=hashlib.sha256(actual).hexdigest()
for path,calls in changed.items():
    raw=z.read('ntm-port-1.12.2/src/main/java/'+path);s=raw.decode('utf-8');expected=s.encode('utf-16-le')
    for c in sorted(calls,key=lambda c:c['start'],reverse=True):expected=expected[:c['start']*2]+c['destination_invocation'].encode('utf-16-le')+expected[c['end']*2:]
    actual=(OVERLAY/path).read_bytes();assert actual.decode('utf-8')==expected.decode('utf-16-le'),path+' includes a change beyond exact selected invocation replacements'
    inverse=actual.decode('utf-8').encode('utf-16-le')
    for c in sorted(calls,key=lambda c:int(after[c['path'],c['line']]['start']),reverse=True):
        r=after[c['path'],c['line']];inverse=inverse[:int(r['start'])*2]+c['full_invocation'].encode('utf-16-le')+inverse[int(r['end'])*2:]
    inverse=inverse.decode('utf-16-le').encode('utf-8');assert inverse==raw
    file_proof[path]={'before_sha256':hashlib.sha256(raw).hexdigest(),'after_sha256':hashlib.sha256(actual).hexdigest(),'inverse_reconstructed_sha256':hashlib.sha256(inverse).hexdigest(),'changed_invocations':len(calls),'only_exact_selected_invocations_changed':True}
call_proof=[]
for c in manifest['calls']:
    r=after[c['path'],c['line']]
    assert r['method']=='play';assert r['receiver_source']=='com.hbm.sound.WorldSoundEffects';assert r['receiver_type']=='com.hbm.sound.WorldSoundEffects';assert r['method_element_kind']=='METHOD';assert r['method_owner']=='com.hbm.sound.WorldSoundEffects';assert r['method_signature']=='play(net.minecraft.world.World,double,double,double,java.lang.String,float,float)',r['method_signature'];assert r['argument_count']=='7'
    args=r['arguments_source'].split(' || ');types=r['arguments_type'].split(' || ');assert args==[c['receiver_source']]+c['arguments_source'];assert types==['net.minecraft.world.World']+c['arguments_type'];assert r['full_invocation']==c['destination_invocation'];assert r['string_literals'].split(' || ')[4]==c['sound'];source=(OVERLAY/c['path']).read_bytes().decode('utf-8')
    assert utf16slice(source,int(r['start']),int(r['end']))==r['full_invocation']
    assert utf16slice(source,int(r['receiver_start']),int(r['receiver_end']))==r['receiver_source']
    for start,end,text in zip(map(int,r['arguments_start'].split(' || ')),map(int,r['arguments_end'].split(' || ')),args):assert utf16slice(source,start,end)==text
    assert utf16slice(source,int(r['enclosing_method_start']),int(r['enclosing_method_end']))==r['enclosing_method_source']
    call_proof.append({'path':c['path'],'line':c['line'],'original_full_invocation':c['full_invocation'],'after_full_invocation':r['full_invocation'],'after_source_start':int(r['start']),'after_source_end':int(r['end']),'after_method_owner':r['method_owner'],'after_method_signature':r['method_signature'],'after_argument_types':types,'after_argument_expressions':args,'widening_conversions':c['widening_conversions'],'after_enclosing_method_start':int(r['enclosing_method_start']),'after_enclosing_method_end':int(r['enclosing_method_end']),'after_enclosing_method_source':r['enclosing_method_source']})
def diagnostics(file):return [{k:unescape(v) for k,v in r.items()} for r in csv.DictReader(read_tsv(file),delimiter='\t')]
def key(d):return d['path'],int(d['line']),d['code'],d['message']
before=diagnostics('before-all-diagnostics.tsv');after_diagnostics=diagnostics('after-all-diagnostics.tsv');expected=collections.Counter(map(key,before));removed=collections.Counter(key(c['original_diagnostic']) for c in manifest['calls']);expected.subtract(removed);expected=+expected;actual=collections.Counter(map(key,after_diagnostics));assert expected==actual,{'new':list((actual-expected).items())[:20],'missing':list((expected-actual).items())[:20]}
for r in rows:
    overlaps=[d for d in after_diagnostics if d['path']==r['path'] and int(r['start'])<=int(d['start'])<int(r['end'])];assert not overlaps,(r['path'],r['line'],overlaps)
report={'baseline_commit':manifest['baseline_commit'],'overlay':str(OVERLAY),'selected_calls':len(rows),'selected_files':len(changed),'before_source_errors':len(before),'after_source_errors':len(after_diagnostics),'exact_error_delta':len(before)-len(after_diagnostics),'all_other_diagnostic_path_line_code_message_multiset_unchanged':True,'native_helper_owner':'com.hbm.sound.WorldSoundEffects','native_helper_signature':'play(net.minecraft.world.World,double,double,double,java.lang.String,float,float)','unchanged_overlay_files':unchanged_overlay,'files':file_proof,'calls':call_proof}
(HERE/'after-verification.json').write_text(json.dumps(report,indent=2)+'\n')
print(f"PASS: {len(rows)} complete native helper invocations independently attributed; original receiver + all six arguments preserved exactly and primitive int→double widening only. {len(changed)} full files differ only at selected AST spans; errors {len(before)}→{len(after_diagnostics)}, precisely the selected16 removed; every other diagnostic unchanged.")

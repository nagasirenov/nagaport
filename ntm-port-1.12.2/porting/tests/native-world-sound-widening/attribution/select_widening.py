#!/usr/bin/env python3
from pathlib import Path
import csv,json,hashlib,zipfile,collections,gzip
HERE=Path(__file__).resolve().parent
PREFIX='ntm-port-1.12.2/src/main/java/'
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
rows=[{k:unescape(v) for k,v in row.items()} for row in csv.DictReader((HERE/'before-all.tsv').open(),delimiter='\t')]
old=json.loads((HERE/'existing-world-sound-manifest.json').read_text());names=set('hbm:'+n for n in old['names']);assert len(names)==45
registry=dict(row.split('\t') for row in (HERE/'existing-registry-ast.tsv').read_text().splitlines());assert len(registry)==45 and set(registry)==names
category={'record':'RECORDS','block':'BLOCKS','player':'PLAYERS','ambient':'AMBIENT','hostile':'HOSTILE'}
for name,value in old['names'].items():assert registry['hbm:'+name]=='SoundCategory.'+category[value['category']]
inputs=json.loads((HERE/'inputs.json').read_text());z=zipfile.ZipFile(inputs['archive']);diagnostics=[{k:unescape(v) for k,v in row.items()} for row in csv.DictReader(read_tsv('before-all-diagnostics.tsv'),delimiter='\t')]
files={};methods={};selected=[];excluded=[];resources={};counts=collections.Counter();shapes=collections.Counter()
target=['double','double','double','java.lang.String','float','float'];double_types={'byte','short','char','int','long','float','double'};float_types=double_types-{'double'}
for row in rows:
    r={k:int(v) if k in ['line','start','end','select_start','select_end','receiver_start','receiver_end','argument_count','enclosing_method_start','enclosing_method_end'] else v for k,v in row.items()}
    for k in ['arguments_source','arguments_tree_kind','arguments_kind','arguments_type','arguments_start','arguments_end','string_literals']:r[k]=r[k].split(' || ')
    for k in ['arguments_start','arguments_end']:r[k]=list(map(int,r[k]))
    assert r['method']=='playSoundEffect'
    reasons=[]
    if r['receiver_type']!='net.minecraft.world.World' or r['receiver_kind']!='DECLARED':reasons.append('receiver_not_exact_native_World')
    if r['argument_count']!=6:reasons.append('not_coordinate_arity_6')
    elif r['arguments_tree_kind'][3]!='STRING_LITERAL':reasons.append('not_direct_string_literal')
    else:
        name=r['string_literals'][3]
        if name not in names:reasons.append('not_existing_45_name_allowlist')
        kinds=r['arguments_type']
        if any(kinds[i] not in double_types for i in range(3)) or kinds[3]!='java.lang.String' or any(kinds[i] not in float_types for i in (4,5)):reasons.append('not_primitive_widening_signature')
        elif kinds==target:reasons.append('already_exact_signature_not_widening_family')
    if reasons:
        counts.update(reasons);excluded.append({'path':r['path'],'line':r['line'],'invocation':r['full_invocation'],'receiver_type':r['receiver_type'],'argument_types':r['arguments_type'],'reasons':reasons});continue
    raw=z.read(PREFIX+r['path']);source=raw.decode('utf-8')
    assert utf16slice(source,r['start'],r['end'])==r['full_invocation']
    assert utf16slice(source,r['receiver_start'],r['receiver_end'])==r['receiver_source']
    for start,end,text in zip(r['arguments_start'],r['arguments_end'],r['arguments_source']):assert utf16slice(source,start,end)==text
    assert utf16slice(source,r['enclosing_method_start'],r['enclosing_method_end'])==r['enclosing_method_source']
    errs=[d for d in diagnostics if d['path']==r['path'] and int(d['line'])==r['line'] and d['code']=='compiler.err.cant.resolve.location.args' and 'playSoundEffect' in d['message'] and int(d['start'])<=r['select_start'] and int(d['end'])>=r['select_end']]
    assert len(errs)==1,(r['path'],r['line'],errs)
    r['original_diagnostic']=errs[0]
    files[r['path']]={'sha256':hashlib.sha256(raw).hexdigest(),'bytes':len(raw)}
    methodkey=r['path']+':'+str(r['enclosing_method_start']);methods[methodkey]={'path':r['path'],'start':r['enclosing_method_start'],'end':r['enclosing_method_end'],'signature':r['enclosing_method_signature'],'source':r['enclosing_method_source']};r['enclosing_method_key']=methodkey;del r['enclosing_method_source']
    r['sound']=name;r['destination_argument_types']=['net.minecraft.world.World']+target
    r['destination_invocation']='com.hbm.sound.WorldSoundEffects.play('+r['receiver_source']+', '+', '.join(r['arguments_source'])+')'
    r['widening_conversions']=[{'argument_index':i,'from':a,'to':b,'identity':a==b} for i,(a,b) in enumerate(zip(r['arguments_type'],target)) if i!=3]
    selected.append(r);resources[name]=old['names'][name[4:]];shapes.update([','.join(r['arguments_type'])])
report={'baseline_commit':inputs['commit'],'basis':'Complete genuine production AST after full JavacTask.parse + analyze; all remaining member invocations playSoundEffect scanned, no regex receiver/argument selection. Java offsets are UTF-16 code units.','universe_call_count':len(rows),'selected_call_count':len(selected),'selected_file_count':len(files),'selected_sound_count':len(resources),'allowlist_size':len(names),'argument_shapes':dict(shapes),'files':files,'sounds':resources,'calls':selected,'methods':methods,'exclusion_counts':dict(counts),'excluded':excluded}
(HERE/'before-selected-manifest.json').write_text(json.dumps(report,indent=2)+'\n')
(HERE/'selected-sites.tsv').write_text(''.join(f"{r['path']}\t{r['line']}\n" for r in selected))
(HERE/'selected-calls.txt').write_text('\n'.join(f"{r['path']}:{r['line']}\n  receiver: {r['receiver_source']} [{r['receiver_type']}]\n  arguments: {r['arguments_source']}\n  types: {r['arguments_type']}\n  original: {r['full_invocation']}\n  destination: {r['destination_invocation']}\n" for r in selected))
print(f"PASS: {len(selected)} existing-allowlist primitive-widening World calls, {len(files)} files, {len(resources)} names; {len(rows)} remaining original calls exhaustively inspected.")
print('Names: '+', '.join(sorted(resources)))
print('Shapes: '+json.dumps(dict(shapes)))
print('Paths: '+json.dumps(sorted(files)))
print('Exclusions: '+json.dumps(dict(counts)))

#!/usr/bin/env python3
"""Gate candidates solely from the attributed whole-expression AST, then retain proof."""
from pathlib import Path
import csv,json,hashlib,subprocess,zipfile
HERE=Path(__file__).resolve().parent
COMMIT='e72ec3d2ae8db6653ebaad529a574ee32c99260b'
REPO='/workspace/shared/nagaport'
PREFIX='ntm-port-1.12.2/src/main/java/'
def unescape(s):
    out=[];i=0
    while i<len(s):
        if s[i]=='\\' and i+1<len(s):
            i+=1;out.append({'n':'\n','r':'\r','t':'\t','\\':'\\'}.get(s[i],'\\'+s[i]))
        else:out.append(s[i])
        i+=1
    return ''.join(out)
def utf16slice(s,start,end):return s.encode('utf-16-le')[start*2:end*2].decode('utf-16-le')
rows=[{k:unescape(v) for k,v in row.items()} for row in csv.DictReader((HERE/'before.tsv').open(),delimiter='\t')]
sounds=json.loads((HERE/'sounds.json').read_text())
sourcezip=Path(json.loads((HERE/'inputs.json').read_text())['archive'])
z=zipfile.ZipFile(sourcezip)
files={};methods={};selected=[];excluded=[];resources={}
normalized={a[0]:(a[1],a[2]) for a in [line.split('\t') for line in (HERE/'native-resource-names.tsv').read_text().splitlines()]}
for row in rows:
    r={k:int(v) if k in ['line','start','end','select_start','select_end','receiver_start','receiver_end','argument_count','enclosing_method_start','enclosing_method_end'] else v for k,v in row.items()}
    for k in ['arguments_source','arguments_tree_kind','arguments_kind','arguments_type','arguments_start','arguments_end','string_literals']:r[k]=r[k].split(' || ')
    for k in ['arguments_start','arguments_end']:r[k]=list(map(int,r[k]))
    assert r['method']=='playSoundAtEntity',(r['path'],r['line'],'method')
    assert r['argument_count']==4,(r['path'],r['line'],'arity')
    reasons=[]
    if r['receiver_type']!='net.minecraft.world.World' or r['receiver_kind']!='DECLARED':reasons.append('receiver not attributed exact native World')
    if r['arguments_type']!=['net.minecraft.entity.player.EntityPlayer','java.lang.String','float','float']:reasons.append('arguments not attributed exact EntityPlayer,String,float,float')
    if r['arguments_tree_kind'][1]!='STRING_LITERAL':reasons.append('name is not a direct string-literal AST')
    name=r['string_literals'][1]
    if not name.startswith('hbm:'):reasons.append('non-hbm sound')
    elif name=='hbm:item.battery':reasons.append('approved existing shared-native item.battery path excluded')
    elif name[4:] not in sounds:reasons.append('sound absent from bundled sounds.json')
    if reasons:excluded.append({'path':r['path'],'line':r['line'],'invocation':r['full_invocation'],'reasons':reasons});continue
    raw=z.read(PREFIX+r['path']);source=raw.decode('utf-8')
    assert utf16slice(source,r['start'],r['end'])==r['full_invocation']
    assert utf16slice(source,r['receiver_start'],r['receiver_end'])==r['receiver_source']
    for start,end,text in zip(r['arguments_start'],r['arguments_end'],r['arguments_source']):assert utf16slice(source,start,end)==text
    assert utf16slice(source,r['enclosing_method_start'],r['enclosing_method_end'])==r['enclosing_method_source']
    files[r['path']]={'sha256':hashlib.sha256(raw).hexdigest(),'bytes':len(raw)}
    methodkey=r['path']+':'+str(r['enclosing_method_start'])
    methods[methodkey]={'path':r['path'],'start':r['enclosing_method_start'],'end':r['enclosing_method_end'],'signature':r['enclosing_method_signature'],'source':r['enclosing_method_source']}
    r['enclosing_method_key']=methodkey;del r['enclosing_method_source'];r['sound']=name
    if name not in resources:
        sound=sounds[name[4:]];samples=[]
        for value in sound['sounds']:
            sample=value if isinstance(value,str) else value['name'];domain,samplepath=normalized[sample];path='ntm-port-1.12.2/src/main/resources/assets/'+domain+'/sounds/'+samplepath+'.ogg'
            data=subprocess.check_output(['git','-C',REPO,'show',COMMIT+':'+path]);samples.append({'path':path,'bytes':len(data),'sha256':hashlib.sha256(data).hexdigest()})
        resources[name]={'bundled_definition':sound,'samples':samples}
    selected.append(r)
assert len(rows)==41,len(rows)
assert len(selected)==36,len(selected)
assert len(files)==21,len(files)
assert len(resources)==11,len(resources)
report={'baseline_commit':COMMIT,'basis':'Complete original production AST after full JavacTask.parse + analyze over 4056 real units; no fabricated fixtures or regex receivers. Java source offsets are UTF-16 code-unit offsets.','candidate_diagnostic_sites':len(rows),'selected_call_count':len(selected),'selected_file_count':len(files),'selected_sound_count':len(resources),'files':files,'sounds':resources,'calls':selected,'methods':methods,'excluded':excluded}
(HERE/'before-selected-manifest.json').write_text(json.dumps(report,indent=2)+'\n')
(HERE/'selected-sites.tsv').write_text(''.join(f"{r['path']}\t{r['line']}\n" for r in selected))
(HERE/'selected-calls.txt').write_text('\n'.join(f"{r['path']}:{r['line']}\n  receiver: {r['receiver_source']} [World]\n  args: {', '.join(r['arguments_source'])} [EntityPlayer, String, float, float]\n  invocation: {r['full_invocation']}\n" for r in selected))
print(f"PASS: {len(selected)} exact World + EntityPlayer,String,float,float literal bundled calls, {len(files)} files, {len(resources)} sounds; all receiver/argument/invocation/enclosing method spans match exact CP59 source.")
print('Names: '+', '.join(sorted(resources)))
print('Excluded: '+json.dumps(excluded))

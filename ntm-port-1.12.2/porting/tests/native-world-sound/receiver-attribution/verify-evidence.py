#!/usr/bin/env python3
"""Verify only javac-emitted selections/attribution; no regex receiver recognizer."""
import csv, hashlib, json, sys
from pathlib import Path
P=Path(__file__).resolve().parent

def rows(name):
    values=list(csv.DictReader((P/name).open(), delimiter='\t'))
    result={(r['path'], int(r['line'])):r for r in values}
    assert len(result)==len(values)==96, (name,len(result),len(values))
    return result

m=json.loads((P/'manifest-frozen.json').read_text())
selected={(f['path'], c['line']) for f in m['files'] for c in f['changes']}
assert len(selected)==96
before=rows('before.tsv'); corrected=rows('corrected.tsv'); current=rows('current.tsv')
spans=rows('before-spans.tsv')
for group in (before,corrected,current,spans): assert set(group)==selected
malformed=[]
for key,b in before.items():
    assert (b['receiver_kind'],b['receiver_type'])==('DECLARED','net.minecraft.world.World'), key
    assert int(b['argument_count'])==6 and 'ERROR' not in b['arguments_kind'], key
    a=corrected[key]
    assert a['receiver_source']=='com.hbm.sound.WorldSoundEffects',key
    assert a['method_element_kind']=='METHOD' and a['method_owner']=='com.hbm.sound.WorldSoundEffects',key
    assert a['method_signature']=='play(net.minecraft.world.World,double,double,double,java.lang.String,float,float)',key
    assert a['arguments_kind'].split(' || ')[0]=='DECLARED',key
    assert a['arguments_type'].split(' || ')[0]=='net.minecraft.world.World',key
    assert a['arguments_source']==b['receiver_source']+' || '+b['arguments_source'],key
    assert a['arguments_type']=='net.minecraft.world.World || '+b['arguments_type'],key
    assert 'ERROR' not in a['arguments_kind'],key
    c=current[key]
    if c['receiver_source']!='com.hbm.sound.WorldSoundEffects' or c['arguments_source']!=b['receiver_source']+' || '+b['arguments_source']:
        malformed.append({'path':key[0],'line':key[1],'before_receiver':b['receiver_source'],'malformed_receiver':c['receiver_source'],'malformed_receiver_type':c['receiver_type'],'malformed_method_kind':c['method_element_kind'],'malformed_method_owner':c['method_owner'],'malformed_first_argument_type':c['arguments_type'].split(' || ')[0]})
    else:
        assert c['method_element_kind']=='METHOD' and c['method_owner']=='com.hbm.sound.WorldSoundEffects', key
        assert c['method_signature']=='play(net.minecraft.world.World,double,double,double,java.lang.String,float,float)', key
        assert c['arguments_type']=='net.minecraft.world.World || '+b['arguments_type'], key
        assert 'ERROR' not in c['arguments_kind'], key
assert len(malformed)==1 and malformed[0]['path'].endswith('/XFactoryAccelerator.java') and malformed[0]['line']==147, malformed
report={'baseline':m['baseline'],'source_call_count':96,'original_native_world_receivers':96,'corrected_native_world_first_arguments':96,'corrected_executable_owner_count':96,'corrected_exact_argument_preservation_count':96,'malformed_original_candidate_sites':malformed,'overall_compilation_success':False,'diagnostics_note':'This is targeted javac type attribution amid pre-existing unfinished-port errors, not full-project compilation success.'}
(P/'audit-summary.json').write_text(json.dumps(report,indent=2)+'\n')
inputs={}
if len(sys.argv)==4:
    for version,root in zip(('before','current','corrected'),sys.argv[1:]):
        inputs[version]={f['path']:hashlib.sha256((Path(root)/f['path']).read_bytes()).hexdigest() for f in m['files']}
    (P/'selected-source-sha256.json').write_text(json.dumps(inputs,indent=2)+'\n')
else:
    assert len(sys.argv)==1, 'Pass all three source roots, or none to validate recorded attribution only.'
print(json.dumps(report,indent=2))

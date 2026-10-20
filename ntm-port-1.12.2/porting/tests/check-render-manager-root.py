#!/usr/bin/env python3
"""Reversible constructor-only/source-registration audit against the recorded pre-change commit.
No Minecraft execution. Rendering methods/fields must be byte-identical after reversing only
manifested constructor/import changes. Other workers may change unrelated ClientProxy registrations.
"""
import hashlib,json,pathlib,re
root=pathlib.Path(__file__).resolve().parents[2]
m=json.loads((root/'porting/tests/render-manager-root-manifest.json').read_text())
assert len(m['renderers'])==52 and len(m['registrations'])==94
for entry in m['renderers']:
    text=(root/entry['path']).read_text()
    for change in reversed(entry['changes']):
        assert text.count(change['new'])==change['count'],(entry['path'],'expected exact constructor/import change',change['new'])
        text=text.replace(change['new'],change['old'])
    assert hashlib.sha256(text.encode()).hexdigest()==entry['baseline_sha256'],(entry['path'],'rendering body/field/original constructor changed')
proxy=(root/'src/main/java/com/hbm/main/ClientProxy.java').read_text()
positions=[]
for entry in m['registrations']:
    assert proxy.count(entry['new'])==1,(entry['entity'],'missing/duplicate factory or eager capture')
    positions.append(proxy.index(entry['new']))
    assert entry['old'] not in proxy,(entry['entity'],'old instance registration remains')
    assert 'getRenderManager' not in entry['new'],'factory must use supplied manager'
    assert entry['new'].count('new '+entry['renderer']+'(')==1,'one renderer construction per registration'
assert positions==sorted(positions),'factory registration order changed'
assert sum('final Item[]' in e['new'] for e in m['registrations'])==4
print('PASS: 52 renderer files revert exactly to baseline SHA256; 94 distinct factory registration sites and four eager item-array captures preserved.')
print('BASELINE:',m['base_commit'])
print('LIMIT: source invariant audit, not compilation or execution of 52 complete renderer bodies.')

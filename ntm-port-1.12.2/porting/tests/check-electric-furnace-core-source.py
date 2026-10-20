#!/usr/bin/env python3
"""Four complete exact UTF-8 source reversals before checkpoint17 historical audit layers.
No Git/pristine object required. Unknown paths pass through; known drift fails closed.
"""
import hashlib
import json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
MANIFEST=json.loads(Path(__file__).with_name('electric-furnace-core-source-manifest.json').read_text(encoding='utf-8'))
FILES={f['path']:f for f in MANIFEST['files']}
def sha(text):return hashlib.sha256(text.encode('utf-8')).hexdigest()
def reverse_source(relative,text):
    entry=FILES.get(str(relative).replace('\\','/'))
    if entry is None:return text
    assert sha(text)==entry['after_sha256'],(relative,'furnace native whole-file drift')
    for edit in reversed(entry['edits']):
        pos=edit['after_offset'];new=edit['new']
        assert text[pos:pos+len(new)]==new,(relative,'furnace reverse edit mismatch')
        text=text[:pos]+edit['old']+text[pos+len(new):]
    assert sha(text)==entry['before_sha256'],(relative,'furnace baseline full-file mismatch')
    return text
def main():
    for relative,entry in FILES.items():
        text=(ROOT/relative).read_bytes().decode('utf-8');before=reverse_source(relative,text)
        for edit in reversed(entry['edits']):
            pos=edit['before_offset'];old=edit['old']
            assert before[pos:pos+len(old)]==old,(relative,'forward old mismatch')
            before=before[:pos]+edit['new']+before[pos+len(old):]
        assert before==text,(relative,'exact forward roundtrip')
        for mutation in [text+'\n',text.replace('public ','private ',1),text[:-1]]:
            try:reverse_source(relative,mutation)
            except AssertionError:pass
            else:raise AssertionError((relative,'mutation accepted'))
    for relative,expected in MANIFEST['unchanged_support'].items():
        assert sha((ROOT/relative).read_bytes().decode('utf-8'))==expected,(relative,'unchanged support drift')
    child=(ROOT/'src/main/java/com/hbm/items/machine/ItemMetaUpgrade.java').read_text(encoding='utf-8')
    assert 'addInformation(' not in child,'sole subtype unexpectedly overrides tooltip'
    container=(ROOT/'src/main/java/com/hbm/inventory/container/ContainerElectricFurnace.java').read_text(encoding='utf-8')
    assert '.onTake(' not in container,'unrequested output callback introduced'
    assert reverse_source('unrelated.java','arbitrary')=='arbitrary'
    print('PASS four exact furnace whole-file reverse/forward reconstructions; 12 drift mutations rejected; unchanged upgrade manager/slot/subtype and no added onTake checked.')
if __name__=='__main__':main()

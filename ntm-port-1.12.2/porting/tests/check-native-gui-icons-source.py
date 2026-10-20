#!/usr/bin/env python3
"""CP25 exact reversible GUI/icon source boundary; not runtime/GL parity proof."""
import hashlib,json,importlib.util
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
_spec=importlib.util.spec_from_file_location('native_callback_items_source',ROOT/'porting/tests/check-native-callback-items-source.py')
_callback=importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_callback)
RAW=Path(__file__).with_name('native-gui-icons-source-manifest.json').read_bytes()
assert hashlib.sha256(RAW).hexdigest() == '9d474bc83015da04fe1484e124c02a8e248fea66292117ba855f825ab92739e4'
MANIFEST=json.loads(RAW)
assert MANIFEST['baseline']=='13c8930411cbff2653a9f75e9818c8af2b900394'
FILES=MANIFEST['files']; NEW_FILES=MANIFEST['new_files']
assert len(FILES)==8 and len(NEW_FILES)==3

def digest(text): return hashlib.sha256(text.encode('utf-8')).hexdigest()
def reverse_source(relative,text):
    text=_callback.reverse_source(relative,text)
    entry=FILES.get(relative)
    if entry is None:
        if relative in NEW_FILES: assert digest(text)==NEW_FILES[relative],(relative,'new native source drift')
        return text
    assert digest(text)==entry['after_sha256'],(relative,'native source drift')
    for edit in reversed(entry['edits']):
        off=edit['after_offset']; new=edit['new']
        assert text[off:off+len(new)]==new
        text=text[:off]+edit['old']+text[off+len(new):]
    assert digest(text)==entry['before_sha256'],(relative,'baseline drift')
    return text

def verify(root=ROOT,verbose=True):
    _callback.verify(root,verbose)
    rejected=0
    for relative in sorted(set(FILES)|set(NEW_FILES)):
        current=(Path(root)/relative).read_bytes().decode('utf-8')
        before=reverse_source(relative,current)
        if relative in FILES:
            forward=before
            for edit in reversed(FILES[relative]['edits']):
                off=edit['before_offset']; old=edit['old']
                assert forward[off:off+len(old)]==old
                forward=forward[:off]+edit['new']+forward[off+len(old):]
            assert forward==_callback.reverse_source(relative,current)
        for mutant in (current+'\n',current[:-1],current.replace('\r\n','\n') if '\r\n' in current else current.replace('\n','\r\n')):
            assert mutant!=current
            try: reverse_source(relative,mutant)
            except AssertionError: rejected+=1
            else: raise AssertionError((relative,'drift accepted'))
    assert reverse_source('unrelated.java','\ufeffuntouched\r\n')=='\ufeffuntouched\r\n'
    if verbose: print('PASS GUI/icon source: 8 byte-exact inverses/round trips, 3 retained new sources, %d rejected mutations. Not runtime/GL proof.' % rejected)
    return MANIFEST
if __name__=='__main__':
    import sys
    assert not sys.flags.optimize
    verify()

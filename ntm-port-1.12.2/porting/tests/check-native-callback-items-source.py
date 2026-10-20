#!/usr/bin/env python3
"""CP26 exact reversible callback item source boundary; not runtime/GL parity proof."""
import hashlib,json,importlib.util
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
_spec=importlib.util.spec_from_file_location('registry_connections_source',ROOT/'porting/tests/check-native-registry-connections-source.py')
_newer=importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_newer)
RAW=Path(__file__).with_name('native-callback-items-source-manifest.json').read_bytes()
assert hashlib.sha256(RAW).hexdigest() == 'b0f6d5e9062bbc600c4c3f0944267ee605bd8cefbecdd166d7a084bc561f20b7'
MANIFEST=json.loads(RAW)
assert MANIFEST['baseline']=='40f6979edd0a80cb7a94cfa91410f40aa5fd02a6'
FILES=MANIFEST['files']; NEW_FILES=MANIFEST['new_files']
assert len(FILES)==77 and len(NEW_FILES)==2

def digest(text): return hashlib.sha256(text.encode('utf-8')).hexdigest()
def reverse_source(relative,text):
    text=_newer.reverse_source(relative,text)
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
    _newer.verify(root,verbose)
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
            assert forward==_newer.reverse_source(relative,current)
        for mutant in (current+'\n',current[:-1],current.replace('\r\n','\n') if '\r\n' in current else current.replace('\n','\r\n')):
            assert mutant!=current
            try: reverse_source(relative,mutant)
            except AssertionError: rejected+=1
            else: raise AssertionError((relative,'drift accepted'))
    assert reverse_source('unrelated.java','\ufeffuntouched\r\n')=='\ufeffuntouched\r\n'
    if verbose: print('PASS callback item source: 77 byte-exact inverses/round trips, 2 retained new sources, %d rejected mutations. Not runtime/GL proof.' % rejected)
    return MANIFEST
if __name__=='__main__':
    import sys
    assert not sys.flags.optimize
    verify()

#!/usr/bin/env python3
"""CP31 exact reversible Sedna source boundary; not runtime/GL parity proof."""
import hashlib,json,importlib.util
from pathlib import Path
ROOT=Path(__file__).resolve().parents[2]
_spec=importlib.util.spec_from_file_location('conveyor_boundary',ROOT/'porting/tests/check-native-conveyor-boundary.py')
_next=importlib.util.module_from_spec(_spec)
_spec.loader.exec_module(_next)
RAW=Path(__file__).with_name('native-sedna-boundary-manifest.json').read_bytes()
assert hashlib.sha256(RAW).hexdigest() == 'b0e36f4752154a54676041defd1b8a0c4e6d7f2d6985550f6958ddfebbf9ca7a'
MANIFEST=json.loads(RAW)
assert MANIFEST['baseline']=='0ea420fc3aede5c97847724502bd47396698706a'
FILES=MANIFEST['files']; NEW_FILES=MANIFEST['new_files']
assert len(FILES)==59 and len(NEW_FILES)==7

def digest(text): return hashlib.sha256(text.encode('utf-8')).hexdigest()
def reverse_source(relative,text):
    text=_next.reverse_source(relative,text)
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
    _next.verify(root,verbose)
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
            assert forward==_next.reverse_source(relative,current)
        for mutant in (current+'\n',current[:-1],current.replace('\r\n','\n') if '\r\n' in current else current.replace('\n','\r\n')):
            assert mutant!=current
            try: reverse_source(relative,mutant)
            except AssertionError: rejected+=1
            else: raise AssertionError((relative,'drift accepted'))
    assert reverse_source('unrelated.java','\ufeffuntouched\r\n')=='\ufeffuntouched\r\n'
    if verbose: print('PASS Sedna source: 59 byte-exact inverses/round trips, 7 retained new sources, %d rejected mutations. Not runtime/GL proof.' % rejected)
    return MANIFEST
if __name__=='__main__':
    import sys
    assert not sys.flags.optimize
    verify()

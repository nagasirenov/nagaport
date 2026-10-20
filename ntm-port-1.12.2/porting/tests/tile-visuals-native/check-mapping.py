#!/usr/bin/env python3
"""Exact three-file CP49 inverse for historical guards. Source proof, not runtime proof."""
from pathlib import Path
import argparse,hashlib,json
HERE=Path(__file__).resolve().parent
RAW=(HERE/'source-manifest.json').read_bytes()
assert hashlib.sha256(RAW).hexdigest()=='474a58b4419a5c7fd4053b80394606f25d390dc2df087e2cdfb973f512b0a2c4','Frozen source-mapping specification drift'
MANIFEST=json.loads(RAW)
assert set(MANIFEST['complete_sources'])=={'src/main/java/com/hbm/render/tileentity/'+n+'.java' for n in ['RenderMagma','RenderMultiblock','RenderSoyuzMultiblock']}
def digest(text):return hashlib.sha256(text.encode('utf-8')).hexdigest()
def reverse_source(relative,text):
    relative=Path(relative).as_posix();entry=MANIFEST['complete_sources'].get(relative)
    if entry is None:return text
    assert digest(text)==entry['native_sha256'],('Changed native tile visual source',relative)
    before=text
    for edit in reversed(entry['edits']):
        at=edit['native_start'];assert before[at:at+len(edit['after'])]==edit['after']
        before=before[:at]+edit['before']+before[at+len(edit['after']):]
    assert digest(before)==entry['baseline_sha256'],('Incorrect CP49 inverse',relative)
    after=before
    for edit in reversed(entry['edits']):
        at=edit['baseline_start'];assert after[at:at+len(edit['before'])]==edit['before']
        after=after[:at]+edit['after']+after[at+len(edit['before']):]
    assert after==text,('Incorrect forward roundtrip',relative)
    return before

def check(root):
    rejected=0
    for relative in MANIFEST['complete_sources']:
        text=(root/relative).read_bytes().decode('utf-8');reverse_source(relative,text)
        for change in [text+'\n',text.replace('\n','\r\n'),text.replace('public class ','public /* drift */ class ',1)]:
            assert change!=text
            try:reverse_source(relative,change)
            except AssertionError:rejected+=1
            else:raise AssertionError('Accepted source drift: '+relative)
    print('PASS three exact CP49 inverse/forward source roundtrips and %d rejected source/CRLF drift mutations; no shared production edits.'%rejected)
if __name__=='__main__':
    p=argparse.ArgumentParser();p.add_argument('--module',type=Path,default=HERE.parents[2]);a=p.parse_args();check(a.module)

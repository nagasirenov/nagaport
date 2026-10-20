#!/usr/bin/env python3
"""Reuse unchanged original/native engine tests, adjusting ONLY integration call counts."""
import json,runpy,sys
from pathlib import Path
HERE=Path(__file__).resolve().parent; old=HERE.parent/'native-world-reads'
sys.path.insert(0,str(HERE));import check_source;check_source.check()
sys.path.insert(0,str(old));import audit
original_manifest=audit.manifest
increment=json.loads((HERE/'manifest.json').read_text())
def combined():
 m=original_manifest()
 for rel,r in increment['files'].items():
  m['files'].setdefault(rel,{'edits':[]})['edits'].extend(r['edits'])
 return m
audit.manifest=combined
assert '--attribute' not in sys.argv
sys.argv.append('--integrated')
runpy.run_path(str(old/'probe.py'),run_name='__main__')

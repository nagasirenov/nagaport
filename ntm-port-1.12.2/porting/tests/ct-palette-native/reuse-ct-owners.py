#!/usr/bin/env python3
"""Rerun existing CT native integration; source invariance only for selected BlockDecoCT owner.
Unrelated reactor breakBlock methods legitimately migrated since CP49. They are not this batch.
The unmodified sibling proof remains frozen and its broader old-source assertion is not weakened.
"""
from pathlib import Path
import hashlib
p=Path(__file__).resolve().parent.parent/'ct-native/owners.py'
manifest=__import__('json').loads((Path(__file__).resolve().parent/'owner-manifest.json').read_text());assert hashlib.sha256(p.read_bytes()).hexdigest()==manifest['files']['porting/tests/ct-native/owners.py']
s=p.read_text()
needle="}.items():\n with zipfile.ZipFile(H/'cp48-mechanics-sources.zip') as z:"
assert s.count(needle)==1
s=s.replace(needle,"}.items():\n if rel!='generic/BlockDecoCT.java':continue # only this actual owner is selected\n with zipfile.ZipFile(H/'cp48-mechanics-sources.zip') as z:")
s=s.replace('PASS exact CP48 mechanics method invariance including complete ICF/PWR nested tile bodies','PASS exact CP48 selected BlockDecoCT mechanics method invariance; unrelated reactor methods not source-certified by this rerun')
exec(compile(s,str(p),'exec'),{'__file__':str(p),'__name__':'__main__'})

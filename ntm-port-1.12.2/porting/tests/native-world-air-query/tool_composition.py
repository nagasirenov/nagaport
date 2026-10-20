"""Strict CP93 World-air inverse for the prior tool-attribute proof only."""
from pathlib import Path
import hashlib,importlib.util,json
REL='src/main/java/com/hbm/items/tool/ItemToolAbility.java'
SCRIPT_SHA='eabfba7eeb68b425db5b0cf490e96f7a6fd6c81b64eebb335d7ad73851a7dd53'
MANIFEST_SHA='c02b61534d8b3ff56bbc17953017e9dcf1fe59825fa57a3878156d08aae56a35'
def normalize(root,rel,raw):
 if rel!=REL:return raw
 h=Path(root)/'porting/tests/native-world-air-query'
 assert hashlib.sha256((h/'check_source.py').read_bytes()).hexdigest()==SCRIPT_SHA
 assert hashlib.sha256((h/'manifest.json').read_bytes()).hexdigest()==MANIFEST_SHA
 spec=importlib.util.spec_from_file_location('world_air_cp93_inverse',h/'check_source.py');m=importlib.util.module_from_spec(spec);spec.loader.exec_module(m)
 e=m.data['files'][rel]
 if hashlib.sha256(raw).hexdigest()==e['before']:return raw
 return m.restore_base(rel,raw)

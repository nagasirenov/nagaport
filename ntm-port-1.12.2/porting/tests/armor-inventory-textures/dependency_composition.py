#!/usr/bin/env python3
"""Strict composition of the one independently audited CP83 item-metadata dependency delta."""
import hashlib,json
from pathlib import Path
import migrate
HERE=Path(__file__).resolve().parent
HANDLER='src/main/java/com/hbm/render/item/NativeItemTextureModelHandler.java'
OLD_SHA='eda9237a3df2c6d55d7758c852e007b3534db8aa77f3cb9c2e83b15c85344ac7'
FIXED_SHA='c2b5cba0fb608e58d7833ca3a5009ac1dc19254a307820c074ff12dc9686d761'
FOUNDATION_SHA='1b7f34d19c9bf3035af47c70233fb3a6be130d7831bbb8005b7af709528a7675'
OLD=b'''                // The original default Item icon is metadata-insensitive. A
                // plain subtype item still uses that same image for every meta.
                if(item.getHasSubtypes()) ModelLoader.setCustomMeshDefinition(item, stack -> location);
                else ModelLoader.setCustomModelResourceLocation(item, 0, location);
'''
NEW=b'''                // The original default Item icon is metadata-insensitive even
                // for non-damageable items that do not declare subtypes.
                ModelLoader.setCustomMeshDefinition(item, stack -> location);
'''
def sha(data):return hashlib.sha256(data).hexdigest()
def restore_cp82(data):
 """Accept only exact historical bytes or exact fixed bytes, then invert the one known edit."""
 digest=sha(data)
 assert digest in (OLD_SHA,FIXED_SHA),('Unrecognized native texture handler drift',digest)
 if digest==OLD_SHA:
  restored=data
 else:
  assert data.count(NEW)==1 and OLD not in data,'Known dependency replacement missing or duplicated'
  restored=data.replace(NEW,OLD,1)
 assert sha(restored)==OLD_SHA,'Dependency inverse does not recover exact CP82 handler'
 assert restored==migrate.baseline(HANDLER).encode(),'Immutable CP82 dependency mismatch'
 return restored

def verify_receipt():
 before=migrate.baseline(HANDLER).encode();assert sha(before)==OLD_SHA and before.count(OLD)==1
 after=before.replace(OLD,NEW,1);assert sha(after)==FIXED_SHA
 assert restore_cp82(before)==before and restore_cp82(after)==before
 rejected=0
 for mutant in (after+b' ',after[:-1],after.replace(b'if(!hasCustomRoute(item))',b'if(true)',1),after.replace(b'if(!eligible(item)) continue;',b'if(false) continue;',1),after.replace(NEW,OLD+NEW,1)):
  assert mutant!=after
  try:restore_cp82(mutant)
  except AssertionError:rejected+=1
  else:raise AssertionError('Unreviewed dependency mutation accepted')
 receipt={'baseline_commit':migrate.BASE,'independent_fix_commit':'f1b2c7f617a16175a606b99506193782b913cbaf','path':HANDLER,'baseline_sha256':OLD_SHA,'fixed_sha256':FIXED_SHA,'exact_old':OLD.decode(),'exact_new':NEW.decode(),'reconstruction':'Require exact fixed hash; replace exact new block once with exact old block; require exact CP82 bytes and hash before historical guard comparison.','rejected_mutations':rejected,'foundation_fixture_sha256':FOUNDATION_SHA}
 assert receipt==json.loads((HERE/'dependency-composition-receipt.json').read_text())
 return receipt

if __name__=='__main__':
 verify_receipt();data=(migrate.ROOT/HANDLER).read_bytes();restore_cp82(data)
 print('PASS strict dependency composition:',sha(data),'-> exact CP82',OLD_SHA,'; five unreviewed mutations rejected.')

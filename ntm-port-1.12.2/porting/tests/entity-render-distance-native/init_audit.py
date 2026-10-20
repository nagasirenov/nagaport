#!/usr/bin/env python3
"""Bounded superclass-constructor callback audit; no claim about foreign Forge event handlers."""
from pathlib import Path
import hashlib,json,os,re,subprocess
H=Path(__file__).resolve().parent;ROOT=H.parents[2]
J=Path(os.environ.get('JAVA8_HOME','/workspace/shared/nagaport-toolchain/java8'))
CP=Path(os.environ.get('NATIVE_CP_FILE','/workspace/shared/nagaport-toolchain/native-classpath.txt')).read_text().strip()
def inspect():
 out={'entity_init':{},'native_data_manager':{}}
 for owner,count in [('EntityBullet',3),('EntityExplosiveBeam',1),('EntityRocket',1),('EntitySchrab',1)]:
  text=(ROOT/('src/main/java/com/hbm/entity/projectile/'+owner+'.java')).read_text()
  body=re.search(r'protected void entityInit\(\)\s*\{([^{}]*)\}',text).group(1)
  statements=[s.strip() for s in body.split(';') if s.strip()]
  assert len(statements)==count and all(re.fullmatch(r'this\.dataManager\.register\((CRITICAL_FLAGS|TAU_FLAGS|CHOPPER_FLAGS), Byte\.valueOf\(\(byte\)\s*0\)\)',s) for s in statements)
  assert 'isInRangeToRender' not in body
  out['entity_init'][owner]=statements
 closure=json.loads((H/'dispatch-closure.json').read_text())
 assert not any(p in out['entity_init'] for c,p in closure['parents'].items()),'unexpected local descendant of variable-weight owner'
 native=subprocess.check_output([str(J/'bin/javap'),'-p','-c','-classpath',CP,'net.minecraft.network.datasync.EntityDataManager'],text=True)
 for header in ['public <T> void register(net.minecraft.network.datasync.DataParameter<T>, T);','private <T> void setEntry(net.minecraft.network.datasync.DataParameter<T>, T);']:
  pos=native.index('\n  '+header);m=re.search(r'\n  \S',native[pos+3:]);body=native[pos:pos+3+m.start()] if m else native[pos:]
  assert 'net/minecraft/entity/' not in body and 'Field entity:' not in body and 'notifyDataManagerChange' not in body
  out['native_data_manager'][header]={'sha256':hashlib.sha256(body.encode()).hexdigest(),'bytecode':body}
 return out
if __name__=='__main__':
 assert inspect()==json.loads((H/'init-audit.json').read_text()),'initialization callback drift'
 print('PASS four variable-weight owners have only data-register entityInit calls, no local descendants; actual native register/setEntry do not call back into an Entity or read it. Foreign constructor re-entrancy remains unsupported.')

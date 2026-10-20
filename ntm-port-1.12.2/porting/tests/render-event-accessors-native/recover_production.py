#!/usr/bin/env python3
"""Recovery from exact historical javac token positions. Not a fresh attribution run."""
from pathlib import Path
import hashlib,json,subprocess
HERE=Path(__file__).resolve().parent; ROOT=HERE.parents[2]
RECOVERY='d25dcb9f0908d76b1e9eca96b75e58f6397de355'
PIN='6fdceab3cef00dddfbccbc7d7d8af309f425ee54613ca067b1cb20d559e64337'
# UTF-16 positions copied from the historical scanner output in retained task history.
scopes={
'com/hbm/blocks/generic/Guide.java':[(4144,'resolution','overlay.Pre')],
'com/hbm/blocks/machine/MachineCrucible.java':[(5053,'player','highlight'),(5084,'partialTicks','highlight'),(6030,'context','highlight')],
'com/hbm/items/armor/ArmorHEV.java':[(1828,'type','overlay.Pre'),(1916,'type','overlay.Pre'),(2767,'resolution','overlay.Pre')],
'com/hbm/items/tool/ItemToolAbility.java':[(24877,'resolution','overlay.Pre'),(24932,'resolution','overlay.Pre')],
'com/hbm/items/weapon/sedna/ItemGunBaseNT.java':[(22365,'resolution','overlay.Pre')],
'com/hbm/items/weapon/sedna/impl/ItemGunStinger.java':[(4870,'resolution','overlay.Pre'),(5013,'resolution','overlay.Pre')],
'com/hbm/main/ModEventHandlerClient.java':[(6689,'type','overlay.Pre'),(6838,'resolution','overlay.Pre'),(6889,'resolution','overlay.Pre'),(8894,'type','overlay.Pre'),(9018,'type','overlay.Pre'),(9329,'resolution','overlay.Pre'),(9442,'type','overlay.Pre'),(15255,'type','overlay.Pre'),(15467,'resolution','overlay.Pre'),(16104,'resolution','overlay.Pre'),(16367,'type','overlay.Pre'),(16534,'resolution','overlay.Pre'),(16782,'type','overlay.Pre'),(16959,'resolution','overlay.Pre'),(18049,'type','overlay.Post'),(18172,'resolution','overlay.Post'),(18266,'type','overlay.Post'),(18423,'resolution','overlay.Post'),(18525,'type','overlay.Post'),(19250,'resolution','overlay.Post'),(19301,'resolution','overlay.Post'),(51931,'partialTicks','worldlast'),(52017,'partialTicks','worldlast'),(52103,'partialTicks','worldlast'),(54057,'partialTicks','worldlast'),(54915,'partialTicks','worldlast'),(54978,'partialTicks','worldlast'),(55154,'entity','living.Pre'),(55583,'entity','living.Pre'),(55649,'entity','living.Pre'),(55684,'entity','living.Pre'),(55909,'entity','living.Pre'),(55923,'x','living.Pre'),(55932,'y','living.Pre'),(55941,'z','living.Pre'),(55950,'renderer','living.Pre')],
'com/hbm/main/ModEventHandlerRenderer.java':[(3823,'partialTicks','highlight'),(3911,'target','highlight'),(4005,'player','highlight'),(4204,'player','highlight'),(4335,'player','highlight'),(5084,'entity','view.FogDensity'),(5211,'entity','view.FogDensity'),(6558,'renderPartialTicks','view.FogColors'),(7500,'type','overlay.Pre'),(7603,'resolution','overlay.Pre'),(7668,'type','overlay.Pre'),(8307,'type','overlay.Pre'),(8413,'resolution','overlay.Pre'),(8464,'resolution','overlay.Pre')],
'com/hbm/render/util/RenderInfoSystem.java':[(1603,'type','overlay.Pre'),(1998,'resolution','overlay.Pre')],
'com/hbm/tileentity/machine/rbmk/TileEntityRBMKBase.java':[(8612,'resolution','overlay.Pre')]
}
owners={'overlay':'RenderGameOverlayEvent','highlight':'DrawBlockHighlightEvent','worldlast':'RenderWorldLastEvent','living':'RenderLivingEvent','view':'EntityViewRenderEvent'}
sha=lambda b:hashlib.sha256(b).hexdigest()
files=[]
for relative,selected in sorted(scopes.items()):
 rel='src/main/java/'+relative;path=ROOT/rel;before=path.read_bytes()
 expected=subprocess.check_output(['git','-C',str(ROOT.parent),'show',RECOVERY+':ntm-port-1.12.2/'+rel]);assert before==expected,(rel,'does not match exact recovered CP76')
 data=before.decode().encode('utf-16-le');edits=[]
 for start,old,alias in reversed(selected):
  end=start+len(old);new='get'+old[0].upper()+old[1:]+'()'
  assert data[start*2:end*2].decode('utf-16-le')==old,(rel,start,old)
  assert data[(start-6)*2:start*2].decode('utf-16-le')=='event.',(rel,start,'receiver differs')
  base,_,sub=alias.partition('.');owner='net.minecraftforge.client.event.'+owners[base];receiver=owner+('.'+sub if sub else '')
  data=data[:start*2]+new.encode('utf-16-le')+data[end*2:]
  edits.append(dict(start=start,end=end,old=old,new=new,receiver=receiver,owner=owner))
 after=data.decode('utf-16-le').encode();path.write_bytes(after)
 files.append(dict(path=rel,before=sha(before),after=sha(after),edits=sorted(edits,key=lambda e:e['start'])))
assert sum(len(f['edits']) for f in files)==65 and len(files)==10
(HERE/'source-manifest.json').write_text(json.dumps(dict(baseline='0aeda80b859b80afc8af26398366ef05f68478af',recovery_commit=RECOVERY,attribution='Recovered historical token inventory; not freshly attributed in replacement workspace',files=files),indent=2)+'\n')
patch=subprocess.check_output(['git','-C',str(ROOT.parent),'diff','--binary','--','ntm-port-1.12.2/src/main/java'])
assert sha(patch)==PIN,('Patch differs from historically retained SHA256',sha(patch))
out=Path('/workspace/shared/nagaport-render-event-accessors-production.patch');out.write_bytes(patch)
(HERE/'recovery-verification.json').write_text(json.dumps(dict(recovery_commit=RECOVERY,production_patch_sha256=sha(patch),matches_historical_patch=True,selected_tokens=65,production_files=10,tests_current_environment='NOT_RUN: authentic native classpath absent; restoration pending',historical_results='Prior report only: 3771 native assertions; 19 attribution fixtures; 65 negative compile controls; 65 source mutants; one runtime mutant; analysis 13021 to 12956. Original logs lost.'),indent=2)+'\n')
print('EXACT PRODUCTION RECOVERY:',len(patch),'bytes SHA256',sha(patch))

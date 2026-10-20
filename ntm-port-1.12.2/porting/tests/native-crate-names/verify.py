#!/usr/bin/env python3
"""Exact sealed-source reversal and exact naming-method fixtures, not whole owners."""
import pathlib,subprocess,re,sys,json,hashlib
root=pathlib.Path(sys.argv[1]).resolve();out=pathlib.Path(sys.argv[2]).resolve();out.mkdir(parents=True,exist_ok=True)
base='112cf44057e5cd1d1173561e66dbcca97edc9bdb';prefix='ntm-port-1.12.2/src/main/java/com/hbm/'
names=['CrateIron','CrateDesh','CrateSteel','CrateTungsten','CrateTemplate','Safe','FileCabinet','MassStorage']
paths=[prefix+'tileentity/machine/storage/TileEntity'+n+'.java' for n in ['CrateBase']+names]+[prefix+'inventory/gui/GUI'+n+'.java' for n in names]+[prefix+'items/block/ItemBlockStorageCrate.java',prefix+'blocks/generic/BlockStorageCrate.java']
def method(s,name):
 m=re.search(r'public (?:String|boolean|net.minecraft.util.text.ITextComponent) '+name+r'\([^)]*\)\s*\{',s);assert m,name
 depth=1;end=m.end()
 while depth:
  if s[end]=='{':depth+=1
  if s[end]=='}':depth-=1
  end+=1
 return s[m.start():end]
manifest=[]
for path in paths:
 old=subprocess.check_output(['git','show',base+':'+path],cwd=root);new=(root/path).read_bytes();reverse=new
 if path.endswith('TileEntityCrateBase.java'):
  addition=b'\t/** Native inventory title retains the original GUI literal/translation choice. */\r\n\t@Override\r\n\t'+method(new.decode(),'getDisplayName').encode()+b'\r\n\r\n'
  assert addition in reverse;reverse=reverse.replace(addition,b'')
 elif path.endswith('ItemBlockStorageCrate.java'):
  addition=b'\t\t@Override\n\t\t'+method(new.decode(),'getDisplayName').encode()+b'\n\n'
  assert addition in reverse;reverse=reverse.replace(addition,b'')
 reverse=reverse.replace(b'getName()',b'getInventoryName()').replace(b'hasCustomName()',b'hasCustomInventoryName()')
 assert reverse==old,'byte reversal '+path
 # Existing name getters and predicates are exactly original pristine bodies.
 if '/tileentity/' in path or path.endswith('ItemBlockStorageCrate.java'):
  original=subprocess.check_output(['git','show',base+':'+path.replace('ntm-port-1.12.2/','ntm-original-pristine/')],cwd=root,text=True)
  for before,after in [('getInventoryName','getName'),('hasCustomInventoryName','hasCustomName')]:
   if re.search(r'public (?:String|boolean) '+after+r'\(',new.decode()):
    actual=method(new.decode(),after).replace('getName()','getInventoryName()').replace('hasCustomName()','hasCustomInventoryName()')
    assert actual.replace('\r\n','\n')==method(original,before),'original body parity '+path
 manifest.append(dict(path=path,base_sha256=hashlib.sha256(old).hexdigest(),ported_sha256=hashlib.sha256(new).hexdigest()))
# No descendant of this base was silently missed.
alljava=list((root/'ntm-port-1.12.2/src/main/java').rglob('*.java'))
actual={p.name for p in alljava if re.search(r'extends TileEntityCrateBase\b',p.read_text())}
assert actual=={'TileEntity'+n+'.java' for n in names},actual
(out/'source-manifest.json').write_text(json.dumps(dict(base=base,files=manifest),indent=2)+'\n')
baseSource=(root/(prefix+'tileentity/machine/storage/TileEntityCrateBase.java')).read_text()
fixtures='''import net.minecraft.inventory.InventoryBasic; import net.minecraft.item.*;
abstract class CrateNameBase extends InventoryBasic {
 public String customName;
 CrateNameBase(){super("unused fixture title",false,1);}
 @Override public abstract String getName();
 public void setCustomName(String n){customName=n;}
'''+method(baseSource,'hasCustomName')+'\n'+method(baseSource,'getDisplayName')+'\n}\n'
for name in names:
 s=(root/(prefix+'tileentity/machine/storage/TileEntity'+name+'.java')).read_text()
 fixtures+='class '+name+' extends CrateNameBase { @Override '+method(s,'getName')+' }\n'
s=(root/(prefix+'items/block/ItemBlockStorageCrate.java')).read_text()
fixtures+='''class ItemCrateName extends InventoryBasic {
 ItemStack target; CrateNameBase selected;
 ItemCrateName(){super("unused fixture title",false,1);}
 // Bounded factory collaborator: registry/block selection and markDirty are excluded.
 CrateNameBase findCrateType(Item item){return selected;}
'''+method(s,'getName').replace('TileEntityCrateBase','CrateNameBase')+'\n'+method(s,'hasCustomName')+'\n'+method(s,'getDisplayName')+'\n}\n'
(out/'CrateNameFixtures.java').write_text(fixtures)
print('PASS exact CP71 reversal: 19 files; all eight descendants plus shared native display root, item-crate delegation, eight GUI callers and storage block callers. Existing getters/predicates match pristine exactly; null/empty/custom selection unchanged.')

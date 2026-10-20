#!/usr/bin/env python3
"""Exact current-source slices, original ownership contracts, and finite reversal."""
import hashlib,json,pathlib,re,sys,os,zipfile
here=pathlib.Path(__file__).resolve().parent;root=here.parents[3]
sha=lambda b:hashlib.sha256(b).hexdigest()
m=json.loads((here/'manifest.json').read_text())
for row in m['ownership_sources']:
 assert sha((root/row['path']).read_bytes())==row['sha256'],row['path']
for path,digest in json.loads((here/'reused-helper-proof.json').read_text()).items():
 assert sha((root/path).read_bytes())==digest,path
helper=root/'ntm-port-1.12.2/src/main/java/com/hbm/util/NullableInventoryStackNbt.java'
assert sha(helper.read_bytes())==m['helper_sha256']
current={};original={}
def original_native(s):
 # Explicitly allowed inherited mappings only, not edits performed by this batch.
 s=re.sub(r'\b(target|armor|mask)\.stackTagCompound = new NBTTagCompound\(\);',r'\1.setTagCompound(new NBTTagCompound());',s)
 s=re.sub(r'\b(armor|target|mask|stack)\.stackTagCompound\b',r'\1.getTagCompound()',s)
 s=s.replace('EnumChatFormatting.','TextFormatting.')
 # The exact two newly performed substitutions are compared independently below.
 return s.replace('ItemStack.loadItemStackFromNBT','NullableInventoryStackNbt.read').replace('mods.hasNoTags()','mods.isEmpty()')
for row in m['callers']:
 p=root/row['path'];b=p.read_bytes();s=b.decode();old=(root/row['pristine_path']).read_bytes()
 assert sha(b)==row['after_sha256'] and sha(old)==row['pristine_sha256']
 nl='\r\n' if '\r\n' in s else '\n'
 inverse=s.replace('import com.hbm.util.NullableInventoryStackNbt;'+nl,'').replace('NullableInventoryStackNbt.read','ItemStack.loadItemStackFromNBT')
 if row['nbt_empty_rename']:
  assert inverse.count('mods.isEmpty()')==1;inverse=inverse.replace('mods.isEmpty()','mods.hasNoTags()')
 assert sha(inverse.encode())==row['before_sha256'],p.name+' not exact finite edit'
 assert s.count('NullableInventoryStackNbt.read(')==row['loader_calls']
 current[p.stem]=s;original[p.stem]=original_native(old.decode())
assert sum(x['loader_calls'] for x in m['callers'])==8 and len(m['callers'])==6
scope=json.loads((here/'scope-audit.json').read_text())
assert scope['base']==m['base'] and sum(r['loader_calls'] for r in scope['owners'])==30
assert {r['path'] for r in scope['owners'] if r['disposition'].startswith('selected:')}=={r['path'] for r in m['callers']}
assert sum(r['loader_calls'] for r in scope['owners'] if r['disposition'].startswith('selected:'))==8

def braced(s,start):
 first=s.index('{',start);end=first+1;depth=1
 while depth:
  depth+=(s[end]=='{')-(s[end]=='}');end+=1
 return s[start:end]
def method(s,signature):return braced(s,s.index(signature))
def body(s,signature):
 value=method(s,signature);return value[value.index('{')+1:-1]
def const(s,name):
 result=re.search(r'public static final (?:int|String) '+name+r'\s*=\s*[^;]+;',s)
 assert result,name;return result.group()
def compact(s):return re.sub(r'\s+','',s)
def extract(s,name):
 if name=='ItemBlockStorageCrate':
  ctor=body(s,'public InventoryCrate(EntityPlayer player, ItemStack crate)')
  load=ctor[ctor.index('if(target.getTagCompound() == null)'):]
  write=body(s,'public void markDirty()')
  initial=write[write.index('NBTTagCompound nbt ='):write.index('int invSize =')]
  remaining=write[write.index('nbt.removeTag("stacklock")'):]
  return {'crateRead':load,'crateWrite':initial+remaining}
 if name=='BlockStorageCrate':
  info=body(s,'public void addInformation(')
  spiders=braced(info,info.index('if(stack.getTagCompound().getBoolean("spiders"))'))
  end=info.index(spiders)+len(spiders)
  locked=braced(info,info.index('if(stack.getTagCompound().hasKey("lock"))',end))
  loads=re.findall(r'ItemStack content = NullableInventoryStackNbt\.read\([^\n]+;',info)
  assert len(loads)==2
  writer=body(s,'public boolean removedByPlayer(')
  serial=writer[writer.index('NBTTagCompound slot ='):writer.index('nbt.setTag("slot" + i, slot);')+len('nbt.setTag("slot" + i, slot);')]
  return {'spiders':spiders,'locked':locked,'crateTooltipLoad':loads[1],'blockCrateWrite':serial}
 if name=='BlockMassStorage':
  info=body(s,'public void addInformation(')
  load=re.search(r'ItemStack type = NullableInventoryStackNbt\.read\([^\n]+;',info).group()
  writer=body(s,'public boolean removedByPlayer(')
  serial=writer[writer.index('NBTTagCompound slot ='):writer.index('nbt.setTag("slot" + i, slot);')+len('nbt.setTag("slot" + i, slot);')]
  return {'massTypeLoad':load,'blockMassWrite':serial}
 if name=='HazardTransformerRadiationContainer':
  return {'hazardCrateLoad':re.search(r'ItemStack held = NullableInventoryStackNbt\.read\([^\n]+;',s).group()}
 if name=='ArmorModHandler':
  result={n:method(s,'public static '+signature) for n,signature in [('hasMods','boolean hasMods('),('clearMods','void clearMods('),('removeMod','void removeMod('),('pryMods','ItemStack[] pryMods('),('pryMod','ItemStack pryMod(')]}
  apply=body(s,'public static void applyMod(')
  result['writeMod']=apply[:apply.index('ItemArmorMod aMod =')]+apply[apply.index('NBTTagCompound cmp ='):]
  for key in ['MOD_SLOTS','MOD_COMPOUND_KEY','MOD_SLOT_KEY']:result[key]=const(s,key)
  assert 'int slot = aMod.type;' in apply
  return result
 if name=='ArmorUtil':
  return {'FILTERK_KEY':const(s,'FILTERK_KEY'),**{n:method(s,'public static '+sig) for n,sig in [('installGasMaskFilter','void installGasMaskFilter('),('removeFilter','void removeFilter('),('getGasMaskFilter','ItemStack getGasMaskFilter(')]}}
 raise AssertionError(name)
parts={};slice_count=0
for name,s in current.items():
 parts[name]=extract(s,name);expected=extract(original[name],name)
 assert set(parts[name])==set(expected)
 for key,value in parts[name].items():
  assert compact(value)==compact(expected[key]),name+'/'+key+' pristine contract mismatch'
  slice_count+=1
# Explicit ownership and consumer proofs in current and original sources.
for name,s in current.items():
 o=original[name]
 if name=='ItemBlockStorageCrate':
  for text in ['this.slots = new ItemStack[this.getSizeInventory()];','if(stack == null)','nbt.removeTag("slot" + i);','nbt.setTag("slot" + i, slot);']:
   assert text in s and text in o
 elif name=='BlockStorageCrate':
  assert s.count('if(content != null)')==2 and o.count('if(content != null)')==2
 elif name=='BlockMassStorage':assert 'if(type != null)' in s and 'if(type != null)' in o
 elif name=='HazardTransformerRadiationContainer':assert 'if(held != null)' in s and 'if(held != null)' in o
 elif name=='ArmorModHandler':
  for text in ['ItemStack[] slots = new ItemStack[MOD_SLOTS];','if(stack != null)','removeMod(armor, i);','removeMod(armor, slot);']:
   assert text in s and text in o
 elif name=='ArmorUtil':
  assert 'if(filter == null && ArmorModHandler.hasMods(mask))' in s and 'if(filter == null && ArmorModHandler.hasMods(mask))' in o
base=(root/'ntm-port-1.12.2/src/main/java/com/hbm/items/ItemInventory.java').read_text()
base_old=(root/'ntm-original-pristine/src/main/java/com/hbm/items/ItemInventory.java').read_text()
getter=method(base,'public ItemStack getStackInSlot(')
assert compact(getter)==compact(method(base_old,'public ItemStack getStackInSlot('))
for text in ['public ItemStack[] slots;','public ItemStack target;','return slots[slot];']:
 assert text in base and text in base_old
print('PASS 8 loader edits across 6 owners and one NBT emptiness rename reverse exactly to sealed d5ef20a8')
print('PASS '+str(slice_count)+' pristine storage slices/constants after explicit inherited tag-access/text-name mappings; nullable owners/consumers verified')
if len(sys.argv)>1:
 out=['import net.minecraft.item.ItemStack;','import net.minecraft.nbt.NBTTagCompound;','import net.minecraft.util.text.TextFormatting;','import com.hbm.util.NullableInventoryStackNbt;','import java.util.List;','import java.util.Random;','public class ExtractedEmbeddedStorage {','public static class Crate {','public ItemStack[] slots;','public ItemStack target;','public static Random rand = new Random();',getter,'public void read() {',parts['ItemBlockStorageCrate']['crateRead'],'}','public void write(int invSize) {',parts['ItemBlockStorageCrate']['crateWrite'],'}','}', 'public static void lockedTooltip(ItemStack stack, List<String> list) {','if(stack.hasTagCompound()) {',parts['BlockStorageCrate']['spiders'],parts['BlockStorageCrate']['locked'],'}','}']
 for name,key,var,params in [('BlockStorageCrate','crateTooltipLoad','content','ItemStack stack, int i'),('BlockMassStorage','massTypeLoad','type','ItemStack stack'),('HazardTransformerRadiationContainer','hazardCrateLoad','held','ItemStack stack, int i')]:
  out+=['public static ItemStack '+key+'('+params+') {',parts[name][key],'return '+var+';','}']
 for name,key in [('BlockStorageCrate','blockCrateWrite'),('BlockMassStorage','blockMassWrite')]:
  out+=['public static void '+key+'(ItemStack stack, NBTTagCompound nbt, int i) {',parts[name][key],'}']
 out+=['public static class Armor {']
 for key,value in parts['ArmorModHandler'].items():
  out+=['public static void writeMod(ItemStack armor, ItemStack mod, int slot) {',value,'}'] if key=='writeMod' else [value]
 out+=['}','public static class Filter {']+list(parts['ArmorUtil'].values())+['}','}']
 pathlib.Path(sys.argv[1]).write_text('\n'.join(out)+'\n')
 print('PASS exact source fragments generated; explicit test-only wrappers, no native API stubs')
if os.environ.get('MINECRAFT_CLASSPATH'):
 runtime=json.loads((here/'../bomb-inventory-nbt-native/runtime-inputs.json').read_text());found=[]
 for path in os.environ['MINECRAFT_CLASSPATH'].split(os.pathsep):
  if not pathlib.Path(path).is_file() or not zipfile.is_zipfile(path):continue
  with zipfile.ZipFile(path) as jar:
   if 'net/minecraft/item/ItemStack.class' in jar.namelist():found.append(sha(jar.read('net/minecraft/item/ItemStack.class')))
 assert found==[runtime['native_ItemStack_class_sha256']],'native ItemStack changed/shadowed'
 print('PASS actual mapped ItemStack bytecode matches sealed native helper runtime')

#!/usr/bin/env python3
import hashlib,json,pathlib,re,sys,os,zipfile
here=pathlib.Path(__file__).resolve().parent;root=here.parents[3];sha=lambda b:hashlib.sha256(b).hexdigest()
m=json.loads((here/'manifest.json').read_text());scope=json.loads((here/'scope-audit.json').read_text())
for row in m['related_sources']:assert sha((root/row['path']).read_bytes())==row['sha256'],row['path']
for p,d in json.loads((here/'reused-helper-proof.json').read_text()).items():assert sha((root/p).read_bytes())==d,p
assert sha((root/'ntm-port-1.12.2/src/main/java/com/hbm/util/NullableInventoryStackNbt.java').read_bytes())==m['helper_sha256']
def oldmap(s):
 return re.sub(r'\b(stack|type|shell)\.stackTagCompound\b',r'\1.getTagCompound()',s).replace('ItemStack.loadItemStackFromNBT','NullableInventoryStackNbt.read').replace('matcher.initPatternSmart(getWorldObj(), item, slot);','matcher.initPatternSmart(getWorld(), item, slot);')
current={};original={}
for row in m['callers']:
 p=root/row['path'];b=p.read_bytes();s=b.decode();o=(root/row['pristine_path']).read_bytes();nl='\r\n' if '\r\n' in s else '\n'
 assert sha(b)==row['after_sha256'] and sha(o)==row['pristine_sha256']
 reverse=s.replace('import com.hbm.util.NullableInventoryStackNbt;'+nl,'').replace('NullableInventoryStackNbt.read','ItemStack.loadItemStackFromNBT')
 assert sha(reverse.encode())==row['before_sha256'],p.stem
 assert s.count('NullableInventoryStackNbt.read(')==row['selected_calls'] and s.count('ItemStack.loadItemStackFromNBT(')==row['deferred_calls']
 current[p.stem]=s;original[p.stem]=oldmap(o.decode())
assert len(current)==14 and sum(r['selected_calls'] for r in m['callers'])==19
assert sum(r['selected_calls']+r['deferred_calls'] for r in scope['owners'])==22
assert sum(r['deferred_calls'] for r in scope['owners'])==3
for row in scope['owners']:
 if row['selected_calls']==0:assert sha((root/row['path']).read_bytes())==row['before_sha256'],row['path']
def brace(s,start):
 first=s.index('{',start);end=first+1;depth=1
 while depth:
  depth+=(s[end]=='{')-(s[end]=='}');end+=1
 return s[start:end]
def branch(s,start):
 result=brace(s,start);end=start+len(result);match=re.match(r'\s*else\s*\{',s[end:])
 if match:result=s[start:end]+s[end:end+match.end()-1]+brace(s,end+match.end()-1)
 return result
def body(s,sig):
 meth=brace(s,s.index(sig));return meth[meth.index('{')+1:-1]
def storage(s,readname='readFromNBT',writename='writeToNBT'):
 return {'read':re.sub(r'super\.'+readname+r'\(nbt\);','',body(s,'void '+readname+'(')), 'write':re.sub(r'super\.'+writename+r'\(nbt\);','',body(s,writename+'(NBTTagCompound nbt)'))}
def comp(s):return re.sub(r'\s+','',s.replace('return nbt;',''))
def take(s,begin,end):return s[s.index(begin):s.index(end,s.index(begin))+len(end)]
def extract(s,name):
 if name in ['BlockLoot','BlockSkeletonHolder','PistonInserter','TileEntityCraneConsole','TileEntityPASource']:r=storage(s)
 elif name=='EntityMinecartContainerBase':r=storage(s,'readEntityFromNBT','writeEntityToNBT')
 elif name=='EntityRideableRocket':
  rb=body(s,'void readEntityFromNBT(');wb=body(s,'void writeEntityToNBT(')
  r={'read':branch(rb,rb.index('if(nbt.hasKey("drive"))')),'write':brace(wb,wb.index('if(navDrive != null)'))}
 elif name=='EntityArtilleryShell':
  rb=body(s,'void readEntityFromNBT(');wb=body(s,'void writeEntityToNBT(')
  r={'read':rb[rb.index('NBTTagCompound compound ='):], 'write':wb[wb.index('if(this.cargo != null)'):], 'setCargo':brace(s,s.index('public void setCargo(')), 'groundDespawn':brace(s,s.index('protected int groundDespawn('))}
 elif name=='ItemAmmoArty':
  r={'tooltip':branch(s,s.index('if(stack.hasTagCompound() && stack.getTagCompound().getCompoundTag("cargo") != null)'))}
 elif name=='TileEntityTurretArty':r={'load':brace(s,s.index('if(type.getItemDamage() == 8 && type.hasTagCompound())'))}
 elif name=='TileEntityMachineAutocrafter':r={'load':take(s,'NBTTagCompound stack = nbt.getCompoundTag("stack");','ItemStack item = NullableInventoryStackNbt.read(stack);')}
 elif name.startswith('IMC'):
  r={'process':body(s,'void process(')}
  if name=='IMCBlastFurnace':
   r['output']=take(s,'final NBTTagCompound outputData =','final ItemStack output = NullableInventoryStackNbt.read(outputData);')
   r['input1']='NullableInventoryStackNbt.read(data.getCompoundTag("input1"))';r['input2']='NullableInventoryStackNbt.read(data.getCompoundTag("input2"))'
  elif name=='IMCCentrifuge':
   r['output']=take(s,'NBTTagCompound output =','ItemStack out = NullableInventoryStackNbt.read(output);')
   r['input']=take(s,'NBTTagCompound input =','ItemStack in = NullableInventoryStackNbt.read(input);')
  else:
   r['output']=take(s,'NBTTagCompound output =','ItemStack out = NullableInventoryStackNbt.read(output);')
   r['input']=take(s,'NBTTagCompound input =','ItemStack in = NullableInventoryStackNbt.read(input);')
 else:raise AssertionError(name)
 if name=='TileEntityPASource':
  start=s.index('private TileEntityPASource source;');ctor=brace(s,s.index('public Particle(TileEntityPASource source,'));r['particle']=s[start:s.index(ctor)+len(ctor)]
 return r
parts={}
for name,s in current.items():
 parts[name]=extract(s,name);old=extract(original[name],name)
 for key,value in parts[name].items():assert comp(value)==comp(old[key]),name+'/'+key+' pristine slice mismatch'
# Independently assert actual consumer/writer facts, not merely expression counts.
facts={'BlockLoot':['if(stack == null || stack.getItem() == null) continue;','items.add(new Quartet(stack, x, y, z));'], 'BlockSkeletonHolder':['if(this.item != null)'], 'PistonInserter':['if(nbt.hasKey("stack"))','this.slot = null;','if(this.slot != null)'], 'EntityMinecartContainerBase':['this.slots = new ItemStack[this.getSizeInventory()];','if(this.slots[i] != null)'], 'EntityRideableRocket':['navDrive = null;','if(navDrive != null)'], 'EntityArtilleryShell':['this.cargo = stack;','return cargo != null ? 0 : 1200;'], 'ItemAmmoArty':['list.add(y + cargo.getDisplayName());'], 'TileEntityTurretArty':['proj.setCargo('], 'TileEntityMachineAutocrafter':['inv.setInventorySlotContents(slot, item);','matcher.initPatternSmart(getWorld(), item, slot);'], 'TileEntityPASource':['this.input1 = input1;','this.input2 = input2;','particle.input1.writeToNBT(inputTag1);','if(!nbt.hasKey("particle")) return;'], 'TileEntityCraneConsole':['if(this.loadedItem != null)'], 'IMCBlastFurnace':['if(output == null)','new ComparableStack(','case "orelist":'], 'IMCCentrifuge':['if(out == null)','if(in != null)','data.getString("oredict")'], 'IMCCrystallizer':['if(out == null)','if(in != null)','data.getString("oredict")']}
for name,strings in facts.items():
 for text in strings:assert text in current[name] and text in original[name],name+' '+text
print('PASS 19 loader-only edits in14 owners reverse exactly to sealed CP78; all3 wire/import exclusions unchanged')
print('PASS complete original storage/consumer fragments and IMC process bodies retain null/fallback/unguarded behavior')
if len(sys.argv)>1:
 out=['import net.minecraft.item.ItemStack;','import net.minecraft.nbt.NBTTagCompound;','import net.minecraft.nbt.NBTTagList;','import java.util.*;','import com.hbm.util.NullableInventoryStackNbt;','import com.hbm.util.Tuple.Quartet;','import com.hbm.lib.ForgeDirection;','import com.hbm.util.EnumUtil;','public class ExtractedRemainingNbt {']
 fields={'BlockLoot':'public List<Quartet<ItemStack, Double, Double, Double>> items = new ArrayList();','BlockSkeletonHolder':'public ItemStack item;','PistonInserter':'public ItemStack slot; public int extend; public boolean isRetracting,lastState;','TileEntityCraneConsole':'public ItemStack loadedItem; public boolean setUpCrane; public int craneRotationOffset,centerX,centerY,centerZ,spanF,spanB,spanL,spanR,height; public double posFront,posLeft;','EntityMinecartContainerBase':'public ItemStack[] slots = new ItemStack[this.getSizeInventory()];','EntityRideableRocket':'public ItemStack navDrive;','EntityArtilleryShell':'public ItemStack cargo;','TileEntityPASource':'public Particle particle;'}
 for name,fields in fields.items():
  p=parts[name];out+=['public static class '+name+' {',fields]
  if name=='EntityMinecartContainerBase':
   owner=(root/'ntm-port-1.12.2/src/main/java/com/hbm/entity/cart/EntityMinecartCrate.java').read_text();size=brace(owner,owner.index('public int getSizeInventory('));out+=[size]
  if name=='TileEntityPASource':out+=['public static class Particle {',p['particle'],'}']
  if name=='EntityArtilleryShell':out+=[p['setCargo'],p['groundDespawn']]
  out+=['public void read(NBTTagCompound nbt) {',p['read'],'}']
  returns='return nbt;' in p['write'];out+=['public '+('NBTTagCompound' if returns else 'void')+' write(NBTTagCompound nbt) {',p['write'],'}','}']
 out+=['public static void ammoTooltip(ItemStack stack, List<String> list, String y, String r) {',parts['ItemAmmoArty']['tooltip'],'}','public static void turretCargo(ItemStack type, EntityArtilleryShell proj) {',parts['TileEntityTurretArty']['load'],'}','public static ItemStack autocrafter(NBTTagCompound nbt) {',parts['TileEntityMachineAutocrafter']['load'],'return item;','}']
 for name in ['IMCBlastFurnace','IMCCentrifuge','IMCCrystallizer']:
  p=parts[name]
  for key,value in p.items():
   if key=='process':continue
   params='NBTTagCompound data'+(', int i' if name=='IMCCentrifuge' and key=='output' else '')
   if key in ['input1','input2']:content='return '+value+';'
   else:content=value+'\nreturn '+('output' if name=='IMCBlastFurnace' else 'out' if key=='output' else 'in')+';'
   out+=['public static ItemStack '+name+'_'+key+'('+params+') {',content,'}']
 # Real native-stack producer fragments for artillery and GUI control payloads.
 craft=(root/'ntm-port-1.12.2/src/main/java/com/hbm/crafting/handlers/CargoShellCraftingHandler.java').read_text();producer='shell.getTagCompound().setTag("cargo", cargo.writeToNBT(new NBTTagCompound()));';assert producer in craft
 gui=(root/'ntm-port-1.12.2/src/main/java/com/hbm/inventory/gui/GuiInfoContainer.java').read_text();prod=take(gui,'NBTTagCompound item = new NBTTagCompound();','tag.setTag("stack", item);')
 out+=['public static void craftCargo(ItemStack shell, ItemStack cargo) {',producer,'}','public static void guiStack(NBTTagCompound tag, ItemStack stack) {',prod,'}','}']
 pathlib.Path(sys.argv[1]).write_text('\n'.join(out)+'\n');print('PASS generated actual persistence bodies, consumer methods, paired particle constructor and bounded native input fragments')
if os.environ.get('MINECRAFT_CLASSPATH'):
 expected=json.loads((here/'../bomb-inventory-nbt-native/runtime-inputs.json').read_text())['native_ItemStack_class_sha256'];found=[]
 for path in os.environ['MINECRAFT_CLASSPATH'].split(os.pathsep):
  if not pathlib.Path(path).is_file() or not zipfile.is_zipfile(path):continue
  with zipfile.ZipFile(path) as z:
   if 'net/minecraft/item/ItemStack.class' in z.namelist():found.append(sha(z.read('net/minecraft/item/ItemStack.class')))
 assert found==[expected];print('PASS genuine mapped native ItemStack class hash, unchanged production helper')

#!/usr/bin/env python3
"""Reconstructed finite source proof and exact production storage-slice extractor.
See recovery-status.json: running this is source verification, not a native run.
"""
import hashlib,json,pathlib,re,sys,zipfile,os
here=pathlib.Path(__file__).resolve().parent; root=here.parents[3]
sha=lambda b:hashlib.sha256(b).hexdigest()
m=json.loads((here/'manifest.json').read_text())
for path,digest in json.loads((here/'reused-proof-inputs.json').read_text()).items():
    assert sha((root/path).read_bytes())==digest, 'sealed previous evidence changed: '+path
helper=root/'ntm-port-1.12.2/src/main/java/com/hbm/util/NullableInventoryStackNbt.java'
assert sha(helper.read_bytes())==m['helper_sha256'], 'production helper changed'
def braced(s,start):
    first=s.index('{',start); end=first+1; depth=1
    while depth:
        depth+=(s[end]=='{')-(s[end]=='}');end+=1
    return s[start:end]
def body(s,name):
    m=re.search(r'(?:public|protected) void '+name+r'\(NBTTagCompound nbt\)',s)
    assert m, name
    method=braced(s,m.start());return method[method.index('{')+1:-1]
def loop(s):return braced(s,s.index('for'))
def compact(s):return re.sub(r'\s+','',s)
def slices(s,name):
    read=body(s,'readEntityFromNBT');write=body(s,'writeEntityToNBT')
    if name=='EntityDeliveryDrone':
        read=read[read.index('NBTTagList nbttaglist'):read.index('if(nbt.hasKey("fluidType"))')].rstrip()
        write=write[write.index('NBTTagList nbttaglist'):write.index('if(fluid != null)')].rstrip()
    elif name=='EntityRequestDrone':
        read=braced(read,read.index('if(nbt.hasKey("held"))'))
        write=braced(write,write.index('if(heldItem != null)'))
    elif name=='EntitySoyuz':
        read='NBTTagList list = nbt.getTagList("items", 10);\n'+loop(read)
        write='NBTTagList list = new NBTTagList();\n'+loop(write)+'\nnbt.setTag("items", list);'
        assert 'NBTTagList list = nbt.getTagList("items", 10);' in s
        assert 'NBTTagList list = new NBTTagList();' in s and 'nbt.setTag("items", list);' in s
    return read,write
out=['import net.minecraft.item.ItemStack;','import net.minecraft.nbt.NBTTagCompound;','import net.minecraft.nbt.NBTTagList;','import com.hbm.util.NullableInventoryStackNbt;','import java.util.List;','import java.util.ArrayList;','public class ExtractedTransportNbt {']
checks=0
for row in m['callers']:
    p=root/row['path'];b=p.read_bytes();s=b.decode();old=(root/row['pristine_path']).read_bytes();o=old.decode();name=p.stem
    assert sha(b)==row['after_sha256'],name+' production changed'
    assert sha(old)==row['pristine_sha256'],name+' pristine changed'
    nl='\r\n' if '\r\n' in s else '\n'
    inverse=s.replace('import com.hbm.util.NullableInventoryStackNbt;'+nl,'').replace('NullableInventoryStackNbt.read','ItemStack.loadItemStackFromNBT')
    assert sha(inverse.encode())==row['before_sha256'],name+' edit not exact substitution'
    assert s.count('NullableInventoryStackNbt.read(')==1
    current=slices(s,name); original=slices(o,name)
    for a,z in zip(current,original):
        assert compact(a.replace('NullableInventoryStackNbt.read','ItemStack.loadItemStackFromNBT'))==compact(z),name+' pristine slice mismatch'
    field=row['field']
    if name=='EntityDeliveryDrone':
        declaration='protected ItemStack[] slots = new ItemStack[this.getSizeInventory()];'
        aux='public int getSizeInventory() { return 18; }'
        assert declaration in s and declaration in o and aux in s and aux in o
    elif name=='EntityMovingPackage':
        declaration='protected ItemStack[] contents = new ItemStack[0];';aux=''
        assert declaration in s and declaration in o
        assert 'this.contents = ItemStackUtil.carefulCopyArray(stacks);' in s and 'this.contents = ItemStackUtil.carefulCopyArray(stacks);' in o
    elif name=='EntityRequestDrone':
        declaration='public ItemStack heldItem;';aux=''
        assert declaration in s and declaration in o
        assert 'this.heldItem = null;' in s and 'this.heldItem = null;' in o
    elif name=='EntityParachuteCrate':
        declaration='public List<ItemStack> items = new ArrayList();';aux=''
        assert declaration in s and declaration in o
        assert 'crate.items.addAll(this.items);' in s and 'crate.items.addAll(this.items);' in o
    elif name=='EntitySoyuz':
        declaration='private ItemStack[] payload;';aux='public EntitySoyuz() { payload = new ItemStack[18]; }'
        assert declaration in s and declaration in o
        assert 'payload = new ItemStack[18];' in s and 'payload = new ItemStack[18];' in o
        assert 'capsule.payload = this.payload;' in s and 'capsule.payload = this.payload;' in o
        assert 'if(payload[0] != null)' in s and 'if(payload[0] != null)' in o
    elif name=='EntitySoyuzCapsule':
        declaration='public ItemStack[] payload = new ItemStack[18];';aux='public int soyuz;'
        assert declaration in s and declaration in o and aux in s and aux in o
        assert 'capsule.setInventorySlotContents(i, payload[i]);' in s and 'capsule.setInventorySlotContents(i, payload[i]);' in o
    elif name=='EntityBobmazon':
        declaration='public ItemStack payload;';aux=''
        assert declaration in s and declaration in o
        assert 'if(payload != null)' in s and 'if(payload != null)' in o
    else:raise AssertionError(name)
    if name not in ('EntityParachuteCrate','EntityBobmazon'):
        assert re.search(r'(?:this\.)?'+field+r'(?:\[i\])?\s*!= null',current[1]),name+' non-null writer guard'
    else:
        assert '!= null' not in current[1],name+' original unguarded writer must remain'
    out+=['// Storage-only host for exact slices from '+row['path'],'public static class '+name+' {',declaration,aux,'public void read(NBTTagCompound nbt) {',current[0],'}','public void write(NBTTagCompound nbt) {',current[1],'}','}']
    checks+=1
out+=['}']
assert checks==7
scope=json.loads((here/'scope-audit.json').read_text())
assert scope['base']==m['base'] and scope['baseline_loader_calls']==37 and scope['selected_calls']==7 and scope['remaining_calls']==30
assert sum(row['load_calls'] for row in scope['owners'])==37
assert {row['path'] for row in scope['owners'] if row['disposition'].startswith('selected:')}=={row['path'] for row in m['callers']}
assert sum(row['load_calls'] for row in scope['owners'] if row['disposition'].startswith('selected:'))==7
print('PASS 7 exact CP76 source reversals and pristine reader/writer slices; nullable ownership, schema and legacy unguarded writers unchanged')
if len(sys.argv)>1:
    pathlib.Path(sys.argv[1]).write_text('\n'.join(out)+'\n')
    print('PASS generated 14 production reader/writer slices without native API stubs; unchanged production helper')
if os.environ.get('MINECRAFT_CLASSPATH'):
    runtime=json.loads((root/'ntm-port-1.12.2/porting/tests/bomb-inventory-nbt-native/runtime-inputs.json').read_text())
    found=[]
    for path in os.environ['MINECRAFT_CLASSPATH'].split(os.pathsep):
        if not pathlib.Path(path).is_file() or not zipfile.is_zipfile(path):continue
        with zipfile.ZipFile(path) as jar:
            if 'net/minecraft/item/ItemStack.class' in jar.namelist():found.append(sha(jar.read('net/minecraft/item/ItemStack.class')))
    assert found==[runtime['native_ItemStack_class_sha256']], 'runtime ItemStack changed or shadowed'
    print('PASS genuine mapped native ItemStack bytecode matches prior sealed runtime')

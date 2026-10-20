#!/usr/bin/env python3
"""Separate bounded guard for the projectile-worker overlay; never a full-file waiver."""
import hashlib,json,pathlib,sys
here=pathlib.Path(__file__).resolve().parent
root=pathlib.Path(sys.argv[1]).resolve() if len(sys.argv)>1 else here.parents[3]
m=json.loads((here/'shell-merge-slices.json').read_text())
s=(root/m['file']).read_bytes().decode()
def brace(start):
    first=s.index('{',start);end=first+1;depth=1
    while depth:
        depth+=(s[end]=='{')-(s[end]=='}');end+=1
    return s[start:end]
def body(sig):
    method=brace(s.index(sig));return method[method.index('{')+1:-1]
r=body('void readEntityFromNBT(');w=body('void writeEntityToNBT(')
parts={'read':r[r.index('NBTTagCompound compound ='):], 'write':w[w.index('if(this.cargo != null)'):], 'setCargo':brace(s.index('public void setCargo(')), 'groundDespawn':brace(s.index('protected int groundDespawn('))}
for name,text in parts.items():
    assert hashlib.sha256(text.encode()).hexdigest()==m['exact_fragment_sha256'][name],name
assert s.count('import com.hbm.util.NullableInventoryStackNbt;')==1
assert parts['read'].count('NullableInventoryStackNbt.read(')==1
assert 'ItemStack.loadItemStackFromNBT(' not in parts['read']
helper=root/'ntm-port-1.12.2/src/main/java/com/hbm/util/NullableInventoryStackNbt.java'
expected=json.loads((here/'manifest.json').read_text())['helper_sha256']
assert hashlib.sha256(helper.read_bytes()).hexdigest()==expected
print('PASS exact Shell cargo read/write/setCargo/groundDespawn fragments and unchanged helper on supplied tree')
print('LIMIT: no whole-Shell approval; compose with independent projectile metadata edit/reversal guard')

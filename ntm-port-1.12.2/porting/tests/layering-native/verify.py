#!/usr/bin/env python3
"""Pin original source; compile original arithmetic, not copies of port formulas."""
import hashlib, pathlib, re, sys
root, out = map(pathlib.Path, sys.argv[1:])
p = root/'ntm-original-pristine/src/main/java/com/hbm/blocks/generic/BlockLayering.java'
s = p.read_text()
assert hashlib.sha256(p.read_bytes()).hexdigest() == '9e4cb92100d5c1ed74af2d1b19522f78919c31e77bd6a34f968053d390ae2313'
def body(signature):
    start=s.index('{',s.index(signature)); level=1; end=start+1
    while level:
        level+=(s[end]=='{')-(s[end]=='}'); end+=1
    return s[start+1:end-1]
bounds=body('protected void func_150154_b(')
quantity=body('public int quantityDropped(')
replacement=body('public boolean isReplaceable(')
replacement=replacement[replacement.index('int meta ='):].replace('int meta = world.getBlockMetadata(x, y, z);','').replace('blockMaterial.isReplaceable()', 'materialReplaceable')
# Collision height is the exact original low-bit/float arithmetic at world origin.
collision=body('public AxisAlignedBB getCollisionBoundingBoxFromPool(')
assert 'int l = p_149668_1_.getBlockMetadata(p_149668_2_, p_149668_3_, p_149668_4_) & 7;' in collision
assert '(double) ((float) p_149668_3_ + (float) l * f)' in collision
collision='int l = meta & 7; float f = 0.125F; return (double) ((float) y + (float) l * f);'
(out/'src/OriginalLayerOracle.java').write_text('''import java.util.Random;
public class OriginalLayerOracle {
 double height;
 void setBlockBounds(float a,float b,float c,float d,float e,float f) { height=e; }
 void bounds(int p_150154_1_) {'''+bounds+'''}
 int quantity(int meta,int fortune,Random random) {'''+quantity+'''}
 boolean replace(int meta,boolean materialReplaceable) {'''+replacement+'''}
 double collision(int meta,int y) {'''+collision+'''}
}
''')
port=root/'ntm-port-1.12.2/src/main/java'
uses=[]
for f in port.rglob('*.java'):
    text=f.read_text()
    assert not re.search(r'extends\s+BlockLayering\b',text), str(f)
    if 'new BlockLayering(' in text:
        uses.extend(re.findall(r'(\w+)\s*=\s*new BlockLayering\(Material\.(\w+)\)',text))
assert set(uses)=={('foam_layer','SNOW'),('sand_boron_layer','SAND'),('leaves_layer','LEAVES'),('oil_spill','GROUND')}, uses
print('PASS pinned original SHA256; original bounds/drop/replacement arithmetic extraction; exact four-instance census; no descendants')

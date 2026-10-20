from pathlib import Path
import json,hashlib,argparse
p=argparse.ArgumentParser();p.add_argument('--source-root',type=Path,required=True);a=p.parse_args()
data=json.loads(Path(__file__).with_name('source-map.json').read_text());seen=[]
def write(path,transform):
 r=data['files'][path];old=r['baseline'];assert hashlib.sha256(old.encode()).hexdigest()==r['before_sha256'];expected=transform(old);actual=(a.source_root/path).read_text();assert expected==actual,path;assert hashlib.sha256(actual.encode()).hexdigest()==r['after_sha256'];seen.append(path)
def tank(t):
 t=t.replace('import net.minecraft.client.renderer.Tessellator;','import net.minecraft.client.renderer.Tessellator;\nimport net.minecraft.client.renderer.BufferBuilder;\nimport net.minecraft.client.renderer.vertex.DefaultVertexFormats;')
 t=t.replace('slots[in] == null','slots[in] == null || slots[in].isEmpty()').replace('slots[in] != null &&','slots[in] != null && !slots[in].isEmpty() &&').replace('slots[out] == null','slots[out] == null || slots[out].isEmpty()').replace('slots[in] = null;','slots[in] = ItemStack.EMPTY;').replace('MathHelper.clamp_int(', 'MathHelper.clamp(')
 t=t.replace('Tessellator.instance','Tessellator.getInstance()').replace('tessellator.startDrawingQuads();','BufferBuilder buffer = tessellator.getBuffer();\n\t\tbuffer.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_TEX);')
 import re
 t=re.sub(r'tessellator.addVertexWithUV\(([^,]+), ([^,]+), ([^,]+), ([^,]+), ([^)]+)\);',r'buffer.pos(\1, \2, \3).tex(\4, \5).endVertex();',t)
 return t
write('src/main/java/com/hbm/inventory/fluid/tank/FluidTank.java',tank)
def standard(t):
 t=t.replace('slots[in] == null','(slots[in] == null || slots[in].isEmpty())').replace('slots[out] == null','(slots[out] == null || slots[out].isEmpty())')
 t=t.replace('slots[in] != null','slots[in] != null && !slots[in].isEmpty()').replace('slots[out] != null','slots[out] != null && !slots[out].isEmpty()')
 t=t.replace('slots[in].stackSize--;','slots[in].shrink(1);').replace('slots[out].stackSize++;','slots[out].grow(1);').replace('slots[in].stackSize','slots[in].getCount()').replace('slots[out].stackSize','slots[out].getCount()').replace('slots[in] = null;','slots[in] = ItemStack.EMPTY;')
 t=t.replace('slots[out] = emptyContainer;','slots[out] = emptyContainer == null ? ItemStack.EMPTY : emptyContainer;')
 return t
write('src/main/java/com/hbm/inventory/fluid/tank/FluidLoaderStandard.java',standard)
write('src/main/java/com/hbm/inventory/fluid/tank/FluidLoaderFillableItem.java',lambda t:t.replace('stack == null','stack == null || stack.isEmpty()').replace('mod != null &&','mod != null && !mod.isEmpty() &&'))
write('src/main/java/com/hbm/inventory/fluid/tank/FluidLoaderInfinite.java',lambda t:t.replace('slots[in] == null ||','slots[in] == null || slots[in].isEmpty() ||'))
def registry(t):
 for name in ['water_bucket','bucket','potionitem','glass_bottle','lava_bucket','experience_bottle','milk_bucket']:
  t=t.replace('Items.'+name,'Items.'+{'potionitem':'POTIONITEM'}.get(name,name.upper()))
 return t.replace('stack == null','stack == null || stack.isEmpty()').replace('sta.stackSize = 1;','sta.setCount(1);').replace('container.emptyContainer != null &&','container.emptyContainer != null && !container.emptyContainer.isEmpty() &&').replace('container.emptyContainer == null ? null','container.emptyContainer == null || container.emptyContainer.isEmpty() ? null')
write('src/main/java/com/hbm/inventory/FluidContainerRegistry.java',registry)
for name in ['api/hbm/fluid/IFluidStandardReceiver.java','api/hbm/fluid/IFluidStandardSender.java','api/hbm/fluidmk2/IFluidPipeMK2.java']:
 write('src/main/java/'+name,lambda t:t.replace('tile.getWorldObj()', 'tile.getWorld()').replace('tile.xCoord','tile.getPos().getX()').replace('tile.yCoord','tile.getPos().getY()').replace('tile.zCoord','tile.getPos().getZ()'))
for name in ['IFluidReceiverMK2','IFluidStandardSenderMK2']:
 write('src/main/java/api/hbm/fluidmk2/'+name+'.java',lambda t:t.replace('world.provider.dimensionId','world.provider.getDimension()'))

assert set(seen)==set(data["files"]);print("PASS 10 exact whole-source native mappings; no unrelated source transformations.")

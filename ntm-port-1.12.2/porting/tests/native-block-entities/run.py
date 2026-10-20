#!/usr/bin/env python3
"""Complete selected renderers/helpers against native Forge. Entity owners are NOT
compiled whole: exact data declarations/initializers/accessors/NBT methods are
extracted into explicitly bounded fixtures, retaining native data-manager wire.
"""
from pathlib import Path
import argparse,subprocess,tempfile,os,re,hashlib,json,sys
p=argparse.ArgumentParser();p.add_argument('--source-root',type=Path,required=True);p.add_argument('--java-home',type=Path,required=True);p.add_argument('--classpath',required=True);p.add_argument('--legacy-client',type=Path,required=True);a=p.parse_args();r=a.source_root.resolve();s=r/'src/main/java';t=Path(__file__).resolve().parent;old=r.parent/'ntm-original-pristine/src/main/java'
assert hashlib.sha256(a.legacy_client.read_bytes()).hexdigest()=='a4fc2284657544e0f4bcc964f927c2fda3e3a205178ed1d5d58883aaf9780cce'
subprocess.run([sys.executable,str(t/'check-source.py'),'--source-root',str(r)],check=True)
def method(text,needle):
 start=text.index(needle);start=text.rfind('\n',0,start)+1;pos=text.index('{',start);depth=1;end=pos+1
 while depth:
  depth+=(text[end]=='{')-(text[end]=='}');end+=1
 return text[start:end]
def declarations(text):return '\n'.join(line for line in text.splitlines() if 'private static final DataParameter<' in line)
baseimports='''import net.minecraft.block.Block;import net.minecraft.entity.*;import net.minecraft.entity.monster.EntityMob;import net.minecraft.world.World;import net.minecraft.nbt.NBTTagCompound;import net.minecraft.network.datasync.*;'''
fixtures={}
text=(s/'com/hbm/entity/item/EntityTNTPrimedBase.java').read_text()
fixtures['com/hbm/entity/item/EntityTNTPrimedBase.java']='package com.hbm.entity.item;'+baseimports+'''public class EntityTNTPrimedBase extends Entity { public int fuse=80;public float testBrightness=.625F; public EntityTNTPrimedBase(World w){super(w);} public float getBrightness(){return testBrightness;}'''+declarations(text)+'\n'+'\n'.join(method(text,n) for n in ['protected void entityInit()','public Block getBlock()','protected void writeEntityToNBT(','protected void readEntityFromNBT('])+'''public void setBlock(Block b){this.dataManager.set(BLOCK_ID,Block.getIdFromBlock(b));}}'''
base=(s/'com/hbm/entity/projectile/EntityThrowableNT.java').read_text()
fixtures['com/hbm/entity/projectile/EntityThrowableNT.java']='package com.hbm.entity.projectile;'+baseimports+'''public class EntityThrowableNT extends Entity {public EntityThrowableNT(World w){super(w);}'''+declarations(base)+method(base,'protected void entityInit()')+'''protected void writeEntityToNBT(NBTTagCompound n){}protected void readEntityFromNBT(NBTTagCompound n){} }'''
text=(s/'com/hbm/entity/projectile/EntityRubble.java').read_text()
fixtures['com/hbm/entity/projectile/EntityRubble.java']='package com.hbm.entity.projectile;'+baseimports+'''public class EntityRubble extends EntityThrowableNT {public EntityRubble(World w){super(w);}'''+declarations(text)+'\n'+'\n'.join(method(text,n) for n in ['public void entityInit()','public void setMetaBasedOnBlock(','public void readEntityFromNBT(','public void writeEntityToNBT(','public int getBlockId()','public int getBlockMetadata()'])+'}'
text=(s/'com/hbm/entity/mob/EntityBlockSpider.java').read_text()
fixtures['com/hbm/entity/mob/EntityBlockSpider.java']='package com.hbm.entity.mob;'+baseimports+'''public class EntityBlockSpider extends EntityMob {public float testBrightness=.625F;public EntityBlockSpider(World w){super(w);}public float getBrightness(){return testBrightness;}'''+declarations(text)+'\n'+'\n'.join(method(text,n) for n in ['protected void entityInit()','public int getBlockId()','public int getBlockMetadata()'])+'''public void setMaterial(Block block,int meta){int b=Block.getIdFromBlock(block);'''+''.join(line.strip() for line in method(text,'public void makeBlock(').splitlines() if 'this.dataManager.set(' in line)+'}}'
fixtures['com/hbm/items/ItemEnumMulti.java']='package com.hbm.items;public class ItemEnumMulti extends net.minecraft.item.Item {public com.hbm.render.icon.IIcon getIconFromDamage(int i){return null;}}'
fixtures['com/hbm/main/ResourceManager.java']='''package com.hbm.main;public class ResourceManager {public static com.hbm.render.mesh.MeshModel spider;public static final net.minecraft.util.ResourceLocation spider_tex=new net.minecraft.util.ResourceLocation("hbm:textures/entity/blockspider.png");}'''
proxy=(s/'com/hbm/main/ClientProxy.java').read_text()
lines=[line.strip() for line in proxy.splitlines() if any('registerEntityRenderingHandler('+name+'.class' in line for name in ['EntityTNTPrimedBase','EntityRubble','EntityBlockSpider'])]
assert len(lines)==3
fixtures['NativeEntityFactories.java']='import net.minecraftforge.fml.client.registry.RenderingRegistry;import com.hbm.entity.item.EntityTNTPrimedBase;import com.hbm.entity.projectile.EntityRubble;import com.hbm.entity.mob.EntityBlockSpider;import com.hbm.render.entity.item.RenderTNTPrimedBase;import com.hbm.render.entity.projectile.RenderRubble;import com.hbm.render.entity.mob.RenderBlockSpider;public class NativeEntityFactories {public static void register(){'+''.join(lines)+'}}'
production=['com/hbm/render/entity/NativeBlockEntityDraw.java','com/hbm/render/block/RenderBarrel.java','com/hbm/render/block/nativeblock/AtlasObjGeometry.java','com/hbm/render/block/nativeblock/BlockPositionLightQuad.java','com/hbm/render/mesh/ObjMeshParser.java','com/hbm/render/block/nativeblock/ConveyorCuboidGeometry.java','com/hbm/render/util/NativeBlockMaterial.java','com/hbm/render/entity/item/RenderTNTPrimedBase.java','com/hbm/render/entity/projectile/RenderRubble.java','com/hbm/render/entity/mob/RenderBlockSpider.java','com/hbm/render/model/ModelBlockSpider.java','com/hbm/render/model/ModelRubble.java','com/hbm/render/model/LegacyBaseBakedModel.java','com/hbm/blocks/BlockBase.java','com/hbm/lib/RefStrings.java','com/hbm/render/icon/IIcon.java','com/hbm/render/icon/IIconRegister.java','com/hbm/render/icon/LegacyTextureAtlasSprite.java','com/hbm/render/mesh/MeshModel.java','com/hbm/render/mesh/ObjMesh.java']
# Compare original render algorithms using full original class bodies, with only
# platform calls replaced by separately tested test-boundary adapters.
originals={}
for name,rel in [('OriginalTNT','com/hbm/render/entity/item/RenderTNTPrimedBase.java'),('OriginalSpider','com/hbm/render/model/ModelBlockSpider.java'),('OriginalRubble','com/hbm/render/entity/projectile/RenderRubble.java')]:
 text=(old/rel).read_text();text=re.sub(r'^package .*?;','',text,flags=re.M)
 text=text.replace('import net.minecraft.client.renderer.RenderBlocks;','').replace('import net.minecraft.client.renderer.entity.Render;','')
 text=text.replace('net.minecraft.util.MathHelper','net.minecraft.util.math.MathHelper').replace('RenderBlocks','OriginalBlockDraw').replace('extends Render {','extends OriginalRenderBoundary {').replace('TextureMap.locationBlocksTexture','TextureMap.LOCATION_BLOCKS_TEXTURE')
 text=text.replace('RenderTNTPrimedBase','OriginalTNT').replace('RenderRubble','OriginalRubble').replace('ModelBlockSpider','OriginalSpider')
 text=text.replace('tnt.getBrightness(f1)','tnt.getBrightness()').replace('entity.getBrightness(scaleFactor)','entity.getBrightness()')
 text=text.replace('entity.getDataWatcher().getWatchableObjectInt(12)','((com.hbm.entity.mob.EntityBlockSpider)entity).getBlockId()').replace('entity.getDataWatcher().getWatchableObjectInt(13)','((com.hbm.entity.mob.EntityBlockSpider)entity).getBlockMetadata()')
 text=text.replace('rocket.getDataWatcher().getWatchableObjectInt(16)','((com.hbm.entity.projectile.EntityRubble)rocket).getBlockId()').replace('rocket.getDataWatcher().getWatchableObjectInt(17)','((com.hbm.entity.projectile.EntityRubble)rocket).getBlockMetadata()')
 originals[name+'.java']=text
with tempfile.TemporaryDirectory(prefix='native-block-entities-') as temp:
 d=Path(temp);files=[]
 for rel,text in {**fixtures,**originals}.items():
  f=d/rel;f.parent.mkdir(parents=True,exist_ok=True);f.write_text(text);files.append(str(f))
 files += [str(s/f) for f in production]+[str(f) for f in t.glob('*.java')]
 result=subprocess.run([str(a.java_home/'bin/javac'),'-source','8','-target','8','-encoding','UTF-8','-sourcepath',temp,'-cp',a.classpath,'-d',temp]+files)
 if result.returncode:sys.exit(result.returncode)
 print('PASS complete renderer/helper/model compilation; 3 entity synchronization-only fixtures, projectile base data fixture, registry/item collaborator fixtures. NOT entity mechanics compilation.',flush=True)
 if (t/'BlockEntityNativeTest.java').exists():result=subprocess.run([str(a.java_home/'bin/java'),'-cp',temp+os.pathsep+a.classpath,'BlockEntityNativeTest',str(a.legacy_client),str(r.parent/'ntm-original-pristine')],cwd=temp)
 if result.returncode:sys.exit(result.returncode)

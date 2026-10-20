#!/usr/bin/env python3
"""Pinned, finite base-method/source/asset preservation audit, not a full-mod compile."""
import hashlib,json,re,subprocess
from pathlib import Path
root=Path(__file__).resolve().parents[3];repo=root.parent;here=Path(__file__).resolve().parent
base='077492a6c31c910f34697783f1aa0cfabd8ff88e'
def original(rel):return subprocess.check_output(['git','show',base+':ntm-port-1.12.2/'+rel],cwd=repo,text=True)
rel='src/main/java/com/hbm/items/ItemCustomLore.java';expected=original(rel)
expected=expected.replace('import net.minecraft.entity.player.EntityPlayer;','import net.minecraft.client.util.ITooltipFlag;\nimport net.minecraft.world.World;\nimport com.hbm.render.item.NativeItemTextureRegistry;')
expected=expected.replace('public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean bool)','public void addInformation(ItemStack itemstack, World world, List<String> list, ITooltipFlag flag)')
expected=expected.replace('\tpublic void addInformation(', '\t@SideOnly(Side.CLIENT)\n\tpublic void addInformation(')
expected=expected.replace('getUnlocalizedName()', 'getTranslationKey()').replace('player.worldObj.rand','world.rand').replace('if(world.rand.nextInt(10) == 0)','if(world == null || world.rand.nextInt(10) == 0)').replace('Item.itemRegistry','Item.REGISTRY').replace('public Item setUnlocalizedName(String uloc)','public Item setTranslationKey(String uloc)').replace("setTextureName(RefStrings.MODID + ':' + uloc);","NativeItemTextureRegistry.texture(this, RefStrings.MODID + ':' + uloc);").replace('super.setUnlocalizedName(uloc)','super.setTranslationKey(uloc)')
assert (root/rel).read_text()==expected,'Lore logic changed outside exact native signatures, item registry and texture declaration'
rel='src/main/java/com/hbm/items/ItemEnumMulti.java';expected=original(rel).replace('setUnlocalizedName','setTranslationKey').replace('getUnlocalizedName','getTranslationKey').replace('\n\t\tEnum[] order = theEnum.getEnumConstants();','\n\t\tif(!this.isInCreativeTab(tab)) return;\n\t\tEnum[] order = theEnum.getEnumConstants();')
assert (root/rel).read_text()==expected,'Enum behavior changed outside native names and caller filtering'
helper=(root/'src/main/java/com/hbm/render/item/NativeItemTextureRegistry.java').read_text()
assert 'net.minecraft.client' not in helper and 'net.minecraftforge.client' not in helper and 'com.hbm.main' not in helper,'Common configuration has client or mod bootstrap dependencies'
# The adapter only qualifies registerIcon in item mode; generated sprites bypass
# it and the default constructor explicitly retains old block behavior.
icons=(root/'src/main/java/com/hbm/render/icon/LegacyIconRegister.java').read_text()
assert 'this(textureMap, false);' in icons and 'registerGeneratedSprite(TextureAtlasSprite sprite)' in icons
assert 'itemTextures\n\t\t\t\t? com.hbm.render.item.NativeItemTextureRegistry.atlasLocation(name) : new ResourceLocation(name)' in icons
original_root=repo/'ntm-original-pristine'
paths=['ingot_steel.png','ingot_copper.png','coke.coal.png','coke.lignite.png','coke.petroleum.png','piston_pneumatic.png','piston_hydraulic.png','piston_electric.png','low_density_element.png','heavy_duty_element.png','glass_polarized.png']
paths += sorted(p.name for p in (root/'src/main/resources/assets/hbm/textures/items').glob('drillbit_*.png'))
manifest={}
for name in paths:
 rel='src/main/resources/assets/hbm/textures/items/'+name
 actual=(root/rel).read_bytes();reference=(original_root/rel).read_bytes();assert actual==reference,'Original texture changed: '+rel
 manifest[rel]=hashlib.sha256(actual).hexdigest()
# Actual original item callbacks are item-atlas-relative: no existing literal
# items/blocks/textures prefixes are being silently reinterpreted by this seam.
for p in (original_root/'src/main/java/com/hbm/items').rglob('*.java'):
 for match in re.finditer(r'registerIcon\(([^;]+)',p.read_text()):
  assert not re.search(r'"[^"\n]*(?:items/|blocks/|textures/)',match[1]),str(p)
(here/'original-textures.json').write_text(json.dumps(manifest,indent=2)+'\n')
print('PASS exact base callback/source preservation, common-side config boundary, item-only atlas policy, and '+str(len(manifest))+' unchanged original PNG assets. Source/asset audit only; lore full-class runtime remains an integration gate.')

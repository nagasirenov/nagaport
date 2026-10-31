"""Wave-4 mechanical API converter for the NTM 1.12.2 port.

Table-driven rewrites on compile-log-flagged lines only. Robust itemIcon /
blockIcon field insertion included (handles brace-on-next-line class decls).
"""
import re
import sys

LOG = 'porting/compile-baseline.log'
ROOT = 'src/main/java/'

err_re = re.compile(r'^.*?build.sources.main.java.(.+?):(\d+): error: ')

BP = r'new net.minecraft.util.math.BlockPos'

RULES = [
    (re.compile(r'net\.minecraft\.init\.Block\.getBlockFromItem'), 'net.minecraft.block.Block.getBlockFromItem'),
    (re.compile(r'ToolMaterial\.EMERALD\b'), 'ToolMaterial.DIAMOND'),
    (re.compile(r'EnumAction\.drink\b'), 'EnumAction.DRINK'),
    (re.compile(r'EnumAction\.eat\b'), 'EnumAction.EAT'),
    (re.compile(r'EnumAction\.block\b'), 'EnumAction.BLOCK'),
    (re.compile(r'EnumAction\.bow\b'), 'EnumAction.BOW'),
    (re.compile(r'ItemDoor\.placeDoorBlock\('), 'com.hbm.items.tool.ItemModDoor.placeDoorBlock('),
    (re.compile(r'\.getBlock\(\)\.getMaterial\(\)'), '.getMaterial()'),
    (re.compile(r'\.getDisplayName\(\)'), '.getDisplayName().getUnformattedText()'),
    (re.compile(r'IEntityLivingData onSpawnWithEgg\(IEntityLivingData (\w+)\)'),
     r'IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance difficulty, IEntityLivingData \1)'),
    (re.compile(r'super\.onSpawnWithEgg\(([^)]*)\)'), r'super.onInitialSpawn(difficulty, \1)'),
    (re.compile(r'(\w+)\.onSpawnWithEgg\(([^)]*)\)'),
     r'\1.onInitialSpawn(\1.world.getDifficultyForLocation(new net.minecraft.util.math.BlockPos(\1)), \2)'),
    (re.compile(r'super\((new \w+\(\)),\s*([0-9.]+F)\)'),
     r'super(net.minecraft.client.Minecraft.getMinecraft().getRenderManager(), \1, \2)'),
    (re.compile(r'\.hasItem\((\w+)\)'), r'.hasItemStack(new net.minecraft.item.ItemStack(\1))'),
    (re.compile(r'\.isEntityInvulnerable\(\)'), '.isEntityInvulnerable(null)'),
    (re.compile(r'StatList\.mineBlockStatArray\[getIdFromBlock\((\w+)\)\]'),
     r'net.minecraft.stats.StatList.getBlockStats(\1)'),
    (re.compile(r'renderItemOverlayIntoGUI\((\w+),\s*(?:this\.mc\.getTextureManager\(\)|this\.mc\.renderEngine|mc\.renderEngine|Minecraft\.getMinecraft\(\)\.getTextureManager\(\)),'),
     r'renderItemOverlayIntoGUI(\1,'),
    (re.compile(r'\.isOpaqueCube\(\)'), '.getDefaultState().isOpaqueCube()'),
    (re.compile(r'(\w+)\.canPlayerEdit\((\w+),\s*(\w+),\s*(\w+),\s*(\w+),\s*(\w+)\)'),
     r'\1.canPlayerEdit(new net.minecraft.util.math.BlockPos(\2, \3, \4), net.minecraft.util.EnumFacing.byIndex(\5), \6)'),
    (re.compile(r'\.getTileEntity\((\w+),\s*(\w+),\s*(\w+)\)'),
     r'.getTileEntity(new net.minecraft.util.math.BlockPos(\1, \2, \3))'),
    (re.compile(r'\.getSavedLightValue\((EnumSkyBlock\.\w+),\s*([^,()]+),\s*([^,()]+),\s*([^()]+)\)'),
     r'.getLightFor(\1, ' + BP + r'(\2, \3, \4))'),
    (re.compile(r'\.getBlockLightValue\(([^,()]+),\s*([^,()]+),\s*([^()]+)\)'),
     r'.getLightFromNeighborsFor(EnumSkyBlock.BLOCK, ' + BP + r'(\1, \2, \3))'),
    (re.compile(r'\.getLightBrightnessForSkyBlocks\(([^,()]+),\s*([^,()]+),\s*([^,()]+),\s*([^()]+)\)'),
     r'.getCombinedLight(' + BP + r'(\1, \2, \3), \4)'),
    (re.compile(r'\.getClosestPlayer\((\w+),\s*(\w+),\s*(\w+),\s*(\w+)\)'), r'.getClosestPlayer(\1, \2, \3, \4, false)'),
    (re.compile(r'\.skylightSubtracted\b'), '.getSkylightSubtracted()'),
    (re.compile(r'!\s*(\w[\w.]*)\.hasNoSky\b'), r'\1.hasSkyLight()'),
    (re.compile(r'(\w[\w.]*)\.hasNoSky\b'), r'!\1.hasSkyLight()'),
    (re.compile(r'\.heightVariation\b'), '.getHeightVariation()'),
    (re.compile(r'\.rootHeight\b'), '.getBaseHeight()'),
    (re.compile(r'\.renderViewEntity\b'), '.getRenderViewEntity()'),
    (re.compile(r'(\w+)\.delayBeforeCanPickup\s*=\s*([^;]+);'), r'\1.setPickupDelay(\2);'),
    (re.compile(r'\.writeNBTTagCompoundToBuffer\('), '.writeCompoundTag('),
    (re.compile(r'\.readNBTTagCompoundFromBuffer\('), '.readCompoundTag()'),
    (re.compile(r'setCurrentItemOrArmor\(0,'), 'setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.MAINHAND,'),
    (re.compile(r'setCurrentItemOrArmor\(1,'), 'setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.FEET,'),
    (re.compile(r'setCurrentItemOrArmor\(2,'), 'setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.LEGS,'),
    (re.compile(r'setCurrentItemOrArmor\(3,'), 'setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.CHEST,'),
    (re.compile(r'setCurrentItemOrArmor\(4,'), 'setItemStackToSlot(net.minecraft.inventory.EntityEquipmentSlot.HEAD,'),
    (re.compile(r'this\.func_150515_a\((\w+),\s*([^,()]+),\s*([^,()]+),\s*([^,()]+),\s*(\w+)\)'),
     r'this.setBlockAndNotifyAdequately(\1, ' + BP + r'(\2, \3, \4), \5.getDefaultState())'),
    (re.compile(r'(\w+)\.modelArmor\b'), r'((net.minecraft.client.model.ModelBiped) \1.getMainModel())'),
    (re.compile(r'(\w+)\.modelBipedMain\b'), r'((net.minecraft.client.model.ModelBiped) \1.getMainModel())'),
    (re.compile(r'event\.chunkX\b'), 'event.getChunkX()'),
    (re.compile(r'event\.chunkZ\b'), 'event.getChunkZ()'),
    (re.compile(r'for\s*\(Object (\w+) :\s*net\.minecraft\.item\.crafting\.CraftingManager\.getInstance\(\)\.getRecipeList\(\)\)'),
     r'for (Object \1 : net.minecraft.item.crafting.CraftingManager.REGISTRY)'),
    (re.compile(r'for\s*\(Object (\w+) :\s*CraftingManager\.getInstance\(\)\.getRecipeList\(\)\)'),
     r'for (Object \1 : net.minecraft.item.crafting.CraftingManager.REGISTRY)'),
    (re.compile(r'List (\w+) = net\.minecraft\.item\.crafting\.CraftingManager\.getInstance\(\)\.getRecipeList\(\);'),
     r'java.util.List \1 = new java.util.ArrayList<>(net.minecraft.item.crafting.CraftingManager.REGISTRY.getValuesCollection());'),
    (re.compile(r'this\.blockMetadata\b'),
     'com.hbm.world.WorldBlockState.metadata(com.hbm.world.WorldBlockState.read(this.world, this.pos))'),
    (re.compile(r'(\w+)\.exploder\b'), r'\1.getExplosivePlacedBy()'),
    (re.compile(r'\.getEntityToAttack\(\)'), '.getAttackTarget()'),
]

class_re = re.compile(r'^\s*(?:public|protected|private|abstract|final|static)\s.*(class|interface|enum)\s+\w+')
field_needed = {'itemIcon': re.compile(r'\bitemIcon\b'), 'blockIcon': re.compile(r'\bblockIcon\b')}
field_decl = re.compile(r'IIcon\s+(itemIcon|blockIcon)\s*;')


def transform(line):
    out = line
    for pat, rep in RULES:
        out = pat.sub(rep, out)
    return out if out != line else None


def insert_field(lines, name):
    """Insert `protected IIcon name;` after the top-level class opening brace."""
    for i, l in enumerate(lines):
        if re.match(r'^package ', l):
            pkg = i
            break
    else:
        return None
    # first top-level type declaration
    depth = 0
    for i in range(pkg + 1, len(lines)):
        if class_re.match(lines[i]):
            # find opening brace (same line or following)
            j = i
            while j < len(lines) and '{' not in lines[j]:
                j += 1
            if j >= len(lines):
                return None
            indent = re.match(r'([\t ]*)', lines[i]).group(1) + '\t'
            return j + 1, indent + 'protected com.hbm.render.icon.IIcon %s;' % name
    return None


def main():
    apply = '--apply' in sys.argv
    errors = {}
    with open(LOG, encoding='utf-8', errors='replace') as f:
        for raw in f:
            m = err_re.match(raw.rstrip('\r\n'))
            if m:
                errors.setdefault(m.group(1).replace(chr(92), '/'), set()).add(int(m.group(2)))
    touched = 0
    for rel in sorted(errors):
        path = ROOT + rel
        with open(path, 'rb') as f:
            lines = f.read().decode('utf-8').split('\r\n')
        changed = []
        need_field = {}
        for ln in sorted(errors[rel]):
            i = ln - 1
            if i < len(lines):
                new = transform(lines[i])
                if new is not None:
                    lines[i] = new
                    changed.append(ln)
                for name, pat in field_needed.items():
                    if pat.search(lines[i]) and 'itemIcon =' not in lines[i] and 'blockIcon =' not in lines[i]:
                        need_field[name] = True
        body = '\n'.join(lines)
        declared = set(m.group(1) for m in field_decl.finditer(body))
        inserts = []
        for name in need_field:
            if name not in declared:
                ins = insert_field(lines, name)
                if ins:
                    inserts.append(ins)
        if changed or inserts:
            touched += 1
            for pos, text in sorted(inserts, reverse=True):
                lines.insert(pos, '')
                lines.insert(pos + 1, text)
            out = '\r\n'.join(lines)
            if not out.endswith('\r\n'):
                out += '\r\n'
            if apply:
                with open(path, 'wb') as f:
                    f.write(out.encode('utf-8'))
            print('%-70s %s (%d edits, %d fields)' % (rel, 'WROTE' if apply else 'would-write',
                  len(changed), len(inserts)))
    print('--- files touched: %d (%s)' % (touched, 'APPLY' if apply else 'PREVIEW'))


if __name__ == '__main__':
    main()

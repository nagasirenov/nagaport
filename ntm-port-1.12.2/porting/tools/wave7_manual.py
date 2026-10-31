import re, glob

def patch(path, pairs, must=True):
    with open(path, 'rb') as f:
        d = f.read().decode('utf-8')
    orig = d
    for old, new in pairs:
        d = d.replace(old, new)
    if d != orig:
        with open(path, 'wb') as f:
            f.write(d.encode('utf-8'))
        return True
    if must:
        print('MISS:', path)
    return False

n = 0
for p in glob.glob('src/main/java/**/*.java', recursive=True):
    if patch(p, [('com.hbm.main.b.getSoundType()', 'b.getSoundType()'),
                 ('com.hbm.main.block.getSoundType()', 'block.getSoundType()')], must=False):
        n += 1
print('class-b fixed:', n)

for p, old, new in [
    ('src/main/java/com/hbm/entity/mob/botprime/EntityBOTPrimeHead.java',
     'public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {',
     'public IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance difficulty, IEntityLivingData data) {'),
    ('src/main/java/com/hbm/entity/mob/EntityBurrowingSwingingBase.java',
     'public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {',
     'public IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance difficulty, IEntityLivingData data) {'),
    ('src/main/java/com/hbm/entity/mob/EntityFBI.java',
     'public IEntityLivingData onSpawnWithEgg(IEntityLivingData data) {',
     'public IEntityLivingData onInitialSpawn(net.minecraft.world.DifficultyInstance difficulty, IEntityLivingData data) {'),
]:
    patch(p, [(old, new)])

for p, name in [('src/main/java/com/hbm/blocks/fluid/AcidFluid.java', 'acid'),
                ('src/main/java/com/hbm/blocks/fluid/CoriumFluid.java', 'corium'),
                ('src/main/java/com/hbm/blocks/fluid/MudFluid.java', 'mud'),
                ('src/main/java/com/hbm/blocks/fluid/RadFluid.java', 'rad'),
                ('src/main/java/com/hbm/blocks/fluid/SchrabidicFluid.java', 'schrabidium'),
                ('src/main/java/com/hbm/blocks/fluid/ToxicFluid.java', 'toxic'),
                ('src/main/java/com/hbm/blocks/fluid/VolcanicFluid.java', 'volcanic')]:
    patch(p, [
        ('super("%s_fluid");' % name,
         'super("%s_fluid", new net.minecraft.util.ResourceLocation("hbm", "blocks/%s_still"), new net.minecraft.util.ResourceLocation("hbm", "blocks/%s_flowing"));' % (name, name, name)),
        ('super(name);',
         'super(name, new net.minecraft.util.ResourceLocation("hbm", "blocks/" + name + "_still"), new net.minecraft.util.ResourceLocation("hbm", "blocks/" + name + "_flowing"));'),
    ], must=False)

patch('src/main/java/com/hbm/entity/missile/EntitySoyuz.java', [
    ('provider.loadChunk(targetX >> 4, targetZ >> 4);', 'world.getChunk(targetX >> 4, targetZ >> 4);')])
for p in glob.glob('src/main/java/**/ItemAnchorRemote.java', recursive=True):
    patch(p, [('world.getChunkProvider().loadChunk(x >> 4, z >> 4);', 'world.getChunk(x >> 4, z >> 4);')], must=False)
for p in glob.glob('src/main/java/**/SatelliteHorizons.java', recursive=True):
    patch(p, [('provider.loadChunk(x >> 4, z >> 4);', 'provider.world.getChunk(x >> 4, z >> 4);')], must=False)

for p in ['src/main/java/com/hbm/entity/mob/EntityDepthSquid.java',
          'src/main/java/com/hbm/entity/mob/EntityFish.java',
          'src/main/java/com/hbm/entity/mob/EntityScuttlecrab.java']:
    with open(p, 'rb') as f:
        d = f.read().decode('utf-8')
    d2 = re.sub(r'\bmoveEntityWithHeading\b', 'travel', d)
    if d2 != d:
        with open(p, 'wb') as f:
            f.write(d2.encode('utf-8'))
        print('travel:', p)

patch('src/main/java/com/hbm/config/FalloutConfigJSON.java', [
    ('Block.blockRegistry.getNameForObject(matchesBlock)', 'matchesBlock.getRegistryName()'),
    ('Block.blockRegistry.getNameForObject(meta.getX())',
     'net.minecraft.init.Block.REGISTRY.getObjectById(meta.getX()).getRegistryName()'),
], must=False)
for p in glob.glob('src/main/java/**/ItemCMStructure.java', recursive=True):
    patch(p, [('Block.blockRegistry.getNameForObject(block)', 'block.getRegistryName()')], must=False)

p = 'src/main/java/com/hbm/tileentity/TileEntityDoorGeneric.java'
with open(p, 'rb') as f:
    d = f.read().decode('utf-8')
d = re.sub(
    r'this\.world\.playSound\(this\.pos\.getX\(\), this\.pos\.getY\(\), this\.pos\.getZ\(\), (doorType\.\w+\(\)), (doorType\.getSoundVolume\(\)), (1F), false\)',
    r'this.world.playSound(null, this.pos.getX(), this.pos.getY(), this.pos.getZ(), \1, net.minecraft.util.SoundCategory.BLOCKS, \2, \3)', d)
with open(p, 'wb') as f:
    f.write(d.encode('utf-8'))
print('door sounds fixed')

for p in glob.glob('src/main/java/**/RenderLaunchPad*.java', recursive=True):
    patch(p, [('this.field_147501_a.field_147553_e',
               'net.minecraft.client.Minecraft.getMinecraft().getTextureManager()')], must=False)

p = 'src/main/java/com/hbm/potion/HbmPotion.java'
with open(p, 'rb') as f:
    lines = f.read().decode('utf-8').split('\r\n')
start = next(i for i, l in enumerate(lines) if 'Potion.potionTypes.length' in l)
depth = 0
end = None
for i in range(start, len(lines)):
    depth += lines[i].count('{') - lines[i].count('}')
    if depth == 0 and i > start:
        end = i
        break
if end and end - start < 30:
    del lines[start:end + 1]
    with open(p, 'wb') as f:
        f.write('\r\n'.join(lines).encode('utf-8'))
    print('hbmPotion hack removed:', start + 1, '-', end + 1)

for p in glob.glob('src/main/java/**/CelestialBodyWorldSavedData.java', recursive=True):
    patch(p, [('provider.worldObj', 'provider.world')])

patch('src/main/java/com/hbm/tileentity/turret/TileEntityTurretArty.java', [
    ('List list = new ArrayList();\r\n\t\tModItems.ammo_arty.getSubItems(ModItems.ammo_arty, MainRegistry.weaponTab, list);',
     'List list = new java.util.ArrayList();\r\n\t\tnet.minecraft.util.NonNullList<net.minecraft.item.ItemStack> sublist = net.minecraft.util.NonNullList.create();\r\n\t\tModItems.ammo_arty.getSubItems(MainRegistry.weaponTab, sublist);\r\n\t\tlist.addAll(sublist);')])
patch('src/main/java/com/hbm/tileentity/turret/TileEntityTurretHIMARS.java', [
    ('ModItems.ammo_himars.getSubItems(ModItems.ammo_himars, MainRegistry.weaponTab, list);',
     'net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> sublist = net.minecraft.util.NonNullList.create();\r\n\t\tModItems.ammo_himars.getSubItems(MainRegistry.weaponTab, sublist);\r\n\t\tlist.addAll(sublist);')])
patch('src/main/java/com/hbm/main/ClientProxy.java', [
    ('List<ItemStack> list = new ArrayList();\r\n\t\tstack.getItem().getSubItems(stack.getItem(), stack.getItem().getCreativeTab(), list);',
     'net.minecraft.util.NonNullList<ItemStack> list = net.minecraft.util.NonNullList.create();\r\n\t\tstack.getItem().getSubItems(stack.getItem().getCreativeTab(), list);')])
print('done')

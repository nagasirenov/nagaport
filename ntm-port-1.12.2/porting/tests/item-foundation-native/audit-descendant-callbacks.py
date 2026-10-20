#!/usr/bin/env python3
"""Bounded source regression audit for the migrated descendant callbacks.

The fixture is a verbatim callback snapshot from the recorded pre-migration commit.
This validates the finite callback closure and the intentionally narrow edits; it
is not a replacement for Java compilation or in-game rendering verification.
"""
import json
import re
from pathlib import Path

HERE = Path(__file__).resolve().parent
ROOT = HERE.parents[2]


def stripped(source):
    return re.sub(r'//[^\n]*|/\*[\s\S]*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'',
                  lambda m: re.sub(r'[^\n]', ' ', m[0]), source)


def methods(source, names):
    clean = stripped(source)
    result = {}
    for match in re.finditer(r'(?:public|protected)\s+(?:static\s+)?[\w<>\[\]]+\s+('
                             + '|'.join(names) + r')\s*\([^)]*\)\s*\{', clean):
        end, depth = match.end(), 1
        while depth:
            depth += (clean[end] == '{') - (clean[end] == '}')
            end += 1
        name = match[1]
        assert name not in result, 'Ambiguous method: ' + name
        result[name] = source[match.start():end]
    return result


def native_callback(path, source):
    source = re.sub(r'public void addInformation\(ItemStack (\w+), EntityPlayer player, List list, boolean (?:bool|ext)\)',
                    r'public void addInformation(ItemStack \1, World world, List<String> list, ITooltipFlag flag)', source)
    source = re.sub(r'super\.addInformation\((\w+), player, list, (?:bool|ext)\)',
                    r'super.addInformation(\1, world, list, flag)', source)
    if 'getSubItems(Item item, CreativeTabs tab, List list)' in source:
        source = source.replace('getSubItems(Item item, CreativeTabs tab, List list)',
                                'getSubItems(CreativeTabs tab, NonNullList<ItemStack> list)')
        source = source.replace('{', '{\n\t\tif(!this.isInCreativeTab(tab)) return;', 1)
        source = source.replace('new ItemStack(item,', 'new ItemStack(this,')
        source = source.replace('stack.stackTagCompound = stackTag;', 'stack.setTagCompound(stackTag);')
    source = source.replace('getUnlocalizedName(', 'getTranslationKey(')
    source = source.replace('setUnlocalizedName(', 'setTranslationKey(')
    if path.endswith('/ItemAmmo.java'):
        source = source.replace("setTextureName(RefStrings.MODID + ':' + uloc);",
                                "NativeItemTextureRegistry.texture(this, RefStrings.MODID + ':' + uloc);")
    if path.endswith('/ItemVOTVdrive.java'):
        source = source.replace('String identifier = stack.stackTagCompound.getString("stationName");',
                                'String identifier = stack.getTagCompound().getString("stationName");')
        source = source.replace('if(player.worldObj.provider.dimensionId != destination.body.getDimensionId()) {',
                                'if(world != null && world.provider.getDimension() != destination.body.getDimensionId()) {')
        source = source.replace('int processingLevel = destination.body.getProcessingLevel(CelestialBody.getBody(player.worldObj));',
                                'int processingLevel = world != null ? destination.body.getProcessingLevel(CelestialBody.getBody(world)) : -1;')
        source = source.replace('list.add("Process requirement: Level " + processingLevel);',
                                'if(world != null) list.add("Process requirement: Level " + processingLevel);')
        source = source.replace('if(player.worldObj.provider.dimensionId == destination.body.getDimensionId()) {',
                                'if(world != null && world.provider.getDimension() == destination.body.getDimensionId()) {')
    return source


def main():
    fixture = json.loads((HERE / 'descendant-callback-oracle.json').read_text())
    count = 0
    for path, callbacks in fixture['callbacks'].items():
        source = (ROOT / path).read_text()
        expected = {}
        for name, original in callbacks.items():
            native = name.replace('getUnlocalizedName', 'getTranslationKey').replace('setUnlocalizedName', 'setTranslationKey')
            expected[native] = native_callback(path, original)
        actual = methods(source, expected.keys())
        assert actual == expected, 'Callback behavior or signature drift: ' + path
        assert 'getUnlocalizedName' not in source, 'Obsolete name call remains: ' + path
        assert 'setUnlocalizedName' not in source, 'Obsolete setter remains: ' + path
        for name, callback in actual.items():
            if name == 'getSubItems':
                assert 'isInCreativeTab(tab)' in callback, path
                assert 'import net.minecraft.util.NonNullList;' in source, path
            if name == 'addInformation':
                body = stripped(callback)
                assert 'EntityPlayer' not in body and 'player' not in body, path
                assert 'Minecraft' not in body, path
                assert 'import net.minecraft.client.util.ITooltipFlag;' in source, path
                assert '@SideOnly(Side.CLIENT)\n\tpublic void addInformation(' in source, path
                assert 'import net.minecraft.world.World;' in source, path
        count += len(actual)

    # This older-looking helper remains native in stable_39. Its first-stage
    # translation is observable, so do not replace it with getTranslationKey.
    for path in ['src/main/java/com/hbm/items/ItemCustomLore.java',
                 'src/main/java/com/hbm/items/special/ItemBedrockOre.java']:
        display = methods((ROOT / path).read_text(), ['getItemStackDisplayName'])
        assert 'this.getUnlocalizedNameInefficiently(stack)' in display['getItemStackDisplayName'], path

    classes = {}
    for path in (ROOT / 'src/main/java').rglob('*.java'):
        source = path.read_text()
        for match in re.finditer(r'\bclass\s+(\w+)(?:\s*<[^{}]*?>)?\s+extends\s+([\w.]+)', stripped(source)):
            classes[match[1]] = (match[2].split('.')[-1], source)
    for base, expected in fixture['descendants'].items():
        closure = {base}
        while True:
            next_closure = closure | {name for name, (parent, _) in classes.items() if parent in closure}
            if next_closure == closure:
                break
            closure = next_closure
        assert closure - {base} == set(expected), 'Descendant closure drift: ' + base
        for name in closure:
            source = stripped(classes[name][1])
            assert not re.search(r'public\s+void\s+addInformation\([^)]*EntityPlayer', source), name
            assert not re.search(r'public\s+\w+\s+(?:get|set)UnlocalizedName\(', source), name
    print('PASS: {} native descendant callbacks across {} files; full foundation descendant closure verified.'.format(count, len(fixture['callbacks'])))


if __name__ == '__main__':
    main()

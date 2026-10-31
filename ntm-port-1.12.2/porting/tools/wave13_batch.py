#!/usr/bin/env python3
# CP167 wave 13: repair the 37 collateral diagnostics from waves 11-12.
import sys, os, re

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), '..', '..')
LISTT = "java.util.List<net.minecraft.item.ItemStack>"

def load(path):
    raw = open(path, 'rb').read()
    text = raw.decode('utf-8')
    parts = re.split(r'(\r\n|\r|\n)', text)
    return parts[0::2], parts[1::2] + ['']

def save(path, contents, terms):
    open(path, 'wb').write(''.join(c + t for c, t in zip(contents, terms)).encode('utf-8'))

def find_line(contents, lineno, frag):
    a, b = max(0, lineno - 6), min(len(contents), lineno + 5)
    hits = [i for i in range(a, b) if frag in contents[i]]
    return hits[0] if len(hits) == 1 else None

def main():
    apply = '--apply' in sys.argv
    fails = []
    done = 0

    def rm_line(path, lineno, frag):
        nonlocal done
        c, t = load(path)
        i = find_line(c, lineno, frag)
        if i is None:
            fails.append((path, lineno, 'MISS', frag[:50])); return
        del c[i]; del t[i]
        if apply: save(path, c, t)
        done += 1

    def rep(path, lineno, old, new):
        nonlocal done
        c, t = load(path)
        i = find_line(c, lineno, old)
        if i is None:
            fails.append((path, lineno, 'MISS', old[:50])); return
        c[i] = c[i].replace(old, new)
        if apply: save(path, c, t)
        done += 1

    # --- ItemSettingsTool: 1.12 onItemUseFirst has no ItemStack param; take it from the hand
    rep('src/main/java/com/hbm/items/tool/ItemSettingsTool.java', 82,
        'int x = pos.getX(), y = pos.getY(), z = pos.getZ();',
        'int x = pos.getX(), y = pos.getY(), z = pos.getZ(); net.minecraft.item.ItemStack stack = player.getHeldItem(hand);')

    # --- BlockNTMFlower: revert func_* renames (the class already delegates canGrow etc. to them)
    p = 'src/main/java/com/hbm/blocks/generic/BlockNTMFlower.java'
    for lineno, decl_old, decl_new in [
        (195,
         'public boolean canGrow(World world, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state, boolean b) {',
         'public boolean func_149851_a(World world, int x, int y, int z, boolean b) {'),
        (219,
         'public boolean canUseBonemeal(World world, Random rand, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {',
         'public boolean func_149852_a(World world, Random rand, int x, int y, int z) {'),
        (233,
         'public void grow(World world, Random rand, net.minecraft.util.math.BlockPos pos, net.minecraft.block.state.IBlockState state) {',
         'public void func_149853_b(World world, Random rand, int x, int y, int z) {'),
    ]:
        c, t = load(p)
        i = find_line(c, lineno, decl_old)
        if i is None:
            fails.append((p, lineno, 'FLOWER-DECL', decl_old[:50])); continue
        c[i] = c[i].replace(decl_old, decl_new)
        if c[i-1].strip() == '@Override':
            del c[i-1]; del t[i-1]
            i -= 1
        if c[i+1].startswith('\t\tint x = pos.getX()'):
            del c[i+1]; del t[i+1]
        if apply: save(p, c, t)
        done += 1

    # --- BlockGasClorine: convert legacy shouldSideBeRendered to 1.12 signature
    p = 'src/main/java/com/hbm/blocks/gas/BlockGasClorine.java'
    c, t = load(p)
    i = find_line(c, 78, 'public boolean shouldSideBeRendered(IBlockAccess p_149646_1_, int p_149646_2_, int p_149646_3_, int p_149646_4_, int p_149646_5_) {')
    if i is None:
        fails.append((p, 78, 'CLORINE', 'decl not found'))
    else:
        c[i] = '\t@Override\n\t\tpublic boolean shouldSideBeRendered(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess p_149646_1_, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumFacing p_149646_5_) {'.replace('\t@Override\n\t\t', '\t')
        # simpler: build exact line
        c[i] = '\tpublic boolean shouldSideBeRendered(net.minecraft.block.state.IBlockState state, net.minecraft.world.IBlockAccess p_149646_1_, net.minecraft.util.math.BlockPos pos, net.minecraft.util.EnumFacing p_149646_5_) {'
        c.insert(i + 1, '\t\tint x = pos.getX(), y = pos.getY(), z = pos.getZ();')
        t.insert(i + 1, t[i])
        if apply: save(p, c, t)
        done += 1

    # --- BlockDoorGeneric: helper needs World
    rep('src/main/java/com/hbm/blocks/generic/BlockDoorGeneric.java', 174,
        'return getBoundingBox(world, x, y, z, false);',
        'return getBoundingBox((net.minecraft.world.World) world, x, y, z, false);')

    # --- MachineChungus/CondenserPowered/OilWell: param pos clashes with local int[] pos
    for f in ['src/main/java/com/hbm/blocks/machine/MachineChungus.java',
              'src/main/java/com/hbm/blocks/machine/MachineCondenserPowered.java',
              'src/main/java/com/hbm/blocks/machine/MachineOilWell.java']:
        rep(f, 211,
            'public void onBlockExploded(World world, net.minecraft.util.math.BlockPos pos, Explosion explosion) {',
            'public void onBlockExploded(World world, net.minecraft.util.math.BlockPos nativePos, Explosion explosion) {')
        c, t = load(f)
        i = next((k for k, l in enumerate(c) if l.strip() == 'int x = pos.getX(), y = pos.getY(), z = pos.getZ();'), None)
        if i is None:
            fails.append((f, 212, 'POS-ADAPTER', '')); continue
        c[i] = c[i].replace('pos.getX()', 'nativePos.getX()').replace('pos.getY()', 'nativePos.getY()').replace('pos.getZ()', 'nativePos.getZ()')
        if apply: save(f, c, t)
        done += 1

    # --- TestCraftingHandler: inherit registry entry impl
    rep('src/main/java/com/hbm/crafting/handlers/TestCraftingHandler.java', 8,
        'public class TestCraftingHandler implements IRecipe {',
        'public class TestCraftingHandler extends net.minecraftforge.registries.IForgeRegistryEntry.Impl<net.minecraft.item.crafting.IRecipe> implements net.minecraft.item.crafting.IRecipe {')

    # --- EntityUFOBase: stray duplicate @Override before the inserted onUpdate hook
    p = 'src/main/java/com/hbm/entity/mob/EntityUFOBase.java'
    c, t = load(p)
    dup = next((k for k in range(len(c) - 1) if c[k].strip() == '@Override' and c[k + 1].strip() == '@Override'), None)
    if dup is None:
        fails.append((p, 43, 'DUP-OVR', '')); continue_ = None
    else:
        del c[dup]; del t[dup]
        if apply: save(p, c, t)
        done += 1

    # --- EntityBurrowingSwingingBase: renamed decl kept a stale @Override
    p = 'src/main/java/com/hbm/entity/mob/EntityBurrowingSwingingBase.java'
    c, t = load(p)
    i = find_line(c, 87, 'protected void updateEntityActionStateNT() {')
    if i is None:
        fails.append((p, 87, 'BURROW', 'decl'))
    elif c[i-1].strip() == '@Override':
        del c[i-1]; del t[i-1]
        if apply: save(p, c, t)
        done += 1
    else:
        done += 1  # nothing to do

    # --- BlockPinkSlab: implement getVariantProperty
    rep('src/main/java/com/hbm/blocks/generic/BlockPinkSlab.java', 39,
        'public Comparable<?> getTypeForItem(net.minecraft.item.ItemStack stack) { return stack.getMetadata(); }',
        'public Comparable<?> getTypeForItem(net.minecraft.item.ItemStack stack) { return stack.getMetadata(); }\n\n\t@Override\n\tpublic net.minecraft.block.properties.IProperty<?> getVariantProperty() { return null; }')

    # --- InventoryUtil: NonNullList -> List so Arrays.asList delegates work; EMPTY-tolerant slot check
    for lineno, old, new in [
        (37,  'public static ItemStack tryAddItemToInventory(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> inv, int start, int end, ItemStack stack) {',
              'public static ItemStack tryAddItemToInventory(' + LISTT + ' inv, int start, int end, ItemStack stack) {'),
        (52,  'public static ItemStack tryAddItemToInventory(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> inv, ItemStack stack) {',
              'public static ItemStack tryAddItemToInventory(' + LISTT + ' inv, ItemStack stack) {'),
        (64,  'public static ItemStack tryAddItemToExistingStack(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> inv, int start, int end, ItemStack stack) {',
              'public static ItemStack tryAddItemToExistingStack(' + LISTT + ' inv, int start, int end, ItemStack stack) {'),
        (96,  'public static boolean tryAddItemToNewSlot(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> inv, int start, int end, ItemStack stack) {',
              'public static boolean tryAddItemToNewSlot(' + LISTT + ' inv, int start, int end, ItemStack stack) {'),
        (103, 'if(inv.get(i).isEmpty()) {', 'if(inv.get(i) == null || inv.get(i).isEmpty()) {'),
        (177, 'public static boolean tryConsumeAStack(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> inv, int start, int end, AStack stack) {',
              'public static boolean tryConsumeAStack(' + LISTT + ' inv, int start, int end, AStack stack) {'),
        (194, 'inv.set(i, net.minecraft.item.ItemStack.EMPTY);', 'inv.set(i, net.minecraft.item.ItemStack.EMPTY);'),
        (590, 'public static int countAStackMatches(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> inventory, AStack stack, boolean ignoreSize) {',
              'public static int countAStackMatches(' + LISTT + ' inventory, AStack stack, boolean ignoreSize) {'),
        (611, 'public static boolean doesInventoryHaveAStack(net.minecraft.util.NonNullList<net.minecraft.item.ItemStack> inventory, AStack stack, boolean shouldRemove, boolean ignoreSize) {',
              'public static boolean doesInventoryHaveAStack(' + LISTT + ' inventory, AStack stack, boolean shouldRemove, boolean ignoreSize) {'),
        (287, 'for(int i = 0; i < original.length; i++) {', 'for(int i = 0; i < original.size(); i++) {'),
        (290, 'original[i] = null;', 'original.set(i, net.minecraft.item.ItemStack.EMPTY);'),
    ]:
        rep('src/main/java/com/hbm/util/InventoryUtil.java', lineno, old, new)

    # --- InventoryUtil: restore ItemStack[] overloads delegating via Arrays.asList
    p = 'src/main/java/com/hbm/util/InventoryUtil.java'
    c, t = load(p)
    i = find_line(c, 37, 'public static ItemStack tryAddItemToInventory(java.util.List<net.minecraft.item.ItemStack> inv, int start, int end, ItemStack stack) {')
    if i is not None:
        ins = ['\tpublic static ItemStack tryAddItemToInventory(ItemStack[] inv, int start, int end, ItemStack stack) {',
           '\t\treturn tryAddItemToInventory(java.util.Arrays.asList(inv), start, end, stack);',
           '\t}',
           '',
           '\tpublic static ItemStack tryAddItemToInventory(ItemStack[] inv, ItemStack stack) {',
           '\t\treturn tryAddItemToInventory(java.util.Arrays.asList(inv), stack);',
           '\t}',
           '']
        for k, ln in enumerate(ins):
            c.insert(i + k, ln)
            t.insert(i + k, t[i])
        if apply: save(p, c, t)
        done += 1
    else:
        fails.append((p, 37, 'OVERLOAD-ANCHOR', ''))

    for f in fails:
        print('FAIL %s L%-5d %s | %s' % f)
    print('total ops: %d, ok: %d, failed: %d, mode: %s' % (done + len(fails), done, len(fails), 'APPLY' if apply else 'PREVIEW'))
    if fails:
        sys.exit(1)

if __name__ == '__main__':
    main()

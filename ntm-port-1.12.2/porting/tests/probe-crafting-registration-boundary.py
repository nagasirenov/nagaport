#!/usr/bin/env python3
"""Run bounded native crafting proofs with pinned Forge source-order checks. No Gradle/downloads."""
import argparse
import hashlib
from pathlib import Path
import subprocess
import tempfile
import re
import zipfile

ROOT = Path(__file__).resolve().parents[2]

def check_lifecycle(sources):
    with zipfile.ZipFile(sources) as archive:
        loader = archive.read('net/minecraftforge/fml/common/Loader.java').decode()
        helper = archive.read('net/minecraftforge/common/crafting/CraftingHelper.java').decode()
        data = archive.read('net/minecraftforge/registries/GameData.java').decode()
    preinit = loader[loader.index('public void preinitializeMods()'):loader.index('private void disableRequestedMods()')]
    assert preinit.index('distributeStateMessage(LoaderState.PREINITIALIZATION') < preinit.index('GameData.fireRegistryEvents(rl -> !rl.equals(GameData.RECIPES))')
    init = loader[loader.index('public void initializeMods()'):loader.index('public ICrashCallable getCallableCrashInformation()')]
    assert init.index('CraftingHelper.loadRecipes(false)') < init.index('distributeStateMessage(LoaderState.INITIALIZATION)')
    assert 'GameData.fireRegistryEvents(rl -> rl.equals(GameData.RECIPES))' in helper
    fire = data[data.index('public static void fireRegistryEvents(Predicate<ResourceLocation> filter)'):]
    assert fire.index('getRegisterEvent(BLOCKS)') < fire.index('getRegisterEvent(ITEMS)') < fire.index('getRegisterEvent(rl)')
    print('PASS pinned Forge source: preInit -> Block/Item events -> recipe event -> init', flush=True)
    for name, text in [('Loader.java', loader), ('CraftingHelper.java', helper), ('GameData.java', data)]:
        print('SOURCE', name, hashlib.sha256(text.encode()).hexdigest(), flush=True)

def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--java-home', type=Path, required=True)
    parser.add_argument('--classpath', required=True)
    parser.add_argument('--forge-sources', type=Path, required=True)
    parser.add_argument('--legacy-client', type=Path, required=True)
    parser.add_argument('--legacy-forge-userdev', type=Path, required=True)
    args = parser.parse_args()
    subprocess.run(["python3", str(ROOT / "porting/tests/check-crafting-registration-source.py")], check=True)
    check_lifecycle(args.forge_sources)
    with zipfile.ZipFile(args.legacy_forge_userdev) as legacy:
        for family in ['ShapedOreRecipe', 'ShapelessOreRecipe']:
            text = legacy.read('src/main/java/net/minecraftforge/oredict/' + family + '.java').decode()
            assert 'output = result.func_77946_l();' in text
            print('PASS authentic Forge1614 ' + family + ' constructor copies output; source SHA256 ' + hashlib.sha256(text.encode()).hexdigest(), flush=True)
    print('ORIGINAL CLIENT SHA256', hashlib.sha256(args.legacy_client.read_bytes()).hexdigest(), flush=True)
    sources = [ROOT / 'src/main/java/com/hbm/lib/RefStrings.java', ROOT / 'src/main/java/com/hbm/registry/GameRegistry.java']
    sources += sorted((ROOT / 'src/main/java/com/hbm/crafting/nativeapi').glob('*.java'))
    sources += [ROOT / 'porting/tests/ForgeEventNativeLauncher.java', ROOT / 'porting/tests/CraftingRegistrationBoundaryNativeTest.java']
    for source in sources:
        print('COMPILE', source.relative_to(ROOT), hashlib.sha256(source.read_bytes()).hexdigest(), flush=True)
    with tempfile.TemporaryDirectory(prefix='ntm-crafting-native-') as out:
        manager = (ROOT / 'src/main/java/com/hbm/main/CraftingManager.java').read_text()
        candidates = re.findall(r'addRecipeAuto\((new ItemStack\(Items.PAPER, 3\)), (new Object\[\] \{ [^\n]*ModBlocks.vinyl_planks[^\n]*?\})\);', manager)
        assert len(candidates) == 1, 'exact original vinyl-paper producer not uniquely found'
        output, definition = candidates[0]
        print('ORIGINAL PRODUCER ARGUMENTS', output + ', ' + definition, flush=True)
        # Only substitute the NTM-owned block identity; shape/count/metadata stay literal source.
        definition = definition.replace('ModBlocks.vinyl_planks', 'vinylPlanks')
        fixture = Path(out) / 'OriginalCraftingFixture.java'
        fixture.write_text('import net.minecraft.block.Block; import net.minecraft.init.Items; import net.minecraft.item.ItemStack; import com.hbm.crafting.nativeapi.NtmCraftingRecipe;\n'
            + 'public final class OriginalCraftingFixture { public static NtmCraftingRecipe vinylPaper(Block vinylPlanks) { return NtmCraftingRecipe.shaped('
            + output + ', false, ' + definition + '); } }\n')
        sources.append(fixture)
        sources.append(ROOT / 'porting/tests/OriginalCraftingOutputAliasTest.java')
        subprocess.run([str(args.java_home / 'bin/javac'), '-encoding', 'UTF-8', '-source', '8', '-target', '8', '-cp', args.classpath, '-d', out] + list(map(str, sources)), check=True)
        subprocess.run([str(args.java_home / 'bin/java'), '-cp', out + ':' + str(args.legacy_client) + ':' + args.classpath, 'craftingproof.OriginalCraftingOutputAliasTest'], cwd=out, check=True)
        identities = []
        for extra in [[], ['earlier-conditional-recipe']]:
            run = subprocess.run([str(args.java_home / 'bin/java'), '-cp', out + ':' + args.classpath, 'ForgeEventNativeLauncher', 'CraftingRegistrationBoundaryNativeTest'] + extra, cwd=out, check=True, capture_output=True, text=True)
            print(run.stdout, end='', flush=True)
            print(run.stderr, end='', flush=True)
            identities.append(re.findall(r'^STABLE_ID=(.*)$', run.stdout, re.MULTILINE))
        assert len(identities[0]) == 1 and identities[0] == identities[1], identities
        print('PASS identical recipe ID across fresh JVMs with/without an earlier distinct conditional recipe: ' + identities[0][0], flush=True)

if __name__ == '__main__': main()

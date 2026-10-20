# Native pedestal block family

CP36 baseline: e146f1df78ba6ce7ebf3198217a67d132e1e7dff. No commits or pushes performed.

## Production scope

- Complete BlockPedestal owner including original nine-slot recipe execution and embedded TileEntityPedestal; native BlockPos/state/hand callbacks, full stack transfer and drop, loaded neighbor guard, exact original moon/eclipse/sun/reputation predicates.
- Native empty held stacks are ItemStack.EMPTY; optional stored item remains null, preserving the existing native RenderPedestalTile contract. NBT reads normalize native empties to null, writes clear stale item data, native update packet and initial chunk update tag carry the item.
- RenderPedestal is now a native baked-quad geometry producer. Original 3 boxes, 18 unculled faces, exact side/top materials and standard renderer UVs. Native AO/diffuse pipeline, no ISBRH or RenderBlocks.
- PedestalBakedModel and automatic client PedestalModelHandler use actual ModelRegistryEvent, delayed ItemBlock lookup, exact hbm:tile.pedestal owner/class matching, and native state/item model routing. Loader has no sprite cache to invalidate. Original texture assets unchanged.

## Integration requested from coordinator

- ClientProxy: remove only RenderingRegistry.registerBlockHandler(new RenderPedestal()); preserve RenderPedestalTile native binding.
- ModBlocks: pedestal factory uses setTranslationKey("pedestal") instead of setBlockName, and removes setBlockTextureName; explicit native model supplies pedestal_top/pedestal_side. Registry insertion must retain hbm:tile.pedestal and ItemBlock identity.
- Existing TileMappings mapping tileentity_ntm_pedestal unchanged. No extra tile migration or registration override.

## Evidence

Run from repository root:

    CP=$(tr '\n' ':' < /tmp/nagaport-ae2-native-classpath.txt)
    python porting/tests/probe-native-pedestal.py --classpath "$CP" --java-home /workspace/shared/nagaport-toolchain/java8

Result: 580 assertions, 8 complete selected production classes compiled against real Forge 1.12.2/Minecraft. Actual event bus registration, state and item mapping, loader/bake, 18-face geometry and UV comparison against extracted pinned original boxes, exact source environmental-condition comparison, full stack transfer both hands, NBT/packet/chunk-tag roundtrips, exact input count rejection, unpowered rejection, nine positions, consumption/output, effect request, drop and tile removal.

The test supplies five explicit unrelated NTM collaborator classes: recipe catalog/ingredient model, celestial provider, player reputation, achievement registry and visual effect sink. It does not replace selected owner classes, Minecraft, Forge, or native rendering contracts. Actual recipe catalog compilation/execution, actual celestial/reputation implementations, GL screenshots and whole-game acceptance are NOT claimed. Existing native item TESR is untouched and not exercised here. Registry harness emits expected test-owner namespace warnings.

## Anvil closure audit, deferred

No anvil production changes in this family. Minimum additional native rendering/fall closure: NTMAnvil, RenderAnvil, BlockFallingNT, EntityFallingBlockNT (288 baseline lines), RenderFallingBlockNT, a native model handler. BlockFallingNT has another concrete owner BlockFallingTint. EntityFallingBlockNT has an ISpotlight exemption. Shared falling API changes must preserve those callers. NTMAnvil also directly references ContainerAnvil (171 baseline lines) and GUIAnvil (476), plus their recipe/packet closure; a rendering-only rewrite cannot honestly claim this owner compiled natively. Anvil declarations/registration must remain until that closure is implemented and validated.

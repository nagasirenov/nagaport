# Battery energy native API slice

## Production scope

Production scope is `api/hbm/energymk2/IBatteryItem.java`, `com/hbm/items/machine/ItemBattery.java`, and the necessary dependent tooltip callback in `com/hbm/items/tool/ItemAnchorRemote.java`. Direct private `ItemStack.stackTagCompound` accesses become native tag getters/setters. ItemBattery uses the native tooltip and creative-list callbacks and uppercase EnumRarity values. The creative callback filters its native requested tab and preserves empty-before-full entries and charge/discharge-rate conditions. Tooltips retain all text and arithmetic.

The complete direct/transitive subclass inventory is ItemPotatos and ItemAnchorRemote. ItemPotatos has no tooltip/creative override. ItemAnchorRemote overrides the tooltip and now uses the native signature, preserving its distinct body and absence of a discharge-rate line. Neither subclass overrides getSubItems or calls super.addInformation/super.getSubItems. The probe checks this inventory to prevent another missed inherited override. ItemBatteryPack, ItemBatterySC, ItemPotatos, Library and the energy network remain untouched. No production helper was introduced solely to make tests compile.

## Test strength and limits

Run `python3 porting/tests/probe-battery-energy.py --java-home <JDK8> --classpath <real mapped Forge/Minecraft classpath>`.

The probe compiles the **whole production IBatteryItem and ItemBatteryCreative**. It extracts exact ItemBattery energy and creative callback methods into an explicitly test-only Item subclass because whole ItemBattery still depends on unported ModItems/BobMathUtil. It extracts the unchanged Library transfer operations into a clearly named fixture, replacing only the ModItems creative-battery identity expression with a harness-owned Item identity. It does not compile whole ItemBattery, Library, any machine or network. No fake Minecraft APIs, production build exclusions, or replacement production services are used.

Before compilation, the probe validates source parity against the supplied `../ntm-original-pristine` tree: IBatteryItem differs only in native NBT access, twelve ItemBattery energy methods differ only in native NBT access, callbacks retain their bodies except specified native signatures/rarity constants/tab filtering, ItemAnchorRemote retains its distinct tooltip body with only its signature changed, the complete subclass inventory is checked, and both Library transfer bodies remain unchanged. This is supplied-tree parity, not verified upstream archive provenance.

Native tests cover missing-NBT full initialization versus existing-empty-compound zero; setters retaining unrelated NBT; copy/count/metadata and custom battery tag names; invalid helper return values; native creative-tab filtering and entry order; real ItemStack serialized through compressed NBT bytes and loaded again; directional energy accounting; exact invocation order; zero-transfer mutation calls; early array-access/clamp order; creative Item identity rather than instanceof; negative and signed-overflow behavior, including randomized boundary accounting. The ItemStack persistence test is not a world/chunk save test. IBatteryItem and Library have no simulation flag; this slice adds no simulation capability.

Original unusual behavior remains visible: an overfilled machine can receive negative energy and increase battery charge; an overfilled battery can accept negative energy and increase machine power. Java signed-long overflow is preserved. These are tested parity characteristics, not endorsed fixes or conservation guarantees outside the original modulo-long arithmetic.

## Remaining acceptance

Whole production compile and full electric-furnace acceptance remain gated. Concrete item registration/tooltip integration, shared inventory and ticking, upgrades, recipes, output XP/events, block states, network subscription/topology, tile unload/reload, payload lifetime/client scheduling and dedicated-server startup still need their own closure. Native extracted-method assertions are not gameplay acceptance.

Correction to preliminary assessment: TileAccessCache is present as the nested `ILoadedTile.TileAccessCache`, inherited by IEnergyReceiverMK2 through IEnergyHandlerMK2. No missing cache implementation should be invented. Existing cache expiration and network topology must be preserved and tested when those production paths become runnable.

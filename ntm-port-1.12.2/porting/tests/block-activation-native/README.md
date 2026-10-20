# Native GUI-only activation family (CP67 baseline)

Baseline: `d860257bb1f4bc6043d5cb80bd752ede1ebc53bd`. This isolated batch changes exactly 105 production callback methods in 105 files. Main was not edited. No dependency downloads, installations, broad Forge compatibility facade, arbitrary offhand guard, body replacement, or balancing change.

## Production rule and bounded selector

Change the old scalar `onBlockActivated(World,int,int,int,EntityPlayer,int,float,float,float)` declaration to the genuine native `onBlockActivated(World,BlockPos,IBlockState,EntityPlayer,EnumHand,EnumFacing,float,float,float)` declaration. Keep the original world/player/hit parameter names. Extract only the old x/y/z variables actually used from `BlockPos`; for 14 methods extracting a GUI ID from the old side, use `EnumFacing.getIndex()`. Preserve every byte of the original body after those locals. Preserve `@Override`. No scalar overload or bridge remains in a selected owner.

The finite selector contains 61 `standardOpenBehavior` delegates, 20 tile-checked direct GUI callbacks, 8 direct GUI callbacks, 5 core-resolved GUI callbacks returning false when sneaking, 5 core-resolved callbacks consuming sneak, 4 core-and-tile GUI callbacks, and 2 disabled callbacks. The selected active callbacks all return true before any GUI action on the remote/client side. None reads either held item, mutates inventories, consumes items, changes world state, or uses the new state/hand/hit arguments. Core traversal retains its original temporary visited-list update and clearing. The 2 disabled methods return false without action. Exact valid native-call arguments are the contract, not null `BlockPos`/`EnumFacing` misuse.

The old scanner found 227 scalar methods, of which 122 are excluded here. Its count is a source-declaration count, not the earlier compiler-error count. Excluded meaningful item-hand predicates, item/world mutations and false-after-side-effect patterns need their own semantic adaptation, not an invented global offhand policy. MachineStardar is excluded despite its GUI-only body because MachineDishControl has a supported direct scalar call to it.

## Source attribution and virtual dispatch

`selection.json` records every original/new complete method, used locals, group and excluded callback. `baseline.zip` records the 105 complete CP67 source files and unchanged BlockDummyable collaborator. `original-methods.json` records the methods from `ntm-original-pristine`. `source-proof.py` verifies exact CP67 whole-file forward/inverse equivalence and original method parity allowing only the already-landed scalar-to-BlockPos tile lookup spelling. The batch itself does not modify those lookup expressions.

The full-source scan resolves top-level classes using package and explicit imports and records all callback/helper occurrences in `attribution.json`. All callback declarations are top-level, so no anonymous/nested callback override is omitted. Eleven descendants of selected roots are audited. Ten inherit the selected callback; TurretHowardDamaged declares its own disabled callback and is selected too, so TurretBaseNT's migration cannot bypass it. The generated native fixtures preserve selected-to-selected ancestry. No selected callback calls `onBlockActivated` (including super calls). Existing `super.standardOpenBehavior` remains an ordinary, unchanged call to the same domain helper.

The remaining scalar super-call sites belong to excluded families: TNT, Guide/LogicBlock, cable/paintable/valve, and TestEventTester. MachineDishControl/MachineStardar remain untouched as a closed direct-call pair. BlockDummyableBeam's scalar call through BlockDummyable was already unresolved against native Block: BlockDummyable declares no scalar activation root. It remains a separate unfinished lifecycle and is not claimed fixed. ItemToolAbility's old-name/SRG reflection compares against scalar Block.class methods that native Block does not have; both paths already fall through their catch blocks. Removing a selected scalar method does not make that existing reflection path work, and this batch does not claim native tool ability detection. The unported Beam overload of findCore is not in the selected inheritance graph. The selected helper resolves to BlockDummyable's sole standardOpenBehavior and IBlockAccess findCore/findCoreRec methods.

## Actual native tests, not a full-owner claim

Run:

```
python3 source-proof.py --report attribution.json
python3 run.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath-file /tmp/nagaport-ae2-native-classpath.txt --mutants
./dispatch/run.sh
```

`run.py` compiles all 105 complete migrated methods with real native Block/World/IBlockState/EntityPlayer/EnumHand/EnumFacing and `@Override`, plus seven complete unchanged BlockDummyable metadata/core/GUI methods in a clearly named bounded helper fixture. The complete original ForgeDirection and ThreeInts source files also compile. It is not compilation of the 105 complete owners, which still have unrelated unported lifecycle/rendering/dependencies. No fake `net.minecraft` or `net.minecraftforge` classes are generated. The original-version evidence here is source parity, not a claim that original 1.7 Minecraft was launched.

45,826 runtime assertions pass: both remote values, sneak values, absent/present core, absent/present correct tile, both hands, all six faces, negative coordinates, deliberately mismatched supplied state, ignored out-of-range/NaN hits, exact return booleans, wrong-tile cast failure, GUI ID/coordinate/world/player identity, once-or-none GUI dispatch, cyclic core traversal and clearing between clicks. Real native World/Chunk state backs core traversal. Tile identities are bounded NTM fixtures, not production tile implementations. The native superclass dispatcher is called through real Block references. Seven pattern representatives also run through the actual PlayerInteractionManager method and counting derived overrides/native super calls, with one eligible callback and correct return/effect count. A native spy covers each hand/face/boolean/empty-stack combination and exact positional/state/hit forwarding. Actual item-use-first SUCCESS suppresses activation.

GUI calls execute genuine FMLNetworkHandler and NetworkRegistry against a registered test mod ID and IGuiHandler. An explicit IFMLSidedHandler presentation seam captures the resulting GUI marker without loading graphics. This checks real registry routing, not a rendered GUI or the EntityPlayerMP server container/packet transport. The player is a bounded native EntityPlayer subclass and tile reads use a typed test map. The standalone native jar lacks launch-time event class transformations, so arbitrary registered Forge listener effects are covered by the separate bytecode proof, not claimed runtime event integration.

Four runtime mutants are rejected: lost face-derived GUI ID, swapped coordinate, duplicated GUI dispatch, and inverted remote guard. A fifth compile mutant restoring the scalar signature is rejected by native `@Override` attribution. `native-attribution.log` records compiled complete methods, and `native-test.log` records the run.

## Hand dispatch and boolean nuance

The independently reproduced `dispatch` proof examines the genuine pinned native Minecraft, PlayerControllerMP, PlayerInteractionManager, NetHandlerPlayServer and EnumHand bytecode. It proves MAIN_HAND precedes OFF_HAND and ordinary client callback true becomes SUCCESS, sends one block packet, and returns before the offhand iteration. The server may return PASS for an empty stack after callback true; its packet handler discards that result and never retries hands. Thus server false/PASS cannot independently cause an offhand double-open.

Do not generalize this to arbitrary other-mod behavior: a Forge event forcing useItem=ALLOW can force item handling after client callback true, including an empty-stack PASS and potential offhand attempt. Native event gates, reentrant callbacks, custom/replayed packets and repeated clicks are outside the ordinary-path guarantee. The batch preserves native hand/event semantics rather than hiding this with a main-hand-only guard. The proof is static actual-native control flow, not a full client launch. See `dispatch/README.md` for precise gates and limits.

## Merge and remaining scope

`apply-fragments.py --module MODULE` dry-runs exact old/new complete-method fragments and verifies inverses preserve every unrelated byte. Add `--write` only when authorized to integrate. `main-fragment-dry-run.json` records successful composition against the frozen CP68 main without writing it. This composes with same-file opacity/naming changes instead of replacing files. Reproduction source proofs deliberately check the isolated exact CP67 baseline; merged trees use fragment inverse receipts to establish unrelated-byte preservation.

No full mod compile, game launch, GUI rendering, tile/container registration, or excluded-owner lifecycle claim. All CP37+ release/checkpoint policy holds remain in force, including unrelated lighting consent. This evidence does not relax that gate.

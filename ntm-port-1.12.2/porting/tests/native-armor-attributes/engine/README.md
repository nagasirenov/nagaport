# Actual-engine armor attribute oracle

From the `ntm-port-1.12.2` directory, using the existing local Java 8/toolchain cache:

```sh
python3 porting/tests/native-armor-attributes/engine/run.py
```

This compiles only two test programs in a temporary directory, runs them against the actual supplied engine/mod artifacts, and writes `runtime-oracle.log`, `runtime-inputs.json`, and `original-payloads.json`. It does not download dependencies, build the mod, or edit production source. Optional paths are available through `run.py --help`.

To regenerate the static exact method/Forge-patch excerpts from the existing artifacts:

```sh
python3 porting/tests/native-armor-attributes/engine/capture.py
```

Latest result: 68 actual-engine assertions plus 12 original custom maps / 16 original modifier tuples. The original tuples are generated from complete supplied `ArmorDiesel`, `ArmorDesh`, and `ArmorLiquidator` classfiles after symbol-only SRG-to-official-name remapping, then actual old `Item.k()` virtual dispatch. No original method body is removed, reconstructed, or replaced. Old `ArmorModHandler` supplies its real UUID array. The legacy client copy strips signature metadata only so independently loaded classes can coexist; client class bytes are unchanged.

`contract-audit.md` explains the old main-hand-plus-four-armor dispatch, new offhand boundary, ItemStack NBT replacement/filtering, native worn armor/toughness baseline, the protection-stripping counterexample, and the finite nine-owner MAINHAND seed rule. `owner-hierarchies.txt` hashes the inspected source snapshot; production edits elsewhere may subsequently change those hashes without adding a hook override.

Scope limits: legacy Forge's stack-sensitive bridge and old-five/native-six equipment loops are established by exact Forge patch/bytecode evidence, not a fully patched legacy runtime or full entity-update simulation. Runtime uses actual engine classes, test-only native item descendants, and constructor-bypassing allocation of original armor objects. Only unrelated constructors are bypassed; all tested attribute bodies and real native AttributeMap operations execute. No fake Minecraft/Forge classes, complete mod lifecycle startup, renderer, client/server gameplay, or malformed-NBT behavior is claimed. Tool/weapon migration is out of scope.

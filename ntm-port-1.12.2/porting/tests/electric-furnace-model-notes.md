# Dedicated native electric-furnace models

## Scope and runtime path

The raw `MachineElectricFurnace extends BlockContainer` is not eligible for
`LegacyBaseModelHandler`, which handles a separate `BlockBase` subset. This
change does not alter or depend on that bridge. It adds one client-only automatic
Forge model-registration subscriber, `ElectricFurnaceModelHandler`, and 67
native JSON resources:

- Two blockstate files use the preserved registry IDs
  `hbm:tile.machine_electric_furnace_off` and
  `hbm:tile.machine_electric_furnace_on`, each with all sixteen `metadata=N` keys.
- Thirty-two block models select original textures for each metadata value.
- Thirty-two item models inherit the matching block models. The complete
  production handler maps native item metadata 0 through 15 to these models.
- One shared furnace-only geometry definition preserves six full-cube faces,
  outward culling, diffuse shading and ambient occlusion. It inherits the native
  `minecraft:block/block` item-display transforms, not an identity/default
  transform or an item built-in renderer.

Standard native JSON loading discovers/stitches the referenced block sprites and
bakes these models. No custom texture-stitch/bake subscriber, production test
helper, reflection, fake Minecraft type, legacy icon callback or legacy renderer
is used in this runtime path. Existing model machinery and other machines are
unchanged. Five PNG resources are byte-identical to supplied pristine original
resources; their hashes are pinned in `electric-furnace-model-manifest.json`.

## Original behavior retained

Each face uses the literal original selection rule:

    metadata == 0 && side == 3 ? front :
        side == metadata ? front : side == 0 ? bottom : side == 1 ? top : side

The front sprite is chosen from the original active/off texture. In particular,
metadata 0 has BOTH a front bottom and a front south face; metadata 1 has a front
top face; metadata 6 through 15 has no front face. These are deliberately not
clamped to horizontal directions. There is no new emissive flag, tint or light
value in the models. Native side-culling lists contain one face each; the null
face list is empty for both blocks and items, so native item traversal emits six
faces rather than duplicating six side faces in a second general list.

Full-cube UV orientation was independently executed against the actual official
1.7.10 client `blm` (`RenderBlocks`) face methods, using its actual `bmh`
(`Tessellator`) raw vertex buffer. The six mappings are:

| Face | U | V |
| --- | --- | --- |
| Down | x | z |
| Up | x | z |
| North | 1 - x | 1 - y |
| South | x | 1 - y |
| West | z | 1 - y |
| East | 1 - z | 1 - y |

The native generic cube's down face instead reverses V. The dedicated furnace
geometry therefore sets down-face UV to `[0,16,16,0]` explicitly, and other faces
to `[0,0,16,16]`. Native `FaceBakery` retains its standard 0.1% UV edge inset;
that native atlas convention is accounted for in the tests, not claimed as
bit-identical legacy UV endpoints. Geometry, face orientation, sprite selection
and UV orientation are checked independently for every baked vertex.

## Tests and reproduction

Run from the project root with Java 8 and the resolved real Minecraft/Forge
classpath (replace the path and use the platform's native classpath separator):

    python3 porting/tests/probe-electric-furnace-models.py \
      --java-home /workspace/shared/nagaport-toolchain/java8 \
      --classpath "$(cat porting/continuation-2026-10-19/native-probe-classpath.txt)"

Optional actual old-bytecode UV oracle:

    python3 porting/tests/probe-electric-furnace-models.py \
      --java-home /workspace/shared/nagaport-toolchain/java8 \
      --classpath "$(cat porting/continuation-2026-10-19/native-probe-classpath.txt)" \
      --legacy-client-jar /tmp/nagaport-legacy-client.jar

The optional binary is not packaged or a production dependency. Its official
URL and required SHA-1 `e80d9b3bf5085002218d4be59e668bac718abbc6` are in the
manifest. The runner validates the entire jar before using it. The ordinary
native test does not need this optional binary, Gradle, Git objects or downloads.
`--source-only` verifies reviewed full-file hashes and the complete asset set.
Python runners use UTF-8, `.exe` on Windows and `os.pathsep`; Windows execution
has not been performed.

Final focused results:

- 40,575 native model assertions pass: full production registration handler and
  actual RefStrings compile; exact automatic client-only event annotation;
  all 32 native metadata state routes; all 64 JSON block/item models baked via
  real Forge `VanillaModelWrapper.bakeImpl`/`bakeNormal`/native `FaceBakery` from
  both FolderResourcePack and FileResourcePack; actual PNG lookup/decode;
  sprite/particle selection, every vertex position, UV orientation, packed
  normal, winding, culling, diffuse flag, AO and native item transforms; exact
  32 native ItemModelMesher metadata selections.
- 150 assertions against actual official 1.7.10 RenderBlocks/Tessellator output
  pass for all six full-cube face methods.
- Five existing PNG resources compare byte-for-byte with supplied pristine
  original assets. No PNG files were changed or synthesized.

Log: `porting/continuation-2026-10-19/electric-furnace-model-native.log`.

## Explicit fixture and acceptance limits

The model probe compiles the entire production handler against actual
Minecraft/Forge classes, not replacement Minecraft classes. It registers two
controlled native BlockContainer metadata fixtures in real native registries to
test state routing without compiling the whole still-incomplete mod. It does
not exercise the production furnace tile or block lifecycle. The test atlas
sprites use deterministic native sprite dimensions and atlas positions; actual
PNG bytes are validated and decoded, but atlas upload is not run.

Only in the test, Unsafe allocates actual ModelLoader/ModelManager instances
without GL/client initialization, reflection injects the actual native
FaceBakery and baked registry, and the actual Forge vanilla-model wrapper is
invoked reflectively. Production has none of those bypasses. The optional old
oracle similarly bypasses the old client singleton constructor but executes the
original face methods and reads the original Tessellator buffer without a draw.

Annotation and direct registration invocation tests do not establish FML's
runtime discovery/event delivery, live atlas stitching/reload, visual lighting,
GPU drawing or dedicated-server/client startup. Standalone registry prefix
warnings result from the missing active HBM mod container; registered IDs are
explicitly tested unchanged. No full Gradle build was run for this model task.
These checks are not a runnable-mod, gameplay, energy, world-save or radiation
acceptance claim.

## Open visual gate: digging, running and dust particles

The JSON `particle` texture is a fixed native model fallback, not legacy debris
parity. Original Forge 1.7 EntityDiggingFX selects a random face with
`world.rand.nextInt(6)` before its superclass constructor, then uses that face's
metadata-sensitive icon. This also affects running `blockcrack_` and
`blockdust_` particles. Native ParticleDigging instead selects the model's fixed
particle sprite, so this visual/RNG difference remains open.

A complete hook-only closure is not available: native hit geometry consumes the
ParticleManager's private RNG, and native `addDestroyEffects` omits the supplied
destroyed state/metadata even though ParticleManager receives that state.
Wrapping crack/dust factories alone would not repair the manager's direct
hit/destroy construction paths. No particle factory, private-field accessor, RNG
change or broader renderer change was added. The passing model tests validate
the declared fixed fallback, not debris or full visual parity. This gate is
deferred while functional machine work remains the priority.

# CP59 bounded native World block-read increment

Exact baseline: e72ec3d2ae8db6653ebaad529a574ee32c99260b. Production scope: 123 previously unconverted getBlock(int,int,int) calls in 62 files. Every selected receiver is attributed by full-source javac to exactly net.minecraft.world.World and a variable element; every coordinate and descendant is resolved as required by the original scanner. No regular-expression receiver selection. ERROR, subclass-declared, IBlockAccess, non-int and unresolved receivers/coordinates remain excluded. All 26 otherwise eligible metadata calls remain untouched: the native state owner cannot generically preserve arbitrary legacy metadata nibbles.

The existing, unchanged WorldBlockState.read boundary uses genuine World.getBlockState(new BlockPos(x,y,z)); the selected calls take IBlockState.getBlock(). No new production helper or Minecraft shim is added. Original bounds X/Z [-30000000,30000000) and Y [0,256), null check after arguments, and exact argument evaluation order remain intact. Native World itself only guards Y, so direct unguarded BlockPos substitution is not equivalent. Native built-in WorldServer/WorldClient inherit getBlockState. The original NTM FauxWorld extends World without overriding getBlock/getBlockMetadata; WorldInAJar/SubChunkSnapshot are different owners and excluded. Arbitrary external World subclass override equivalence is not certified.

Height calls deliberately remain unchanged. Actual original ahb.f(int,int) returns constant 64 outside the horizontal bounds, while native World.getHeight(int,int) returns getSeaLevel()+1. Native custom-sea-level worlds make a general rename unsound. The evidence file retains exact relevant bytecode sections.

## Reproduce

From module root:

    python3 porting/tests/native-world-reads-increment/check_source.py
    python3 porting/tests/native-world-reads-increment/attribute.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)"
    python3 porting/tests/native-world-reads-increment/probe.py --java-home /workspace/shared/nagaport-toolchain/java8 --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --original-client /tmp/nagaport-legacy-client.jar --mutants

The exact before/after manifest reconstructs all 62 whole files byte-for-byte and preserves every receiver and argument. Attribution reconstructs the CP59 baseline source into a temporary directory, runs the original fail-closed scanner over ALL production sources, then verifies actual native read/getBlock executable identities and exact receiver/coordinate AST spans for all 123 selected sites. Neither attribution run is a successful build; unfinished-port diagnostics are expected.

The runtime runner reuses hash-verified unchanged CP54 tests, adjusting only the integration count inventory to include this increment. The real native/original World/Chunk proof passes 4,721 assertions, five previously migrated complete extracted production methods pass 82 assertions, and actual original invocation evaluation passes six. The two positive/eight negative attribution controls and six semantic mutants pass. The five production-method tests are reuse of prior bodies, NOT new whole-class or newly selected body coverage. No stubbed platform compile or source exclusion is used. Original signed client bytes are unchanged; only the generated evaluation harness class is repackaged as documented by the original suite.

No full-game, full affected-class, save, rendering, arbitrary external subclass, arbitrary raw metadata, original crash-report text/wrapping, or clean compilation claim. Existing CP37 gates remain unresolved. Preserve this isolated baseline when integrating independent changes; do not replace historical manifests. Run full destination attribution again on the integrated tree if it changes overlapping source.

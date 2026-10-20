# Reinforced glass/brick placement increment

Baseline `86ab1487fe74afedea997ae0bda2b9d8394da20e`; 127 six-argument World placements, metadata0/flags3 only. Production changes only the calls in Barrel43, DesertAtom00216, DesertAtom00352, Silo6, Spaceship10. Of these,76 use reinforced_glass and51 reinforced_brick. Existing DungeonToolbox.placeUnorientedStructureBlock and placeRandomConcrete remain byte-identical.

Actual fields remain package-private mutable instance fields, evaluated once at the original argument position. No initializer substitution, caching, casts or new validation. Null/bounds/state-conversion timing, coordinate overflow, flags and virtual native World dispatch follow the previously proven unchanged helper. Arbitrary external field replacement/reflection/mod subclasses are not certified. Two permutations of the actual two native owners demonstrate field identity remains live, rather than constant-folded.

Whole-source lexical closure audits eight declarations including one unused Spaceship2 declaration; seven fields actually have migrated calls. Actual compiler attribution independently identifies declaring fields, World receiver, int coordinates, literal0/3 and unused result. All owned declarations, constructor writes, subclass/anonymous/shadow exposure and external field references checked, with nine closure mutations and20 Java8 attribution controls. Exact source inverse and127 per-site corruption controls preserve all unrelated bytes. All127 selected calls occur in latest CP64 compile diagnostic multiset. No independent full-mod compile run.

## Owner semantics, reused evidence

reinforced_glass is actual BlockNTMGlassCT(0, hbm:reinforced_glass, ROCK), hardness2/resistance25 (native getter15), opacity0. Actual native CT state metadata0 round-trips; default no ordinary drop, protected silk permission true. Original BlockNTMGlass scalar drop/silk method bodies and false-dropping constructor are unchanged. reinforced_brick is actual BlockGeneric/BlockBase ROCK, hardness15/resistance300 (native getter180), one metadata0 drop. No custom metadata normalization is added or nonzero Generic metadata claimed supported.

Nine complete owner/model/helper source hashes match existing CT49 and Generic proofs. Three exact original texture assets and five prior proof logs are pinned. They are reused, not re-executed; no CT visual polish or new lighting/overlay acceptance. Current full production owners and CT state helpers compile in the new probe. Full ModBlocks is represented only by its two exact construction statements; MainRegistry.blockTab is an explicit native creative-tab reference fixture. Generic uses actual BlockBase, not a fake platform API. Native ItemBlock registration is explicit; complete custom ItemBlockBlastInfo is not newly compiled or certified.

## Fresh bounded native results

45 actual owner assertions: exact constructors, metadata/native IDs, real World/Chunk placement, no tile, four fortune ordinary drop checks, native silk stack/eligibility, actual Block.harvestBlock with genuine diamond pickaxe and SILK_TOUCH, resulting EntityItems, compressed Anvil save/load, and World.destroyBlock. The bounded real EntityPlayer subclass supplies only spectator/creative and stats/exhaustion sinks. Both glass and brick gameplay classes are complete actual source. Two runtime mutants change glass dropping or metadata conversion and are rejected.

344 total assertions including those45 and896 exact original-expression/native placement events over nine coordinate contexts and two owner permutations. Original 1.7 World bounds bytecode reused from pinned legacy client. Existing argument evaluation probe rerun against these owners; historical six helper semantic mutations remain hash-pinned rather than repeated. Seven unsplit selected-expression methods compile to1267/187/311/1485/161/193/307 byte Code lengths. Complete surrounding generators still have unrelated unported APIs; no whole-generator bytecode or full game claim. Prior direct-expansion size justification is not reasserted for this smaller batch.

## Reproduce / integrate

Set J=/workspace/shared/nagaport-toolchain/java8 and CP=/tmp/nagaport-ae2-native-classpath.txt.

- migrate.py --java-home $J --classpath-file $CP (checks frozen selection; --apply only against exact baseline)
- ownership.py --java-home $J --classpath-file $CP
- check-owner-closure.py
- reuse-owners.py
- source-proof.py
- prove.py --java-home $J --classpath-file $CP --legacy-client /tmp/nagaport-legacy-client.jar --mutants
- merge.py --module MAIN_MODULE (read-only; --apply requires --receipt PATH)

Use evidence then exact fragment merger for main, preserving concurrent edits. No helper/owner/asset changes. Main dry-run passed. Existing visual gate remains open.

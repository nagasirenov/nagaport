# Independent native world-sound receiver audit

## Findings

- All 96 selected CP57 calls have a full receiver expression attributed by Java 8 javac as declared `net.minecraft.world.World`.
- Exactly one of the initial CP58 transformations is malformed: `XFactoryAccelerator.java:147`. Its complete original receiver is `ctx.getPlayer().world`, but the candidate produces `ctx.getPlayer().com.hbm.sound.WorldSoundEffects.play(world, ...)`.
- The corrected snapshot uses `com.hbm.sound.WorldSoundEffects.play(ctx.getPlayer().world, ...)` at that site.
- All 96 corrected calls resolve to the executable `com.hbm.sound.WorldSoundEffects.play(net.minecraft.world.World,double,double,double,java.lang.String,float,float)`. Their first arguments are declared `net.minecraft.world.World`.
- For all 96 corrected calls, the full original receiver becomes the complete first argument and all six original argument source expressions and attributed types are preserved. No selected argument has an ERROR type.

## Method and scope

The original tree was extracted from CP57 commit `324f35ad53f98e42045ad81c7ec4b45410d663df` using git archive. The initial current tree was frozen before the repair. The corrected tree is that snapshot with only the separately supplied corrected XFactoryAccelerator.java substituted. No main or worker source was edited by this audit.

Manifest path and line select the 96 sites. `ReceiverAudit.java` uses `JavacTask.parse()` plus `JavacTask.analyze()` over the entire source tree with the actual native dependency classpath. It visits MethodInvocationTree nodes and obtains the entire MemberSelectTree expression, `Trees.getTypeMirror`, executable element, enclosing owner, all argument expressions, and all argument types. It does not use a regex receiver selector. `ReceiverAuditSpans.java` provides independent parse-stage full receiver and invocation UTF-16 ranges.

CP57 attribution covered 4,051 compilation units and reported 20,681 unfinished-port source errors. The malformed current candidate covered 4,056 compilation units and reported 19,883 source errors. Its 95 other calls resolve correctly, while XFactoryAccelerator has ERROR receiver and first-argument types and a recovered CLASS rather than executable METHOD element. The corrected candidate covered 4,056 compilation units and reported 19,881 source errors. These are targeted attribution results in an unfinished port, NOT a claim that the whole project compiles. Diagnostic totals are not attributed solely to these sound changes because the candidate includes other integrated work. Selected-file diagnostics are retained in TSV files.

## Evidence files

- `ReceiverAudit.java`: full-source attribution auditor.
- `ReceiverAuditSpans.java`: parse-stage structural ranges auditor.
- `before.tsv`, `current.tsv`, `corrected.tsv`: complete full receiver, method owner/signature, argument source and type evidence.
- `before-spans.tsv`, `current-spans.tsv`, `corrected-spans.tsv`: full AST source ranges.
- `receiver-evidence.json`: per-site full original receiver, six original arguments, raw argument list, expected complete replacement invocation, native receiver type and UTF-16 ranges.
- `structural-report.json`: initial malformed transformation evidence.
- `audit-summary.json`: verified final counts and malformed site attribution.
- `selected-source-sha256.json`: exact selected source snapshot fingerprints.
- `run-audit.sh`: reproducible full attribution, structural scan and independent verification.

`receiver-evidence.json` SHA-256: `926fa4fc2428afd7a012a17addc3c067d61b0d21bfb4b278d75fc16975b0c8ff`.

# Native World air-query coordinates (CP93 candidate)

Base is sealed CP92. Fourteen compiler-attributed World.isAirBlock(int,int,int) calls in thirteen files now pass a genuine BlockPos(int,int,int), at the original call site. Receiver, argument expressions, integer arithmetic/overflow, short-circuiting and all surrounding source bytes are unchanged. No block-state helper or old-world API shim is added. Full-source javac attributes every selected receiver to native World and every original argument to int; the destination pass requires the actual World method and exact integer BlockPos constructor.

The native platform contract is explicit: original Forge1614 World.isAirBlock read one Block then called its Forge air hook. Actual native World.isAirBlock performs two state reads, selects the first state's Block, and passes the second state to its native hook. We call that real native API, not a homemade approximation. Custom worlds that mutate during reads can therefore observe the native two-read behavior. This is ordinary native engine behavior, not a claim that every foreign world behaves like 1.7.10.

82,283 bounded checks cover 5,485 actual native registry states at five coordinate extremes, real World.isAirBlock dispatch into a state-sensitive native Block override, first-owner/second-state identity, receiver/coordinate evaluation order, null/throwing arguments and short circuits. The fixture subclasses actual World but skips its unrelated constructor and overrides only state access; it does not prove chunk loading, placement, explosions, complete item behavior or an in-game world. The actual method's bytecode and authentic original Forge hook patch are recorded. No fake Minecraft class or production reflection is used.

Thirteen complete-file inverses and fourteen omitted-wrapper mutations protect all source bytes. No ItemStack semantics or gameplay formulas change. This family is separate from direct Block.isAir capture migration: it deliberately preserves World-level dispatch rather than replacing it with a direct block query.

Reproduce with the existing offline toolchain:

```
python3 porting/tests/native-world-air-query/check_source.py
python3 porting/tests/native-world-air-query/run.py
python3 porting/tests/native-world-air-query/attribute-after.py
```

The before-attribution command requires a fresh CP92 Java-only tree. migrate.py is a guarded one-time transformation against the exact sealed baseline, not an arbitrary project-wide regex. Partial attribution diagnostics are not a successful full compilation. No proprietary binaries are included.

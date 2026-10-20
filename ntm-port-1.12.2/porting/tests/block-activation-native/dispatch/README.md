# Native 1.12.2 activation dispatcher proof

## Reproduce

Run `./run.sh` in this directory. It runs Java 8 `javap -p -c` on the real, unmodified native classes resolved from `/tmp/nagaport-ae2-native-classpath.txt`, then executes bytecode/control-flow assertions. `NATIVE_CLASSPATH_FILE` and `JAVAP` may override those locations. The classpath must resolve exactly one copy of each inspected class, in the existing Forge 1.12.2 / 14.23.5.2768 `forgeBin` jar. No Minecraft substitute classes, compilation, downloads, installations, class transformations, game initialization, or client/server launch are involved. The output contains per-class SHA-256 provenance.

Artifacts: `prove_native_dispatch.py`, `run.sh`, `proof.log`, complete `*.javap.txt` disassemblies, and short per-method `*.txt` excerpts. Assertions intentionally refer to offsets in this native build and fail rather than silently generalizing to another version.

## Proven native control flow

1. `EnumHand.values()` returns a clone of an array ordered MAIN_HAND then OFF_HAND.
2. `Minecraft.rightClickMouse()` iterates that array from index zero. It calls `PlayerControllerMP.processRightClickBlock` for the current hand. When that call returns SUCCESS, all normal successor branches terminate at return 306; none reaches hand increment 388 or another block-dispatch call. Main-hand SUCCESS therefore prevents the offhand attempt in that invocation, including when the main-hand stack is empty.
3. Once the client controller invokes `Block.onBlockActivated` and receives true, it records success, bypasses placement checks, sends one `CPacketPlayerTryUseItemOnBlock` with the current hand, and, provided the Forge event's `getUseItem()` is not ALLOW, returns SUCCESS at 634. This ordinary true-activation path does not execute the empty-stack test.
4. This is not unconditional under Forge overrides: `getUseItem()==ALLOW` forces item handling after activation=true. With an empty stack it returns PASS, so the client may continue to offhand. Event cancellation, `useBlock==DENY`, item-use-first results, sneaking/bypass rules, spectator handling, world border, and other ordinary native gates also affect whether activation is reached.
5. Server `PlayerInteractionManager.processRightClickBlock` invokes `onBlockActivated` at 313 before testing the held stack at 325. If that stack is empty, the method returns PASS at 334 regardless of whether the callback returned true or false. The eligible activation has already happened. A cooldown also leads to PASS. With a nonempty ordinary item, no cooldown, survival mode, and no forced item ALLOW, callback=true retains SUCCESS. Item overrides and other guards mean server callback=true must not be universally equated to manager return SUCCESS.
6. Both controller methods have exactly one direct native `Block.onBlockActivated` invocation site and no backward control-flow edges or exception table. Thus each invocation reaches that direct callback zero times when gated/skipped, or once when eligible; neither directly loops or retries activation. This is a static structural invocation bound, not an observed in-game invocation count.
7. `NetHandlerPlayServer.processTryUseItemOnBlock` gets the hand from the packet, passes it to the interaction manager, invokes the manager at most once, and discards the return value with `pop` immediately after the call. It contains no hand enumeration and does not use a server-side PASS to retry the other hand.

## What this means for the selected GUI-only migration

Combining this native proof with the parent's separate verification that the selected active callbacks return true early on the remote/client side: an ordinary, uncancelled eligible main-hand GUI activation, without a Forge forced-item ALLOW override, produces one main-hand block packet and stops client hand iteration. Server missing-core or sneak branches returning false, or an empty server-held stack causing PASS after activation, do not initiate an offhand retry. An arbitrary MAIN_HAND-only callback guard is therefore not required to prevent an ordinary dispatcher-driven offhand double-open.

The two deliberately disabled callbacks returning false are not covered by the client-true consumption premise, but they also must not be claimed to open a GUI. This proof does not inspect those mod callbacks or GUI handlers; that is separate migration/harness evidence.

## Limits

Static proof of this exact Forge-native bytecode, not a full Minecraft client launch, network/in-game integration test, screenshot, or live GUI count. It does not guarantee exactly one GUI for arbitrary event listeners, runtime coremods, callbacks reentering activation, multiple/replayed/custom packets, explicit separate main/offhand calls, server plugin behavior, or repeated clicks. The packet bound is per ordinary dispatcher invocation, not a general server anti-duplication rule. Exceptions can abort normal paths. No claims are made about visual GUI correctness or tile-entity/container registration.

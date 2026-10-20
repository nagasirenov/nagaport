# Native armor/man model root

## Frozen production scope

Only `ModelArmorBase.java` and `ModelMan.java` change. Exact source and portable
original-fixture SHA-256 values are in `native-armor-models-scope.json`.
The original fixtures are the complete files from checkpoint `0ea420fc`.
The probe's exact substitution guard rejects unrelated production edits and does
not need Git or remote history when run.

`RenderManager.instance` becomes the current native Minecraft render manager;
`RenderPlayer.modelBipedMain` and the mob-biped field become `getMainModel()`.
Public OBJ mesh parts, constructors, type, both ModelMan render overloads, source
copy order, six-part draw order, offsets and original fallback formulas remain.
`onGround` becomes native `swingProgress`; native ArmPose maps to original held
weights EMPTY=0, ITEM/BOW=1, BLOCK=3. BOW_AND_ARROW activates the original fallback
bow equations. That fallback retains the original right-handed equations; native
biped/player paths copy the already-posed actual model, including native handedness.
No generic old pose fields or compatibility renderer shim are added.

The caller/descendant audit found 23 direct ModelArmorBase descendants and no
writes to the removed held-item/aimed-bow fields in them. ModelJetPack's separate
old ModelBiped implementation is not part of this root. Public OBJ `doRender` and
first-person `rightArm.postRender` remain available. Armor-disable, manly selection,
player event ordering, installation and four armor-pass interleaving belong to the
separate native player-layer integration; this model probe does not certify them.

A real native cache defect was reproduced before freeze: ModelMan's old raw
lighting enable set GL true without updating GlStateManager's false cache, so a
following native disable did nothing. Both enables now use the native setter.
This preserves the original lighting enable and deliberately does not add a new
lighting restore/default. The final test includes the following-draw regression.

## Evidence

Run `probe-native-armor-models.py` with the existing native classpath and Java 8.
It compiles the complete production ModelArmorBase, ModelMan, unchanged ModelArmorHEV
and ModelRendererObj against genuine Forge 2768, then runs on real Mesa EGL/LWJGL
through the existing genuine Forge event-transforming launcher. No downloads,
installs, fake platform classes or Unsafe object construction are used.

The runtime uses genuine RenderManager, RenderPlayer, RenderBiped, ModelPlayer,
ModelBiped, native entity constructors, TextureManager and GL matrices. The world
fixture supplies no chunks; the declared ResourceManager fixture records NTM mesh
calls and their real GL transforms instead of loading the complete mod resources.

The final log is `porting/continuation-2026-10-19/native-armor-models-FINAL.log`:
373 checks, including 256 original fallback combinations and transitions (both
arms' native poses, swing sentinels/values, riding and sneak), default/slim native
player pose copies, native mob-biped copies, native attribute propagation, both
complete ModelMan draw overloads, explicit-renderer precedence, hidden native skin
versus visible replacement mesh, public OBJ controls, original HEV four-slot draws,
balanced matrices and following-native-lighting-cache correctness.

The initial 371-check pass preceded the added cache regression and correction;
it is superseded by the final 373-check run. Full-game armor visuals, every
descendant, resource pack behavior and layer/event integration remain separate.

# Native RTG owner/model boundary

All three original registrations are covered: machine_rtg_grey (MachineRTG), machine_minirtg and machine_powerrtg (MachineMiniRTG). The latter alias was discovered during registry audit and included before freezing. Actual owner/tile behavior and energy accounting are in rtg-energy-native-notes.md; the same complete native owners/tiles compile in this rendering probe.

RenderRTGBlock becomes the native atlas item Gen mesh. Exact original named group, unshaded white inventory color, UV stream and translation are retained. The world baked model emits no quads, matching the old renderWorldBlock=false; the original tile renderer owns world Gen and optional four Connector parts. Native ENTITYBLOCK_ANIMATED avoids duplicate chunk geometry. Three blockstate resources retain all16 metadata values and the original rtg/rtg_cell/rtg_polonium materials. The original OBJ and six textures are byte-identical to the pinned original.

The native ModelRegistryEvent handler runs after real block and item registration; a premature lookup test genuinely rejects missing ItemBlock identity. Directory and JAR resource packs, material ownership and resource-reload cache invalidation are tested. The inventory stream is independently reconstructed from the original OBJ's parsed corners and pinned original call facts; the shared OBJ parser/atlas geometry has separate earlier native/original proofs. This is not a new independent proof of every parser implementation detail.

The complete RenderRTG TESR is invoked through the real TileEntityRendererDispatcher for all three owners and16 connector layouts. An observing mesh delegates every draw to the actual original HFR OBJ loader/renderer on Mesa EGL. Exact original transforms, groups, native GL/cache/matrix ownership and exception restoration pass. Library.canConnect's current complete production method is extracted unchanged, with real native World/Chunk and original connector interfaces; unrelated Library methods are outside scope. External registry/config/GUI/packet-thread/topology collaborators are inherited explicitly from the RTG energy probe, not replacements for Minecraft/Forge classes.

Result:15847 assertions. Full game startup, screenshot visual comparison, real electrical topology/network transport, old-world conversion and Windows runtime remain unverified. Native damage overlays are not newly enabled: original RTG had no custom-breaking support. Unknown new RTG subclasses/material definitions are not accepted as audited originals.

Reproduce from module with existing JDK8/native classpath:

    python3 porting/tests/probe-native-rtg-render.py --classpath "$(cat /tmp/nagaport-ae2-native-classpath.txt)" --java-home /workspace/shared/nagaport-toolchain/java8

The source manifest pins the included ntm-original-pristine tree without requiring unpublished Git objects. No dependency is downloaded by this probe.

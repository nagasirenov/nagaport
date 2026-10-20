# Native tutorial item panel

The complete ActorBasicPanel and ISpecialActor compile against actual Forge 2768/MCP stable39. Only the unused WorldInAJar/JarScene collaborator parameter types are fixture classes. Existing NativeGuiItemDraw, NativeItemQuadRenderer and native item/model interfaces are compiled unchanged, not reimplemented.

37 focused assertions pass. The panel resolves the live native RenderItem, uses native BufferBuilder POSITION_COLOR for the exact old gradient vertex/color order, and retains original text/item spacing, width/height clipping, first-line spacing and zero-count warning rectangles. Existing preview normalization preserves intentional zero-count item/NBT on a separate display copy; it never changes the input stack. Its shared native renderer zLevel is restored to the incoming value even if an item callback fails, rather than leaking the old separate renderer's reset into the shared owner. GL state changes use native GlStateManager so native item/font renderers do not observe stale depth, alpha, blend or texture caches.

Run from module:

    python3 porting/tests/actor-panel-native/run.py --java-home /path/to/java8 --classpath "native-classpath"

The Linux cloud proof uses the existing LWJGL native JAR and Mesa surfaceless EGL, with no downloads or installs. Native Bootstrap, Minecraft, RenderItem constructors, model manager, FontRenderer constructor, framebuffer, Tessellator and BufferBuilder execute. Four actual gradient framebuffer images compare pixel-for-pixel to an independent immediate-mode reference using original ordered positions and packed colors. Additional assertions cover mixed lines, right/bottom clipping, empty content, counts 0/1/64, NBT, item-overlay ordering, owner replacement and exception cleanup.

Text glyph draw and item mesh calls are recording subclass boundaries: this does not claim whole tutorial/world rendering, actual font glyph visual parity, whole client startup or in-game acceptance. The scoped repository currently contains only a commented example caller of ActorBasicPanel; this is a complete dormant class declaration root, not a newly activated tutorial feature. The broader existing native GUI helper has its own independent item rendering tests. Negative-count external previews are not certified; native ItemStack presents nonpositive counts through its empty semantics.

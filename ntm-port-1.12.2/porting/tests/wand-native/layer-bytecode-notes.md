# Original and native terrain state evidence

Inspected with the supplied Java 8 javap -c -p against the pinned original
Minecraft 1.7.10 client and the genuine current Forge 1.12.2 classpath. No network
source, native-production shim or inferred shader override is used.

Original EntityRenderer = blt, method a(float,long):
- Offsets 24..39 enable GL_CULL_FACE (2884), GL_DEPTH_TEST (2929), GL_ALPHA_TEST (3008).
- Offsets 42..47 call glAlphaFunc(GL_GREATER=516, 0.5f).
- Offsets 631..638 invoke RenderGlobal's terrain pass 0.
- Offsets 648..653 change alpha threshold to 0.1 only after terrain pass 0.

Original TextureMap = bpz:
- Atlas upload calls TextureUtil.a(int[][],int,int,int,int,boolean,boolean)
  at offset 921. TextureUtil = bqi; its upload computes mipmap enabled from
  the mipmap-array length > 1 (offsets 41..52), and forwards this to the sampler
  setup. The latter selects GL_NEAREST_MIPMAP_LINEAR (9986) for a nonblurred
  mipmapped terrain atlas. The original world renderer does not opt this owner
  out of that atlas state.

Native EntityRenderer:
- renderWorld enables depth and alpha, and calls alphaFunc(GREATER,0.5f)
  at offsets 34..46.
- renderWorldPass disables alpha at 567, draws SOLID at 572..580, and enables
  alpha again at 584 before CUTOUT_MIPPED at 626..634.
- CUTOUT_MIPPED honors the configured mipmapLevels; CUTOUT instead forces
  setBlurMipmap(false,false) at 669..671 before drawing at 678..686.
- Alpha threshold is lowered to 0.1 only at 714..720, after all terrain layers.

The wand PNG is binary-alpha, not blended glass. Using SOLID would drop alpha
cutout; using CUTOUT would unnecessarily drop legacy terrain mipmap support.
CUTOUT_MIPPED retains both, without blending or transparent-depth sorting.

The framebuffer proof uses alpha GREATER 0.5, ordinary depth writes with LEQUAL,
CCW front faces/back-face culling and blending disabled. It tests 0-level nearest
and linear samplers plus generated mipmaps, but is an order-equivalence test,
not a comparison of old versus native mipmap-generation algorithms.

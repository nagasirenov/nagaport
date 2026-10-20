# BufferUtil: complete native codec root

## Scope and provenance

Only `src/main/java/com/hbm/util/BufferUtil.java` is changed in production. The
whole utility is compiled and exercised against actual Minecraft 1.12.2,
Forge 14.23.5.2768 / MCP stable_39 and Netty 4.1.9.Final with Java 8. No platform
substitute, extracted utility method, NTM collaborator, wire redesign, packet
handler, transport thread, reference-count fix or source exclusion is involved.

Checkpoint18 baseline: `2cc79304086a82ddb5af1c32e74024b2db2ba05e`.
Original/pristine source is pinned to commit
`dff345a2411f7d3d6bef5c0b6c3aa8ab47ac357e`, path
`ntm-original-pristine/src/main/java/com/hbm/util/BufferUtil.java`, SHA256
`3e3cf70554e20340f35a9370549bc8ba76c25ebd9059d303e42976da5e47be2d`.
Original/pristine is not modified. The baseline already contains the native
Vec3d spelling; its string, integer-array and vector method bodies are unchanged.

`check-buffer-util-native-source.py` exports `FILES` and
`reverse_source(relative, text)`. It reconstructs the complete checkpoint18
source exactly using pinned before/after hashes and position-specific edits,
rejects six deliberate mutations and passes unrelated files through unchanged.
Compose this layer before checkpoint18/historical guards; do not refresh their
manifests. Reproduction does not require local Git baseline objects. If the
original sibling tree is present, its immutable original file hash is checked.

## Wire and failure contracts

- Strings retain signed int byte length, all-negative-length null behavior,
  raw UTF-8 and native charset replacement for malformed input/surrogates.
- Integer arrays retain one signed int element count followed by precisely that
  many signed ints. Successive singular arrays have no extra enclosing frame.
  Null writing still throws NullPointerException; negative lengths still throw
  NegativeArraySizeException after consuming the length.
- Vectors retain a boolean followed by x/y/z doubles. Null, nonzero boolean
  decoding and nonfinite values are not normalized by this utility.
- NBT retains signed short compressed-byte length, GZIP named-compound data,
  and -1 decoding to a fresh empty compound, not null. `writeCompressed` is the
  actual native encoder. The native `readCompressed` convenience API uses an
  unlimited size tracker, so the decoder explicitly opens the same GZIP/data
  stream stack and calls native `CompressedStreamTools.read` with the original
  2097152-byte NBTSizeTracker. Native 1.12/Forge accounting rules determine the
  exact decoded-size threshold; no legacy accounting implementation is faked.
- NBT compressor IOException is still printed and followed by a zero-length
  frame. Decoder IOException is still printed and returns a fresh empty
  compound; runtime/array/index exceptions are not swallowed.
- Oversized NBT is not clamped or repaired. Actual payloads with compressed
  lengths 32767, 32768, 65535 and 65536 are tested. The complete compressed
  payload is always written; 32768 decodes as negative array length, 65535
  collides with -1 and leaves all payload unread, and 65536 wraps to zero,
  prints EOF and leaves all payload unread. These are preserved legacy hazards,
  not recommendations for a new protocol.
- Item records retain short registry ID, signed byte count, short damage, then
  NBT. Positive counts beyond 127 are narrowed rather than clamped. Every
  negative ID still returns null after just the ID. The damageable-or-share-tag
  short circuit and raw stack-tag choice remain, rather than adopting native
  PacketBuffer or Forge share-tag/capability serialization.
- Partial read/write failure keeps prior fields consumed/written. No rollback,
  extra length preflight, clear, retain or release was added to caller buffers.

## Native representation boundary, not hidden parity

1. `writeItemStack(null)` and native empty stacks write the existing -1 short.
   This includes zero/negative-count and AIR stacks: native public ItemStack
   item/count access no longer exposes their old nonempty identity. No
   reflection, field shim or fabricated item is used to recover it. In 1.7 a
   non-null zero-count stack could have carried an item identity in this format;
   that producer representation is not preserved in native empty stacks.
2. The reader deliberately remains nullable for negative IDs, preserving this
   mod utility's existing API rather than globally changing callers to EMPTY.
   A nonnegative-ID record with signed count <= 0 is nevertheless fully read
   into a distinct native ItemStack (not shared EMPTY). It has native empty
   semantics and public count zero, while its NBT is still assigned. Thus a
   positive writer count 128 emits the unchanged byte 0x80, but the resulting
   native empty value cannot roundtrip back to that same positive count.
3. Metadata retains signed-short narrowing and the real ItemStack constructor's
   negative-damage handling. Unknown positive registry IDs produce the actual
   native empty-stack representation after consuming all fields; no registry
   fallback is synthesized. Native registry IDs are not promised to match every
   modded 1.7 save/session, only the original numeric wire format.

## Evidence and reproduction

`BufferUtilNativeCodecTest.java` compiles with the complete production utility.
It covers all ten public read/write methods: independent literal bytes for
strings/int arrays/vectors/NBT/items; common nested NBT types; item identity,
count, metadata/tag gating; null/EMPTY; malformed and truncated inputs; signed
boundaries and exact reader/writer indices; native 2 MiB limit; compression IO
failure; oversized wrap/sentinel hazards; and preservation of nullable return.

NBT fixtures start with manually specified uncompressed NBT byte sequences.
`buffer-util-native-fixtures.json` pins their framed GZIP bytes. The source
check independently validates the RFC1952 header, DEFLATE, CRC32 and byte count
using Python's zlib, then checks those exact bytes are pinned in the Java test.
Expected bytes are not generated by production serialization. Rich compound
roundtrips supplement, rather than replace, these independent byte fixtures.

From the project root with the resolved actual native classpath:

    python3 porting/tests/probe-buffer-util-native.py --java-home /path/to/java8 --classpath "<native classpath>"
    python3 porting/tests/probe-buffer-util-native.py --source-only

Runner uses explicit UTF-8, the host's executable suffix and `os.pathsep`, a
fresh temporary output directory and no source rewriting. On Windows, supply
a Windows-native classpath and Java installation. Native Windows was not run.
Final successful grouped log:
`porting/continuation-2026-10-19/buffer-util-native-codec-FINAL.log`.
This proves the complete utility's bounded native contract, not production
packet dispatch, threading, network topology, server/client gameplay or a full
mod build. The family coordinator owns the single full Gradle compile.

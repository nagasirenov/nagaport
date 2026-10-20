# Exact CP92 source composition

Run from the repository root:

```sh
python3 ntm-port-1.12.2/porting/tests/legacy-press-promotion/cache-audit/scan.py \
  --compose-colored --compose-replaceability --compose-cp92
```

This opt-in layer restores only four complete source files using the existing
strict inverse APIs, before the unchanged historical inventory and hash checks:

- `native-block-air-captures/check_source.py:reverse`: BlockVolcano,
  TileEntityCoreEmitter and TileEntityICFController.
- `remaining-format-batches/check_source.py:reverse_source`: RenderBobble.

The full implementation, manifests and transitive imported implementation/data
files are SHA-256 pinned in `composition-cp92.json`. Each family's intersection
with the historical protected source set must equal the explicit file list.
Each inverse checks its complete current-file hash and reconstructed baseline
hash. The reconstructed bytes must also match the original cache-audit hash.
An unrelated appended edit, truncation and line-ending change are rejected for
each file: 12 negative controls. Files outside this finite intersection pass
through untouched.

Only RenderBobble, TileEntityCoreEmitter and TileEntityICFController had shifted
getter line numbers. BlockVolcano additionally required a complete-file inverse
because its protected hash changed, despite unchanged getter occurrence rows.
The combined source view reproduces all 518 historical occurrences, including
their exact paths, line numbers, receiver roles and source lines. No new source
files, deleted files or other CP92 families required normalization.

The original `manifest.json`, all source hashes, both occurrence TSVs, the
historical README and the bytecode/diagnostic artifacts remain unchanged.
The manifest itself is additionally pinned as
`8f465ddad6b8e3b24d50e6ae8b256768539201cc81791276c0d908c51939f465`.
This is source-audit composition only. It adds no runtime, gameplay, cache or
renderer parity claim and does not weaken the documented diagnostic boundary.

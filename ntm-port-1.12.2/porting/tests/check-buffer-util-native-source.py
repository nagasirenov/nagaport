#!/usr/bin/env python3
"""Exact full BufferUtil source reversal before checkpoint18 guards. No Git object required."""
import hashlib
import json
from pathlib import Path
import struct
import zlib

ROOT = Path(__file__).resolve().parents[2]
MANIFEST = json.loads(Path(__file__).with_name('buffer-util-native-source-manifest.json').read_text(encoding='utf-8'))
FILES = {entry['path']: entry for entry in MANIFEST['files']}
BASELINE = '2cc79304086a82ddb5af1c32e74024b2db2ba05e'
ORIGINAL_SHA256 = '3e3cf70554e20340f35a9370549bc8ba76c25ebd9059d303e42976da5e47be2d'

def sha(text):
    return hashlib.sha256(text.encode('utf-8')).hexdigest()

def reverse_source(relative, text):
    entry = FILES.get(str(relative).replace('\\', '/'))
    if entry is None:
        return text
    assert sha(text) == entry['after_sha256'], (relative, 'BufferUtil native full-source drift')
    for edit in reversed(entry['edits']):
        position = edit['after_offset']
        assert text[position:position + len(edit['new'])] == edit['new'], (relative, 'exact reverse edit mismatch')
        text = text[:position] + edit['old'] + text[position + len(edit['new']):]
    assert sha(text) == entry['before_sha256'], (relative, 'BufferUtil checkpoint18 full-source mismatch')
    return text

def main():
    assert MANIFEST['baseline'] == BASELINE
    assert MANIFEST['original_sha256'] == ORIGINAL_SHA256
    assert MANIFEST['original_commit'] == 'dff345a2411f7d3d6bef5c0b6c3aa8ab47ac357e'
    assert set(FILES) == {'src/main/java/com/hbm/util/BufferUtil.java'}
    for relative, entry in FILES.items():
        text = (ROOT / relative).read_bytes().decode('utf-8')
        before = reverse_source(relative, text)
        for edit in reversed(entry['edits']):
            position = edit['before_offset']
            assert before[position:position + len(edit['old'])] == edit['old'], (relative, 'exact forward edit mismatch')
            before = before[:position] + edit['new'] + before[position + len(edit['old']):]
        assert before == text, (relative, 'full-source forward reconstruction')
        for mutation in [text + '\n', text[:-1], text.replace('2097152L', '2097153L'),
                         text.replace('buf.writeShort(-1)', 'buf.writeInt(-1)', 1),
                         text.replace('buf.readByte()', 'buf.readUnsignedByte()', 1),
                         text.replace('buf.writeInt(array.length)', 'buf.writeShort(array.length)', 1)]:
            assert mutation != text
            try:
                reverse_source(relative, mutation)
            except AssertionError:
                pass
            else:
                raise AssertionError((relative, 'BufferUtil drift accepted'))
    assert reverse_source('unrelated.java', 'unrelated text') == 'unrelated text'
    pristine = ROOT.parent / 'ntm-original-pristine/src/main/java/com/hbm/util/BufferUtil.java'
    if pristine.is_file():
        assert hashlib.sha256(pristine.read_bytes()).hexdigest() == ORIGINAL_SHA256, 'Original/pristine BufferUtil drift'
    # Independent wire oracles start with manually specified NBT, not production serialization.
    fixtures = json.loads(Path(__file__).with_name('buffer-util-native-fixtures.json').read_text(encoding='utf-8'))
    test = Path(__file__).with_name('BufferUtilNativeCodecTest.java').read_text(encoding='utf-8')
    for name, fixture in fixtures.items():
        raw = bytes.fromhex(fixture['raw_nbt_hex'])
        compressor = zlib.compressobj(6, zlib.DEFLATED, -15)
        gzip = bytes.fromhex('1f8b0800000000000000') + compressor.compress(raw) + compressor.flush()
        gzip += struct.pack('<II', zlib.crc32(raw) & 0xffffffff, len(raw))
        framed = struct.pack('>h', len(gzip)) + gzip
        assert framed.hex() == fixture['framed_hex'], (name, 'independent RFC1952 fixture mismatch')
        assert '"' + fixture['framed_hex'] + '"' in test, (name, 'native fixture not pinned in test')
    print('PASS BufferUtil exact checkpoint18 reverse/forward source reconstruction, six drift rejections, immutable original hash, independent NBT wire fixtures.', flush=True)

if __name__ == '__main__':
    main()

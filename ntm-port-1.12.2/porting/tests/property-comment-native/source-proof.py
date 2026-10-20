#!/usr/bin/env python3
"""Exact three-file migration/inverse guard; no normalizing source text."""
import hashlib, json, pathlib, re, zipfile
HERE = pathlib.Path(__file__).resolve().parent
ROOT = HERE.parents[3]

def verify(sources):
    manifest = json.loads((HERE / 'selection.json').read_text())
    count = 0
    with zipfile.ZipFile(HERE / 'baseline.zip') as baseline:
        for path, entry in manifest['files'].items():
            before = baseline.read(path)
            assert hashlib.sha256(before).hexdigest() == entry['sha256_before']
            expected = before
            # CommonConfig intentionally has seven identical writes. Consume exactly one each.
            for change in entry['changes']:
                old, new = change['old'].encode(), change['new'].encode()
                assert old in expected
                expected = expected.replace(old, new, 1)
                count += 1
            actual = sources[path]
            assert actual == expected, path + ': not the exact finite substitution'
            assert hashlib.sha256(actual).hexdigest() == entry['sha256_after']
            inverse = actual
            for change in entry['changes']:
                inverse = inverse.replace(change['new'].encode(), change['old'].encode(), 1)
            assert inverse == before, path + ': inverse differs'
            assert b'import net.minecraftforge.common.config.Property;' in actual
            assert not re.search(rb'\.comment\b', actual)
            # Every receiver is a local of the imported, native Property type from Configuration.get.
            for receiver in re.findall(rb'(\w+)\.setComment\(', actual):
                assert re.search(rb'Property\s+' + receiver + rb'\s*=\s*config\.get\(', actual)
    assert count == 27
    return count

def main():
    manifest = json.loads((HERE / 'selection.json').read_text())
    sources = {p: (ROOT / p).read_bytes() for p in manifest['files']}
    verify(sources)
    rejected = 0
    for path, entry in manifest['files'].items():
        # Each individual call removal must be rejected, even when spellings repeat.
        original = sources[path]
        spans = list(re.finditer(rb'\w+\.setComment\([^\r\n]*\);', original))
        for span in spans:
            mutated = dict(sources)
            mutated[path] = original[:span.start()] + original[span.end():]
            try:
                verify(mutated)
            except AssertionError:
                rejected += 1
            else:
                raise AssertionError('Accepted missing comment setter')
    for path, old, new in [
        ('CommonConfig', b'return prop.getInt();', b'return 0;'),
        ('CommonConfig', b'null, min, max', b'comment, min, max'),
        ('BombConfig', b'"Radius of the Gadget"', b'"Changed text"'),
        ('BombConfig', b'"3.00_gadgetRadius", 150', b'"3.00_gadgetRadius", 151'),
        ('WeaponConfig', b'public static void loadFromConfig(Configuration config) {',
         b'public static void loadFromConfig(Configuration config) { config.save();')]:
        key = 'ntm-port-1.12.2/src/main/java/com/hbm/config/' + path + '.java'
        assert old in sources[key]
        mutated = dict(sources); mutated[key] = sources[key].replace(old, new, 1)
        try:
            verify(mutated)
        except AssertionError:
            rejected += 1
        else:
            raise AssertionError('Accepted unrelated behavior change')
    print('PASS: 27 exact native Property substitutions; three full-file inverses; %d source mutants rejected' % rejected)

if __name__ == '__main__':
    main()

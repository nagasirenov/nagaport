#!/usr/bin/env python3
"""Finite positive and tamper-negative checks; writes only temporary evidence copies."""

import copy
import importlib.util
import json
from pathlib import Path
import shutil
import sys
import tempfile
import unittest

sys.dont_write_bytecode = True
HERE = Path(__file__).resolve().parent
SPEC = importlib.util.spec_from_file_location("verify_provenance", HERE / "verify_provenance.py")
VERIFIER = importlib.util.module_from_spec(SPEC)
SPEC.loader.exec_module(VERIFIER)


class ProvenanceChecks(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory(prefix=".provenance-test-", dir=HERE)
        self.root = Path(self.temp.name)
        self.evidence = self.root / "evidence"
        self.evidence.mkdir()
        for path in HERE.iterdir():
            if path.is_file() and path.suffix in (".json", ".txt"):
                shutil.copyfile(path, self.evidence / path.name)
        self.manifest = self.root / "manifest.json"
        self.sounds = self.root / "sounds.json"
        shutil.copyfile(HERE.parent / "manifest.json", self.manifest)
        shutil.copyfile(VERIFIER.PROJECT / "src/main/resources/assets/minecraft/sounds.json", self.sounds)

    def tearDown(self):
        self.temp.cleanup()

    def validate(self):
        return VERIFIER.validate(self.evidence, self.manifest, self.sounds)

    def mutate(self, path, function):
        document = VERIFIER.read_json(path)
        function(document)
        path.write_text(json.dumps(document))

    def test_unmodified_evidence_passes(self):
        result = self.validate()
        self.assertEqual((result["aliases_checked"], result["sample_slots_checked"],
                          result["unique_sample_asset_hashes"]), (15, 22, 22))

    def test_changed_category_fails(self):
        self.mutate(self.sounds, lambda d: d["random.explode"].update(category="master"))
        with self.assertRaisesRegex(ValueError, "Production differs from exact original"):
            self.validate()

    def test_changed_volume_fails(self):
        self.mutate(self.sounds, lambda d: d["random.explode"].update(
            sounds=[{"name": "random/explode1", "volume": 0.5}]))
        with self.assertRaisesRegex(ValueError, "Production differs from exact original"):
            self.validate()

    def test_lost_sample_multiplicity_fails(self):
        self.mutate(self.sounds, lambda d: d["random.explode"]["sounds"].pop())
        with self.assertRaisesRegex(ValueError, "Production differs from exact original"):
            self.validate()

    def test_missing_alias_fails(self):
        self.mutate(self.sounds, lambda d: d.pop("random.orb"))
        with self.assertRaisesRegex(ValueError, "not exactly the scoped 15"):
            self.validate()

    def test_extra_alias_fails(self):
        self.mutate(self.sounds, lambda d: d.update({"random.unscoped": copy.deepcopy(d["random.orb"])}))
        with self.assertRaisesRegex(ValueError, "not exactly the scoped 15"):
            self.validate()

    def test_duplicate_json_member_fails(self):
        raw = self.sounds.read_text()
        self.sounds.write_text(raw.rstrip()[:-1] + ',"random.orb":{"sounds":[]}}')
        with self.assertRaisesRegex(ValueError, "Duplicate JSON member: random.orb"):
            self.validate()

    def test_wrong_native_mapping_fails(self):
        self.mutate(self.manifest, lambda d: d["aliases"][0].update(native="entity.lightning.impact"))
        with self.assertRaisesRegex(ValueError, "manifest aliases/mapping/order changed"):
            self.validate()

    def test_wrong_manifest_category_fails(self):
        self.mutate(self.manifest, lambda d: d["aliases"][0].update(category="MASTER"))
        with self.assertRaisesRegex(ValueError, "manifest aliases/mapping/order changed"):
            self.validate()

    def test_changed_native_index_hash_fails(self):
        path = self.evidence / "native-index.json"
        self.mutate(path, lambda d: d["objects"]["minecraft/sounds/random/orb.ogg"].update(hash="0" * 40))
        # Even changing the local download receipt cannot bypass the pinned source.
        self.mutate(self.evidence / "downloads.json", lambda d: d["native-index.json"].update(
            VERIFIER.digests(path.read_bytes())))
        with self.assertRaisesRegex(ValueError, "Pinned official/snapshot bytes changed: native-index"):
            self.validate()

    def test_added_native_version_lf_fails(self):
        path = self.evidence / "native-version.json"
        path.write_bytes(path.read_bytes() + b"\n")
        with self.assertRaisesRegex(ValueError, "Pinned official/snapshot bytes changed: native-version"):
            self.validate()

    def test_changed_download_url_fails(self):
        self.mutate(self.evidence / "downloads.json", lambda d: d["native-sounds.json"].update(
            url="https://example.invalid/975a99ed9870f51bbae348533d775d730e3fee18"))
        with self.assertRaisesRegex(ValueError, "Asset URL hash mismatch"):
            self.validate()

    def test_changed_registry_listing_fails(self):
        path = self.evidence / "native-SoundEvents.javap.txt"
        path.write_text(path.read_text().replace("String entity.lightning.thunder", "String entity.lightning.impact"))
        self.mutate(self.evidence / "local-evidence.json", lambda d: d["runtime_bytecode"]["native-SoundEvents"].update(
            javap_hashes=VERIFIER.digests(path.read_bytes())))
        with self.assertRaisesRegex(ValueError, "Bytecode evidence changed: native-SoundEvents"):
            self.validate()

    def test_effective_multiset_preserves_duplicates_and_defaults(self):
        source = {"sha1": "0" * 40, "bytes": 10, **VERIFIER.DEFAULTS}
        self.assertNotEqual(VERIFIER.effective_multiset([source, source]), VERIFIER.effective_multiset([source]))
        for key, changed in (("volume", 0.5), ("pitch", 2), ("weight", 2), ("stream", True),
                             ("type", "event"), ("sha1", "1" * 40), ("bytes", 11)):
            self.assertNotEqual(VERIFIER.effective_multiset([source]),
                                VERIFIER.effective_multiset([{**source, key: changed}]))


if __name__ == "__main__":
    unittest.main(verbosity=2)

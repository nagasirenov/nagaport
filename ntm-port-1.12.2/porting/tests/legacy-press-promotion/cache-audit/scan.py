#!/usr/bin/env python3
"""Reproduce the finite source-getter inventory; verify the manually traced inputs."""
from pathlib import Path
import argparse
import hashlib
import importlib.util
import json
import re
import sys

HERE = Path(__file__).resolve().parent
sys.path.insert(0, str(HERE.parent))
from audit import code_only


def inventory(module, normalize=lambda path, text: text):
    found = []
    for path in sorted((module / "src").rglob("*.java")):
        text = normalize(str(path.relative_to(module)), path.read_bytes().decode())
        code = code_only(text)
        for match in re.finditer(r"\bgetBlockMetadata\s*\(\s*\)", code):
            line = code.count("\n", 0, match.start()) + 1
            prefix = code[:match.start()]
            role = "this" if re.search(r"\bthis\s*\.\s*$", prefix) else (
                "qualified" if re.search(r"\.\s*$", prefix) else "implicit_or_decl")
            found.append([str(path.relative_to(module)), line, role,
                          text.splitlines()[line - 1].strip()])
    return found


def colored_inverse(module, manifest):
    """Explicitly compose only the pinned sibling's intersecting full-file inverse."""
    composition = json.loads((HERE / "composition-colored.json").read_text())
    for path, want in composition["pins"].items():
        assert hashlib.sha256((module / path).read_bytes()).hexdigest() == want, path
    spec = importlib.util.spec_from_file_location(
        "press_cache_remaining_colored", module / composition["implementation"])
    checker = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(checker)
    intersect = sorted(set(checker.FILES) & set(manifest["source_sha256"]))
    assert intersect == composition["files"], intersect

    def inverse(path, text):
        if path not in intersect:
            return text
        return checker.reverse_source(path, text)

    for path in intersect:
        text = (module / path).read_bytes().decode()
        restored = inverse(path, text)
        assert hashlib.sha256(restored.encode()).hexdigest() == manifest["source_sha256"][path]
        try:
            inverse(path, text + "\n// unrelated mutation\n")
        except AssertionError:
            pass
        else:
            raise AssertionError("composed inverse accepted unrelated edit: " + path)
    print("PASS pinned remaining-colored full-file inverse for exactly " + ", ".join(intersect))
    return inverse


def replaceability_inverse(module, manifest, evidence=None):
    composition = json.loads((HERE / "composition-replaceability.json").read_text())
    evidence = evidence or module / "porting/tests/native-block-replaceability"
    for path, want in composition["pins"].items():
        assert hashlib.sha256((evidence / path).read_bytes()).hexdigest() == want, path
    spec = importlib.util.spec_from_file_location(
        "press_cache_replaceability", evidence / "check_source.py")
    checker = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(checker)
    intersect = sorted(set(checker.data["files"]) & set(manifest["source_sha256"]))
    assert intersect == composition["files"], intersect

    def inverse(path, text):
        if path not in intersect:
            return text
        return checker.reverse(text.encode(), checker.data["files"][path]).decode()

    for path in intersect:
        text = (module / path).read_bytes().decode()
        restored = inverse(path, text)
        assert hashlib.sha256(restored.encode()).hexdigest() == manifest["source_sha256"][path]
        try:
            inverse(path, text + "\n// unrelated mutation\n")
        except AssertionError:
            pass
        else:
            raise AssertionError("composed inverse accepted unrelated edit: " + path)
    print("PASS pinned replaceability full-file inverse for exactly " + ", ".join(intersect))
    return inverse


def cp92_inverse(module, manifest, prepare=lambda path, text: text):
    """Compose only four intersecting, hash-pinned CP92 full-file inverses."""
    composition = json.loads((HERE / "composition-cp92.json").read_text())
    assert hashlib.sha256((HERE / "manifest.json").read_bytes()).hexdigest() == composition["audit_manifest_sha256"]
    for path, want in composition["pins"].items():
        assert hashlib.sha256((module / path).read_bytes()).hexdigest() == want, path

    inverses = {}
    for family in composition["families"]:
        spec = importlib.util.spec_from_file_location(
            "press_cache_cp92_" + family["name"], module / family["implementation"])
        checker = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(checker)
        if family["name"] == "air":
            files = checker.data["files"]
            restore = lambda path, text, checker=checker: checker.reverse(
                text.encode(), checker.data["files"][path]).decode()
        else:
            assert family["name"] == "format"
            files = checker.FILES
            restore = checker.reverse_source
        intersect = sorted(set(files) & set(manifest["source_sha256"]))
        assert intersect == family["files"], intersect
        for path in intersect:
            assert path not in inverses, ("overlapping CP92 inverses", path)
            inverses[path] = restore

    def inverse(path, text):
        return inverses[path](path, text) if path in inverses else text

    rejected = 0
    for path in sorted(inverses):
        text = prepare(path, (module / path).read_bytes().decode())
        restored = inverse(path, text)
        assert hashlib.sha256(restored.encode()).hexdigest() == manifest["source_sha256"][path], path
        for mutant in (text + "\n// unrelated mutation\n", text[:-1],
                       text.replace("\r\n", "\n") if "\r\n" in text else text.replace("\n", "\r\n")):
            assert mutant != text
            try:
                inverse(path, mutant)
            except AssertionError:
                rejected += 1
            else:
                raise AssertionError("CP92 composed inverse accepted unrelated edit: " + path)
    assert len(inverses) == 4 and rejected == 12
    assert inverse("unrelated.java", "\ufeffunchanged\r\n") == "\ufeffunchanged\r\n"
    print("PASS pinned CP92 full-file inverses for exactly four audited files; 12 rejected mutations")
    return inverse


def cp93_inverse(module, manifest):
    composition = json.loads((HERE / "composition-cp93.json").read_text())
    for path, want in composition["pins"].items():
        assert hashlib.sha256((module / path).read_bytes()).hexdigest() == want, path
    spec = importlib.util.spec_from_file_location(
        "press_cache_cp93", module / "porting/tests/check-native-normal-world-air-source.py")
    checker = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(checker)
    intersect = sorted(set(checker.FILES) & set(manifest["source_sha256"]))
    assert intersect == composition["files"] and len(intersect) == 6, intersect
    def inverse(path, text):
        return checker.reverse_source(path, text) if path in intersect else text
    rejected = 0
    for path in intersect:
        text = (module / path).read_bytes().decode()
        inverse(path, text)  # The sibling checks complete CP93 and CP92 hashes.
        for mutant in (text + "\n// unrelated mutation\n", text[:-1],
                       text.replace("\r\n", "\n") if "\r\n" in text else text.replace("\n", "\r\n")):
            assert mutant != text
            try:
                inverse(path, mutant)
            except AssertionError:
                rejected += 1
            else:
                raise AssertionError("CP93 composed inverse accepted unrelated edit: " + path)
    assert rejected == 18
    print("PASS pinned CP93 full-file inverse for exactly six audit inputs; 18 rejected mutations")
    return inverse


def verify(module, compose_colored=False, compose_replaceability=False, replaceability_evidence=None,
           compose_cp92=False, compose_cp93=False):
    manifest = json.loads((HERE / "manifest.json").read_text())
    color = colored_inverse(module, manifest) if compose_colored else lambda path, text: text
    query = replaceability_inverse(module, manifest, replaceability_evidence) if compose_replaceability else lambda path, text: text
    cp93 = cp93_inverse(module, manifest) if compose_cp93 else lambda path, text: text
    cp92 = cp92_inverse(module, manifest, cp93) if compose_cp92 else lambda path, text: text
    normalize = lambda path, text: query(path, color(path, cp92(path, cp93(path, text))))
    actual = inventory(module, normalize)
    assert actual == manifest["active_occurrences"], "getter source inventory changed"
    for path, want in manifest["source_sha256"].items():
        data = normalize(path, (module / path).read_bytes().decode()).encode()
        assert hashlib.sha256(data).hexdigest() == want, path
    for path, want in manifest["artifact_sha256"].items():
        assert hashlib.sha256((HERE / path).read_bytes()).hexdigest() == want, path
    assert len(actual) == 518
    print("PASS 518 active getter occurrences and pinned receiver/side/packet audit inputs; diagnostic boundary remains explicit")


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--module", type=Path, default=HERE.parents[3])
    parser.add_argument("--compose-colored", action="store_true")
    parser.add_argument("--compose-replaceability", action="store_true")
    parser.add_argument("--replaceability-evidence", type=Path)
    parser.add_argument("--compose-cp92", action="store_true")
    parser.add_argument("--compose-cp93", action="store_true")
    args = parser.parse_args()
    verify(args.module, args.compose_colored, args.compose_replaceability,
           args.replaceability_evidence, args.compose_cp92, args.compose_cp93)

#!/usr/bin/env python3
"""Offline, finite provenance and sample-definition check. Python standard library only.

No network, OGG reads, audio decoding, client bootstrap, or production writes occur.
Run from any directory. Optional --report writes the full machine-readable receipt.
"""

import argparse
from collections import Counter
import hashlib
import json
from pathlib import Path
import re
import sys
from urllib.parse import urlparse


HERE = Path(__file__).resolve().parent
PROJECT = HERE.parents[3]
BASE = "5e6ba284ef32b46ad5f81cd1e1570c08b53cb24f"
PINS = {
    "legacy-version.json": "ed5d8789ed29872ea2ef1c348302b0c55e3f3468",
    "native-version.json": "832d95b9f40699d4961394dcf6cf549e65f15dc5",
    "legacy-index.json": "1863782e33ce7b584fc45b037325a1964e095d3e",
    "native-index.json": "a21e1ded1a24ea1548dd8db0cf30b6acb02655a9",
    "legacy-sounds.json": "fce0bb3e14ade7b292a7c1d5fa785533d8106d82",
    "native-sounds.json": "975a99ed9870f51bbae348533d775d730e3fee18",
    "version-manifest.json": "20c7d2dd973dcc13fa7c98548214b113eb83a88a",
}
BYTECODE_PINS = {
    "native-SoundEvents": "29fac8405e64009a43013914897c6b8c3ce16ce1827be04c92a2f54840923dd3",
    "native-SoundListSerializer": "ed64a598b8af8a2d30b4c6aed69812efe5823d3f3107c885bdbee92dfe124e9d",
    "legacy-SoundListSerializer": "1c7678228f03fc1b8811e32120513606d8ee3eb058f44123ab64977e4fee9529",
    "legacy-SoundEntry": "2da7e1555666b01742408c1a4ba16570b2cd29070199efc5171f8822ad9fa451",
    "legacy-SoundEntryType": "7f07f1bc45e14121b44cf430debc3023dbb7749e96d51fb465d044549784ae04",
}
# Keep the bounded acceptance surface independent of the editable parent manifest.
EXPECTED = [
    ("ambient.weather.thunder", "ENTITY_LIGHTNING_THUNDER", "entity.lightning.thunder", "WEATHER", 3),
    ("random.explode", "ENTITY_GENERIC_EXPLODE", "entity.generic.explode", "BLOCKS", 4),
    ("random.fizz", "BLOCK_FIRE_EXTINGUISH", "block.fire.extinguish", "BLOCKS", 1),
    ("random.click", "BLOCK_LEVER_CLICK", "block.lever.click", "BLOCKS", 1),
    ("fire.fire", "BLOCK_FIRE_AMBIENT", "block.fire.ambient", "BLOCKS", 1),
    ("random.break", "ENTITY_ITEM_BREAK", "entity.item.break", "PLAYERS", 1),
    ("mob.zombie.woodbreak", "ENTITY_ZOMBIE_BREAK_DOOR_WOOD", "entity.zombie.break_door_wood", "HOSTILE", 1),
    ("mob.endermen.portal", "ENTITY_ENDERMEN_TELEPORT", "entity.endermen.teleport", "HOSTILE", 2),
    ("fireworks.blast", "ENTITY_FIREWORK_BLAST", "entity.firework.blast", "AMBIENT", 1),
    ("fire.ignite", "ITEM_FLINTANDSTEEL_USE", "item.flintandsteel.use", "BLOCKS", 1),
    ("tile.piston.out", "BLOCK_PISTON_EXTEND", "block.piston.extend", "BLOCKS", 1),
    ("tile.piston.in", "BLOCK_PISTON_CONTRACT", "block.piston.contract", "BLOCKS", 1),
    ("minecart.base", "ENTITY_MINECART_RIDING", "entity.minecart.riding", "NEUTRAL", 1),
    ("game.neutral.swim.splash", "ENTITY_GENERIC_SPLASH", "entity.generic.splash", "NEUTRAL", 2),
    ("random.orb", "ENTITY_EXPERIENCE_ORB_PICKUP", "entity.experience_orb.pickup", "PLAYERS", 1),
]
CATEGORIES = {
    "weather": "WEATHER", "block": "BLOCKS", "player": "PLAYERS",
    "hostile": "HOSTILE", "ambient": "AMBIENT", "neutral": "NEUTRAL",
}
DEFAULTS = {"type": "file", "volume": 1.0, "pitch": 1.0, "weight": 1, "stream": False}


def require(condition, message):
    if not condition:
        raise ValueError(message)


def no_duplicates(pairs):
    value = {}
    for key, item in pairs:
        require(key not in value, "Duplicate JSON member: " + key)
        value[key] = item
    return value


def read_json(path):
    return json.loads(Path(path).read_bytes(), object_pairs_hook=no_duplicates)


def digests(data):
    return {"bytes": len(data), "sha1": hashlib.sha1(data).hexdigest(),
            "sha256": hashlib.sha256(data).hexdigest()}


def resource_url(sha1):
    return "https://resources.download.minecraft.net/" + sha1[:2] + "/" + sha1


def check_url_hash(url, sha1):
    parsed = urlparse(url)
    require(parsed.scheme == "https", "Not an HTTPS provenance URL: " + url)
    if parsed.hostname in ("piston-meta.mojang.com", "launchermeta.mojang.com"):
        require(parsed.path.split("/")[-2] == sha1, "Version/index URL hash mismatch: " + url)
        require(parsed.path.startswith("/v1/packages/"), "Unexpected metadata URL: " + url)
    else:
        require(url == resource_url(sha1), "Asset URL hash mismatch: " + url)


def sample_rows(event, index):
    rows = []
    for sample in event["sounds"]:
        # All 22 scoped source slots are bare file strings. Reject implicit widening
        # to nested events or object-level behavior rather than approximating it.
        require(isinstance(sample, str), "Scoped sample is not a bare file string")
        require(":" not in sample and ".." not in sample, "Unexpected sample namespace/path")
        asset_name = "minecraft/sounds/" + sample + ".ogg"
        asset = index["objects"][asset_name]
        require(re.fullmatch(r"[0-9a-f]{40}", asset["hash"]) is not None, "Invalid asset hash")
        require(type(asset["size"]) is int and asset["size"] > 0, "Invalid asset size")
        rows.append({"name": sample, "asset_key": asset_name, "sha1": asset["hash"],
                     "bytes": asset["size"], "url": resource_url(asset["hash"]), **DEFAULTS})
    return rows


def effective_multiset(rows):
    return Counter((r["sha1"], r["bytes"], r["type"], r["volume"], r["pitch"],
                    r["weight"], r["stream"]) for r in rows)


def check_bytecode(evidence):
    receipt = read_json(evidence / "local-evidence.json")
    texts = {}
    for label, expected_sha256 in BYTECODE_PINS.items():
        entry = receipt["runtime_bytecode"][label]
        require(entry["javap_file"] == label + ".javap.txt", "Unexpected bytecode evidence path")
        data = (evidence / entry["javap_file"]).read_bytes()
        require(digests(data) == entry["javap_hashes"], "Bytecode evidence receipt mismatch: " + label)
        require(hashlib.sha256(data).hexdigest() == expected_sha256, "Bytecode evidence changed: " + label)
        texts[label] = data.decode("utf-8")
    native = texts["native-SoundListSerializer"]
    # Verify the actual constructor operands for bare strings: volume/pitch 1,
    # weight 1, FILE, stream false. The archived -c listing is generated from the
    # existing deobfuscated 1.12.2 build classpath, not a mock serializer.
    native_defaults = (
        r"fconst_1\s+\d+: fconst_1\s+\d+: iconst_1\s+"
        r"\d+: getstatic[^\n]*Sound\$Type.FILE:[^\n]*\n\s*"
        r"\d+: iconst_0\s+\d+: invokespecial[^\n]*"
        r"Sound\.\"<init>\":\(Ljava/lang/String;FFILnet/minecraft/client/audio/Sound\$Type;Z\)V"
    )
    require(re.search(native_defaults, native) is not None, "Native string defaults not established")
    constructor = texts["legacy-SoundEntry"].split("public bsq();", 1)[1].split("public java.lang.String", 1)[0]
    for opcode, field in (("fconst_1", "b:F"), ("fconst_1", "c:F"),
                          ("iconst_1", "d:I"), ("iconst_0", "f:Z")):
        require(re.search(opcode + r"\s+\d+: putfield[^\n]*// Field " + re.escape(field), constructor),
                "Legacy constructor default missing: " + field)
    require("Field bsr.a:Lbsr;" in constructor, "Legacy default file type missing")
    require(re.search(r"// String file\n\s*\d+: invokespecial[^\n]*\n\s*\d+: putstatic[^\n]*// Field a:Lbsr;",
                      texts["legacy-SoundEntryType"]), "Legacy enum FILE mapping missing")
    legacy_serializer = texts["legacy-SoundListSerializer"]
    require(re.search(r"new[^\n]*// class bsq\n[^\n]*\n\s*\d+: invokespecial[^\n]*bsq\.\"<init>\":\(\)V",
                      legacy_serializer), "Legacy serializer constructor path missing")
    require("// Method bsq.a:(Ljava/lang/String;)V" in legacy_serializer,
            "Legacy serializer string-name assignment missing")
    return texts["native-SoundEvents"], receipt


def validate(evidence, manifest_path, sounds_path):
    evidence = Path(evidence)
    files = {}
    for name, expected_sha1 in PINS.items():
        data = (evidence / name).read_bytes()
        actual = digests(data)
        require(actual["sha1"] == expected_sha1, "Pinned official/snapshot bytes changed: " + name)
        files[name] = actual
    downloads = read_json(evidence / "downloads.json")
    require(set(downloads) == {"legacy-version.json", "legacy-index.json", "native-index.json",
                              "legacy-sounds.json", "native-sounds.json"}, "Fetched file set changed")
    for name, fetched in downloads.items():
        require(all(files[name][k] == fetched[k] for k in ("bytes", "sha1", "sha256")),
                "Fetched bytes/hash receipt mismatch: " + name)
        check_url_hash(fetched["url"], files[name]["sha1"])
        files[name]["url"] = fetched["url"]
        files[name]["origin"] = "Previously fetched official bytes, copied unchanged"

    registry, local = check_bytecode(evidence)
    recovery = local["native_version_recovery"]
    require(digests((evidence / "native-version.json").read_bytes()) == recovery["recovered_file"],
            "Native version recovery receipt changed")
    require(digests((evidence / "native-version.json").read_bytes() + b"\n") == recovery["cached_file"],
            "Native metadata is not exactly the documented one-LF recovery")
    files["native-version.json"].update(url=recovery["official_url"],
        origin="Existing cache with exactly one trailing LF removed; resulting bytes match official URL SHA-1")
    check_url_hash(files["native-version.json"]["url"], PINS["native-version.json"])
    version_manifest = read_json(evidence / "version-manifest.json")
    version_links = {v["id"]: v for v in version_manifest["versions"] if v["id"] in ("1.7.10", "1.12.2")}
    require(set(version_links) == {"1.7.10", "1.12.2"}, "Version manifest links missing")
    definitions = {}
    indexes = {}
    for label, version_id, asset_id in (("legacy", "1.7.10", "1.7.10"), ("native", "1.12.2", "1.12")):
        version_name = label + "-version.json"
        version = read_json(evidence / version_name)
        require(version["id"] == version_id, "Wrong Minecraft version")
        require(version_links[version_id]["url"] == files[version_name]["url"], "Launcher version URL mismatch")
        check_url_hash(version_links[version_id]["url"], files[version_name]["sha1"])
        index_name = label + "-index.json"
        index_link = version["assetIndex"]
        require(index_link["id"] == asset_id and version["assets"] == asset_id, "Wrong asset version")
        require(index_link["sha1"] == files[index_name]["sha1"], "Version to index SHA-1 mismatch")
        require(index_link["size"] == files[index_name]["bytes"], "Version to index size mismatch")
        require(index_link["url"] == files[index_name]["url"], "Version to index URL mismatch")
        index = read_json(evidence / index_name)
        definition_name = label + "-sounds.json"
        definition_link = index["objects"]["minecraft/sounds.json"]
        require(definition_link["hash"] == files[definition_name]["sha1"], "Index to sounds SHA-1 mismatch")
        require(definition_link["size"] == files[definition_name]["bytes"], "Index to sounds size mismatch")
        require(resource_url(definition_link["hash"]) == files[definition_name]["url"], "Index to sounds URL mismatch")
        definitions[label] = read_json(evidence / definition_name)
        indexes[label] = index

    manifest = read_json(manifest_path)
    require(manifest["base"] == BASE, "Unexpected parent manifest base")
    expected_aliases = [{"legacy": old, "field": field, "native": native, "category": category}
                        for old, field, native, category, count in EXPECTED]
    require(manifest["aliases"] == expected_aliases, "Parent manifest aliases/mapping/order changed")
    production = read_json(sounds_path)
    require(set(production) == {a[0] for a in EXPECTED}, "Production alias namespace is not exactly the scoped 15 names")
    aliases = []
    for old, field, native, category, count in EXPECTED:
        legacy_event = definitions["legacy"][old]
        require(old not in definitions["native"], "Legacy name is already defined natively: " + old)
        require(native in definitions["native"], "Selected native event is absent: " + native)
        native_event = definitions["native"][native]
        require(production[old] == legacy_event, "Production differs from exact original event definition: " + old)
        require(CATEGORIES[legacy_event["category"]] == category, "Original category mismatch: " + old)
        require(not legacy_event.get("replace", False) and not native_event.get("replace", False),
                "Unexpected replace semantics: " + old)
        require(legacy_event["sounds"] == native_event["sounds"], "Native sample order/definition differs: " + old)
        registry_pattern = (r"// String " + re.escape(native) + r"\n\s*\d+: invokestatic[^\n]*"
                            r"getRegisteredSoundEvent:[^\n]*\n\s*\d+: putstatic[^\n]*// Field "
                            + re.escape(field) + r":Lnet/minecraft/util/SoundEvent;")
        require(re.search(registry_pattern, registry), "Native SoundEvents field points elsewhere: " + field)
        legacy_rows = sample_rows(legacy_event, indexes["legacy"])
        native_rows = sample_rows(native_event, indexes["native"])
        require(len(legacy_rows) == len(native_rows) == count, "Unexpected sample count: " + old)
        require(effective_multiset(legacy_rows) == effective_multiset(native_rows),
                "Native audio-asset/default multiset differs: " + old)
        require(legacy_rows == native_rows, "Ordered native audio-asset/default rows differ: " + old)
        aliases.append({"legacy": old, "native": native, "field": field,
                        "legacy_category": legacy_event["category"], "native_category": category,
                        "sample_count": count, "legacy_name_absent_natively": True,
                        "production_exact_original_definition": True,
                        "native_subtitle_not_copied": native_event.get("subtitle"),
                        "samples": legacy_rows})
    sample_count = sum(a["sample_count"] for a in aliases)
    unique = {s["sha1"] for a in aliases for s in a["samples"]}
    require(len(aliases) == 15 and sample_count == 22 and len(unique) == 22, "Scoped totals changed")
    legacy_client = read_json(evidence / "legacy-version.json")["downloads"]["client"]
    for key in ("legacy-SoundEntry", "legacy-SoundEntryType", "legacy-SoundListSerializer"):
        jar = local["runtime_bytecode"][key]["source_jar_hashes"]
        require(jar["sha1"] == legacy_client["sha1"] and jar["bytes"] == legacy_client["size"],
                "Legacy bytecode source is not the official pinned client")
    return {"result": "PASS", "base": BASE, "aliases_checked": 15, "sample_slots_checked": sample_count,
            "unique_sample_asset_hashes": len(unique), "effective_defaults_per_sample": DEFAULTS,
            "definition_and_index_files": files, "version_manifest_entries": version_links,
            "production_sounds_sha256": digests(Path(sounds_path).read_bytes())["sha256"],
            "aliases": aliases,
            "scope": "Offline JSON/index/archived-bytecode evidence only. No OGG downloaded, opened, decoded, or played. Asset identities are proven by equality of official index SHA-1 and size records, not by inspecting audio payloads. Native subtitle additions are documented, not copied into legacy aliases. Runtime dispatch, mix/category behavior, and sound-engine execution are outside this check."}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--evidence", type=Path, default=HERE)
    parser.add_argument("--manifest", type=Path, default=HERE.parent / "manifest.json")
    parser.add_argument("--sounds", type=Path, default=PROJECT / "src/main/resources/assets/minecraft/sounds.json")
    parser.add_argument("--report", type=Path)
    args = parser.parse_args()
    try:
        report = validate(args.evidence, args.manifest, args.sounds)
    except (ValueError, KeyError, TypeError, IndexError, OSError) as exc:
        print("FAIL: " + str(exc), file=sys.stderr)
        return 1
    if args.report:
        args.report.write_text(json.dumps(report, indent=2) + "\n")
    print("PASS: 15 exact original aliases; 15 native registry mappings; 22 sample slots / 22 unique official asset hashes")
    print("PASS: both official version -> index -> sounds chains; volume=1 pitch=1 weight=1 stream=false type=file")
    for alias in report["aliases"]:
        print("  {legacy} -> {native}: {sample_count} samples; {legacy_category} -> {native_category}".format(**alias))
    print("Scope: no OGG payloads or audio/client execution; see --report JSON for all sample URLs and hashes.")
    return 0


if __name__ == "__main__":
    sys.exit(main())

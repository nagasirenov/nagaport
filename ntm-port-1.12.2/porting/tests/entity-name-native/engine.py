#!/usr/bin/env python3
"""Read-only, binary-pinned name/dispatch audit. Does not compile or run Minecraft.

Run from any directory:
  python3 engine.py
  python3 engine.py --dump

Paths can be overridden by the matching command-line options. The native archive
is resolved from the supplied classpath, then inspected by itself. No dependency
classes, production sources, network resources, or test doubles are executed.
The committed manifest is a reviewed baseline, never updated by this script.
"""

import argparse
import hashlib
import json
import pathlib
import re
import subprocess
import sys
import zipfile

HERE = pathlib.Path(__file__).resolve().parent

# Each method is selected by JVM name and descriptor. None means the class must
# not declare that method; inherited resolution is documented in the evidence.
QUERIES = [
    ("legacy", "sa", "b_", "()Ljava/lang/String;", True),
    ("legacy", "sa", "c_", "()Lfj;", True),
    ("legacy", "sv", "b_", "()Ljava/lang/String;", False),
    ("legacy", "sw", "b_", "()Ljava/lang/String;", True),
    ("legacy", "sw", "bG", "()Ljava/lang/String;", True),
    ("legacy", "sw", "bH", "()Z", True),
    ("legacy", "yz", "b_", "()Ljava/lang/String;", True),
    ("legacy", "xl", "b_", "()Ljava/lang/String;", True),
    ("legacy", "xl", "a", "(Ljava/lang/String;)V", True),
    ("legacy", "xk", "b_", "()Ljava/lang/String;", True),
    ("legacy", "wi", "b_", "()Ljava/lang/String;", True),
    ("legacy", "wn", "b_", "()Ljava/lang/String;", True),
    ("legacy", "tg", "b_", "()Ljava/lang/String;", False),
    ("legacy", "wf", "b_", "()Ljava/lang/String;", False),
    ("legacy", "rx", "b_", "()Ljava/lang/String;", False),
    ("legacy", "td", "b_", "()Ljava/lang/String;", False),
    ("legacy", "ac", "b_", "()Ljava/lang/String;", True),
    ("legacy", "agp", "b_", "()Ljava/lang/String;", True),
    ("legacy", "net.minecraft.server.MinecraftServer", "b_", "()Ljava/lang/String;", True),
    ("legacy", "ov", "b_", "()Ljava/lang/String;", True),
    ("native", "net.minecraft.entity.Entity", "getName", "()Ljava/lang/String;", True),
    ("native", "net.minecraft.entity.Entity", "hasCustomName", "()Z", True),
    ("native", "net.minecraft.entity.Entity", "getCustomNameTag", "()Ljava/lang/String;", True),
    ("native", "net.minecraft.entity.Entity", "getDisplayName",
     "()Lnet/minecraft/util/text/ITextComponent;", True),
    ("native", "net.minecraft.entity.EntityLivingBase", "getName", "()Ljava/lang/String;", False),
    ("native", "net.minecraft.entity.EntityLiving", "getName", "()Ljava/lang/String;", False),
    ("native", "net.minecraft.entity.player.EntityPlayer", "getName", "()Ljava/lang/String;", True),
    ("native", "net.minecraft.entity.item.EntityMinecart", "getName", "()Ljava/lang/String;", False),
    ("native", "net.minecraft.entity.item.EntityItem", "getName", "()Ljava/lang/String;", True),
    ("native", "net.minecraft.entity.passive.EntityHorse", "getName", "()Ljava/lang/String;", False),
    ("native", "net.minecraft.entity.passive.AbstractHorse", "getName", "()Ljava/lang/String;", False),
    ("native", "net.minecraft.entity.passive.EntityOcelot", "getName", "()Ljava/lang/String;", True),
    ("native", "net.minecraft.entity.passive.EntityTameable", "getName", "()Ljava/lang/String;", False),
    ("native", "net.minecraft.entity.passive.EntityAnimal", "getName", "()Ljava/lang/String;", False),
    ("native", "net.minecraft.entity.EntityAgeable", "getName", "()Ljava/lang/String;", False),
    ("native", "net.minecraft.entity.EntityCreature", "getName", "()Ljava/lang/String;", False),
    ("native", "net.minecraft.command.ICommandSender", "getName", "()Ljava/lang/String;", True),
    ("native", "net.minecraft.tileentity.CommandBlockBaseLogic", "getName", "()Ljava/lang/String;", True),
    ("native", "net.minecraft.server.MinecraftServer", "getName", "()Ljava/lang/String;", True),
    ("native", "net.minecraft.network.rcon.RConConsoleSource", "getName", "()Ljava/lang/String;", True),
    ("native", "net.minecraft.command.CommandSenderWrapper", "getName", "()Ljava/lang/String;", True),
]

FINDINGS = """SEMANTIC FINDINGS (high confidence, directly supported by the bytecode below)

1. Both name contracts return raw java.lang.String, not a text component.
   Legacy func_70005_c_ is getCommandSenderName in the 1.7 source contract;
   this native archive declares getName with the same ()Ljava/lang/String;
   descriptor. Do not substitute getDisplayName, unformatted display text,
   translated chat components, a profile lookup helper, or toString.

2. EntityPlayer is an exact implementation match: each loads its GameProfile
   field, invokes GameProfile.getName(), and returns that raw String.
   A virtual getName call retains native overriding dispatch; a direct profile
   lookup would bypass it. Statically player-typed receivers and an existing
   instanceof EntityPlayer guard are a defensible bounded migration.

3. Legacy Entity localizes entity.<EntityList name or generic>.name without
   checking a custom name. Native Entity first virtually calls hasCustomName()
   and getCustomNameTag(), then takes the same registry-key fallback.
   Native base custom names use Entity's CUSTOM_NAME data-manager entry and count
   as custom only when the stored String is nonempty.

4. Legacy EntityLiving adds the custom-name branch itself; its watcher-slot-10
   predicate checks length > 0. Native EntityLiving/EntityLivingBase declare no
   getName and inherit that branch from Entity. This is equivalent for ordinary
   native living implementations, not a proof for all EntityLivingBase
   subclasses or arbitrary mod overrides.

5. Legacy EntityMinecart returns its private String b whenever b != null,
   including the empty String. Its setter stores its argument without filtering.
   Native EntityMinecart declares no getName and inherits Entity's nonempty
   custom-name channel. The two local custom cart classes retain another
   entityName field and a null-only ternary. Renaming that override while using
   super.getName changes its null fallback: a native nonempty custom tag is now
   observed. Keeping the own-field empty-string branch alone does not prove the
   entire override equivalent. Park those overrides absent additional proof.

6. Legacy EntityItem always translates item.<ItemStack unlocalized name>.
   Native EntityItem first honors an entity custom name, then translates
   item.<ItemStack translation key>. It does not return ItemStack display text.
   A generic entity receiver can hit this new branch.

7. Legacy EntityHorse explicitly chooses horse/donkey/mule/skeletonhorse/
   zombiehorse translation keys after its custom-name check. Native EntityHorse
   and AbstractHorse do not declare getName; horse kinds are separate native
   classes. This is not a blanket implementation-identity proof.
   EntityOcelot matches the custom-name / tamed Cat / superclass structure.

8. Both ICommandSender interfaces declare the raw String method. Known
   non-entity implementations match: MinecraftServer returns "Server";
   RConConsoleSource returns "Rcon"; command-block logic returns its stored
   custom-name String. However a generic sender can be an Entity, and native
   CommandSenderWrapper dispatches either to Entity.getName or the delegate's
   ICommandSender.getName. Exclude generic ICommandSender sites from this
   narrow proof rather than inferring safety from the three concrete examples.

9. Generic Entity call sites must not be mechanically renamed under a strict
   legacy-output requirement. The current source also has native getName on
   EntityRailCarCargo for inventory naming, which creates another possible
   override target. A type-switch helper could bypass mod virtual overrides
   and is not justified by this audit.

10. Safest bounded production change: migrate player-proven calls and rename
    EntityDummy's health/max-health override with its body unchanged. Retain
    virtual calls and exact string operations; do not broaden identity tests,
    normalize case, trim, translate, or add custom-name precedence. Keep generic
    entity/sender sites and custom-cart fallbacks outside this bounded change.

SCOPE AND LIMITS
This is an implementation/signature/dispatch proof, not an in-game test.
The original client jar is Mojang vanilla, while the native archive is the
project's Forge binary. Missing third-party overrides, translation resources,
registry contents, and runtime state are not reconstructed or assumed equal.
The source-context observations in findings 5, 9 and 10 are advisory scope
boundaries; production-source verification belongs to the companion source
tests. This script verifies only the pinned binaries and mapping inputs.
"""


def sha256(data):
    return hashlib.sha256(data).hexdigest()


def file_digest(path):
    digest = hashlib.sha256()
    with open(path, "rb") as stream:
        for block in iter(lambda: stream.read(1024 * 1024), b""):
            digest.update(block)
    return digest.hexdigest()


def find_native_archive(classpath_file):
    target = "net/minecraft/entity/Entity.class"
    entries = pathlib.Path(classpath_file).read_text().strip().split(":")
    for entry in entries:
        path = pathlib.Path(entry)
        if path.is_dir() and (path / target).exists():
            raise AssertionError("Unpinned directory shadows native Entity: " + entry)
        if path.is_file() and zipfile.is_zipfile(path):
            with zipfile.ZipFile(path) as archive:
                if target in archive.namelist():
                    return path
    raise AssertionError("Native Entity archive not found in supplied classpath")


def parse_methods(output):
    lines = output.splitlines()
    starts = [
        i for i, line in enumerate(lines)
        if re.match(r"^  \S.*;\s*$", line) and "(" in line
    ]
    result = {}
    for index, start in enumerate(starts):
        end = starts[index + 1] if index + 1 < len(starts) else len(lines)
        signature = lines[start].strip()
        method = signature.split("(", 1)[0].split()[-1]
        descriptor = lines[start + 1].strip()
        if not descriptor.startswith("descriptor: "):
            raise AssertionError("Missing javap descriptor for " + signature)
        descriptor = descriptor[len("descriptor: "):]
        body = lines[start:end]
        while body and (not body[-1].strip() or body[-1] == "}"):
            body.pop()
        result[(method, descriptor)] = "\n".join(body) + "\n"
    return result


def collect(args):
    paths = {
        "legacy_jar": pathlib.Path(args.legacy_jar).resolve(),
        "srg_zip": pathlib.Path(args.srg_zip).resolve(),
        "native_jar": find_native_archive(args.native_classpath_file).resolve(),
        "javap": pathlib.Path(args.javap).resolve(),
    }
    inputs = {
        name: {"path": str(path), "sha256": file_digest(path)}
        for name, path in paths.items()
    }
    with zipfile.ZipFile(paths["srg_zip"]) as archive:
        mapping = archive.read("joined.srg").decode("utf-8")
    legacy_classes = {
        class_name.replace(".", "/")
        for origin, class_name, _, _, _ in QUERIES if origin == "legacy"
    }
    mappings = [
        line for line in mapping.splitlines()
        if (line.startswith("CL: ") and line.split()[1] in legacy_classes)
        or (line.startswith("MD: ") and (
            "/func_70005_c_ " in line
            or any(token in line for token in (
                "MD: sa/c_ ", "MD: sw/bG ", "MD: sw/bH ", "MD: xl/a (Ljava/lang/String;)",
                "MD: dd/a (Ljava/lang/String;)", "MD: sg/b (Lsa;)", "MD: add/a ()",
            ))
        ))
    ]
    classes = {}
    methods = {}
    method_bodies = {}
    cache = {}
    for origin, class_name, method, descriptor, present in QUERIES:
        archive_path = paths[origin + "_jar"]
        class_key = origin + ":" + class_name
        if class_key not in cache:
            with zipfile.ZipFile(archive_path) as archive:
                class_bytes = archive.read(class_name.replace(".", "/") + ".class")
            output = subprocess.check_output(
                [str(paths["javap"]), "-classpath", str(archive_path),
                 "-p", "-c", "-s", class_name],
                text=True, stderr=subprocess.STDOUT,
            )
            header = next(line for line in output.splitlines() if line.endswith("{"))
            classes[class_key] = {"sha256": sha256(class_bytes), "declaration": header}
            cache[class_key] = parse_methods(output)
        body = cache[class_key].get((method, descriptor))
        if present != (body is not None):
            raise AssertionError("Unexpected declaration presence: " +
                                 class_key + "." + method + descriptor)
        key = class_key + "." + method + descriptor
        methods[key] = {
            "declared": present,
            "sha256": sha256(body.encode("utf-8")) if body is not None else None,
        }
        method_bodies[key] = body
    snapshot = {
        "schema": 1,
        "inputs": inputs,
        "joined_srg_sha256": sha256(mapping.encode("utf-8")),
        "mapping_lines": mappings,
        "classes": classes,
        "methods": methods,
    }
    return snapshot, method_bodies


def render_evidence(snapshot, method_bodies):
    parts = [
        "Entity raw-name engine audit\n",
        "Reproduce with: python3 porting/tests/entity-name-native/engine.py --dump\n",
        "No Minecraft execution, compilation, download, or production edit occurs.\n\n",
        FINDINGS,
        "\nBINARY INPUTS (SHA-256)\n",
    ]
    for name, data in snapshot["inputs"].items():
        parts.append(name + ": " + data["sha256"] + "\n  " + data["path"] + "\n")
    parts.append("joined.srg entry: " + snapshot["joined_srg_sha256"] + "\n")
    parts.append("\nEXACT ORIGINAL MAPPING LINES\n")
    parts.extend(line + "\n" for line in snapshot["mapping_lines"])
    parts.append("\nEXACT DECLARED SIGNATURES AND BYTECODE (javap -p -c -s)\n")
    last_class = None
    for key, body in method_bodies.items():
        class_key = key.rsplit(".", 1)[0]
        if class_key != last_class:
            data = snapshot["classes"][class_key]
            parts.append("\n" + data["declaration"] + "\n")
            parts.append("class SHA-256: " + data["sha256"] + "\n")
            last_class = class_key
        parts.append("\n" + key + "\n")
        parts.append(body if body is not None else "  NOT DECLARED: resolves through inheritance.\n")
    return "".join(parts)


def arguments():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--legacy-jar", default="/tmp/nagaport-legacy-client.jar")
    parser.add_argument("--srg-zip", default="/tmp/nagaport1710-srg.zip")
    parser.add_argument("--native-classpath-file", default="/tmp/nagaport-ae2-native-classpath.txt")
    parser.add_argument("--javap", default="/workspace/shared/nagaport-toolchain/java8/bin/javap")
    parser.add_argument("--dump", action="store_true", help="Print the full verified evidence")
    return parser.parse_args()


def main():
    args = arguments()
    baseline = json.loads((HERE / "binary-inputs.json").read_text())
    snapshot, bodies = collect(args)
    # Paths are informational; relocated copies with exactly the same content
    # remain valid. Every other bytecode/mapping fingerprint is required.
    expected = json.loads(json.dumps(baseline))
    observed = json.loads(json.dumps(snapshot))
    for data in (expected, observed):
        for item in data["inputs"].values():
            item.pop("path")
    if observed != expected:
        raise AssertionError("Pinned binary/mapping/signature evidence changed; review required")
    evidence = render_evidence(snapshot, bodies)
    # Relocation may change display paths, so compare committed text using the
    # baseline's informational paths while retaining all observed method bodies.
    canonical = dict(snapshot)
    canonical["inputs"] = baseline["inputs"]
    if render_evidence(canonical, bodies) != (HERE / "engine-evidence.txt").read_text():
        raise AssertionError("Committed engine evidence does not match verified bytecode")
    if args.dump:
        sys.stdout.write(evidence)
    else:
        print("PASS: {} pinned class declarations, {} method presence/bytecode checks, "
              "{} exact mapping lines.".format(
                  len(snapshot["classes"]), len(snapshot["methods"]), len(snapshot["mapping_lines"])))
        print("Player raw-name equivalence proved; generic entity/sender/cart equivalence excluded.")


if __name__ == "__main__":
    main()

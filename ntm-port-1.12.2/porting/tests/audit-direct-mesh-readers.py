#!/usr/bin/env python3
"""Audit the bounded geometry-only reader migration; optionally run a CPU geometry fixture.

This does not compile ObjUtil or RenderMirror, provide Minecraft/Forge stubs, or
claim native block rendering. No Gradle invocation or persistent build directory.
"""
import argparse
import hashlib
import json
import os
from pathlib import Path
import subprocess
import tempfile


ROOT = Path(__file__).resolve().parents[2]
TESTS = Path(__file__).resolve().parent
MANIFEST = TESTS / "direct-mesh-readers-manifest.json"


def sha256(data):
    return hashlib.sha256(data).hexdigest()


def operations(path):
    is_util = path.endswith("/ObjUtil.java")
    anchor = ("import net.minecraft.util.math.Vec3d;\n" if is_util
              else "import net.minecraft.world.IBlockAccess;\n")
    return [
        ("import com.hbm.render.loader.S_Face;\nimport com.hbm.render.loader.S_GroupObject;",
         "import com.hbm.render.mesh.ObjMesh;", 1),
        (anchor + "import net.minecraftforge.client.model.obj.TextureCoordinate;\n"
         + "import net.minecraftforge.client.model.obj.Vertex;\n", anchor, 1),
        ("S_GroupObject", "ObjMesh.Part", 3 if is_util else 2),
        ("S_Face", "ObjMesh.Face", 2 if is_util else 1),
        (".groupObjects", ".getMesh().parts", 2 if is_util else 1),
        ("Vertex n = f.faceNormal;", "ObjMesh.Vec n = f.normal;", 2 if is_util else 1),
        ("f.vertices.length", "f.corners.size()", 3 if is_util else 1),
        ("Vertex v = f.vertices[i];", "ObjMesh.Vec v = f.corners.get(i).position;",
         2 if is_util else 1),
        ("TextureCoordinate t = f.textureCoordinates[i];", "ObjMesh.Vec t = f.corners.get(i).uv;",
         2 if is_util else 1),
        ("t.u * 16", "t.x * 16", 4 if is_util else 2),
        ("t.v * 16", "t.y * 16", 4 if is_util else 2),
    ]


def transform(source, path, reverse=False):
    changes = operations(path)
    if reverse:
        changes = [(new, old, count) for old, new, count in reversed(changes)]
    for old, new, count in changes:
        actual = source.count(old)
        if actual != count:
            raise AssertionError("%s: expected %d occurrences of %r, got %d"
                                 % (path, count, old, actual))
        source = source.replace(old, new)
    return source


def assert_source(baseline, current, path):
    if transform(baseline, path) != current:
        raise AssertionError(path + ": changes exceed the allowed geometry substitution")
    if transform(current, path, reverse=True) != baseline:
        raise AssertionError(path + ": reverse substitution did not reproduce baseline exactly")


def reject_mutations(baseline, current, path):
    mutations = [
        ("normal.rotateAroundZRad(pitch);\n\t\t\t\tnormal.rotateAroundYRad(rot);",
         "normal.rotateAroundYRad(rot);\n\t\t\t\tnormal.rotateAroundZRad(pitch);"),
        ("((float)normal.y + 0.7F) * 0.9F", "((float)normal.y + 0.7F) * 0.8F"),
        ("obj.name.equals(name)", "obj.name.equalsIgnoreCase(name)"),
        ("if(i % 3 == 2)", "if(f.corners.size() == 3 && i % 3 == 2)"),
        ("t.x * 16)", "t.x * 16D)"),
        ("if(shadow || hasColor)", "if(shadow)"),
        ("if(go == null)\n\t\t\treturn;", "if(go == null)\n\t\t\tthrow new IllegalStateException();"),
    ]
    for old, new in mutations:
        if old not in current:
            raise AssertionError("missing mutation target: " + old)
        try:
            assert_source(baseline, current.replace(old, new, 1), path)
        except AssertionError:
            continue
        raise AssertionError("source audit accepted semantic mutation: " + old)
    print("PASS: rejected %d deliberate formula/order/selection/guard/UV/tint/null-return changes"
          % len(mutations))


def run_geometry_fixture(java_home):
    javac = java_home / "bin" / ("javac.exe" if os.name == "nt" else "javac")
    java = java_home / "bin" / ("java.exe" if os.name == "nt" else "java")
    sources = [ROOT / "src/main/java/com/hbm/render/mesh/ObjMesh.java",
               ROOT / "src/main/java/com/hbm/render/mesh/ObjMeshParser.java",
               TESTS / "DirectMeshReaderGeometryProbe.java"]
    for source in sources:
        print("Fixture source SHA256 %s %s" % (sha256(source.read_bytes()), source.relative_to(ROOT)),
              flush=True)
    with tempfile.TemporaryDirectory(prefix="direct-mesh-readers-") as build:
        subprocess.run([str(javac), "-source", "8", "-target", "8", "-encoding", "UTF-8",
                        "-d", build] + [str(source) for source in sources], check=True)
        subprocess.run([str(java), "-cp", build, "DirectMeshReaderGeometryProbe"], check=True)


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--java-home", type=Path,
                        help="also compile actual ObjMesh/ObjMeshParser plus the bounded CPU fixture")
    args = parser.parse_args()
    manifest = json.loads(MANIFEST.read_text(encoding="utf-8"))
    git_root = Path(subprocess.check_output(["git", "rev-parse", "--show-toplevel"], cwd=ROOT,
                                            text=True).strip())
    util = None
    for item in manifest["readers"]:
        path = item["path"]
        source_path = ROOT / path
        repository_path = source_path.relative_to(git_root).as_posix()
        baseline_bytes = subprocess.check_output(
            ["git", "show", manifest["baseline_commit"] + ":" + repository_path], cwd=git_root)
        current_bytes = source_path.read_bytes()
        if sha256(baseline_bytes) != item["baseline_sha256"]:
            raise AssertionError(path + ": pinned baseline fingerprint mismatch")
        if sha256(current_bytes) != item["migrated_sha256"]:
            raise AssertionError(path + ": migrated reader fingerprint mismatch")
        baseline = baseline_bytes.decode("utf-8")
        current = current_bytes.decode("utf-8")
        assert_source(baseline, current, path)
        print("PASS: exact forward/reverse geometry-only source parity: " + path, flush=True)
        if path.endswith("/ObjUtil.java"):
            util = (baseline, current, path)
    if util is None:
        raise AssertionError("manifest is missing ObjUtil")
    reject_mutations(*util)
    if args.java_home:
        run_geometry_fixture(args.java_home.resolve())
    else:
        print("NOT RUN: optional Java CPU fixture (supply --java-home with an existing JDK)")
    print("BOUNDARY: ObjUtil and RenderMirror were not compiled or rendered; legacy body APIs remain.")


if __name__ == "__main__":
    main()

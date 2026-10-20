#!/usr/bin/env python3
"""Exact CP91 inverse plus an independent, statement-level owned-stream audit.

This is finite source evidence, not a Java compiler or a rendered-image test.
The statement audit does not call the production transform or trust its edits:
it checks every changed statement, reconstructs the old methods, and compares
the rest of each complete file byte-for-byte, including same-line statements.
"""
import hashlib
import importlib.util
import json
import pathlib
import re
import subprocess
import sys

sys.dont_write_bytecode = True
HERE = pathlib.Path(__file__).resolve().parent
ROOT = HERE.parents[2]
BASE = "f5ce86cd607379cc9810896cf91d1fee86ce6331"
MANIFEST = json.loads((HERE / "manifest.json").read_text())
FILES = MANIFEST["files"]
spec = importlib.util.spec_from_file_location("remaining_format_transform", HERE / "transform.py")
transform = importlib.util.module_from_spec(spec)
spec.loader.exec_module(transform)
IMPORTS = (
    "import net.minecraft.client.renderer.BufferBuilder;",
    "import net.minecraft.client.renderer.vertex.DefaultVertexFormats;",
)


def digest(text):
    return hashlib.sha256(text.encode()).hexdigest()


def reverse_source(relative, text):
    """Exported exact full-file inverse used by older compatibility evidence."""
    if relative not in FILES:
        return text
    record = FILES[relative]
    assert digest(text) == record["after_sha256"], ("changed native source", relative)
    lines = text.splitlines(True)
    for edit in reversed(record["edits"]):
        start = edit["new_start"]
        assert lines[start:start + len(edit["new"])] == edit["new"], relative
        lines[start:start + len(edit["new"])] = edit["old"]
    original = "".join(lines)
    assert digest(original) == record["before_sha256"], ("changed inverse", relative)
    original_lines = original.splitlines(True)
    for edit in record["edits"]:
        start = edit["old_start"]
        assert original_lines[start:start + len(edit["old"])] == edit["old"], relative
    return original


def scrub(text):
    """Preserve offsets while excluding strings and comments from recognition."""
    return re.sub(
        r'//[^\n]*|/\*.*?\*/|"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'',
        lambda match: "".join("\n" if char == "\n" else " " for char in match[0]),
        text,
        flags=re.S,
    )


def arguments(text):
    """Independent balanced argument reader; do not split nested expressions."""
    stack, parts, start = [], [], 0
    pairs = {")": "(", "]": "[", "}": "{"}
    for index, char in enumerate(scrub(text)):
        if char in "([{":
            stack.append(char)
        elif char in ")]}":
            assert stack and stack.pop() == pairs[char], text
        elif char == "," and not stack:
            parts.append(text[start:index].strip())
            start = index + 1
    assert not stack, text
    if text.strip():
        parts.append(text[start:].strip())
    return parts


def method_span(text, selector):
    method, _, discriminator = selector.partition(":")
    clean = scrub(text)
    pattern = (r"\b(?:public|protected|private)\s+(?:static\s+)?[\w<>\[\]]+\s+"
               + re.escape(method) + r"\([^;{]*\)\s*\{")
    matches = [match for match in re.finditer(pattern, clean) if discriminator in match[0]]
    assert len(matches) == 1, (selector, len(matches))
    match = matches[0]
    depth, end = 1, match.end()
    while depth:
        assert end < len(clean), selector
        depth += (clean[end] == "{") - (clean[end] == "}")
        end += 1
    return match.start(), end


def old_events(body, name):
    clean = scrub(body)
    pattern = (
        r"(?P<declaration>(?:final\s+)?Tessellator " + re.escape(name)
        + r" = Tessellator\.instance;)|\b" + re.escape(name)
        + r"\.(?P<call>\w+)\((?P<arguments>[^;]*)\);"
    )
    events = []
    for match in re.finditer(pattern, clean):
        events.append({
            "start": match.start(), "end": match.end(),
            "call": "acquire" if match["declaration"] else match["call"],
            "arguments": [] if match["declaration"] else arguments(match["arguments"]),
            "raw": body[match.start():match.end()],
        })
    assert len(re.findall(r"\b" + re.escape(name) + r"\b", clean)) == len(events), "escaped local"
    return events


def audit_scope(old, current, info):
    """Check independent batch metadata and each native statement at its site."""
    name, buffer = info["name"], info["name"] + "Buffer"
    events = old_events(old, name)
    batches, setters, vertices, acquisitions, resets = [], [], [], 0, 0
    active = None
    for event in events:
        call, args = event["call"], event["arguments"]
        if call == "acquire":
            acquisitions += 1
        elif call in ("startDrawing", "startDrawingQuads"):
            assert active is None
            assert len(args) == (1 if call == "startDrawing" else 0)
            active = {"mode": args[0] if args else "7", "calls": []}
        elif call == "draw":
            assert active is not None and not args
            methods = [entry["call"] for entry in active["calls"]]
            uv = "addVertexWithUV" in methods
            color = any(method.startswith("setColor") for method in methods)
            assert not (uv and "addVertex" in methods), "mixed vertex arity"
            active["format"] = "POSITION" + ("_TEX" if uv else "") + ("_COLOR" if color else "")
            batches.append(active)
            active = None
        elif call in ("setColorOpaque_F", "setColorRGBA_F", "setColorOpaque_I",
                      "setColorRGBA_I", "addVertex", "addVertexWithUV"):
            if active is None:
                assert call == "setColorOpaque_F" and args == ["1F", "0F", "0F"]
                resets += 1
            else:
                entry = {"call": call, "arguments": args}
                active["calls"].append(entry)
                if call.startswith("setColor"):
                    setters.append(entry)
                else:
                    vertices.append(args)
        else:
            raise AssertionError(("unexpected legacy operation", call))
    assert active is None and batches
    independent = {
        "name": name, "acquisitions": acquisitions, "batches": batches,
        "setters": setters, "vertices": vertices, "reset_setters": resets,
    }
    assert independent == info, "independent stream metadata differs"

    # Match from one owned statement to the next, preserving ALL intervening
    # bytes. In particular a radar setter and vertex may share one line.
    old_cursor = new_cursor = setter_index = 0
    batch_index, active, color = -1, False, None
    replacements = []
    for event in events:
        call, args = event["call"], event["arguments"]
        surrounding = old[old_cursor:event["start"]]
        assert current[new_cursor:new_cursor + len(surrounding)] == surrounding, "surrounding source changed"
        new_cursor += len(surrounding)
        indent = re.match(r"[\t ]*", old[old.rfind("\n", 0, event["start"]) + 1:event["start"]])[0]
        if call == "acquire":
            final = "final " if event["raw"].startswith("final ") else ""
            expected = (final + "Tessellator " + name + " = Tessellator.getInstance();\n"
                        + indent + final + "BufferBuilder " + buffer + " = " + name + ".getBuffer();")
        elif call in ("startDrawing", "startDrawingQuads"):
            active, color = True, None
            batch_index += 1
            batch = batches[batch_index]
            expected = buffer + ".begin(" + batch["mode"] + ", DefaultVertexFormats." + batch["format"] + ");"
        elif call.startswith("setColor"):
            if not active:
                expected = ("// Legacy begin resets this attribute; retain its argument evaluation.\n"
                            + indent + "float " + name + "ResetRed = " + args[0] + ", "
                            + name + "ResetGreen = " + args[1] + ", " + name + "ResetBlue = " + args[2] + ";")
            else:
                prefix = name + "Color" + str(setter_index)
                setter_index += 1
                if call.endswith("_F"):
                    values = args + (["1.0F"] if call == "setColorOpaque_F" else [])
                    assert len(values) == 4
                    color = [prefix + channel for channel in ("Red", "Green", "Blue", "Alpha")]
                    statements = ["int " + variable + " = Math.max(0, Math.min(255, (int) (("
                                  + value + ") * 255.0F)));" for variable, value in zip(color, values)]
                else:
                    packed = prefix + "Packed"
                    statements = ["int " + packed + " = " + args[0] + ";"]
                    color = ["(" + packed + " >> 16) & 255", "(" + packed + " >> 8) & 255",
                             packed + " & 255", "255"]
                    if call == "setColorRGBA_I":
                        assert len(args) == 2
                        color[-1] = prefix + "Alpha"
                        statements.append("int " + color[-1] + " = Math.max(0, Math.min(255, " + args[1] + "));")
                    else:
                        assert call == "setColorOpaque_I" and len(args) == 1
                expected = ("\n" + indent).join(statements)
        elif call in ("addVertex", "addVertexWithUV"):
            assert active
            textured = call == "addVertexWithUV"
            assert len(args) == (5 if textured else 3)
            fmt = batches[batch_index]["format"]
            assert textured == ("TEX" in fmt) and (color is not None) == ("COLOR" in fmt)
            expected = buffer + ".pos(" + ", ".join(args[:3]) + ")"
            if textured:
                expected += ".tex(" + ", ".join(args[3:]) + ")"
            if color is not None:
                expected += ".color(" + ", ".join(color) + ")"
            expected += ".endVertex();"
        else:
            assert call == "draw" and active
            active, color = False, None
            expected = event["raw"]
        assert current[new_cursor:new_cursor + len(expected)] == expected, (call, event["raw"])
        replacements.append((new_cursor, new_cursor + len(expected), event["raw"]))
        new_cursor += len(expected)
        old_cursor = event["end"]
    assert current[new_cursor:] == old[old_cursor:], "method tail changed"
    reconstructed = current
    for start, end, original in reversed(replacements):
        reconstructed = reconstructed[:start] + original + reconstructed[end:]
    assert reconstructed == old, "independent full-method inverse differs"
    return independent


def audit_file(relative, old, current, scopes):
    replacements = []
    for method, info in scopes.items():
        oa, ob = method_span(old, method)
        na, nb = method_span(current, method)
        audit_scope(old[oa:ob], current[na:nb], info)
        replacements.append((na, nb, old[oa:ob]))
    reconstructed = current
    for start, end, original in sorted(replacements, reverse=True):
        reconstructed = reconstructed[:start] + original + reconstructed[end:]
    for statement in IMPORTS:
        old_count = old.count(statement)
        assert old_count in (0, 1) and reconstructed.count(statement) == 1
        if not old_count:
            reconstructed = reconstructed.replace(statement + "\n", "", 1)
    assert reconstructed == old, ("changes outside owned scopes/import additions", relative)


def must_reject(check, label):
    try:
        check()
    except AssertionError:
        return
    raise AssertionError(("accepted mutation", label))


def mutation_checks(relative, old, current, scopes):
    """Exercise both the SHA gate and the separate source audit per owner."""
    cases = []
    for method, info in scopes.items():
        start, end = method_span(current, method)
        body = current[start:end]
        name, buffer = info["name"], info["name"] + "Buffer"

        def change(label, before, after, required=True):
            if before not in body:
                assert not required, (relative, method, label)
                return
            cases.append((method + ":" + label, current[:start] + body.replace(before, after, 1) + current[end:]))

        change("acquisition", "Tessellator.getInstance()", "new Tessellator(1024)")
        change("buffer acquisition", name + ".getBuffer()", name + ".getBufferBROKEN()")
        change("draw", name + ".draw();", name + ".getBuffer();")
        change("end vertex", ".endVertex();", ";")
        change("position expression", buffer + ".pos(", buffer + ".pos(1 + ")
        change("absent normal", ".endVertex();", ".normal(0F, 1F, 0F).endVertex();")
        change("absent lightmap", ".endVertex();", ".lightmap(240, 240).endVertex();")
        for index, batch in enumerate(info["batches"]):
            begin = buffer + ".begin(" + batch["mode"] + ", DefaultVertexFormats." + batch["format"] + ");"
            # Select the actual batch ordinal, including identical begin calls.
            begins = list(re.finditer(re.escape(buffer) + r"\.begin\([^;]+;", body))
            match = begins[index]
            assert match[0] == begin
            for label, replacement in (
                ("mode", begin.replace(batch["mode"] + ",", "0,", 1)),
                ("format", begin.replace("DefaultVertexFormats." + batch["format"], "DefaultVertexFormats.BLOCK")),
            ):
                cases.append((method + ":batch " + str(index) + " " + label,
                              current[:start] + body[:match.start()] + replacement + body[match.end():] + current[end:]))
        change("UV expression", ".tex(", ".tex(1 + ", required=False)
        change("color expression", ".color(", ".color(1 + ", required=False)
        change("clamp arithmetic", "Math.min(255,", "Math.min(254,", required=False)
        change("packed channel", " >> 16", " >> 15", required=False)
        change("discarded reset", "ResetRed = 1F", "ResetRed = 0F", required=False)
        # Deliberately mutate unchanged loop/control source rather than an
        # emitted statement, to prove the audit does not just compare streams.
        loop = re.search(r"\b(for|while|if)\s*\(", scrub(body))
        if loop:
            pos = loop.end()
            cases.append((method + ":control flow", current[:start] + body[:pos] + "false && " + body[pos:] + current[end:]))
    cases.append(("file tail", current + "// unexpected source edit\n"))
    for label, mutated in cases:
        assert mutated != current, (relative, label)
        must_reject(lambda: reverse_source(relative, mutated), relative + ":" + label + ":hash")
        must_reject(lambda: audit_file(relative, old, mutated, scopes), relative + ":" + label + ":audit")
    # Protect the manifest's two hash gates as well as mutated source inputs.
    record = FILES[relative]
    for field in ("before_sha256", "after_sha256"):
        saved = record[field]
        record[field] = "0" * 64
        try:
            must_reject(lambda: reverse_source(relative, current), relative + ":" + field)
        finally:
            record[field] = saved
    return len(cases), 2


def validate():
    assert MANIFEST["base"] == BASE
    assert set(FILES) == set(transform.SCOPES) and len(FILES) == 7
    counts = dict(scopes=0, acquisitions=0, batches=0, vertices=0, setters=0, resets=0)
    mutation_count = hash_count = 0
    formats = set()
    for relative, record in FILES.items():
        current = (ROOT / relative).read_bytes().decode("utf-8")
        old = reverse_source(relative, current)
        checkpoint = subprocess.run(
            ["git", "show", BASE + ":./" + relative], cwd=ROOT,
            stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=True,
        ).stdout.decode("utf-8")
        assert old == checkpoint, ("inverse does not equal complete CP91 file", relative)
        replay, metadata = transform.forward_source(relative, old)
        assert replay == current and metadata == record["scopes"], ("transform replay", relative)
        assert set(metadata) == set(transform.SCOPES[relative])
        audit_file(relative, old, current, metadata)
        for info in metadata.values():
            counts["scopes"] += 1
            counts["acquisitions"] += info["acquisitions"]
            counts["batches"] += len(info["batches"])
            counts["vertices"] += len(info["vertices"])
            counts["setters"] += len(info["setters"])
            counts["resets"] += info["reset_setters"]
            formats.update(batch["format"] for batch in info["batches"])
        rejected, hashes = mutation_checks(relative, old, current, metadata)
        mutation_count += rejected
        hash_count += hashes
    assert counts == dict(scopes=10, acquisitions=11, batches=21, vertices=117, setters=17, resets=1), counts
    assert formats == {"POSITION", "POSITION_TEX", "POSITION_COLOR", "POSITION_TEX_COLOR"}
    assert reverse_source("unrelated.java", "unchanged") == "unchanged"
    print(
        "PASS 7 exact reversible full CP91 files; 10 nonescaping scopes, 11 acquisitions, "
        "21 batch sites, 117 vertex expressions, 17 active setters, 1 discarded reset; "
        + str(mutation_count) + " source mutations rejected by BOTH hash and independent audit, "
        + str(hash_count) + " manifest-hash mutations rejected; exact arguments, same-line radar "
        "setters, per-batch attributes, import-only changes, and all surrounding source preserved.",
        flush=True,
    )


if __name__ == "__main__":
    validate()

#!/usr/bin/env python3
"""Static audit of block render-type parity between NTM 1.7.10 and the 1.12.2 port.

Usage: audit_render_type_parity.py [PRISTINE_SRC_ROOT] [PORT_SRC_ROOT] [OUT_MD]

1.7.10: Block#getRenderType() default 0 (cube), -1 none, 1 cross, others ISBRH.
1.12.2: Block#getRenderType(IBlockState) default MODEL; BlockContainer -> INVISIBLE.
"""
import re
import sys
from collections import Counter
from pathlib import Path

HERE = Path(__file__).resolve().parent
PORT_ROOT = HERE.parents[1]  # ntm-port-1.12.2
DEFAULT_PRISTINE = Path(r"C:\Users\nagas\Desktop\port\ntm-original-pristine\src\main\java")
DEFAULT_PORT = PORT_ROOT / "src/main/java"
DEFAULT_OUT = PORT_ROOT / "porting/render-type-parity/REPORT.md"
BLOCKS = "com/hbm/blocks"
MARK = "<!-- FIXES-BEGIN -->"

CLASS_RE = re.compile(
    r"^\s*(?:public\s+|protected\s+|private\s+)?(?:static\s+)?(abstract\s+)?class\s+(\w+)"
    r"(?:<[^>{]*>)?\s+extends\s+([\w.]+)", re.M)
LEGACY_RE = re.compile(r"\bint\s+getRenderType\s*\(\s*\)\s*\{\s*return\s+([^;]+);", re.S)
LEGACY_ANY_RE = re.compile(r"\bint\s+getRenderType\s*\(\s*\)")
NEW_RE = re.compile(
    r"\bEnumBlockRenderType\s+getRenderType\s*\(\s*[\w.]*IBlockState\s+\w+\s*\)\s*\{\s*return\s+([^;]+);", re.S)
NEW_ANY_RE = re.compile(r"\bEnumBlockRenderType\s+getRenderType\s*\(")
TE_METHOD_RE = re.compile(r"\b(?:createNewTileEntity|createTileEntity)\s*\([^)]*\)\s*\{(.*?)\n\t\}", re.S)
TE_NEW_RE = re.compile(r"\bnew\s+([\w.]+)\s*\(")
BIND_RE = re.compile(r"bindTileEntitySpecialRenderer\(\s*([\w.]+)\.class\s*,\s*new\s+(\w+)")


def strip_comments(t):
    t = re.sub(r"/\*.*?\*/", "", t, flags=re.S)
    return re.sub(r"//[^\n]*", "", t)


def index(root):
    """simple class name -> info dict, for files under com/hbm/blocks."""
    out = {}
    base = root / BLOCKS
    if not base.is_dir():
        return out
    for p in sorted(base.rglob("*.java")):
        text = strip_comments(p.read_text(encoding="utf-8", errors="ignore"))
        rel = p.relative_to(root).as_posix()
        for m in CLASS_RE.finditer(text):
            name = m.group(2)
            top = name == p.stem
            if name in out and not top:
                continue
            out[name] = {"rel": rel, "abstract": bool(m.group(1)), "super": m.group(3).split(".")[-1],
                         "text": text if top else "", "top": top}
    return out


def classify_legacy(expr):
    if re.fullmatch(r"-?\d+", expr):
        return ({"0": "0", "-1": "-1", "1": "1"}.get(expr, "lit:" + expr), expr)
    return ("dyn", expr)


def legacy_of(idx, name):
    seen = set()
    while name in idx and name not in seen:
        seen.add(name)
        e = idx[name]
        if e["text"] and LEGACY_ANY_RE.search(e["text"]):
            m = LEGACY_RE.search(e["text"])
            expr = m.group(1).strip() if m else "?"
            return classify_legacy(expr), name
        name = e["super"]
    return ("0", "default"), name


def port_of(idx, name):
    start = name
    seen = set()
    while name in idx and name not in seen:
        seen.add(name)
        e = idx[name]
        if e["text"] and NEW_ANY_RE.search(e["text"]):
            m = NEW_RE.search(e["text"])
            if m:
                lit = re.fullmatch(r"(?:[\w.]*\.)?EnumBlockRenderType\.(\w+)", m.group(1).strip())
                if lit:
                    return lit.group(1), name
            if m and re.search(r"getRenderType\(\)\s*<\s*0", m.group(1)):
                # delegates to the legacy int getRenderType() (BlockDummyable style)
                (lk, lx), _ = legacy_of(idx, start)
                if lk in ("0", "-1", "1") or lk.startswith("lit:"):
                    v = int(lx)
                    return ("INVISIBLE" if v < 0 else "MODEL"), name + "(via int getRenderType=" + lx + ")"
            return "dynamic", name
        name = e["super"]
    if name == "BlockContainer":
        return "INVISIBLE", "BlockContainer(default)"
    return "MODEL", "default"


def te_of(idx, name):
    seen = set()
    while name in idx and name not in seen:
        seen.add(name)
        e = idx[name]
        if e["text"]:
            for m in TE_METHOD_RE.finditer(e["text"]):
                n = TE_NEW_RE.findall(m.group(1))
                if n:
                    return n[0].split(".")[-1]
        name = e["super"]
    return None


def main(argv):
    pristine = Path(argv[1]) if len(argv) > 1 else DEFAULT_PRISTINE
    port = Path(argv[2]) if len(argv) > 2 else DEFAULT_PORT
    out_md = Path(argv[3]) if len(argv) > 3 else DEFAULT_OUT
    pidx, nidx = index(pristine), index(port)
    cp = port / "com/hbm/main/ClientProxy.java"
    binds = {}
    if cp.exists():
        for te, r in BIND_RE.findall(cp.read_text(encoding="utf-8", errors="ignore")):
            binds[te.split(".")[-1]] = r
    rows = []
    for name, e in sorted(nidx.items()):
        if not e["top"] or e["abstract"]:
            continue
        if name in pidx:
            leg, legsrc = legacy_of(pidx, name)
        else:
            leg, legsrc = ("n/a", "-"), "not in pristine"
        new, newsrc = port_of(nidx, name)
        te = te_of(nidx, name)
        tesr = binds.get(te) if te else None
        legk = leg[0]
        if legk == "n/a":
            cls = "port-only"
        elif legk == "0" and new == "MODEL":
            cls = "OK"
        elif legk == "-1" and new == "INVISIBLE":
            cls = "OK"
        elif legk == "1":
            cls = "cross(report)"
        elif legk == "0" and new == "INVISIBLE":
            cls = "A:container-default-invisible" + ("+TESR" if tesr else "")
        elif legk == "-1" and new == "MODEL":
            cls = "B:invisible-as-model"
        elif legk == "dyn" or legk.startswith("lit:"):
            cls = "ISBRH/dynamic(report)"
        elif new == "dynamic":
            cls = "port-dynamic(report)"
        else:
            cls = "other(report)"
        rows.append({"name": name, "rel": e["rel"], "leg": legk, "legexpr": leg[1], "legsrc": legsrc,
                     "new": new, "newsrc": newsrc, "te": te, "tesr": tesr, "cls": cls})
    counts = Counter(r["cls"] for r in rows)
    mism = [r for r in rows if r["cls"] != "OK"]
    L = ["# Render type parity (1.7.10 vs 1.12.2 port)", "",
         "Generated by `porting/tools/audit_render_type_parity.py` (static, best effort). "
         "The FIXES section at the end is hand-maintained and preserved on regeneration.", "",
         "## Summary", "", "Concrete block classes: %d" % len(rows), ""]
    for k, v in sorted(counts.items()):
        L.append("- %s: %d" % (k, v))
    L += ["", "## Mismatches", "",
          "| class | legacy | legacy src | port 1.12 | port src | TE | TESR | class |",
          "|---|---|---|---|---|---|---|---|"]
    for r in mism:
        lg = r["leg"] if r["leg"] != "dyn" else "dyn(" + r["legexpr"] + ")"
        L.append("| %s | %s | %s | %s | %s | %s | %s | %s |" % (
            r["name"], lg, r["legsrc"], r["new"], r["newsrc"], r["te"] or "-", r["tesr"] or "-", r["cls"]))
    L += ["", "## All classes", "", "| class | legacy | port | TE | TESR |", "|---|---|---|---|---|"]
    for r in rows:
        L.append("| %s | %s | %s | %s | %s |" % (r["name"], r["leg"], r["new"], r["te"] or "-", r["tesr"] or "-"))
    gen = "\n".join(L) + "\n"
    notes = ""
    if out_md.exists():
        old = out_md.read_text(encoding="utf-8")
        i = old.find(MARK)
        if i >= 0:
            notes = old[i:]
    out_md.parent.mkdir(parents=True, exist_ok=True)
    out_md.write_text(gen + "\n" + notes, encoding="utf-8", newline="\n")
    print("classes=%d mismatches=%d" % (len(rows), len(mism)))
    for k, v in sorted(counts.items()):
        print("  %s: %d" % (k, v))
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))

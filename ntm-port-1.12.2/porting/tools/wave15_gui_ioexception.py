# Wave 15: Add throws java.io.IOException to GUI override methods calling super
import os
import re
import sys

BASE_DIR = r"C:\Users\nagas\Desktop\port\ntm-port-1.12.2"
SRC_DIR = os.path.join(BASE_DIR, "src", "main", "java")

def main():
    apply_mode = "--apply" in sys.argv
    print(f"Running Wave 15 GUI IOException pass ({'APPLY' if apply_mode else 'PREVIEW'})...")

    targets = {}
    for root, dirs, files in os.walk(SRC_DIR):
        for f in files:
            if f.endswith(".java"):
                p = os.path.join(root, f)
                with open(p, "rb") as jf:
                    raw = jf.read()
                lines = raw.split(b"\n")
                for i, l in enumerate(lines):
                    if re.search(rb"super\.(mouseClicked|keyTyped|actionPerformed)\(", l):
                        for k in range(i, max(0, i - 40), -1):
                            ml = lines[k]
                            if any(m in ml for m in [b"void mouseClicked", b"void keyTyped", b"void actionPerformed"]) and any(acc in ml for acc in [b"protected", b"public"]):
                                if b"IOException" not in ml:
                                    targets[(p, k)] = ml
                                break

    print(f"Found {len(targets)} methods across {len(set(p for p, k in targets.keys()))} files.")

    # Group targets by file
    from collections import defaultdict
    by_file = defaultdict(list)
    for (p, k), ml in targets.items():
        by_file[p].append((k, ml))

    success_files = 0
    for p, items in by_file.items():
        with open(p, "rb") as jf:
            raw = jf.read()
        lines = raw.split(b"\n")

        for k, ml in items:
            cur = lines[k]
            if b"{" in cur:
                new_line = re.sub(rb"\)\s*\{", b") throws java.io.IOException {", cur)
            else:
                new_line = re.sub(rb"\)\s*(\r?)$", rb") throws java.io.IOException\1", cur)
            lines[k] = new_line

        new_raw = b"\n".join(lines)
        if apply_mode:
            with open(p, "wb") as jf:
                jf.write(new_raw)
        success_files += 1

    print(f"Processed {success_files}/{len(by_file)} files successfully.")

if __name__ == "__main__":
    main()

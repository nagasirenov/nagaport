"""Pinned original test inputs; explicit pristine tree remains optional for cross-checks."""
from pathlib import Path
import hashlib, zipfile

def pristine_root(override=None):
    if override is not None:
        return Path(override)
    archive = Path(__file__).resolve().parent / "fixtures/side-rotation-pristine.zip"
    digest = hashlib.sha256(archive.read_bytes()).hexdigest()
    if digest != "b952912575e58616f142e8fc30aaa3ad3009c2aa0560ee714d6c7f523bbb46aa":
        raise AssertionError("Pinned side-rotation pristine fixture changed: " + digest)
    return zipfile.Path(zipfile.ZipFile(archive))

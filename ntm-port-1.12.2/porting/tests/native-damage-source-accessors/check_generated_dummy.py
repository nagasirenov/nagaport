"""Exact semantic pin for two ForgeGradle-generated, classless dummy JARs only."""
import hashlib,zipfile
from pathlib import Path
GENERATOR=Path('/workspace/shared/nagaport-toolchain/gradle-user-home/caches/modules-2/files-2.1/net.minecraftforge.gradle/ForgeGradle/2.3-SNAPSHOT/d26d73dd46e54d4f633edb17ab8c1efd90c7604e/ForgeGradle-2.3-SNAPSHOT.jar')
GENERATOR_SHA256='5e8d1af585b0936a40ccc6489fa9871da008a41b4f481e462d1dc61279f0274e'
CLASS='net/minecraftforge/gradle/user/TaskDepDummy.class'
CLASS_SHA256='fafb44c4a199f831c6e29ac97160ae83c3d408a90863eff86e0f9d2d4ec996fe'
DUMMIES={
 '/workspace/shared/nagaport-toolchain/gradle-user-home/caches/minecraft/deobfedDeps/compileDummy.jar',
 '/workspace/shared/nagaport-toolchain/gradle-user-home/caches/minecraft/deobfedDeps/providedDummy.jar',
}
def verify_generated_dummy(path):
 assert str(path) in DUMMIES,('not a reviewed generated container',path)
 assert hashlib.sha256(GENERATOR.read_bytes()).hexdigest()==GENERATOR_SHA256,'ForgeGradle generator changed'
 with zipfile.ZipFile(GENERATOR) as generator:
  assert hashlib.sha256(generator.read(CLASS)).hexdigest()==CLASS_SHA256,'TaskDepDummy generator changed'
 # Pinned makeEmptyJar bytecode creates one JarEntry("dummyThing") then writes(-1).
 # Exact entry multiset and payload reject additional/duplicate entries and any classes.
 with zipfile.ZipFile(path) as archive:
  assert archive.namelist()==['dummyThing'],('generated dummy entries changed',path)
  assert archive.read('dummyThing')==b'\xff',('generated dummy payload changed',path)

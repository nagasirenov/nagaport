#!/usr/bin/env python3
"""Focused real 1.12 Forge slice, never a full-mod build assertion."""
import argparse, hashlib, os, subprocess, tempfile, zipfile
from pathlib import Path
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=Path,required=True);a=p.parse_args()
root=Path(__file__).resolve().parents[3];here=Path(__file__).resolve().parent
names=['items/ItemEnumMulti.java','items/ItemEnums.java','items/ItemGenericPart.java','items/machine/ItemDrillbit.java','items/machine/ItemCircuit.java','interfaces/IOrderedEnum.java','lib/RefStrings.java','util/EnumUtil.java','render/item/NativeItemTextureRegistry.java','render/item/NativeItemTextureModelHandler.java','render/icon/IIcon.java','render/icon/IIconRegister.java','render/icon/LegacyIconRegister.java','render/icon/LegacyTextureAtlasSprite.java','render/item/ItemRenderBase.java','render/item/nativeitem/callback/CallbackItemRegistration.java','render/item/nativeitem/callback/CallbackItemModel.java','render/item/nativeitem/StaticMeshItemRegistration.java','render/item/nativeitem/StaticMeshItemModel.java','render/item/nativeitem/ItemTransformMatrix.java','render/item/nativeitem/AngelCoreFrameCorrection.java','render/item/nativeitem/staticfamily/OrdinaryMeshItemModel.java','render/item/nativeitem/staticfamily/StaticItemPerspectives.java','render/util/NativeItemBaseModel.java']
production=[root/'src/main/java/com/hbm'/n for n in names]
vanilla=next(Path(s) for s in a.classpath.split(os.pathsep) if 'forgeBin-' in s and s.endswith('.jar'))
client=(root/'src/main/java/com/hbm/main/ClientProxy.java').read_text();assert client.count('new com.hbm.render.item.NativeItemTextureModelHandler()')==1
icon=(root/'src/main/java/com/hbm/main/LegacyIconRegistrationHandler.java').read_text();assert 'int blockCount = registerBlocks(register);' in icon and 'int itemCount = registerItems(new LegacyIconRegister(map, true));' in icon
with tempfile.TemporaryDirectory(prefix='item-foundation-native-') as tmp:
 out=Path(tmp);jar=out/'items-resources.jar'
 with zipfile.ZipFile(jar,'w') as z:
  for f in (root/'src/main/resources/assets/hbm/textures/items').glob('*.png'):z.write(f,f.relative_to(root/'src/main/resources'))
 files=production+[here/'NativeItemFoundationTest.java',root/'porting/tests/ForgeEventNativeLauncher.java']
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-sourcepath',tmp,'-cp',a.classpath,'-d',tmp]+list(map(str,files)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-Xmx1000m','-Djava.awt.headless=true','-cp',tmp+os.pathsep+a.classpath,'ForgeEventNativeLauncher','NativeItemFoundationTest',str(root/'src/main/resources'),str(jar),str(vanilla)],cwd=tmp,check=True)
 for f in production:print(hashlib.sha256(f.read_bytes()).hexdigest(),f.relative_to(root))

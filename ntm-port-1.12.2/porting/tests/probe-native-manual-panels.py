#!/usr/bin/env python3
"""Compile complete manual/tutorial panel classes against native APIs and declared NTM fixtures."""
import argparse,pathlib,subprocess,tempfile
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args();root=pathlib.Path(__file__).resolve().parents[2]
fixtures={
'com/hbm/wiaj/WorldInAJar.java':'package com.hbm.wiaj; public class WorldInAJar {}',
'com/hbm/wiaj/JarScene.java':'package com.hbm.wiaj; public class JarScene {}',
'com/hbm/util/i18n/I18nUtil.java':'package com.hbm.util.i18n; public class I18nUtil { public static java.util.List<String> autoBreak(Object font,String text,int width){return ((net.minecraft.client.gui.FontRenderer)font).listFormattedStringToWidth(text,width);} }',
'com/hbm/qmaw/QuickManualAndWiki.java':'package com.hbm.qmaw; public class QuickManualAndWiki { public net.minecraft.item.ItemStack icon; }',
'com/hbm/qmaw/QMAWLoader.java':'package com.hbm.qmaw; public class QMAWLoader { public static java.util.Map<String,QuickManualAndWiki> qmaw=new java.util.HashMap<>(); }',
'com/hbm/qmaw/GuiQMAW.java':'package com.hbm.qmaw; public class GuiQMAW extends net.minecraft.client.gui.GuiScreen {public java.util.List<String> back=new java.util.ArrayList<>(); public String qmawID;public GuiQMAW(QuickManualAndWiki wiki){} }'
}
files=['com/hbm/render/util/NativeItemBaseModel.java','com/hbm/render/util/NativeItemQuadRenderer.java','com/hbm/render/util/NativeGuiItemDraw.java','com/hbm/qmaw/ManualElement.java','com/hbm/qmaw/components/QComponentLink.java','com/hbm/wiaj/actors/ISpecialActor.java','com/hbm/wiaj/actors/ActorFancyPanel.java','com/hbm/lib/RefStrings.java']
with tempfile.TemporaryDirectory(prefix='native-manual-panels-') as tmp:
 paths=[]
 for name,text in fixtures.items():
  target=pathlib.Path(tmp)/'fixtures'/name;target.parent.mkdir(parents=True,exist_ok=True);target.write_text(text);paths.append(target)
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',tmp]+[str(root/'src/main/java'/f) for f in files]+list(map(str,paths)),check=True)
print('PASS six complete production classes compile against real native Minecraft/Forge. Six explicit NTM navigation/world/localization fixtures; not complete manual/tutorial, GUI, game or GL acceptance.')

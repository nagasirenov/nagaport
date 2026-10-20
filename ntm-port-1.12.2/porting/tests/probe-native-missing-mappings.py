#!/usr/bin/env python3
"""Complete native mapping handler; original byte-identical data oracle, explicit item registry identities."""
import argparse,os,pathlib,subprocess,tempfile,hashlib
p=argparse.ArgumentParser();p.add_argument('--classpath',required=True);p.add_argument('--java-home',type=pathlib.Path,required=True);a=p.parse_args()
r=pathlib.Path(__file__).resolve().parents[2];t=r/'porting/tests';s=r/'src/main/java'
body=(t/'fixtures/MissingMappings-original-data.txt').read_bytes()
assert hashlib.sha256(body).hexdigest()=='75a2655949272d4d7062de565c17a0bcc071e479eda3b31dbd4b525678408c0f','Pinned original data fixture changed'
production=(s/'com/hbm/main/MissingMappingHandler.java').read_bytes()
assert body in production,'Original mapping data changed'
assert b'FMLMissingMappingsEvent' not in (s/'com/hbm/main/MainRegistry.java').read_bytes()
with tempfile.TemporaryDirectory(prefix='native-missing-mappings-') as d:
 out=pathlib.Path(d);f=out/'com/hbm/items/ModItems.java';f.parent.mkdir(parents=True)
 f.write_text('package com.hbm.items;public class ModItems {public static net.minecraft.item.Item early_explosive_lenses,explosive_lenses,briquette,fuel_additive,toolbox;}')
 oracle=out/'LegacyMissingMappingOracle.java';oracle.write_bytes(b'import java.util.*;import com.hbm.items.ModItems;import net.minecraft.item.Item;public class LegacyMissingMappingOracle {static HashSet<String> ignoreMappings=new HashSet<>();static HashMap<String,Item> remapItems=new HashMap<>();static void prepare(){\n'+body+b'}}\n')
 inputs=[s/'com/hbm/main/MissingMappingHandler.java',s/'com/hbm/lib/RefStrings.java',f,oracle,t/'MissingMappingsNativeTest.java',t/'ForgeEventNativeLauncher.java']
 subprocess.run([str(a.java_home/'bin/javac'),'-encoding','UTF-8','-source','8','-target','8','-cp',a.classpath,'-d',d]+list(map(str,inputs)),check=True)
 subprocess.run([str(a.java_home/'bin/java'),'-cp',d+os.pathsep+a.classpath,'ForgeEventNativeLauncher','MissingMappingsNativeTest'],cwd=d,check=True)
 print('PRODUCTION',hashlib.sha256(production).hexdigest(),'com/hbm/main/MissingMappingHandler.java')
 print('ORIGINAL_DATA',hashlib.sha256(body).hexdigest())

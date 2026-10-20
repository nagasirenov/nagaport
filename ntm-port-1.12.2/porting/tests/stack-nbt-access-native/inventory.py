#!/usr/bin/env python3
from pathlib import Path
import json
ROOT=Path(__file__).resolve().parents[3]
EXCLUDED_NAMES={'ModBlocks.java','BlockBase.java','ClientProxy.java','LegacyIconRegister.java','LegacyIconRegistrationHandler.java','ModItems.java','ItemCustomLore.java','ItemEnumMulti.java'}
EXCLUDED_ITEMS='weapon/ItemMissile weapon/ItemAmmo special/ItemSchraranium special/ItemExpensive special/ItemHolotapeImage special/ItemTrain special/ItemBedrockOre machine/ItemBatteryPack machine/ItemBatterySC machine/ItemCircuit machine/ItemDrillbit machine/ItemPACoil machine/ItemPWRFuel machine/ItemPistons machine/ItemWatzPellet machine/ItemZirnoxRod food/ItemConserve tool/ItemDrone ItemVOTVdrive'.split()
def eligible(p):
 s=p.as_posix()
 return p.name not in EXCLUDED_NAMES and '/world/dungeon/' not in s and not any(s.endswith('/com/hbm/items/'+x+'.java') for x in EXCLUDED_ITEMS)
if __name__=='__main__':
 files=[p for p in sorted((ROOT/'src/main/java').rglob('*.java')) if eligible(p) and '.stackTagCompound' in p.read_text()]
 Path(__file__).with_name('candidate-files.txt').write_text(''.join(str(p)+'\n' for p in files))
 print(json.dumps({'lexical_occurrences':sum(p.read_text().count('.stackTagCompound') for p in files),'candidate_files':len(files)}))

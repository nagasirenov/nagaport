#!/usr/bin/env python3
"""Compare the migrated registrations against the shipped read-only pristine."""
from pathlib import Path
import re

project = Path(__file__).resolve().parents[2]
pristine = project.parent / "ntm-original-pristine"
old_main = (pristine / "src/main/java/com/hbm/main/MainRegistry.java").read_text()
new_main = (project / "src/main/java/com/hbm/main/MainRegistry.java").read_text()
categories = {
    "VILLAGE_BLACKSMITH": "CHESTS_VILLAGE_BLACKSMITH",
    "MINESHAFT_CORRIDOR": "CHESTS_ABANDONED_MINESHAFT",
    "DUNGEON_CHEST": "CHESTS_SIMPLE_DUNGEON",
    "PYRAMID_DESERT_CHEST": "CHESTS_DESERT_PYRAMID",
    "PYRAMID_JUNGLE_CHEST": "CHESTS_JUNGLE_TEMPLE",
    "BONUS_CHEST": "CHESTS_SPAWN_BONUS_CHEST",
}
old_entries = re.findall(
    r"ChestGenHooks.addItem\(ChestGenHooks.(\w+), new WeightedRandomChestContent\(new ItemStack\(ModItems.(\w+)\), (\d+), (\d+), (\d+)\)\);",
    old_main,
)
expected = []
for table, item, minimum, maximum, weight in old_entries:
    assert minimum == maximum == "1", "New adapter must encode non-default counts"
    expected.append((categories[table], item, weight))
actual = re.findall(
    r"chestLoot.addItem\(LootTableList.(\w+), ModItems.(\w+), (\d+)\);",
    new_main,
)
assert len(expected) == 13
assert expected == actual, (expected, actual)
assert "MinecraftForge.EVENT_BUS.register(chestLoot);" in new_main

old_library = (pristine / "src/main/java/com/hbm/world/dungeon/LibraryDungeon.java").read_text()
new_library = (project / "src/main/java/com/hbm/world/dungeon/LibraryDungeon.java").read_text()
old_fills = re.findall(r"WeightedRandomChestContent.generateChestContents\(rand, ChestGenHooks.getItems\(DUNGEON_CHEST, rand\), (tileentitychest\d*),", old_library)
new_fills = re.findall(r"fillDungeonChest\(world, rand, (tileentitychest\d*)\)", new_library)
assert len(old_fills) == 8
assert old_fills == new_fills
assert "LootTableList.CHESTS_SIMPLE_DUNGEON" in new_library
assert ".fillInventory(chest, random, context);" in new_library
for source in (project / "src/main/java").rglob("*.java"):
    text = source.read_text()
    assert "import net.minecraft.util.WeightedRandomChestContent;" not in text, source
    assert "import net.minecraftforge.common.ChestGenHooks;" not in text, source
print("PASS: all 13 pristine vanilla item/table/weight/count registrations retained; preInit event registration present")
print("PASS: all 8 LibraryDungeon chest-fill consumers migrated; removed loot imports absent from production sources")

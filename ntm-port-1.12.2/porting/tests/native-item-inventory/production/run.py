#!/usr/bin/env python3
"""Complete current production base, no extracted method or platform stubs."""
import hashlib,json,pathlib,subprocess,tempfile,zipfile
H=pathlib.Path(__file__).resolve().parent
R=H.parents[3]
S=R/"src/main/java"
T=pathlib.Path("/workspace/shared/nagaport-toolchain")
CP=(T/"native-classpath.txt").read_text().strip()
J=str(T/"java8/bin/java");JC=str(T/"java8/bin/javac")
paths=["com/hbm/items/ItemInventory.java","com/hbm/util/NullableInventoryStackNbt.java",
       "com/hbm/sound/WorldSoundEffects.java","com/hbm/main/ModSoundEvents.java","com/hbm/lib/RefStrings.java"]
def sha(b):return hashlib.sha256(b).hexdigest()
def run(args,cwd):
    p=subprocess.run(args,cwd=cwd,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
    print(p.stdout,end="",flush=True)
    if p.returncode:raise RuntimeError("Oracle failed: "+str(p.returncode))
    return p.stdout
receipt={"production_source_sha256":{p:sha((S/p).read_bytes()) for p in paths},
         "test_source_sha256":sha((H/"ProductionInventoryTest.java").read_bytes()),
         "scope":"Complete production base; actual engine classes; observed spawn, not full server gameplay"}
with tempfile.TemporaryDirectory(prefix="item-inventory-production-") as td:
    d=pathlib.Path(td);out=d/"classes";out.mkdir()
    mapper=R/"porting/tests/difurnace-raw-reader/RemapQuantityField.java"
    run([JC,"-encoding","UTF-8","-cp",CP,"-d",str(out)]+[str(S/p) for p in paths]+[str(H/"ProductionInventoryTest.java"),str(H/"DamageFacts.java"),str(mapper)],d)
    run([J,"-Xmx400m","-cp",str(out)+":"+CP,"inventoryproduction.ProductionInventoryTest"],d)
    original=None
    for entry in CP.split(":"):
        p=pathlib.Path(entry)
        if p.is_file() and zipfile.is_zipfile(p):
            with zipfile.ZipFile(p) as z:
                if "net/minecraft/item/ItemStack.class" in z.namelist():original=z.read("net/minecraft/item/ItemStack.class");break
    assert original is not None
    raw=d/"ItemStack.class";raw.write_bytes(original);renamed=d/"srg/net/minecraft/item/ItemStack.class"
    run([J,"-cp",str(out)+":"+CP,"RemapQuantityField",str(raw),str(renamed)],d)
    run([J,"-Xmx400m","-cp",str(d/"srg")+":"+str(out)+":"+CP,"inventoryproduction.ProductionInventoryTest"],d)
    dependencies=[p for p in CP.split(":") if pathlib.Path(p).is_file() and "forgeBin-" not in p and "deobfedDeps/" not in p]
    legacy_cp=":".join([str(out),"/tmp/nagaport-legacy-client.jar"]+dependencies)
    oldfacts=run([J,"-Xmx400m","-cp",legacy_cp,"inventoryproduction.DamageFacts","old"],d)
    nativefacts=run([J,"-Xmx400m","-cp",str(out)+":"+CP,"inventoryproduction.DamageFacts","native"],d)
    header="kind,engine,input_damage,get_damage,item_is_diamond,raw_count,visible_count,is_empty,nbt_signed_short,loaded_damage,loaded_item_is_diamond,copy_damage,copy_item_is_diamond,drop_item_is_diamond,drop_is_empty\n"
    facts=header+"\n".join(line for line in (oldfacts+nativefacts).splitlines() if line.startswith("DAMAGE,"))+"\n"
    (H/"damage-facts.csv").write_text(facts)
    rows=[line.split(",") for line in facts.splitlines()[1:]]
    assert len(rows)==22
    by={(r[1],int(r[2])):r for r in rows}
    for damage in (65536,2147483647):
        assert by[("old",damage)][4]=="true" and by[("native",damage)][4]=="false"
        assert by[("old",damage)][13]=="true" and by[("native",damage)][14]=="true"
    receipt["damage_facts_sha256"]=sha(facts.encode())
    receipt["native_class_sha256"]=sha(original);receipt["srg_quantity_class_sha256"]=sha(renamed.read_bytes())
    production=(S/"com/hbm/items/ItemInventory.java").read_text()
    begin=production.index("\t\t\t\t\t\t\tif(",production.index("// Edge-only public snapshot"))
    end=production.index("\n\t\t\t\t\t\t\t}",begin)+len("\n\t\t\t\t\t\t\t}")
    fallback=production[begin:end]
    assert "writeToNBT" in fallback and "Item.getByNameOrId" in fallback and "item.getDamage(itemstack)" in fallback
    mutant=d/"mutant/com/hbm/items/ItemInventory.java";mutant.parent.mkdir(parents=True)
    mutant.write_text(production[:begin]+production[end:])
    mutant_out=d/"mutant-classes";mutant_out.mkdir()
    run([JC,"-encoding","UTF-8","-cp",str(out)+":"+CP,"-d",str(mutant_out),str(mutant)],d)
    failed=subprocess.run([J,"-Xmx400m","-cp",str(mutant_out)+":"+str(out)+":"+CP,
                           "inventoryproduction.ProductionInventoryTest"],cwd=d,stdout=subprocess.PIPE,stderr=subprocess.STDOUT,text=True)
    assert failed.returncode!=0 and "malformed ejection raw registry identity" in failed.stdout,failed.stdout
    receipt["rejected_mutant"]={"name":"remove edge-only raw item/damage fallback","assertion":"malformed ejection raw registry identity","mutant_sha256":sha(mutant.read_bytes())}
    print("PASS rejected mutant: remove edge-only raw item/damage fallback",flush=True)
    assert receipt["production_source_sha256"]=={p:sha((S/p).read_bytes()) for p in paths},"production changed while testing"
(H/"receipt.json").write_text(json.dumps(receipt,indent=2)+"\n")
print("PASS complete production base in MCP and SRG quantity layouts; no nested subclass/gameplay claim")

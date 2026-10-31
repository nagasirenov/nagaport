from pathlib import Path
import re

files = [
    'src/main/java/com/hbm/blocks/gas/BlockGasClorine.java',
    'src/main/java/com/hbm/blocks/generic/TrappedBrick.java',
    'src/main/java/com/hbm/entity/effect/EntityMist.java',
    'src/main/java/com/hbm/entity/projectile/EntityChemical.java',
    'src/main/java/com/hbm/explosion/ExplosionChaos.java',
    'src/main/java/com/hbm/explosion/ExplosionThermo.java',
    'src/main/java/com/hbm/handler/ability/IWeaponAbility.java',
    'src/main/java/com/hbm/handler/EntityEffectHandler.java',
    'src/main/java/com/hbm/items/armor/ItemModDefuser.java',
    'src/main/java/com/hbm/items/armor/ItemModSerum.java',
    'src/main/java/com/hbm/items/armor/ItemModServos.java',
    'src/main/java/com/hbm/items/food/ItemAppleSchrabidium.java',
    'src/main/java/com/hbm/items/food/ItemCanteen.java',
    'src/main/java/com/hbm/items/food/ItemCottonCandy.java',
    'src/main/java/com/hbm/items/food/ItemEnergy.java',
    'src/main/java/com/hbm/items/food/ItemLemon.java',
    'src/main/java/com/hbm/items/food/ItemPill.java',
    'src/main/java/com/hbm/items/food/ItemSchnitzelVegan.java',
    'src/main/java/com/hbm/items/ModItems.java',
    'src/main/java/com/hbm/items/ModItemsArmor.java',
    'src/main/java/com/hbm/items/special/ItemGlitch.java',
    'src/main/java/com/hbm/items/special/ItemSimpleConsumable.java',
    'src/main/java/com/hbm/items/special/ItemSyringe.java',
    'src/main/java/com/hbm/items/tool/WeaponSpecial.java',
    'src/main/java/com/hbm/items/weapon/sedna/factory/Orchestras.java',
    'src/main/java/com/hbm/items/weapon/sedna/factory/XFactoryEnergy.java'
]

mapping = [
    ('damageBoost', 'net.minecraft.init.MobEffects.STRENGTH'),
    ('digSpeed', 'net.minecraft.init.MobEffects.HASTE'),
    ('jump', 'net.minecraft.init.MobEffects.JUMP_BOOST'),
    ('weakness', 'net.minecraft.init.MobEffects.WEAKNESS'),
    ('digSlowdown', 'net.minecraft.init.MobEffects.MINING_FATIGUE'),
    ('hunger', 'net.minecraft.init.MobEffects.HUNGER'),
]

for fpath in files:
    p = Path(fpath)
    text = p.read_bytes().decode('utf-8')
    orig = text
    
    if fpath == 'src/main/java/com/hbm/items/ModItems.java':
        text = text.replace('setPotionEffect(Potion.damageBoost.id, 180, 1, 1F)',
                            'setPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.STRENGTH, 180, 1), 1F)')

    for old_name, new_name in mapping:
        # replace with .id or .getId() first
        text = re.sub(r'Potion\.' + old_name + r'\.(?:id|getId\(\))', new_name, text)
        # then bare Potion.xxx
        text = re.sub(r'Potion\.' + old_name + r'\b', new_name, text)

    if text != orig:
        crlf_text = text.replace('\r\n', '\n').replace('\n', '\r\n')
        p.write_bytes(crlf_text.encode('utf-8'))
        print(f'Updated potion effects in {fpath}')

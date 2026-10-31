from pathlib import Path
import re

# 1. ModBlocks.java: revert withSoundType -> setStepSound on lines with setBlockTextureName
p = Path('src/main/java/com/hbm/blocks/ModBlocks.java')
lines = p.read_bytes().decode('utf-8').splitlines()

fixed_count = 0
for idx, line in enumerate(lines):
    if 'withSoundType' in line and 'setBlockTextureName' in line:
        m = re.search(r'\.withSoundType\(([^)]+)\)', line)
        if m:
            sound_arg = m.group(1)
            # Remove withSoundType
            newline = line.replace(m.group(0), '')
            # Put setStepSound right before setBlockTextureName
            newline = newline.replace('.setBlockTextureName(', f'.setStepSound({sound_arg}).setBlockTextureName(')
            # If BlockHazardFalling was introduced on this line, revert back to BlockFalling so no cascading
            newline = newline.replace('new BlockHazardFalling(', 'new BlockFalling(')
            lines[idx] = newline
            fixed_count += 1

print(f'Reverted {fixed_count} lines with setBlockTextureName in ModBlocks.java')
crlf_text = '\r\n'.join(lines) + '\r\n'
p.write_bytes(crlf_text.encode('utf-8'))

# 2. ModItems.java line 4211
p = Path('src/main/java/com/hbm/items/ModItems.java')
text = p.read_bytes().decode('utf-8')
text = text.replace('setPotionEffect(new PotionEffect(net.minecraft.init.MobEffects.STRENGTH, 180, 1), 1F)',
                    'setPotionEffect(new net.minecraft.potion.PotionEffect(net.minecraft.init.MobEffects.STRENGTH, 180, 1), 1F)')
p.write_bytes(text.replace('\r\n', '\n').replace('\n', '\r\n').encode('utf-8'))
print('Fixed ModItems.java')

# 3. ItemModMinecart.java
p = Path('src/main/java/com/hbm/items/tool/ItemModMinecart.java')
text = p.read_bytes().decode('utf-8')
text = text.replace('BlockPos railPos = source.getBlockPos().offset(enumfacing);',
                    'net.minecraft.util.math.BlockPos railPos = source.getBlockPos().offset(enumfacing);')
p.write_bytes(text.replace('\r\n', '\n').replace('\n', '\r\n').encode('utf-8'))
print('Fixed ItemModMinecart.java')

# 4. DispenserBehaviorHandler.java
p = Path('src/main/java/com/hbm/handler/DispenserBehaviorHandler.java')
text = p.read_bytes().decode('utf-8')
text = text.replace('BlockPos targetPos = source.getBlockPos().offset(facing);',
                    'net.minecraft.util.math.BlockPos targetPos = source.getBlockPos().offset(facing);')
p.write_bytes(text.replace('\r\n', '\n').replace('\n', '\r\n').encode('utf-8'))
print('Fixed DispenserBehaviorHandler.java')

# 5. XFactory12ga.java
p = Path('src/main/java/com/hbm/items/weapon/sedna/factory/XFactory12ga.java')
text = p.read_bytes().decode('utf-8')
text = text.replace('mop.hitVec.addVector(side.getXOffset() * 0.1, side.getYOffset() * 0.1, side.getZOffset() * 0.1)',
                    'mop.hitVec.add(side.getXOffset() * 0.1, side.getYOffset() * 0.1, side.getZOffset() * 0.1)')
p.write_bytes(text.replace('\r\n', '\n').replace('\n', '\r\n').encode('utf-8'))
print('Fixed XFactory12ga.java')

# 6. XFactoryEnergy.java
p = Path('src/main/java/com/hbm/items/weapon/sedna/factory/XFactoryEnergy.java')
text = p.read_bytes().decode('utf-8')
text = text.replace('hitVec.addVector(side.getXOffset() * 0.5, side.getYOffset() * 0.5, side.getZOffset() * 0.5)',
                    'hitVec.add(side.getXOffset() * 0.5, side.getYOffset() * 0.5, side.getZOffset() * 0.5)')
p.write_bytes(text.replace('\r\n', '\n').replace('\n', '\r\n').encode('utf-8'))
print('Fixed XFactoryEnergy.java')

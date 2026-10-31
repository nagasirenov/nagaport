import pathlib
import re

files = [
    pathlib.Path('src/main/java/com/hbm/items/tool/ItemAmmoBag.java'),
    pathlib.Path('src/main/java/com/hbm/items/tool/ItemPlasticBag.java'),
    pathlib.Path('src/main/java/com/hbm/items/tool/ItemCasingBag.java'),
]

for path in files:
    data = path.read_bytes()
    data = data.replace(
        b'@Override public ItemStack getStackInSlot(int slot) { return slots[slot]; }',
        b'@Override public ItemStack getStackInSlot(int slot) { ItemStack stack = slots[slot]; return stack == null || stack.isEmpty() ? ItemStack.EMPTY : stack; }'
    )
    data = data.replace(b'if (stack != null) {\n\t\t\t\tif (stack.getCount() > amount)', b'if (!stack.isEmpty()) {\n\t\t\t\tif (stack.getCount() > amount)')
    data = data.replace(b'if(getStackInSlot(i) != null && getStackInSlot(i).getCount() == 0)', b'if(!getStackInSlot(i).isEmpty() && getStackInSlot(i).getCount() == 0)')
    path.write_bytes(data)
    print(path)

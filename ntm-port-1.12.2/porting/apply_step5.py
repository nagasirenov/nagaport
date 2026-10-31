from pathlib import Path

p = Path("src/main/java/com/hbm/tileentity/machine/rbmk/TileEntityRBMKConsole.java")
data = p.read_bytes()
data = data.replace(b"import net.minecraft.util.math.Vec3d;",
                    b"import net.minecraft.util.math.MathHelper;\r\nimport net.minecraft.util.text.TextFormatting;\r\nimport net.minecraft.util.math.Vec3d;")
data = data.replace(b"data.func_150296_c()", b"data.getKeySet()")
data = data.replace(b"MathHelper.clamp_double(data.getDouble(\"level\"), 0, 1)",
                    b"MathHelper.clamp(data.getDouble(\"level\"), 0.0, 1.0)")
p.write_bytes(data)
print("Updated TileEntityRBMKConsole")

p = Path("src/main/java/com/hbm/world/gen/util/LogicBlockActions.java")
data = p.read_bytes()
data = data.replace(b"import net.minecraft.util.text.TextComponentString;",
                    b"import net.minecraft.util.text.TextComponentString;\r\nimport net.minecraft.util.text.TextFormatting;")
p.write_bytes(data)
print("Updated LogicBlockActions")

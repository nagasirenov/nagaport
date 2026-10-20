import net.minecraft.client.Minecraft;import net.minecraft.client.gui.GuiScreen;import net.minecraft.util.ResourceLocation;import net.minecraftforge.fml.relauncher.Side;import net.minecraftforge.fml.relauncher.SideOnly;
public final class RepresentativeMethods extends GuiScreen {
@Override
	public void initGui() {
		mc.getSoundHandler().playSound(com.hbm.sound.GuiSound.create(new ResourceLocation("hbm:block.bobble"), 1.0F));
	}
public void click() { mc.getSoundHandler().playSound(com.hbm.sound.GuiSound.create(new ResourceLocation("gui.button.press"), 1.0F)); }
@SideOnly(Side.CLIENT)
	private void playSwing(float pitchProbablyIDontFuckingCare) {
		Minecraft.getMinecraft().getSoundHandler().playSound(com.hbm.sound.GuiSound.create(new ResourceLocation("hbm:weapon.cSwing"), pitchProbablyIDontFuckingCare));
	}
public static void invoke(Minecraft mc){RepresentativeMethods r=new RepresentativeMethods();r.mc=mc;r.initGui();r.click();r.playSwing(.83F);}}

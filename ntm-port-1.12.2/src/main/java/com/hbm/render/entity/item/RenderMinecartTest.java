package com.hbm.render.entity.item;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderMinecart;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

import com.hbm.entity.item.EntityMinecartTest;
import com.hbm.lib.RefStrings;
import com.hbm.render.mesh.MeshModel;
import com.hbm.render.mesh.ObjMeshModel;

public class RenderMinecartTest extends RenderMinecart<EntityMinecartTest>
{
    private static final ResourceLocation objTesterModelRL = new ResourceLocation(RefStrings.MODID, "models/LilBoy1.obj");
    private MeshModel boyModel;
    private ResourceLocation boyTexture;

    public RenderMinecartTest(RenderManager renderManager) {
        super(renderManager);
        this.shadowSize = 0.5F;
        boyModel = new ObjMeshModel(objTesterModelRL);
        boyTexture = new ResourceLocation(RefStrings.MODID, "textures/models/LilBoy2.png");
    }

    public RenderMinecartTest() {
        this(net.minecraft.client.Minecraft.getMinecraft().getRenderManager());
    }

    @Override
    protected void renderCartContents(EntityMinecartTest p_147910_1_, float p_147910_2_, IBlockState state)
    {
        int j = p_147910_1_.getFuseTicks();

        if (j > -1 && j - p_147910_2_ + 1.0F < 10.0F)
        {
            float f1 = 1.0F - (j - p_147910_2_ + 1.0F) / 10.0F;
            f1 = MathHelper.clamp(f1, 0.0F, 1.0F);
            f1 *= f1;
            f1 *= f1;
            float f2 = 1.0F + f1 * 0.3F;
            GlStateManager.scale(f2, f2, f2);
        }

        GlStateManager.pushMatrix();
        bindTexture(boyTexture);
        boyModel.renderAll();
        GlStateManager.popMatrix();

        if (j > -1 && j / 5 % 2 == 0)
        {
            GlStateManager.disableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.DST_ALPHA);
            GlStateManager.color(1.0F, 1.0F, 1.0F, (1.0F - (j - p_147910_2_ + 1.0F) / 100.0F) * 0.8F);
            GlStateManager.pushMatrix();
            bindTexture(boyTexture);
            boyModel.renderAll();
            GlStateManager.popMatrix();
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            GlStateManager.disableBlend();
            GlStateManager.enableLighting();
            GlStateManager.enableTexture2D();
        }
    }
}

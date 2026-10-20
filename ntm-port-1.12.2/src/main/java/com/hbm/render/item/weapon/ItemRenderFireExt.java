package com.hbm.render.item.weapon;

import org.lwjgl.opengl.GL11;

import com.hbm.items.weapon.sedna.ItemGunBaseNT;
import com.hbm.items.weapon.sedna.factory.XFactoryTool;
import com.hbm.items.weapon.sedna.mags.IMagazine;
import com.hbm.main.ResourceManager;

import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import javax.vecmath.AxisAngle4f;
import javax.vecmath.Matrix4f;
import com.hbm.render.item.nativeitem.ItemTransformMatrix;
import com.hbm.render.item.nativeitem.direct.DirectMeshItemRenderer;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms.TransformType;

public class ItemRenderFireExt extends DirectMeshItemRenderer {
	
	public ItemRenderFireExt() { }

    @Override public Matrix4f localTransform(TransformType type, long timeMillis) {
        ItemTransformMatrix matrix = new ItemTransformMatrix();
        switch(type) {
        case FIRST_PERSON_LEFT_HAND:
        case FIRST_PERSON_RIGHT_HAND:
            return matrix.rotateZ(25).translate(.5, -.5, -.5).rotateY(80).scale(.35).build();
        case THIRD_PERSON_LEFT_HAND:
        case THIRD_PERSON_RIGHT_HAND:
            Matrix4f diagonalRotation = new Matrix4f();
            diagonalRotation.set(new AxisAngle4f(0, 1, 1, (float)Math.toRadians(-5)));
            return matrix.scale(.5).rotateZ(20).multiply(diagonalRotation).rotateY(10)
                    .rotateX(15).translate(.75, -2.75, .5).build();
        case GUI:
            return matrix.translate(2, 14, 0).rotateY(-90).rotateX(-135)
                    .rotateY(timeMillis / 10 % 360).scale(4.5, 4.5, -4.5).build();
        default: return matrix.scale(.3).build();
        }
    }

    @Override protected void draw(ItemStack item) {
        GL11.glEnable(GL11.GL_CULL_FACE);
        ItemGunBaseNT gun = (ItemGunBaseNT) item.getItem();
        IMagazine mag = gun.getConfig(item, 0).getReceivers(item)[0].getMagazine(item);
        ResourceLocation tex = ResourceManager.fireext_tex;
        if(mag.getType(item, null) == XFactoryTool.fext_foam) tex = ResourceManager.fireext_foam_tex;
        if(mag.getType(item, null) == XFactoryTool.fext_sand) tex = ResourceManager.fireext_sand_tex;
        Minecraft.getMinecraft().getTextureManager().bindTexture(tex);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        ResourceManager.fireext.renderAll();
        GL11.glShadeModel(GL11.GL_FLAT);
    }
}

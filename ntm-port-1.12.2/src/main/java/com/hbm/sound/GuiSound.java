package com.hbm.sound;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

/** NTM resource-backed, non-positional one-shot sounds used by screens and local UI effects. */
public final class GuiSound extends PositionedSoundRecord {

    private GuiSound(ResourceLocation location, float pitch) {
        super(location, SoundCategory.MASTER, 0.25F, pitch, false, 0,
                ISound.AttenuationType.NONE, 0.0F, 0.0F, 0.0F);
    }

    public static PositionedSoundRecord create(ResourceLocation location, float pitch) {
        // The original vanilla GUI click was renamed. HBM and other resource names
        // stay resource-backed: client playback must not require SoundEvent registration.
        if(location != null && "minecraft".equals(location.getNamespace())
                && "gui.button.press".equals(location.getPath())) {
            location = new ResourceLocation("minecraft", "ui.button.click");
        }
        return new GuiSound(location, pitch);
    }

    @Override
    public SoundCategory getCategory() {
        return SoundCategoryResolver.get(positionedSoundLocation);
    }

    // Forge playback observers run before the native sample accessor is populated.
    // Keep the original raw values available there; native playback later multiplies
    // by the selected sample, as the original SoundManager did.
    @Override
    public float getVolume() { return sound == null ? volume : super.getVolume(); }

    @Override
    public float getPitch() { return sound == null ? pitch : super.getPitch(); }
}

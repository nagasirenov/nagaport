package com.hbm.sound;

import com.hbm.lib.RefStrings;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.Sound;
import net.minecraft.client.audio.SoundEventAccessor;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

/** Only the newly ported, explicitly allowlisted world-effect records, never arbitrary mod sounds or loops. */
@Mod.EventBusSubscriber(modid = RefStrings.MODID, value = Side.CLIENT)
public final class WorldSoundCategories {
    private WorldSoundCategories() { }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlaySound(PlaySoundEvent event) {
        ISound sound = event.getResultSound();
        // Respect cancellation and replacements made by the existing NTM handlers
        // (including vacuum suppression), and do not wrap custom/subclassed sounds.
        if(sound != event.getSound() || sound == null || sound.getClass() != PositionedSoundRecord.class
            || !com.hbm.main.ModSoundEvents.isWorldEffect(sound.getSoundLocation())) return;
        event.setResultSound(new CategorizedSound(sound));
    }

    private static final class CategorizedSound implements ISound {
        private final ISound delegate;
        private CategorizedSound(ISound delegate) { this.delegate = delegate; }
        public ResourceLocation getSoundLocation() { return delegate.getSoundLocation(); }
        public SoundEventAccessor createAccessor(SoundHandler handler) { return delegate.createAccessor(handler); }
        public Sound getSound() { return delegate.getSound(); }
        public SoundCategory getCategory() { return SoundCategoryResolver.get(getSoundLocation()); }
        public boolean canRepeat() { return delegate.canRepeat(); }
        public int getRepeatDelay() { return delegate.getRepeatDelay(); }
        public float getVolume() { return delegate.getVolume(); }
        public float getPitch() { return delegate.getPitch(); }
        public float getXPosF() { return delegate.getXPosF(); }
        public float getYPosF() { return delegate.getYPosF(); }
        public float getZPosF() { return delegate.getZPosF(); }
        public AttenuationType getAttenuationType() { return delegate.getAttenuationType(); }
    }
}

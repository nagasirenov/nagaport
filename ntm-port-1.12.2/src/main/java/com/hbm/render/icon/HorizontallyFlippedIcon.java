package com.hbm.render.icon;

/** Horizontal atlas-coordinate view for NTM's two mirrored door textures.
 * This is a value view, not a renderer or a replacement Minecraft class.
 */
public final class HorizontallyFlippedIcon implements IIcon {
    private final IIcon source;

    public HorizontallyFlippedIcon(IIcon source) { this.source = source; }

    @Override public String getIconName() { return source.getIconName(); }
    @Override public int getIconWidth() { return source.getIconWidth(); }
    @Override public int getIconHeight() { return source.getIconHeight(); }
    @Override public float getMinU() { return source.getMaxU(); }
    @Override public float getMaxU() { return source.getMinU(); }
    @Override public float getMinV() { return source.getMinV(); }
    @Override public float getMaxV() { return source.getMaxV(); }
    @Override public float getInterpolatedU(double coordinate) {
        float span = getMaxU() - getMinU();
        return getMinU() + span * ((float) coordinate / 16.0F);
    }
    @Override public float getInterpolatedV(double coordinate) {
        float span = getMaxV() - getMinV();
        return getMinV() + span * ((float) coordinate / 16.0F);
    }
}

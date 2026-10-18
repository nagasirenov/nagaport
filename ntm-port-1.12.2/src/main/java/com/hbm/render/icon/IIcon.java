package com.hbm.render.icon;

/** NTM-owned compatibility contract for legacy icon consumers. */
public interface IIcon {

	String getIconName();

	int getIconWidth();

	int getIconHeight();

	float getMinU();

	float getMaxU();

	float getInterpolatedU(double u);

	float getMinV();

	float getMaxV();

	float getInterpolatedV(double v);
}

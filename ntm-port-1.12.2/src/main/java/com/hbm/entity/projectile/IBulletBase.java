package com.hbm.entity.projectile;

import java.util.List;
import com.hbm.util.Tuple.Pair;
import net.minecraft.util.math.Vec3d;

public interface IBulletBase {
	public double prevX(); public double prevY(); public double prevZ();
	public void prevX(double d); public void prevY(double d); public void prevZ(double d);
	public List<Pair<Vec3d, Double>> nodes();
}

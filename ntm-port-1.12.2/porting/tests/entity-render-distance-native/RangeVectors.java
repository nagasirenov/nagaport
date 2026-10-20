public final class RangeVectors {
 static final double[][] BOXES={
 {0,0,0,1,1,1},{-3,-7,-11,5,17,29},{0,0,0,0,0,0},
 {0,0,0,Double.MIN_VALUE,Double.MIN_VALUE,Double.MIN_VALUE},
 {0,0,0,Double.MIN_NORMAL,1e-200,1e-150},
 {0,0,0,1e100,1e120,1e150},{0,0,0,Double.MAX_VALUE,Double.MAX_VALUE,Double.MAX_VALUE},
 {0,0,0,Double.POSITIVE_INFINITY,1,1},{0,0,0,Double.NaN,1,1},
 {Double.NEGATIVE_INFINITY,0,0,Double.POSITIVE_INFINITY,1,1},
 {0,0,0,0.1,0.2,0.3},{1e15,-1e15,1e15,1e15+0.125,-1e15+0.25,1e15+0.5}
 };
 static double[] distances(double threshold){return new double[]{Double.NEGATIVE_INFINITY,-Double.MAX_VALUE,-1,-Double.MIN_VALUE,-0.0,0.0,Double.MIN_VALUE,1,10000,Math.nextDown(threshold),threshold,Math.nextUp(threshold),Double.MAX_VALUE,Double.POSITIVE_INFINITY,Double.NaN};}
}

import java.lang.reflect.*;
import java.net.*;
/** Original obfuscated client is isolated from the production native-model process. */
public final class LegacyCRTRotationTest {
 public static void main(String[] args)throws Exception {
  try(URLClassLoader legacy=new URLClassLoader(new URL[]{new java.io.File(args[0]).toURI().toURL()},LegacyCRTRotationTest.class.getClassLoader())){
   Class<?> vec=Class.forName("azw",true,legacy);Method create=vec.getMethod("a",double.class,double.class,double.class),z=vec.getMethod("c",float.class),y=vec.getMethod("b",float.class);Field xField=vec.getField("a"),yField=vec.getField("b"),zField=vec.getField("c");
   float[] angles={0F,(float)Math.PI,(float)(Math.PI*.5),90F/180F*(float)Math.PI,270F/180F*(float)Math.PI,180F/180F*(float)Math.PI,.73F,1.31F,-.7F};java.util.Random random=new java.util.Random(7101122);int checks=0;
   for(float yaw:angles)for(float pitch:angles)for(int i=0;i<32;i++){
    float[] point={random.nextFloat()*4-2,random.nextFloat()*4-2,random.nextFloat()*4-2};Object original=create.invoke(null,(double)point[0],(double)point[1],(double)point[2]);z.invoke(original,pitch);y.invoke(original,yaw);double[] expected=CRTNativeTest.rotate(point,yaw,pitch),actual={xField.getDouble(original),yField.getDouble(original),zField.getDouble(original)};
    for(int axis=0;axis<3;axis++){if(actual[axis]!=expected[axis])throw new AssertionError("Original Vec3 lookup-table rotation differs at "+yaw+","+pitch+" axis "+axis+": "+actual[axis]+" != "+expected[axis]);checks++;}
   }
   System.out.println("PASS actual cached original 1.7.10 Vec3 Z-then-Y rotations: "+checks+" exact double comparisons; float MathHelper table inputs, native 1.12 helper matches original signs/precision.");
  }
 }
}

final class OriginalAtlasOracle {
static final String[] groups={"Top","Bottom","Front","Back","Left","Right"};
static float yaw(int meta,boolean item){if(item)return (float)Math.PI;float rotation = 0;

		if(meta == 2)
			rotation = 90F / 180F * (float) Math.PI;

		if(meta == 3)
			rotation = 270F / 180F * (float) Math.PI;

		if(meta == 4)
			rotation = 180F / 180F * (float)Math.PI;

		return rotation;}
static class Normal {double xCoord,yCoord,zCoord;Normal(double[] n){xCoord=n[0];yCoord=n[1];zCoord=n[2];}}
static class Color {int value=0xffffffff;void setColorOpaque(int r,int g,int b){r=Math.max(0,Math.min(255,r));g=Math.max(0,Math.min(255,g));b=Math.max(0,Math.min(255,b));value=0xff000000|(b<<16)|(g<<8)|r;}void setColorOpaque_F(float r,float g,float b){setColorOpaque((int)(r*255F),(int)(g*255F),(int)(b*255F));}}
static int color(double[] n,boolean shadow,boolean whole){Normal normal=new Normal(n);Color tes=new Color();boolean hasColor=false;int red=255,green=255,blue=255;if(shadow || hasColor) {
				
				float brightness = 1.0F;
				
				if(shadow) {
					brightness = ((float)normal.yCoord * 0.3F + 0.7F) - (float)Math.abs(normal.xCoord) * 0.1F + (float)Math.abs(normal.zCoord) * 0.1F;
	
					if(brightness < 0.45F)
						brightness = 0.45F;
				}

				if(hasColor) {
					tes.setColorOpaque((int)(red * brightness), (int)(green * brightness), (int)(blue * brightness));
				} else {
					tes.setColorOpaque_F(brightness, brightness, brightness);
				}
			}

			return tes.value;}}

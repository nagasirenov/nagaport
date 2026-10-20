#!/usr/bin/env python3
"""Real offscreen Mesa OpenGL rasterization of native renderer streams.

Tests only draw-order equivalence, not old-vs-new AO light parity. Context/state
are controlled EGL desktop OpenGL, not a live Minecraft frame or shader pack.
No production source contributes geometry: streams come from native renderers.
"""
import ctypes as C, json, math, struct, sys, os
os.environ.setdefault("MESA_SHADER_CACHE_DISABLE", "true")
from ctypes.util import find_library
E=C.CDLL(find_library('EGL'));G=C.CDLL(find_library('GL'))
def fn(lib,name,result,*args):
 f=getattr(lib,name);f.restype=result;f.argtypes=args;return f
ptr=C.c_void_p;integer=C.c_int;uint=C.c_uint;flt=C.c_float;dbl=C.c_double
getproc=fn(E,'eglGetProcAddress',ptr,C.c_char_p)
getdisplay=C.CFUNCTYPE(ptr,uint,ptr,C.POINTER(integer))(getproc(b'eglGetPlatformDisplayEXT'))
display=getdisplay(0x31DD,None,None)
major=integer();minor=integer()
assert fn(E,'eglInitialize',uint,ptr,C.POINTER(integer),C.POINTER(integer))(display,C.byref(major),C.byref(minor)), 'EGL initialization unavailable'
assert fn(E,'eglBindAPI',uint,uint)(0x30A2), 'Desktop OpenGL unavailable'
attrs=(integer*17)(0x3033,1,0x3040,8,0x3024,8,0x3023,8,0x3022,8,0x3021,8,0x3025,24,0x3038,0,0)
config=ptr();count=integer();assert fn(E,'eglChooseConfig',uint,ptr,C.POINTER(integer),C.POINTER(ptr),integer,C.POINTER(integer))(display,attrs,C.byref(config),1,C.byref(count)) and count.value
size=128
surface=fn(E,'eglCreatePbufferSurface',ptr,ptr,ptr,C.POINTER(integer))(display,config,(integer*5)(0x3057,size,0x3056,size,0x3038))
context=fn(E,'eglCreateContext',ptr,ptr,ptr,ptr,C.POINTER(integer))(display,config,None,(integer*1)(0x3038))
assert surface and context and fn(E,'eglMakeCurrent',uint,ptr,ptr,ptr,ptr)(display,surface,surface,context), 'OpenGL pbuffer context unavailable'
for name,args in {
 'glEnable':(uint,),'glDisable':(uint,),'glDepthFunc':(uint,),'glCullFace':(uint,),'glFrontFace':(uint,),
 'glAlphaFunc':(uint,flt),'glViewport':(integer,integer,integer,integer),'glClearColor':(flt,flt,flt,flt),'glClear':(uint,),
 'glMatrixMode':(uint,),'glLoadIdentity':(),'glFrustum':(dbl,dbl,dbl,dbl,dbl,dbl),'glRotated':(dbl,dbl,dbl,dbl),'glTranslated':(dbl,dbl,dbl),
 'glBegin':(uint,),'glEnd':(),'glColor4f':(flt,flt,flt,flt),'glTexCoord2f':(flt,flt),'glVertex3f':(flt,flt,flt),'glFinish':(),
 'glTexParameteri':(uint,uint,integer),'glBindTexture':(uint,uint),'glTexImage2D':(uint,integer,integer,integer,integer,integer,uint,uint,ptr),
 'glReadPixels':(integer,integer,integer,integer,uint,uint,ptr),'glPixelStorei':(uint,integer)
}.items():fn(G,name,None,*args)
getstring=fn(G,'glGetString',C.c_char_p,uint);error=fn(G,'glGetError',uint)
print('OpenGL draw context:',getstring(0x1F02).decode(),getstring(0x1F01).decode(),flush=True)
data=json.load(open(sys.argv[1]));alpha={((p&0xffffffff)>>24) for p in data['pixels']};assert alpha=={0,255},'Original texture no longer binary-alpha'
# Same alpha-test/depth-write, no-blending state used for native CUTOUT_MIPPED.
G.glViewport(0,0,size,size);G.glEnable(0x0B71);G.glDepthFunc(0x0203);G.glEnable(0x0B44);G.glCullFace(0x0405);G.glFrontFace(0x0901);G.glDisable(0x0BE2);G.glEnable(0x0BC0);G.glAlphaFunc(0x0204,.5);G.glEnable(0x0DE1);G.glBindTexture(0x0DE1,1);G.glPixelStorei(0x0CF5,1)
pixels=bytearray(512*128*4)
for y in range(data['height']):
 for x in range(data['width']):
  p=data['pixels'][y*data['width']+x]&0xffffffff;i=((32+y)*512+64+x)*4;pixels[i:i+4]=bytes([(p>>16)&255,(p>>8)&255,p&255,p>>24])
buf=(C.c_ubyte*len(pixels)).from_buffer_copy(pixels)
G.glTexImage2D(0x0DE1,0,0x1908,512,128,0,0x1908,0x1401,buf)
G.glTexParameteri(0x0DE1,0x2802,0x812F);G.glTexParameteri(0x0DE1,0x2803,0x812F)
def floatbits(x):return struct.unpack('f',struct.pack('I',x&0xffffffff))[0]
def draw(vertices,camera,global_passes):
 G.glClearColor(.13,.19,.29,1);G.glClear(0x4000|0x100)
 G.glMatrixMode(0x1701);G.glLoadIdentity();near=.01;f=near*math.tan(math.radians(55)/2);G.glFrustum(-f,f,-f,f,near,100)
 G.glMatrixMode(0x1700);G.glLoadIdentity();distance,yaw,pitch=camera;G.glTranslated(0,0,-distance);G.glRotated(pitch,1,0,0);G.glRotated(yaw,0,1,0);G.glTranslated(-.5,-.5,-.5)
 # Native stream buckets are [inner four, outer four]. Regroup without changing
 # coordinates/material/light to isolate only the original global pass ordering.
 quads=[vertices[i:i+4] for i in range(0,len(vertices),4)]
 if global_passes:quads=quads[::2]+quads[1::2]
 G.glBegin(7)
 for quad in quads:
  for v in quad:
   c=v[3]&0xffffffff;light=v[6]&0xffffffff
   # Native lightmap coordinates participate through a deterministic nonuniform
   # lightmap surrogate. Both compared orders use the exact same native values.
   l=.35+.65*max(light&0xffff,(light>>16)&0xffff)/240
   G.glColor4f((c&255)/255*l,((c>>8)&255)/255*l,((c>>16)&255)/255*l,(c>>24)/255)
   G.glTexCoord2f(floatbits(v[4]),floatbits(v[5]));G.glVertex3f(floatbits(v[0])-8,floatbits(v[1])-64,floatbits(v[2])-8)
 G.glEnd();G.glFinish();rgba=(C.c_ubyte*(size*size*4))();depth=(C.c_float*(size*size))();G.glReadPixels(0,0,size,size,0x1908,0x1401,rgba);G.glReadPixels(0,0,size,size,0x1902,0x1406,depth);assert error()==0,'OpenGL error';return bytes(rgba),bytes(depth)
fn(G,"glGenerateMipmap",None,uint)(0x0DE1)
cameras=[(12,35,25),(40,135,-25),(2.5,0,0),(2.5,35,25),(2.5,135,-25),(2.5,225,65),(2.5,315,-70),(.0,0,0),(.0,45,0),(.0,135,35),(.0,225,-45),(.0,315,70),(.8,0,0),(.8,44,31)]
checks=0;nonempty=0
for filter_ in [0x2600,0x2601,0x2702]:
 G.glTexParameteri(0x0DE1,0x2801,filter_);G.glTexParameteri(0x0DE1,0x2800,0x2600 if filter_==0x2702 else filter_)
 for case in data['cases']:
  for camera in cameras:
   a=draw(case['vertices'],camera,False);b=draw(case['vertices'],camera,True)
   assert a==b,('Framebuffer differs for native per-face versus legacy global order',case['forge'],case['smooth'],case['asymmetric'],case['mask'],camera,filter_)
   nonempty+=len(set(a[0][i:i+4] for i in range(0,len(a[0]),4)))>1
   checks+=1
assert nonempty>0,'All rasterized views were empty'
print('PASS actual OpenGL RGBA8+depth framebuffer equality:',checks,'comparisons;',nonempty,'nonempty views; native Forge/vanilla flat/smooth, uniform/asymmetric light, ten masks, outside/inside/near cameras, nearest+linear+mipmapped filters. Original binary-alpha CUTOUT_MIPPED with depth/culling and no blending.',flush=True)
fn(E,'eglMakeCurrent',uint,ptr,ptr,ptr,ptr)(display,None,None,None);fn(E,'eglDestroyContext',uint,ptr,ptr)(display,context);fn(E,'eglDestroySurface',uint,ptr,ptr)(display,surface);fn(E,'eglTerminate',uint,ptr)(display)

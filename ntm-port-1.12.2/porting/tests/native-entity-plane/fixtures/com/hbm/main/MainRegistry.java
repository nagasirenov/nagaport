package com.hbm.main;
import com.hbm.sound.AudioWrapper;
/** Test-only mod-instance input and audio observation sink, never a replacement for Minecraft/Forge. */
public final class MainRegistry {
 public static final Object instance=new Object(); public static final Proxy proxy=new Proxy();
 public static final class Proxy {
  public int requests; public AudioWrapper last;
  public AudioWrapper getLoopedSound(String name,float x,float y,float z,float volume,float range,float pitch,int delay){requests++;return last=new AudioWrapper(name);}
 }
}

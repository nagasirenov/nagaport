package com.hbm.sound;
/** Observation sink only. This harness does not play audio or test its engine. */
public final class AudioWrapper {
 public final String sound; public boolean playing; public int starts, keeps, positions, stops;
 public AudioWrapper(String name) { sound=name; }
 public boolean isPlaying(){return playing;}
 public void startSound(){playing=true;starts++;}
 public void keepAlive(){keeps++;}
 public void updatePosition(float x,float y,float z){positions++;}
 public void stopSound(){playing=false;stops++;}
}

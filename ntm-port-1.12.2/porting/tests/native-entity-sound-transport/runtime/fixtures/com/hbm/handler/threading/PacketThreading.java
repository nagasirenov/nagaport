package com.hbm.handler.threading;
/** Test-only boundary: unrelated executor/config/logger machinery is not under test. */
public final class PacketThreading {
    public static final String threadPrefix = "NTM-Packet-Thread-";
    public static java.util.concurrent.locks.ReentrantLock lock = new java.util.concurrent.locks.ReentrantLock();
}

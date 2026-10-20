package com.hbm.packet;
import com.hbm.main.NetworkHandler;
/** Project bootstrap boundary only: the real dispatcher initializes this exact type.
 * Unrelated packet registrations are omitted; the tested packet keeps discriminator33. */
public final class PacketDispatcher {
    public static final NetworkHandler wrapper = new NetworkHandler("hbm");
}

import java.lang.reflect.*;
import java.util.*;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.management.PlayerList;
import net.minecraft.network.play.server.SPacketCustomSound;
import net.minecraft.util.SoundCategory;

/** Exact sleep arithmetic from both engine methods, plus real native recipient filtering. */
public final class PlayerSoundCoordinateProbe {
    static int checks;
    static void check(boolean ok,String text){checks++;if(!ok)throw new AssertionError(text);}
    static double oldSleep(int bedY){return (double)((float)bedY+0.9375F)-(double)0.2F;}
    static double newSleep(int bedY){return (double)((float)bedY+0.6875F);}
    public static void main(String[] args)throws Exception {
        for(int bedY=0;bedY<=255;bedY++){double oldY=oldSleep(bedY),newY=newSleep(bedY);check(Double.doubleToRawLongBits(oldY)!=Double.doubleToRawLongBits(newY),"raw sleep origins differ "+bedY);check((int)(oldY*8D)==(int)(newY*8D),"vanilla-height packet Y equal "+bedY);}
        for(int bedY=-256;bedY<0;bedY++){check((int)(oldSleep(bedY)*8D)==(int)(newSleep(bedY)*8D),"negative test-range packet Y equal "+bedY);}
        check((int)(oldSleep(16777216)*8D)!=(int)(newSleep(16777216)*8D),"not blanket out-of-range packet equality");
        net.minecraft.init.Bootstrap.register();
        NativeWorldSoundCallbackProbe.DispatchPlayerList players=NativeWorldSoundCallbackProbe.alloc(NativeWorldSoundCallbackProbe.DispatchPlayerList.class);
        EntityPlayerMP recipient=NativeWorldSoundCallbackProbe.alloc(EntityPlayerMP.class);recipient.posY=80.7D;recipient.dimension=7;
        NativeWorldSoundCallbackProbe.CaptureConnection connection=NativeWorldSoundCallbackProbe.alloc(NativeWorldSoundCallbackProbe.CaptureConnection.class);recipient.connection=connection;
        NativeWorldSoundCallbackProbe.set(players,PlayerList.class,"playerEntityList",new ArrayList<>(Arrays.asList(recipient)));
        SPacketCustomSound packet=new SPacketCustomSound("entityaudit:sleep",SoundCategory.MASTER,0,oldSleep(64),0,1,1);
        players.sendToAllNearExcept(null,0,oldSleep(64),0,16D,7,packet);check(connection.sends==1,"old raw sleep origin includes recipient at Y=80.7");connection.sends=0;
        players.sendToAllNearExcept(null,0,newSleep(64),0,16D,7,packet);check(connection.sends==0,"native raw sleep origin excludes same recipient despite identical packet source Y");
        System.out.println("PASS player coordinate probe: "+checks+" checks. Valid 0..255 bed Y values all preserve packet Y but differ raw; tested -256..-1 also preserve truncated packet Y. Actual native PlayerList recipient differs at source bedY=64 and recipientY=80.7. Old="+oldSleep(64)+", native="+newSleep(64)+". Arithmetic derived from exact javap; sleep methods themselves not executed.");
    }
}

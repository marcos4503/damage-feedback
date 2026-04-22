package xyz.windsoft.damagefeedback.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;
import xyz.windsoft.damagefeedback.Main;

/*
 * This class manage the Packets registration, for example, how the Packets will encode/decode their data, process the request, etc. Packets
 * can't be sended from Client to Server (and vice-versa), if was not registered here.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [X] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class ModPacketHandler {

    //Private static final variables
    private static final String PROTOCOL_VERSION = "1";

    //Public static final variables
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(new ResourceLocation(Main.MODID, "main"), () -> { return PROTOCOL_VERSION; }, PROTOCOL_VERSION::equals, PROTOCOL_VERSION::equals);

    public static void Register(){
        //Register the "Server To Client Damage Feedback Packet" packet...
        INSTANCE.messageBuilder(ServerToClient_DamageFeedbackPacket.class, 1001)
                .encoder(ServerToClient_DamageFeedbackPacket::Encode)
                .decoder(ServerToClient_DamageFeedbackPacket::new)
                .consumerMainThread(ServerToClient_DamageFeedbackPacket::Handle)
                .add();
    }

    public static void SendToServer(Object msg){
        //Send the message from Client to Server
        INSTANCE.send(PacketDistributor.SERVER.noArg(), msg);
    }

    public static void SendToPlayer(Object msg, ServerPlayer serverPlayer){
        //Send the message from Server to specific Player
        INSTANCE.send(PacketDistributor.PLAYER.with(() -> { return serverPlayer; }), msg);
    }

    public static void SendToAllClients(Object msg){
        //Send the message from Server to all Clients
        INSTANCE.send(PacketDistributor.ALL.noArg(), msg);
    }
}
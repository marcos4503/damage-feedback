package xyz.windsoft.damagefeedback.network;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import xyz.windsoft.damagefeedback.utils.*;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/*
 * This class is a Packet, and store data that will be created in Server/Client and sended to Server/Client, to be handled. Once the
 * Server/Client receive this Packet, the side get the data and handles it with a code.
 *
 * This Packet is CREATED in:
 * - Server
 * This Packet is HANDLED in:
 * - Client
 * What this Packet does?
 * - This Packet is created by the Server to send a Signal to the Client, of the Damage caused by a Player.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class ServerToClient_DamageFeedbackPacket {

    //Private final encodable data to store in this Packet
    private long existingDamageTypesBitmask;
    private boolean isTargetKilled;

    //Public methods

    public ServerToClient_DamageFeedbackPacket(long existingDamageTypesBitmask, boolean isTargetKilled){
        //This constructor is runned in two scenarios:
        //- On the Client/Server side, when this Packet is created, to be sended to Client/Server.
        //- On the Client/Server side, when the Client/Server receive this Packet, right after the next constructor runs, decoding the data, and then, calling this constructor, to store the decoded data. After this, the "Handle" method is runned.

        //Get the data to be stored in this Packet...
        this.existingDamageTypesBitmask = existingDamageTypesBitmask;
        this.isTargetKilled = isTargetKilled;
    }

    public ServerToClient_DamageFeedbackPacket(FriendlyByteBuf decodeBuffer){
        //This constructor is runned in one scenario:
        //- On the Client/Server side, when the Client/Server receive this Packet, so, this constructor is called by the Forge, informing a "decodeBuffer" to be decoded, containing the encoded data that was sended by the Client/Server.

        //Decode each data sended by the Client or Server side, that was created this Packet, and call the previous constructor, informing all the decoded data.
        //NOTE: The decoding order should be the SAME ORDER of the variables from the previous constructor.
        this(decodeBuffer.readLong(), decodeBuffer.readBoolean());
    }

    public void Encode(FriendlyByteBuf encodeBuffer){
        //This method is called automatically by the Forge, to encode this Packet data, right before send this Packet to the Client/Server side, that will receive this.

        //Encode each data that will be sended by the Client or Server side. So the side that will receive this Packet will decode this and handle it.
        //NOTE: The decoding order should be the SAME ORDER of the variables from the FIRST constructor.
        encodeBuffer.writeLong(existingDamageTypesBitmask);
        encodeBuffer.writeBoolean(this.isTargetKilled);
    }

    public void Handle(Supplier<NetworkEvent.Context> contextSupplier){
        //This method is called automatically by the Forge, on the Client/Server side that receives this Packet. This method is called after the Decoding of this Packet data,
        //on the side that receive this.

        //Get the context
        NetworkEvent.Context context = contextSupplier.get();

        //Get the direction of packet, info
        NetworkDirection directionOfThisPacket = context.getDirection();

        //Run this code in the Main Thread of target side
        context.enqueueWork(() -> {
            //Reset the Cross Damage Feedback and increase Combo Count
            DamageCrossManager.getInstance().SetDamageCrossTotalTime(0.500f);
            DamageCrossManager.getInstance().SetDamageCrossRemaingTime(0.500f);
            if (DamageCrossManager.getInstance().GetDamageComboCount() < 5)
                DamageCrossManager.getInstance().IncreaseComboCount();

            //If was killed the entity, reset the Skull Damage Feedback (also, set a high combo count)
            if (isTargetKilled == true){
                if (DamageCrossManager.getInstance().GetDamageComboCount() < 4)
                    DamageCrossManager.getInstance().SetDamageComboCount(4);
                DamageSkullManager.getInstance().ResetSkullRender();
            }

            //System.out.println("Damage Feedback. Damage Types: " + Arrays.stream(DamageTypeHandler.GetListOfDamageTypesExistingInBitmask(existingDamageTypesBitmask)).map(Enum::name).collect(Collectors.joining(", ", "[", "]")));

            //Play a Hitmark sound
            SoundClientPlayer.PlayHitmarkSound();
            //If is a Arrow, Trident or Firework, play a Arrow sound
            if (DamageTypeHandler.isDamageTypeExistingInBitmask(existingDamageTypesBitmask, DamageType.Arrow) == true)
                SoundClientPlayer.PlayProjectileSound();
            if (DamageTypeHandler.isDamageTypeExistingInBitmask(existingDamageTypesBitmask, DamageType.Trident) == true)
                SoundClientPlayer.PlayProjectileSound();
            if (DamageTypeHandler.isDamageTypeExistingInBitmask(existingDamageTypesBitmask, DamageType.Firework) == true)
                SoundClientPlayer.PlayProjectileSound();

            //Inform that the packet was handled now
            context.setPacketHandled(true);
        });
    }
}
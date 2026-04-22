package xyz.windsoft.damagefeedback.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.windsoft.damagefeedback.network.ModPacketHandler;
import xyz.windsoft.damagefeedback.network.ServerToClient_DamageFeedbackPacket;
import xyz.windsoft.damagefeedback.utils.DamageType;
import xyz.windsoft.damagefeedback.utils.DamageTypeHandler;

/*
 * This class do actions when a Player change or have their Game Mode changed.
 * This class is used to detect when a Player was changed to Spectator, after being attacked by some Player. This logic
 * helps to include Minigames, where the Victim is not killed because they are moved to Spectator. This ensure that the
 * attacker receives the Feedback of the kill.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnPlayerChangeGameMode {

    //Public events

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onPlayerChangeGameMode(PlayerEvent.PlayerChangeGameModeEvent event) {
        //If the entity is null, stop here
        if (event.getEntity() == null)
            return;

        //If not is the logical server, stop here
        if (event.getEntity().level().isClientSide() == true)
            return;



        //Get the Server data
        ServerPlayer victimServerPlayer = ((ServerPlayer)event.getEntity());
        GameType oldGameMode = event.getCurrentGameMode();
        GameType newGameMode = event.getNewGameMode();

        //If the new Game Mode is not a Spectator, means that the Player still alive
        if (newGameMode != GameType.SPECTATOR)
            return;

        //Load the NBT data of the victim...
        CompoundTag nbtData = victimServerPlayer.getPersistentData();
        //If was found a custom information about the last attacker...
        if (nbtData.contains("df_lastPlayerAttackerUuid") == true)
            if ((victimServerPlayer.level().getGameTime() - nbtData.getLong("df_lastPlayerAttackerTime")) <= 600){   //<- If the last attacker was attacked in the last 600 ticks (30s), consider this Player to send the Feedback...
                //Try to load the unofficial last attacker Player reference
                ServerPlayer unofficialLastAttackerServerPlayer = victimServerPlayer.getServer().getPlayerList().getPlayer(nbtData.getUUID("df_lastPlayerAttackerUuid"));

                //If the unofficial last attacker Player, was found...
                if (unofficialLastAttackerServerPlayer != null){
                    //Get the extra information, about the Damage Taken
                    boolean isVictimDead = true;
                    long existingDamageTypesBitmask = DamageTypeHandler.AddDamageTypeToBitmask(0l, DamageType.GenericOrMelee);

                    //Send a Packet to do the Feedback to the unofficial last attacker Player that was caused the Damage
                    ModPacketHandler.SendToPlayer(new ServerToClient_DamageFeedbackPacket(existingDamageTypesBitmask, isVictimDead), unofficialLastAttackerServerPlayer);
                }
            }
    }
}
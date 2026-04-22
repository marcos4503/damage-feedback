package xyz.windsoft.damagefeedback.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownPotion;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/*
 * This class do actions when a Potion explodes in the Ground.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnPotionImpact {

    //Public events

    @SubscribeEvent
    public void onPotionImpact(ProjectileImpactEvent event) {
        //If the entity is null, stop here
        if (event.getEntity() == null)
            return;

        //If not is the logical server, stop here
        if (event.getEntity().level().isClientSide() == true)
            return;



        //Get the Server data
        Projectile thrownProjectile = event.getProjectile();
        Entity projectileOwner = thrownProjectile.getOwner();

        //Detect if the thrown projectile is a Potion
        ThrownPotion thrownPotion = null;
        if (thrownProjectile instanceof ThrownPotion potion)
            thrownPotion = potion;
        //Detect if the owner is a Player
        ServerPlayer potionOwnerServerPlayer = null;
        if (projectileOwner instanceof ServerPlayer serverPlayer)
            potionOwnerServerPlayer = serverPlayer;

        //If the projectile is not a Potion, or is not binded to a Player, stop here
        if (thrownPotion == null || potionOwnerServerPlayer == null)
            return;

        //Get all LivingEntities near of the Potion explosion area (5 meters)
        List<LivingEntity> affectedLivingEntities = thrownPotion.level().getEntitiesOfClass(LivingEntity.class, thrownPotion.getBoundingBox().inflate(5.0f));
        //Apply a unofficial last attacker custom information on all affected LivingEntities...
        for (int i = 0; i < affectedLivingEntities.size(); i++){
            //Add the data...
            CompoundTag nbtData = affectedLivingEntities.get(i).getPersistentData();
            nbtData.putUUID("df_lastPlayerAttackerUuid", potionOwnerServerPlayer.getUUID());
            nbtData.putLong("df_lastPlayerAttackerTime", affectedLivingEntities.get(i).level().getGameTime());
        }
    }
}
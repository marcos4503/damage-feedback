package xyz.windsoft.damagefeedback.events;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.level.GameType;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import xyz.windsoft.damagefeedback.network.ModPacketHandler;
import xyz.windsoft.damagefeedback.network.ServerToClient_DamageFeedbackPacket;
import xyz.windsoft.damagefeedback.utils.DamageType;
import xyz.windsoft.damagefeedback.utils.DamageTypeHandler;

/*
 * This class do actions When a Living Entity receives a Damage. This uses the "OnLivingDamage" event, to run only
 * after Armor and other Damage reductions, calcs. Run on LOWEST priority to run after all mods that also uses this hook or a similar. This ensure
 * that the Damage Feedback really shows the reality of the Damage that effectively comes to the Victim Entity.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnLivingDamage {

    //Public events

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void OnLivingDamage(LivingDamageEvent event){
        //If the entity is null, stop here
        if (event.getEntity() == null)
            return;

        //If not is the logical server, stop here
        if (event.getEntity().level().isClientSide() == true)
            return;



        //Get the Server data
        DamageSource damageSource = event.getSource();
        LivingEntity victimEntity = event.getEntity();
        LivingEntity victimLastAttackerEntity = victimEntity.getLastAttacker();
        Entity victimCurrentDirectAttackerEntity = damageSource.getEntity();

        //Detect if attacker informations, is binded to a Player
        ServerPlayer victimLastAttackerServerPlayer = null;
        if (victimLastAttackerEntity instanceof ServerPlayer serverPlayer)
            victimLastAttackerServerPlayer = serverPlayer;
        ServerPlayer victimCurrentDirectAttackerServerPlayer = null;
        if (victimCurrentDirectAttackerEntity instanceof ServerPlayer serverPlayer)
            victimCurrentDirectAttackerServerPlayer = serverPlayer;

        //If the current direct attacker or official last attacker is a known Player...
        if (victimLastAttackerServerPlayer != null || victimCurrentDirectAttackerServerPlayer != null){
            //Get the extra information, about the Damage Taken
            float damageReceived = event.getAmount();
            boolean isVictimDead = (damageReceived >= (victimEntity.getHealth() + victimEntity.getAbsorptionAmount()));
            long existingDamageTypesBitmask = GetExistingDamageTypesBitmask(damageSource);

            //If the current direct attacker is known...
            if (victimCurrentDirectAttackerServerPlayer != null){
                //Insert in the victim, the custom information of the last attacker, and timestamp of attack
                CompoundTag nbtData = victimEntity.getPersistentData();
                nbtData.putUUID("df_lastPlayerAttackerUuid", victimCurrentDirectAttackerServerPlayer.getUUID());
                nbtData.putLong("df_lastPlayerAttackerTime", victimEntity.level().getGameTime());
                //Send a Packet to do the Feedback to Player that was caused the Damage
                ModPacketHandler.SendToPlayer(new ServerToClient_DamageFeedbackPacket(existingDamageTypesBitmask, isVictimDead), victimCurrentDirectAttackerServerPlayer);
            }
            //If the current direct attacker is unknown, but the official last attacker is known, send a Packet to the last attacker, to do the Feedback to Player that was caused the last Damage
            if (victimCurrentDirectAttackerServerPlayer == null && victimLastAttackerServerPlayer != null)
                ModPacketHandler.SendToPlayer(new ServerToClient_DamageFeedbackPacket(existingDamageTypesBitmask, isVictimDead), victimLastAttackerServerPlayer);
        }
        //If the current direct attacker and the official last attacker is also unknown...
        if (victimLastAttackerServerPlayer == null && victimCurrentDirectAttackerServerPlayer == null){
            //Load the NBT data of the victim...
            CompoundTag nbtData = victimEntity.getPersistentData();

            //If was found a custom information about the last attacker...
            if (nbtData.contains("df_lastPlayerAttackerUuid") == true)
                if ((victimEntity.level().getGameTime() - nbtData.getLong("df_lastPlayerAttackerTime")) <= 600){ //<- If the last attacker was attacked in the last 600 ticks (30s), consider this Player to send the Feedback...
                    //Try to load the unofficial last attacker Player reference
                    ServerPlayer unofficialLastAttackerServerPlayer = victimEntity.getServer().getPlayerList().getPlayer(nbtData.getUUID("df_lastPlayerAttackerUuid"));

                    //If the unofficial last attacker Player, was found...
                    if (unofficialLastAttackerServerPlayer != null){
                        //Get the extra information, about the Damage Taken
                        float damageReceived = event.getAmount();
                        boolean isVictimDead = (damageReceived >= (victimEntity.getHealth() + victimEntity.getAbsorptionAmount()));
                        long existingDamageTypesBitmask = GetExistingDamageTypesBitmask(damageSource);

                        //Send a Packet to do the Feedback to the unofficial last attacker Player that was caused the Damage
                        ModPacketHandler.SendToPlayer(new ServerToClient_DamageFeedbackPacket(existingDamageTypesBitmask, isVictimDead), unofficialLastAttackerServerPlayer);
                    }
                }
        }
    }

    //Private auxiliar methods

    private long GetExistingDamageTypesBitmask(DamageSource damageSource){
        //Prepare the Bitmask to return
        long toReturn = 0l;

        //Collect the additional data
        Entity directEntity = damageSource.getDirectEntity();
        Entity ownerEntity =  damageSource.getEntity();

        //Try to detect Types of Damage, that this Damage can fit in, and add the Types detected, to the Bitmask
        if (damageSource.is(DamageTypes.PLAYER_ATTACK) == true || damageSource.is(DamageTypes.MOB_ATTACK) == true || damageSource.is(DamageTypes.MOB_ATTACK_NO_AGGRO) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.GenericOrMelee);
        if (damageSource.is(DamageTypes.ARROW) == true || (directEntity instanceof AbstractArrow) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.Arrow);
        if (damageSource.is(DamageTypes.TRIDENT) == true || (directEntity instanceof ThrownTrident) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.Trident);
        if (damageSource.is(DamageTypeTags.IS_PROJECTILE) == true || (directEntity instanceof ThrowableProjectile) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.ThrownAnyProjectile);
        if (damageSource.is(DamageTypes.STING) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.BeeSting);
        if (damageSource.is(DamageTypes.THORNS) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.ArmorThorn);
        if ((directEntity instanceof Projectile && ownerEntity instanceof Mob) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.MobAnyProjectile);
        if (damageSource.is(DamageTypes.MAGIC) == true || damageSource.is(DamageTypeTags.WITCH_RESISTANT_TO) == true || damageSource.typeHolder().unwrapKey().get().location().getPath().equals("magic") == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.Magic);
        if (damageSource.is(DamageTypes.INDIRECT_MAGIC) == true || damageSource.typeHolder().unwrapKey().get().location().getPath().equals("indirect_magic") == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.IndirectMagic);
        if (damageSource.is(DamageTypes.WITHER) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.WitherDecomposition);
        if (damageSource.is(DamageTypes.DRAGON_BREATH) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.DragonBreath);
        if (damageSource.is(DamageTypes.SONIC_BOOM) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.SonicBoom);
        if (damageSource.is(DamageTypeTags.IS_EXPLOSION) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.Explosion);
        if (damageSource.is(DamageTypeTags.IS_EXPLOSION) == true && ownerEntity instanceof ServerPlayer)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.ExplosionByPlayer);
        if (damageSource.is(DamageTypes.FIREWORKS) == true || (directEntity instanceof FireworkRocketEntity) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.Firework);
        if (damageSource.is(DamageTypes.FLY_INTO_WALL) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.FlyCrashIntoWall);
        if (damageSource.is(DamageTypes.FALL) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.Fall);
        if (damageSource.is(DamageTypes.IN_FIRE) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.InsideFire);
        if (damageSource.is(DamageTypes.ON_FIRE) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.BurningOutOfFire);
        if (damageSource.is(DamageTypes.LAVA) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.Lava);
        if (damageSource.is(DamageTypes.HOT_FLOOR) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.HotFloor);
        if (damageSource.is(DamageTypes.DROWN) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.Drowning);
        if (damageSource.is(DamageTypes.STARVE) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.Starve);
        if (damageSource.is(DamageTypes.CACTUS) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.Cactus);
        if (damageSource.is(DamageTypes.FREEZE) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.Freezing);
        if (damageSource.is(DamageTypes.SWEET_BERRY_BUSH) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.SweetBerryBush);
        if (damageSource.is(DamageTypes.STALAGMITE) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.PiercingStalagmite);
        if (damageSource.is(DamageTypes.FALLING_STALACTITE) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.FallingStalactite);
        if (damageSource.is(DamageTypes.FALLING_BLOCK) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.FallingBlock);
        if (damageSource.is(DamageTypes.FALLING_ANVIL) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.FallingAnvil);
        if (damageSource.is(DamageTypes.LIGHTNING_BOLT) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.LightningBolt);
        if (damageSource.is(DamageTypes.CRAMMING) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.SqueezedLackOfSpace);
        if (damageSource.is(DamageTypes.DRY_OUT) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.DryOut);
        if (damageSource.is(DamageTypes.FELL_OUT_OF_WORLD) == true)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.InsideVoid);
        //If was not detect any Damage Type, add the Type of Unknown to the Bitmask
        if (DamageTypeHandler.GetCountOfDamageTypesExisingInBitmask(toReturn) == 0)
            toReturn = DamageTypeHandler.AddDamageTypeToBitmask(toReturn, DamageType.Unknown);

        //Return the Bitmask
        return toReturn;
    }
}
package xyz.windsoft.damagefeedback.utils;

import java.util.ArrayList;
import java.util.List;

/*
 * This class has useful methods and utils for handling Damage Types in this mod.
 * This class is useful because the Server and Client knowns Damage Types existing in attacks, by using Bitmasks.
 *
 * Information about side that this Class will run:
 * [X] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class DamageTypeHandler {

    //Public static methods

    public static long AddDamageTypeToBitmask(long sourceBitmask, DamageType damageType){
        //Prepare the Bitmask to Return
        long toReturn = sourceBitmask;

        //Add the Damage Type ID in the Bitmask, setting the Bit "1" in the Index of the Damage Type ID...
        toReturn |= (1l << GetDamageTypeID(damageType));

        //Return the Bitmask
        return toReturn;
    }

    public static int GetCountOfDamageTypesExisingInBitmask(long sourceBitmask){
        //Prepare the value to return
        int toReturn = 0;

        //Check, each Bit of the Bitmask
        for (int i = 0; i < 64; i++)
            if ((sourceBitmask & (1l << i)) != 0)
                toReturn += 1;

        //Return the value
        return toReturn;
    }

    public static DamageType[] GetListOfDamageTypesExistingInBitmask(long sourceBitmask){
        //Prepare the value to return
        List<DamageType> toReturn = new ArrayList<>();

        //Check, each Bit of the Bitmask, and get the Damage Type of each enabled Bit
        for (int i = 0; i < 64; i++)
            if ((sourceBitmask & (1l << i)) != 0)
                toReturn.add(GetDamageTypeValue(i));

        //Return the value
        return toReturn.stream().toArray(DamageType[]::new);
    }

    public static boolean isDamageTypeExistingInBitmask(long sourceBitmask, DamageType damageType){
        //Prepare the value to return
        boolean toReturn = false;

        //Check if the Bit in the index of the Damage Type ID, is equal to "1"
        toReturn = ((sourceBitmask & (1l << GetDamageTypeID(damageType))) != 0);

        //Return the value
        return toReturn;
    }

    public static long RemoveDamageTypeOfBitmask(long sourceBitmask, DamageType damageType){
        //Prepare the Bitmask to Return
        long toReturn = sourceBitmask;

        //Remove the Damage Type ID of the Bitmask, setting the Bit "0" in the Index of the Damage Type ID...
        toReturn &= ~(1l << GetDamageTypeID(damageType));

        //Return the Bitmask
        return toReturn;
    }

    //Private static methods

    private static int GetDamageTypeID(DamageType damageType){
        //Prepare the value to return
        int toReturn = 0;

        //Do the conversion
        toReturn = switch (damageType) {
            case Unknown -> 0;
            case GenericOrMelee -> 1;
            case Arrow -> 2;
            case Trident -> 3;
            case ThrownAnyProjectile -> 4;
            case BeeSting -> 5;
            case ArmorThorn -> 6;
            case MobAnyProjectile -> 7;
            case Magic -> 8;
            case IndirectMagic -> 9;
            case WitherDecomposition -> 10;
            case DragonBreath -> 11;
            case SonicBoom -> 12;
            case Explosion -> 13;
            case ExplosionByPlayer -> 14;
            case Firework -> 15;
            case FlyCrashIntoWall -> 16;
            case Fall -> 17;
            case InsideFire -> 18;
            case BurningOutOfFire -> 19;
            case Lava -> 20;
            case HotFloor -> 21;
            case Drowning -> 22;
            case Starve -> 23;
            case Cactus -> 24;
            case Freezing -> 25;
            case SweetBerryBush -> 26;
            case PiercingStalagmite -> 27;
            case FallingStalactite -> 28;
            case FallingBlock -> 29;
            case FallingAnvil -> 30;
            case LightningBolt -> 31;
            case SqueezedLackOfSpace -> 32;
            case DryOut -> 33;
            case InsideVoid -> 34;
            default -> 0;
        };

        //Return the value
        return toReturn;
    }

    private static DamageType GetDamageTypeValue(int damageTypeID){
        //Prepare the value to return
        DamageType toReturn = DamageType.Unknown;

        //Do the conversion
        toReturn = switch (damageTypeID) {
            case  0 -> DamageType.Unknown;
            case  1 -> DamageType.GenericOrMelee;
            case  2 -> DamageType.Arrow;
            case  3 -> DamageType.Trident;
            case  4 -> DamageType.ThrownAnyProjectile;
            case  5 -> DamageType.BeeSting;
            case  6 -> DamageType.ArmorThorn;
            case  7 -> DamageType.MobAnyProjectile;
            case  8 -> DamageType.Magic;
            case  9 -> DamageType.IndirectMagic;
            case 10 -> DamageType.WitherDecomposition;
            case 11 -> DamageType.DragonBreath;
            case 12 -> DamageType.SonicBoom;
            case 13 -> DamageType.Explosion;
            case 14 -> DamageType.ExplosionByPlayer;
            case 15 -> DamageType.Firework;
            case 16 -> DamageType.FlyCrashIntoWall;
            case 17 -> DamageType.Fall;
            case 18 -> DamageType.InsideFire;
            case 19 -> DamageType.BurningOutOfFire;
            case 20 -> DamageType.Lava;
            case 21 -> DamageType.HotFloor;
            case 22 -> DamageType.Drowning;
            case 23 -> DamageType.Starve;
            case 24 -> DamageType.Cactus;
            case 25 -> DamageType.Freezing;
            case 26 -> DamageType.SweetBerryBush;
            case 27 -> DamageType.PiercingStalagmite;
            case 28 -> DamageType.FallingStalactite;
            case 29 -> DamageType.FallingBlock;
            case 30 -> DamageType.FallingAnvil;
            case 31 -> DamageType.LightningBolt;
            case 32 -> DamageType.SqueezedLackOfSpace;
            case 33 -> DamageType.DryOut;
            case 34 -> DamageType.InsideVoid;
            default -> DamageType.Unknown;
        };

        //Return the value
        return toReturn;
    }
}
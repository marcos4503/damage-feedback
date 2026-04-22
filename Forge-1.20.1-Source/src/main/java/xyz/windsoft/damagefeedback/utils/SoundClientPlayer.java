package xyz.windsoft.damagefeedback.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import xyz.windsoft.damagefeedback.sounds.ModSounds;

/*
 * This class store data/state of the Client Sound Player.
 * The main goal of this Class, is to avoid many sound play at same time.
 *
 * Information about side that this Class will run:
 * [X] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class SoundClientPlayer {

    //Private static constants
    private static int MIN_TIME_TO_PLAY_SOUND = 50;

    //Private static variables
    private static long lastPlayedHitmarkSoundTimeMs = 0;
    private static long lastPlayedProjectileSoundTimeMs = 0;

    //Public static methods

    public static void PlayHitmarkSound(){
        //Get current time
        long currentTime = System.currentTimeMillis();

        //If has passed desired time...
        if ((currentTime - lastPlayedHitmarkSoundTimeMs) >= MIN_TIME_TO_PLAY_SOUND){
            //Play the Hitmark sound
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.HIT_FEEDBACK_3.get(), 1.1f, 0.2f));
            //Inform the last played sound time in ms
            lastPlayedHitmarkSoundTimeMs = currentTime;
        }
    }

    public static void PlayProjectileSound(){
        //Get current time
        long currentTime = System.currentTimeMillis();

        //If has passed desired time...
        if ((currentTime - lastPlayedProjectileSoundTimeMs) >= MIN_TIME_TO_PLAY_SOUND){
            //Play the Arrow Hit sound
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.ARROW_HIT_PLAYER, 1.0f, 0.12f));
            //Inform the last played sound time in ms
            lastPlayedProjectileSoundTimeMs = currentTime;
        }
    }
}
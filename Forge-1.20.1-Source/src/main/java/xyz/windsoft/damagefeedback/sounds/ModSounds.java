package xyz.windsoft.damagefeedback.sounds;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import xyz.windsoft.damagefeedback.Main;

/*
 * This class is responsible by the registering of sounds of this mod
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [X] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class ModSounds {

    //Public static final variables
    public static final DeferredRegister<SoundEvent> SOUNDS_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, Main.MODID);

    //Public static variables
    public static RegistryObject<SoundEvent> HIT_FEEDBACK_1 = null;
    public static RegistryObject<SoundEvent> HIT_FEEDBACK_2 = null;
    public static RegistryObject<SoundEvent> HIT_FEEDBACK_3 = null;

    //Public static methods

    public static void Register(IEventBus eventBus){
        //Register the deferred register for sounds events of this mod in the event bus of Forge
        SOUNDS_EVENTS.register(eventBus);

        //Register the "Hit Feedback 1" Sound Event...
        HIT_FEEDBACK_1 = SOUNDS_EVENTS.register("hit_feedback_1", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Main.MODID, "hit_feedback_1")));
        //Register the "Hit Feedback 2" Sound Event...
        HIT_FEEDBACK_2 = SOUNDS_EVENTS.register("hit_feedback_2", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Main.MODID, "hit_feedback_2")));
        //Register the "Hit Feedback 3" Sound Event...
        HIT_FEEDBACK_3 = SOUNDS_EVENTS.register("hit_feedback_3", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation(Main.MODID, "hit_feedback_3")));
    }
}
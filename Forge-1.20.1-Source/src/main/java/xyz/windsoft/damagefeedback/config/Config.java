package xyz.windsoft.damagefeedback.config;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import net.minecraftforge.registries.ForgeRegistries;
import xyz.windsoft.damagefeedback.Main;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/*
 * This class handle the mod configuration using the Forge Configuration API
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [X] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

@Mod.EventBusSubscriber(modid = Main.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {

    //Private static constant variables
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    //Private static constant variables, that is the configs available to the user
    private static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> NORMAL_RETICLE_FEEDBACK_RGB_COLOR = BUILDER
            .comment("Is the color of the Feedback on normal damage. A list of channels R, G and B color. (Type of INT_ARRAY. For each INT item: Range of 1~255.)")
            .defineListAllowEmpty("normalReticleFeedbackRgbColor", List.of(255, 255, 255), Config::ValidateRgbColors);
    private static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> CRIT_RETICLE_FEEDBACK_RGB_COLOR = BUILDER
            .comment("Is the color of the Feedback on accumulated damage. A list of channels R, G and B color. (Type of INT_ARRAY. For each INT item: Range of 1~255.)")
            .defineListAllowEmpty("critReticleFeedbackRgbColor", List.of(255, 0, 0), Config::ValidateRgbColors);
    private static final ForgeConfigSpec.ConfigValue<List<? extends Integer>> SKULL_RETICLE_FEEDBACK_RGB_COLOR = BUILDER
            .comment("Is the color of the Feedback of elimination. A list of channels R, G and B color. (Type of INT_ARRAY. For each INT item: Range of 1~255.)")
            .defineListAllowEmpty("skullReticleFeedbackRgbColor", List.of(255, 0, 0), Config::ValidateRgbColors);

    //Public static constant variables
    public static final ForgeConfigSpec SPEC = BUILDER.build();

    //Public static variables
    public static List<Integer> normalReticleFeedbackRgbColor = null;
    public static List<Integer> critReticleFeedbackRgbColor = null;
    public static List<Integer> skullReticleFeedbackRgbColor = null;

    //Private static methods

    private static boolean ValidateRgbColors(final Object obj) {
        //Return true if the item is INT
        return (obj instanceof final Integer itemName && true);
    }

    //Public events

    @SubscribeEvent
    public static void onLoad(final ModConfigEvent event) {
        //Get the configs loaded from file
        normalReticleFeedbackRgbColor = NORMAL_RETICLE_FEEDBACK_RGB_COLOR.get().stream().map(val -> (int) val).collect(Collectors.toList());
        critReticleFeedbackRgbColor = CRIT_RETICLE_FEEDBACK_RGB_COLOR.get().stream().map(val -> (int) val).collect(Collectors.toList());
        skullReticleFeedbackRgbColor = SKULL_RETICLE_FEEDBACK_RGB_COLOR.get().stream().map(val -> (int) val).collect(Collectors.toList());
    }
}
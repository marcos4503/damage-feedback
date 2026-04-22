package xyz.windsoft.damagefeedback;

import com.mojang.logging.LogUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;
import xyz.windsoft.damagefeedback.config.Config;
import xyz.windsoft.damagefeedback.events.*;
import xyz.windsoft.damagefeedback.network.ModPacketHandler;
import xyz.windsoft.damagefeedback.sounds.ModSounds;

/*
 * This class is the Entry Point for this mod
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Main.MODID)
public class Main
{
    //Public classes
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        //Can use "@Mod.EventBusSubscriber" to automatically register all static methods in the class annotated with @SubscribeEvent...

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            // Some client setup code
            LOGGER.info("Damage Feedback mod starting on client... >> {}", Minecraft.getInstance().getUser().getName());
        }
    }

    //Public static variables
    public static final String MODID = "damagefeedback";
    private static final Logger LOGGER = LogUtils.getLogger();

    //Public methods

    public Main() {
        //Get the mod event bus
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        //Register the mod needed events, in forge mod event bus
        MinecraftForge.EVENT_BUS.register(new OnLivingDamage());
        MinecraftForge.EVENT_BUS.register(new OnPotionImpact());
        MinecraftForge.EVENT_BUS.register(new OnRightClickFire());
        MinecraftForge.EVENT_BUS.register(new OnPlayerChangeGameMode());
        MinecraftForge.EVENT_BUS.register(new OnRenderGui());

        //Start the register of the needed mod sounds events
        ModSounds.Register(modEventBus);

        //Register the "CommonSetup" method for modloading
        modEventBus.addListener(this::CommonSetup);

        //Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        //Register the mod ForgeConfigSpec, for Forge can create and load the config file
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        //Can use "@SubscribeEvent" and let the Event Bus discover methods to call...

        //Do something when the server starts
        LOGGER.info("Damage Feedback mod starting on server...");
    }

    //Private methods

    private void CommonSetup(final FMLCommonSetupEvent event) {
        //Do enqueued work
        event.enqueueWork(() -> {
            //Start the register of the mod custom packets
            ModPacketHandler.Register();
        });

        //Some common setup code
        LOGGER.info("Damage Feedback mod starting!");
        LOGGER.info("Configs loaded...");
        LOGGER.info("normalReticleFeedbackRgbColor: " + Config.normalReticleFeedbackRgbColor.toString());
        LOGGER.info("critReticleFeedbackRgbColor: " + Config.critReticleFeedbackRgbColor.toString());
        LOGGER.info("skullReticleFeedbackRgbColor: " + Config.skullReticleFeedbackRgbColor.toString());
    }
}
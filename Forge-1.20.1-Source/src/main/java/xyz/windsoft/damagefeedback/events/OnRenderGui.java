package xyz.windsoft.damagefeedback.events;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import xyz.windsoft.damagefeedback.Main;
import xyz.windsoft.damagefeedback.config.Config;
import xyz.windsoft.damagefeedback.utils.DamageCrossManager;
import xyz.windsoft.damagefeedback.utils.DamageSkullManager;

import java.util.ArrayList;
import java.util.List;

/*
 * This class manages the HUD renderization of Feedback Damage.
 *
 * Information about side that this Class will run:
 * [X] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnRenderGui {

    //Private static final variables
    private static final ResourceLocation DAMAGE_FEEDBACK_TEXTURE = new ResourceLocation(Main.MODID, "textures/gui/damage_feedback.png");

    //Private cache variables
    private Font minecraftFont = null;
    private double lastNanoTime = 0;
    private double frameDeltaTime = 0.0d;

    //Public events

    @SubscribeEvent
    public void onRenderGui(RenderGuiOverlayEvent.Post event) {
        //If not is the logical client, stop here
        if (FMLEnvironment.dist != Dist.CLIENT)
            return;

        //If the renderization layer is different from "HOTBAR", cancel here (run the Damage Feedback logic in the Hotbar layer, because some mods stop the Crosshair layer on some situations, like Vic's Point Blank and similars)
        if (event.getOverlay().id().equals(VanillaGuiOverlay.HOTBAR.id()) == false)
            return;



        //Get the Minecraft Font on cache...
        if (minecraftFont == null)
            minecraftFont = Minecraft.getInstance().font;
        //Get the GUI Graphics and data useful
        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = event.getWindow().getGuiScaledWidth();
        int screenHeight = event.getWindow().getGuiScaledHeight();

        //Process the Delta Time
        double currentNanoTime = System.nanoTime();
        frameDeltaTime = ((currentNanoTime - lastNanoTime) / 1_000_000_000.0f);
        lastNanoTime = currentNanoTime;

        //If have a Cross Damage Feedback available, anime it
        if (DamageCrossManager.getInstance().GetDamageCrossRemaingTime() > 0.0f){
            //Calculate the alpha of the Cross Damage Feedback, using the animation time as reference
            float targetAlfa = (DamageCrossManager.getInstance().GetDamageCrossRemaingTime() / DamageCrossManager.getInstance().GetDamageCrossTotalTime());
            float targetAlfaCube = (targetAlfa * targetAlfa * targetAlfa);
            //Render the Cross Damage Feedback
            if (DamageCrossManager.getInstance().GetDamageComboCount() < 5)
                RenderSystem.setShaderColor(((float)Config.normalReticleFeedbackRgbColor.get(0) / 255.0f), ((float)Config.normalReticleFeedbackRgbColor.get(1) / 255.0f), ((float)Config.normalReticleFeedbackRgbColor.get(2) / 255.0f), targetAlfaCube);
            if (DamageCrossManager.getInstance().GetDamageComboCount() >= 5)
                RenderSystem.setShaderColor(((float)Config.critReticleFeedbackRgbColor.get(0) / 255.0f), ((float)Config.critReticleFeedbackRgbColor.get(1) / 255.0f), ((float)Config.critReticleFeedbackRgbColor.get(2) / 255.0f), targetAlfaCube);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            guiGraphics.blit(DAMAGE_FEEDBACK_TEXTURE, (int)((float)(screenWidth - 81) / 2.0f), (int)((float)(screenHeight - 81) / 2.0f), (405 - (DamageCrossManager.getInstance().GetDamageComboCount() * 81)), 0, 81, 81, 512, 512);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();

            //Decrease the Cross Damage Feedback animation time
            DamageCrossManager.getInstance().DecreaseDamageCrossRemaingTime((float)frameDeltaTime);

            //If the Cross Damage Feedback animation time is less than zero, fix it
            if (DamageCrossManager.getInstance().GetDamageCrossRemaingTime() < 0.0f)
                DamageCrossManager.getInstance().SetDamageCrossRemaingTime(0.0f);
        }
        //Reset the Combo Count, if the animation was finished
        if (DamageCrossManager.getInstance().GetDamageCrossRemaingTime() <= 0.0f)
            DamageCrossManager.getInstance().SetDamageComboCount(-1);

        //Render the Skull frame that the Damage Skull Manager is requiring
        if (DamageSkullManager.getInstance().GetSkullFrameToRender() > -1){
            float skullAlpha = DamageSkullManager.getInstance().GetSkullAlphaToRender();
            RenderSystem.setShaderColor(((float)Config.skullReticleFeedbackRgbColor.get(0) / 255.0f), ((float)Config.skullReticleFeedbackRgbColor.get(1) / 255.0f), ((float)Config.skullReticleFeedbackRgbColor.get(2) / 255.0f), skullAlpha);
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            if (DamageSkullManager.getInstance().GetSkullFrameToRender() == 12)
                guiGraphics.blit(DAMAGE_FEEDBACK_TEXTURE, (int)((float)(screenWidth - 32) / 2.0f), (int)((float)(screenHeight - 30) / 2.0f), 192, 118, 32, 30, 512, 512);
            if (DamageSkullManager.getInstance().GetSkullFrameToRender() == 11)
                guiGraphics.blit(DAMAGE_FEEDBACK_TEXTURE, (int)((float)(screenWidth - 32) / 2.0f), (int)((float)(screenHeight - 30) / 2.0f), 160, 118, 32, 30, 512, 512);
            if (DamageSkullManager.getInstance().GetSkullFrameToRender() == 10)
                guiGraphics.blit(DAMAGE_FEEDBACK_TEXTURE, (int)((float)(screenWidth - 32) / 2.0f), (int)((float)(screenHeight - 30) / 2.0f), 128, 118, 32, 30, 512, 512);
            if (DamageSkullManager.getInstance().GetSkullFrameToRender() == 9)
                guiGraphics.blit(DAMAGE_FEEDBACK_TEXTURE, (int)((float)(screenWidth - 32) / 2.0f), (int)((float)(screenHeight - 30) / 2.0f), 96, 118, 32, 30, 512, 512);
            if (DamageSkullManager.getInstance().GetSkullFrameToRender() == 8)
                guiGraphics.blit(DAMAGE_FEEDBACK_TEXTURE, (int)((float)(screenWidth - 32) / 2.0f), (int)((float)(screenHeight - 30) / 2.0f), 64, 118, 32, 30, 512, 512);
            if (DamageSkullManager.getInstance().GetSkullFrameToRender() == 7)
                guiGraphics.blit(DAMAGE_FEEDBACK_TEXTURE, (int)((float)(screenWidth - 32) / 2.0f), (int)((float)(screenHeight - 30) / 2.0f), 32, 118, 32, 30, 512, 512);
            if (DamageSkullManager.getInstance().GetSkullFrameToRender() == 6)
                guiGraphics.blit(DAMAGE_FEEDBACK_TEXTURE, (int)((float)(screenWidth - 32) / 2.0f), (int)((float)(screenHeight - 30) / 2.0f), 0, 118, 32, 30, 512, 512);
            if (DamageSkullManager.getInstance().GetSkullFrameToRender() == 5)
                guiGraphics.blit(DAMAGE_FEEDBACK_TEXTURE, (int)((float)(screenWidth - 32) / 2.0f), (int)((float)(screenHeight - 30) / 2.0f), 32, 118, 32, 30, 512, 512);
            if (DamageSkullManager.getInstance().GetSkullFrameToRender() == 4)
                guiGraphics.blit(DAMAGE_FEEDBACK_TEXTURE, (int)((float)(screenWidth - 32) / 2.0f), (int)((float)(screenHeight - 30) / 2.0f), 64, 118, 32, 30, 512, 512);
            if (DamageSkullManager.getInstance().GetSkullFrameToRender() == 3)
                guiGraphics.blit(DAMAGE_FEEDBACK_TEXTURE, (int)((float)(screenWidth - 32) / 2.0f), (int)((float)(screenHeight - 30) / 2.0f), 96, 118, 32, 30, 512, 512);
            if (DamageSkullManager.getInstance().GetSkullFrameToRender() == 2)
                guiGraphics.blit(DAMAGE_FEEDBACK_TEXTURE, (int)((float)(screenWidth - 32) / 2.0f), (int)((float)(screenHeight - 30) / 2.0f), 128, 118, 32, 30, 512, 512);
            if (DamageSkullManager.getInstance().GetSkullFrameToRender() == 1)
                guiGraphics.blit(DAMAGE_FEEDBACK_TEXTURE, (int)((float)(screenWidth - 32) / 2.0f), (int)((float)(screenHeight - 30) / 2.0f), 160, 118, 32, 30, 512, 512);
            if (DamageSkullManager.getInstance().GetSkullFrameToRender() == 0)
                guiGraphics.blit(DAMAGE_FEEDBACK_TEXTURE, (int)((float)(screenWidth - 32) / 2.0f), (int)((float)(screenHeight - 30) / 2.0f), 192, 118, 32, 30, 512, 512);
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
            RenderSystem.disableBlend();
        }
        //Do update of the Skull Damage Feedback
        if (DamageSkullManager.getInstance().GetSkullFrameToRender() > -1)
            DamageSkullManager.getInstance().DoUpdateOnFrame();
        if (DamageSkullManager.getInstance().GetSkullFrameToRender() > -1)
            DamageSkullManager.getInstance().DoUpdateOnAlpha();
    }
}
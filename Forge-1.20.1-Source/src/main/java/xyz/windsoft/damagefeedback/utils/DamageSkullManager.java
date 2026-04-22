package xyz.windsoft.damagefeedback.utils;

/*
 * This class store data/state of the Skull of Damage Feedback on Reticle.
 * This class is a Singleton used by the OnRenderGUI.
 *
 * Information about side that this Class will run:
 * [X] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class DamageSkullManager {

    //Private final static variables
    private static final DamageSkullManager INSTANCE = new DamageSkullManager();

    //Public static getters
    public static DamageSkullManager getInstance(){ return INSTANCE; }

    //Private cache variables
    private long lastFrameDecrementTime = 0;
    private double lastNanoTime = 0;
    private double frameDeltaTime = 0.0d;

    //Private variables
    private int skullFrameDurationInMs = 0;
    private float skullFrameToRender = -1;
    private float skullAlphaDecayTotalTime = 0.0f;
    private float skullAlphaDecayRemaingTime = 0.0f;
    private float skullAlphaToRender = 1.0f;

    //Public methods

    public void DoUpdateOnFrame(){
        //Get current time
        long currentTime = System.currentTimeMillis();

        //If the elapsed time, since the last combo increase, is equal or greather than Xms, decrease the frame to render
        if ((currentTime - lastFrameDecrementTime) >= skullFrameDurationInMs){
            //Decrease the frame to render
            if (this.skullFrameToRender > 0)
                this.skullFrameToRender -= 1;
            //Reset the alpha timer
            lastNanoTime = System.nanoTime();
            //Inform the new last frame decrement time
            this.lastFrameDecrementTime = currentTime;
        }
    }

    public void DoUpdateOnAlpha(){
        //If is not in the frame 0, stop here...
        if (this.skullFrameToRender > 0)
            return;

        //Process the Delta Time
        double currentNanoTime = System.nanoTime();
        frameDeltaTime = ((currentNanoTime - lastNanoTime) / 1_000_000_000.0f);
        lastNanoTime = currentNanoTime;

        //Calculate the alpha to render
        float targetAlfa = (skullAlphaDecayRemaingTime / skullAlphaDecayTotalTime);
        skullAlphaToRender = (targetAlfa * targetAlfa * targetAlfa);

        //Decrease remaing time
        skullAlphaDecayRemaingTime -= frameDeltaTime;

        //If have ended the remaing time, set the frame to -1
        if (skullAlphaDecayRemaingTime <= 0.0f)
            this.skullFrameToRender = -1;
    }

    public float GetSkullFrameToRender(){
        //Return the skull frame to render
        return skullFrameToRender;
    }

    public float GetSkullAlphaToRender(){
        //Return the skull alpha to render
        return skullAlphaToRender;
    }

    public void ResetSkullRender(){
        //Reset the skull frame to render
        lastFrameDecrementTime = System.currentTimeMillis();
        lastNanoTime = 0;
        frameDeltaTime = 0.0d;
        skullFrameDurationInMs = 25;
        skullFrameToRender = 12;
        skullAlphaDecayTotalTime = 0.350f;
        skullAlphaDecayRemaingTime = 0.350f;
        skullAlphaToRender = 1.0f;
    }
}
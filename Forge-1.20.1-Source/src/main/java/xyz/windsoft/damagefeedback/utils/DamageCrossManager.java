package xyz.windsoft.damagefeedback.utils;

/*
 * This class store data/state of the Cross of Damage Feedback on Reticle.
 * This class is a Singleton used by the OnRenderGUI.
 *
 * Information about side that this Class will run:
 * [X] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [ ] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class DamageCrossManager {

    //Private final static variables
    private static final DamageCrossManager INSTANCE = new DamageCrossManager();

    //Public static getters
    public static DamageCrossManager getInstance(){ return INSTANCE; }

    //Private cache variables
    private long lastComboIncrementTime = 0;

    //Private variables
    private float damageCrossTotalTime = 0;
    private float damageCrossRemaingTime = 0;
    private int damageComboCount = -1;

    //Public methods

    public float GetDamageCrossTotalTime(){
        //Return the damage cross total time
        return damageCrossTotalTime;
    }

    public void SetDamageCrossTotalTime(float newValue){
        //Set the damage cross total time
        this.damageCrossTotalTime = newValue;
    }

    public float GetDamageCrossRemaingTime(){
        //Returnt the damage cross remaing time
        return damageCrossRemaingTime;
    }

    public void SetDamageCrossRemaingTime(float newValue){
        //Set the damage cross remaing time
        this.damageCrossRemaingTime = newValue;
    }

    public void DecreaseDamageCrossRemaingTime(float decreaseTime){
        //Decrease time in the damage cross remaing time
        damageCrossRemaingTime -= decreaseTime;
    }

    public int GetDamageComboCount(){
        //Return the damage combo count
        return damageComboCount;
    }

    public void SetDamageComboCount(int newValue){
        //Set the damage combo count
        this.damageComboCount = newValue;
    }

    public void IncreaseComboCount(){
        //Get current time
        long currentTime = System.currentTimeMillis();

        //If the elapsed time, since the last combo increase, is equal or greather than 25ms, increase the combo
        if ((currentTime - lastComboIncrementTime) >= 25){
            //Increase the combo count
            this.damageComboCount += 1;
            //Inform the new last combo increment time
            this.lastComboIncrementTime = currentTime;
        }
    }
}
package xyz.windsoft.damagefeedback.events;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;

/*
 * This class do actions when a Player put fire in a Block.
 *
 * Information about side that this Class will run:
 * [ ] Only in Client at all - [ ] Only in Server at all - [ ] Both at all - [X] In Both sides, but some Standard/Events/Overrides Methods run on Client and Server at SAME time AND some Standard/Events/Overrides Methods run ONLY on Client OR Server.
 *                                                                               The Synchronization of some variables/properties from this Class, running in the Server to Clients running this, MAY be needed, according to needs of this Class
 */

public class OnRightClickFire {

    //Public events

    @SubscribeEvent
    public void onRightClickFire(PlayerInteractEvent.RightClickBlock event) {
        //If the entity is null, stop here
        if (event.getEntity() == null)
            return;

        //If not is the logical server, stop here
        if (event.getEntity().level().isClientSide() == true)
            return;



        //Get the Server data
        ItemStack usedItemStack = event.getItemStack();
        Entity ownerOfItemStack = event.getEntity();

        //If the used item stack is not a fire putter, stop here
        if (usedItemStack.is(ItemTags.create(new ResourceLocation("minecraft", "igniters"))) == false && usedItemStack.is(ItemTags.create(new ResourceLocation("forge", "tools/igniters"))) == false
                && usedItemStack.is(Items.FLINT_AND_STEEL) == false && usedItemStack.is(Items.FIRE_CHARGE) == false)
            return;

        //Get the position where the Fire will appear
        BlockPos firePos = event.getPos().relative(event.getFace());
        //Get all LivingEntities near of where the Fire will appear (~1 meters)
        List<LivingEntity> affectedLivingEntities = event.getLevel().getEntitiesOfClass(LivingEntity.class, new AABB(firePos).inflate(0.5f));
        //Apply a unofficial last attacker custom information on all affected LivingEntities...
        for (int i = 0; i < affectedLivingEntities.size(); i++){
            //Add the data...
            CompoundTag nbtData = affectedLivingEntities.get(i).getPersistentData();
            nbtData.putUUID("df_lastPlayerAttackerUuid", ownerOfItemStack.getUUID());
            nbtData.putLong("df_lastPlayerAttackerTime", affectedLivingEntities.get(i).level().getGameTime());
        }
    }
}
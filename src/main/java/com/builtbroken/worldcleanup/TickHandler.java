package com.builtbroken.worldcleanup;

import com.builtbroken.worldcleanup.obj.RemoveBlock;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/14/2016.
 */
public class TickHandler
{
    //TODO add event for chunk updates to priorities player edited chunks
    //TODO updated chunks near players first
    //TODO add registry system
    //TODO add API
    //TODO add config
    //TODO add commands (Add(Block), Remove(Block), Pause(time), start(start this instance only), stop(stop this instance only), disable(disable until enabled), enable Trigger(chunk), disable(world), enable(world)
    //TODO add replace object instead of just remove, eg replace greg furnace with te furnace
    //TODO add conditional removals (Fire on logs, Fire on leaves, Taint in world 3, TE furnace with power over 1M, Tile matching NBT)

    /** Current removal map being processed */
    public HashMap<Integer, Queue<RemoveBlock>> removalMap = new HashMap();

    /** Dump from the thread currently processing the world */
    public HashMap<Integer, Queue<RemoveBlock>> removalMapDump = new HashMap();

    private int ticks = 0;
    public static int blocksRemovedPerTick = 20; //TODO add config, per world TODO accelerate when TPS is high, slow when TPS is low

    @SubscribeEvent
    public void onWorldTickPost(TickEvent.WorldTickEvent event)
    {
        //TODO maybe kill event when there is nothing left to remove? eg all chunks scanned, nothing changes
        if (event.side == Side.SERVER && event.phase == TickEvent.Phase.END)
        {
            //Avoid updating map often as this can lock the main thread every so often
            if (event.world.provider.dimensionId == 0)
            {
                ticks++;
                if (ticks % 1000 == 0)
                {
                    ticks = 0;
                    synchronized (removalMapDump)
                    {
                        for (Map.Entry<Integer, Queue<RemoveBlock>> entry : removalMapDump.entrySet())
                        {
                            if (entry.getValue() != null && !entry.getValue().isEmpty())
                            {
                                if (removalMap.get(entry.getKey()) == null)
                                {
                                    removalMap.put(entry.getKey(), entry.getValue());
                                }
                                else
                                {
                                    removalMap.get(entry.getKey()).addAll(entry.getValue());
                                }
                            }
                        }
                        removalMapDump.clear();
                    }
                }
            }

            if (removalMap.containsKey(event.world.provider.dimensionId))
            {
                Queue<RemoveBlock> list = removalMap.get(event.world.provider.dimensionId);
                for (int i = 0; i < blocksRemovedPerTick && !list.isEmpty(); i++)
                {
                    if (!list.isEmpty())
                    {
                        RemoveBlock block = list.poll();
                        if (block.isValid(event.world))
                        {
                            block.doAction(event.world);
                        }
                    }
                }
                if (list.isEmpty())
                {
                    removalMap.remove(event.world.provider.dimensionId);
                }
            }
        }
    }
}

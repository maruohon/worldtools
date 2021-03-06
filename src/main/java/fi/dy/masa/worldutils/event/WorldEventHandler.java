package fi.dy.masa.worldutils.event;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import fi.dy.masa.worldutils.util.ChunkUtils;

public class WorldEventHandler
{
    @SubscribeEvent
    public void onWorldSaveEvent(WorldEvent.Save event)
    {
        ChunkUtils.instance().writeToDisk(event.getWorld());
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        ChunkUtils.instance().readFromDisk(event.getWorld());
    }
}

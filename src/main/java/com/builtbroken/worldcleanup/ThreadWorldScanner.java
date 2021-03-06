package com.builtbroken.worldcleanup;

import com.builtbroken.worldcleanup.obj.BlockMeta;
import com.builtbroken.worldcleanup.obj.PlaceBlock;
import com.builtbroken.worldcleanup.obj.RemoveBlock;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.DimensionManager;

import java.util.*;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/15/2016.
 */
public final class ThreadWorldScanner extends Thread
{
    /** Delay before re-scanning a chunk that has already been scanned */
    public static int SCAN_DELAY = 2 * 60 * 1000; //10 mins

    /** Checked in all loops in order to kill the thread if false */
    public boolean shouldRun = true;

    /** Current world being scanned, using dim id to be thread safe */
    private int currentScanningWorld = 0;

    /** Map of worlds, to maps of chunks and when they were scanned last( in milli-seconds) */
    private HashMap<Integer, HashMap<ChunkCoordIntPair, Long>> lastScanTimes = new HashMap();

    public ThreadWorldScanner()
    {
        super("WorldCleanup[Scanner]");
    }

    @Override
    public void run()
    {
        while (shouldRun)
        {
            try
            {
                if (world() instanceof WorldServer)
                {
                    WorldServer world = (WorldServer) world();

                    HashMap<ChunkCoordIntPair, Long> lastScanned = lastScanTimes.get(currentScanningWorld);
                    if (lastScanned == null)
                    {
                        lastScanned = new HashMap();
                    }

                    Queue<Chunk> que = new LinkedList();
                    que.addAll(world.theChunkProviderServer.loadedChunks);
                    while (!que.isEmpty() && shouldRun)
                    {
                        Chunk chunk = que.poll();
                        if (chunk != null)
                        {
                            ChunkCoordIntPair pair = chunk.getChunkCoordIntPair();
                            if (!lastScanned.containsKey(pair) || (System.currentTimeMillis() - lastScanned.get(pair)) >= SCAN_DELAY)
                            {
                                lastScanned.put(pair, System.currentTimeMillis());
                                List<RemoveBlock> worldEditList = new ArrayList();
                                try
                                {
                                    if (chunk.isChunkLoaded && chunk.isTerrainPopulated)
                                    {
                                        int[] heightMap = chunk.heightMap;
                                        for (int x = 0; x < 16 && shouldRun; x++)
                                        {
                                            for (int z = 0; z < 16 && shouldRun; z++)
                                            {
                                                int y = heightMap[z << 4 | x];
                                                for (; y >= 0 && shouldRun; y--)
                                                {
                                                    Block block = chunk.getBlock(x, y, z);
                                                    if(block != Blocks.air)
                                                    {
                                                        int meta = chunk.getBlockMetadata(x, y, z);
                                                        BlockMeta blockMeta = new BlockMeta(block, meta);

                                                        if (Plugin.blocksToRemove.contains(block))
                                                        {
                                                            if (Plugin.blockMetaToRemove.containsKey(block))
                                                            {
                                                                List<Integer> metaValues = Plugin.blockMetaToRemove.get(block);
                                                                if (metaValues.contains(meta))
                                                                {
                                                                    worldEditList.add(new RemoveBlock(world, (chunk.xPosition << 4) + x, y, (chunk.zPosition << 4) + z));
                                                                }
                                                            }
                                                            else
                                                            {
                                                                worldEditList.add(new RemoveBlock(world, (chunk.xPosition << 4) + x, y, (chunk.zPosition << 4) + z));
                                                            }
                                                        }
                                                        else if (Plugin.blocksToReplace.containsKey(block))
                                                        {
                                                            worldEditList.add(new PlaceBlock(world, (chunk.xPosition << 4) + x, y, (chunk.zPosition << 4) + z, Plugin.blocksToReplace.get(block)));
                                                        }
                                                        else if (Plugin.blockMetaToReplace.containsKey(blockMeta))
                                                        {
                                                            worldEditList.add(new PlaceBlock(world, (chunk.xPosition << 4) + x, y, (chunk.zPosition << 4) + z, Plugin.blockMetaToReplace.get(block)));
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                catch (Exception e)
                                {
                                    System.out.println("Failed to scan chunk " + chunk);
                                    e.printStackTrace();
                                }
                                synchronized (Plugin.handler.threadInbox)
                                {
                                    if (Plugin.handler.threadInbox.containsKey(currentScanningWorld))
                                    {
                                        Queue d = Plugin.handler.threadInbox.get(currentScanningWorld);
                                        d.addAll(worldEditList); //TODO ensure it doesn't contain block
                                        Plugin.handler.threadInbox.put(currentScanningWorld, d);
                                    }
                                    else
                                    {
                                        Queue d = new LinkedList();
                                        d.addAll(worldEditList); //TODO ensure it doesn't contain block
                                        Plugin.handler.threadInbox.put(currentScanningWorld, d);
                                    }
                                }
                            }
                        }
                    }
                    lastScanTimes.put(currentScanningWorld, lastScanned);
                }
                nextWorld(); //TODO maybe pause? TODO slow down if CPU usage is high TODO stop if dump is too full
            }
            catch (Exception e)
            {
                Plugin.logger().error("Scanned thread has experience an unexpected error, but has recovered", e);
            }
        }
        Plugin.logger().info("Scanner Thread has stopped");
    }

    private World world()
    {
        World world = DimensionManager.getWorld(currentScanningWorld);
        while (world == null)
        {
            nextWorld();
            world = DimensionManager.getWorld(currentScanningWorld);
        }
        return world;
    }

    private void nextWorld()
    {
        //TODO next world in list of worlds
    }

    public void startScanner()
    {
        shouldRun = true;
        if (!isAlive())
        {
            start();
        }
    }

    public void stopScanner()
    {
        shouldRun = false;
    }

    public void kill()
    {
        stopScanner();
        lastScanTimes.clear();
    }
}

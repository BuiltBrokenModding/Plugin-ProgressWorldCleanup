package com.builtbroken.worldcleanup;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Simple plugin to process chunks over time to remove junk
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/14/2016.
 */
@Mod(modid = "progressiveworldcleanup", name = "Progressive World Cleanup", version = "@MAJOR@.@MINOR@.@REVIS@.@BUILD@", acceptableRemoteVersions = "*")
public class Plugin
{
    /** Tick handler for the mod, does all world update actions */
    public static TickHandler handler;
    /** Thread for scanning the world to reduce lag caused by the mod */
    public static ThreadWorldScanner scanner; //TODO piggy back VE if installed

    /** List of blocks to remove, is multi-threaded so ensure thread safe actions */
    public static final List<Block> blocksToRemove = new ArrayList();

    Configuration config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        config = new Configuration(new File(event.getModConfigurationDirectory(), "ProgressiveWorldCleanup.cfg"));
        handler = new TickHandler();
        FMLCommonHandler.instance().bus().register(handler);
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        config.load();
        String removeBlocks = config.getString("BlocksToRemove", Configuration.CATEGORY_GENERAL, "ThaumicTinkerer:fireOrder,ThaumicTinkerer:fireAir,ThaumicTinkerer:fireEarth,ThaumicTinkerer:fireChaos,ThaumicTinkerer:fireFire,ThaumicTinkerer:fireWater,AncientWarfareAutomation:windmill_blade,minecraft:tnt", "Add blocks to the list separated by a ',', any block in the list will be removed from the world over time.");
        if(removeBlocks != null)
        {
            removeBlocks = removeBlocks.trim();
            //TODO remove all spaces
            if(!removeBlocks.isEmpty())
            {
                String[] blocksByName = removeBlocks.split(",");
                for(String s : blocksByName)
                {
                    String name = s.trim();
                    if(!name.isEmpty())
                    {
                        Object object = Block.blockRegistry.getObject(name);
                        if(object != null && object instanceof Block && object != Blocks.air)
                        {
                            blocksToRemove.add((Block) object);
                        }
                        else
                        {
                            System.out.println("Error: " + name + " was not found in the block list");
                        }
                    }
                }
            }
        }
        config.save();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        scanner = new ThreadWorldScanner();
        if(!blocksToRemove.isEmpty())
        {
            scanner.start();
        }
        else
        {
            System.out.println("Progress World Cleanup thread was not started as there are no blocks to remove. Add some to the config and restart for the mod to have an affect.");
        }
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event)
    {
        scanner.shouldRun = false;
    }
}

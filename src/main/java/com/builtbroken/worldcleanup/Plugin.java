package com.builtbroken.worldcleanup;

import com.builtbroken.worldcleanup.command.CommandPWC;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import net.minecraft.block.Block;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
public final class Plugin
{
    /** Tick handler for the mod, does all world update actions */
    public static TickHandler handler;
    /** Thread for scanning the world to reduce lag caused by the mod */
    public static ThreadWorldScanner scanner; //TODO piggy back VE if installed

    /** List of blocks to remove, is multi-threaded so ensure thread safe actions */
    public static final List<Block> blocksToRemove = new ArrayList();

    @Mod.Instance("progressiveworldcleanup")
    public static Plugin instance;

    private Configuration config;
    private Logger logger = LogManager.getLogger("ProgressiveWorldCleanup");

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
        logger.info("Loading blocks from config...");
        config.load();
        TickHandler.blocksRemovedPerTick = config.getInt("BlocksToEditPerTick", Configuration.CATEGORY_GENERAL, TickHandler.blocksRemovedPerTick, 0, 10000, "Number of blocks to edit per tick, there are 20 ticks in a second. Keep this low to improve performance, increase to speed up the effect of the mod.");
        String removeBlocks = config.getString("BlocksToRemove", Configuration.CATEGORY_GENERAL, "ThaumicTinkerer:fireOrder,ThaumicTinkerer:fireAir,ThaumicTinkerer:fireEarth,ThaumicTinkerer:fireChaos,ThaumicTinkerer:fireFire,ThaumicTinkerer:fireWater,AncientWarfareAutomation:windmill_blade,minecraft:tnt", "Add blocks to the list separated by a ',', any block in the list will be removed from the world over time.");
        if (removeBlocks != null)
        {
            removeBlocks = removeBlocks.trim();
            //TODO remove all spaces
            if (!removeBlocks.isEmpty())
            {
                String[] blocksByName = removeBlocks.split(",");
                for (String s : blocksByName)
                {
                    String name = s.trim();
                    if (name.startsWith("@Ore:") || name.startsWith("@ore:"))
                    {
                        String oreName = name.replace("@Ore:", "").replace("@ore:", "");
                        logger.info("\tOreName: " + oreName);

                        List<ItemStack> stacks = OreDictionary.getOres(oreName, false);
                        for(ItemStack stack : stacks)
                        {
                            if(stack != null && stack.getItem() instanceof ItemBlock)
                            {
                                blocksToRemove.add(((ItemBlock) stack.getItem()).field_150939_a);
                                logger.info("\t\tAdded: " + ((ItemBlock) stack.getItem()).field_150939_a);
                            }
                        }
                    }
                    else if (!name.isEmpty())
                    {
                        Object object = Block.blockRegistry.getObject(name);
                        if (object != null && object instanceof Block && object != Blocks.air)
                        {
                            blocksToRemove.add((Block) object);
                            logger.info("\tAdded: " + object);
                        }
                        else
                        {
                            logger.error("\tError: " + name + " was not found in the block list");
                        }
                    }
                }
            }
        }
        config.save();
        logger.info("Done...");
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        ICommandManager commandManager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
        ServerCommandManager serverCommandManager = ((ServerCommandManager) commandManager);
        serverCommandManager.registerCommand(new CommandPWC());

        scanner = new ThreadWorldScanner();
        if (!blocksToRemove.isEmpty())
        {
            scanner.startScanner();
        }
        else
        {
            logger.error("Progress World Cleanup thread was not started as there are no blocks to remove. Add some to the config and restart for the mod to have an affect.");
        }
    }

    @Mod.EventHandler
    public void serverStopping(FMLServerStoppingEvent event)
    {
        scanner.kill();
    }

    public static Logger logger()
    {
        return instance != null ? instance.logger : null;
    }
}

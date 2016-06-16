package com.builtbroken.worldcleanup;

import com.builtbroken.worldcleanup.command.CommandPWC;
import com.builtbroken.worldcleanup.obj.BlockMeta;
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
import java.util.HashMap;
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
    /** Map of blocks to meta values to remove, is multi-threaded so ensure thread safe actions */
    public static final HashMap<Block, List<Integer>> blockMetaToRemove = new HashMap();

    /** Map of block & meta pairs to replace, is multi-threaded so ensure thread safe actions */
    public static final HashMap<Block, BlockMeta> blocksToReplace = new HashMap();
    /** Map of block & meta pairs to replace, is multi-threaded so ensure thread safe actions */
    public static final HashMap<BlockMeta, BlockMeta> blockMetaToReplace = new HashMap();

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
        loadConfig();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        ICommandManager commandManager = FMLCommonHandler.instance().getMinecraftServerInstance().getCommandManager();
        ServerCommandManager serverCommandManager = ((ServerCommandManager) commandManager);
        serverCommandManager.registerCommand(new CommandPWC());

        scanner = new ThreadWorldScanner();
        if (!blocksToRemove.isEmpty() || !blocksToReplace.isEmpty() || !blockMetaToReplace.isEmpty())
        {
            scanner.startScanner();
        }
        else
        {
            logger.error("Progress World Cleanup thread was not started as there are no blocks to remove. Add some to the config and restart for the mod to have an affect.");
        }
    }

    /**
     * Called to load the config from disk
     */
    public void loadConfig()
    {
        config.load();
        ThreadWorldScanner.SCAN_DELAY = config.getInt("ChunkScanDelay", Configuration.CATEGORY_GENERAL, ThreadWorldScanner.SCAN_DELAY, 0, Integer.MAX_VALUE, "Delay in ticks, 20 ticks a second, to wait before rescanning a previously scanned chunk. Delay is not saved on restart so is reset to zero per chunk each time a world loads. Only after a chunk is scanned does the count down start.");
        logger.info("Loading blocks from config...");
        logger.info("Loading remove list...");
        TickHandler.blocksRemovedPerTick = config.getInt("BlocksToEditPerTick", Configuration.CATEGORY_GENERAL, TickHandler.blocksRemovedPerTick, 0, 10000, "Number of blocks to edit per tick, there are 20 ticks in a second. Keep this low to improve performance, increase to speed up the effect of the mod.");
        String removeBlocks = config.getString("BlocksToRemove", Configuration.CATEGORY_GENERAL, "ThaumicTinkerer:fireOrder,ThaumicTinkerer:fireAir,ThaumicTinkerer:fireEarth,ThaumicTinkerer:fireChaos,ThaumicTinkerer:fireFire,ThaumicTinkerer:fireWater,AncientWarfareAutomation:windmill_blade,minecraft:tnt", "Add blocks to the list separated by a ',', any block in the list will be removed from the world over time. Using @ at the end of the block name to market meta values, meta is between 0 - 15. Several values can be listed using a -, ex 1-10. OreNames can be used using *Ore:Name, ex *Ore:Log");
        if (removeBlocks != null)
        {
            removeBlocks = removeBlocks.trim();
            //TODO remove all spaces
            if (!removeBlocks.isEmpty())
            {
                String[] blocksByNames = removeBlocks.split(",");
                for (final String s : blocksByNames)
                {
                    try
                    {
                        String name = s.trim();
                        if (name.startsWith("*Ore:") || name.startsWith("*ore:"))
                        {
                            String oreName = name.replace("*Ore:", "").replace("*ore:", "");
                            logger.info("\tOreName: " + oreName);

                            List<ItemStack> stacks = OreDictionary.getOres(oreName, false);
                            for (ItemStack stack : stacks)
                            {
                                if (stack != null && stack.getItem() instanceof ItemBlock)
                                {
                                    blocksToRemove.add(((ItemBlock) stack.getItem()).field_150939_a);
                                    logger.info("\t\tAdded: " + ((ItemBlock) stack.getItem()).field_150939_a);
                                }
                            }
                        }
                        else if (name.contains("@"))
                        {
                            String[] split = name.split("@");
                            name = split[0];

                            if(split[1].contains("-"))
                            {
                                split = split[1].split("-");
                                int start = Integer.parseInt(split[0]);
                                int end = Integer.parseInt(split[1]);
                                for(;start <= end; start++)
                                {
                                    addBlockToRemove(name, start);
                                }
                            }
                            //Add single meta value
                            else
                            {
                                addBlockToRemove(name, Integer.parseInt(split[1]));
                            }
                        }
                        else if (!name.isEmpty())
                        {
                            addBlockToRemove(name);
                        }
                    }
                    catch (Exception e)
                    {
                        logger.error("Failed to process entry " + s, e);
                    }
                }
            }
        }

        logger.info("Loading replace list...");
        removeBlocks = config.getString("BlocksToReplace", Configuration.CATEGORY_GENERAL, "", "Replaces one block with another block, each entry must use this format [modName:block@meta > modName:block@meta] or [modName:block > modName:block@meta] in order to work. First part is the block to replace, second is what block to replace it with. Separate each entry with a ',' as a list, avoid spaces as well.");
        removeBlocks.trim();
        if(removeBlocks != null && !removeBlocks.isEmpty())
        {
            String[] blocksByNames = removeBlocks.split(",");
            for (final String s : blocksByNames)
            {
                try
                {
                    String name = s.trim().replace("[", "").replace("]", "");
                    String[] split = name.split(">");

                    String blockOne = split[0].trim();
                    String blockTwo = split[1].trim();

                    int meta1 = -1;
                    int meta2 = 0;

                    if(blockOne.contains("@"))
                    {
                        split = blockOne.split("@");
                        blockOne = split[0].trim();
                        meta1 = Integer.parseInt(split[1].trim());
                    }


                    if(blockTwo.contains("@"))
                    {
                        split = blockTwo.split("@");
                        blockTwo = split[0].trim();
                        meta2 = Integer.parseInt(split[1].trim());
                    }

                    Block block1 = getBlock(blockOne);
                    Block block2 = getBlock(blockTwo);

                    if(meta1 == -1)
                    {
                        blocksToReplace.put(block1, new BlockMeta(block2, meta2));
                    }
                    else
                    {
                        blockMetaToReplace.put(new BlockMeta(block1, meta1), new BlockMeta(block2, meta2));
                    }
                }
                catch (Exception e)
                {
                    logger.error("Failed to process entry " + s, e);
                }
            }
        }

        config.save();
        logger.info("Done...");
    }

    private Block addBlockToRemove(String name, int meta)
    {
        Block block = addBlockToRemove(name);
        if(meta >= 0 && meta < 16)
        {
            List<Integer> list = null;
            if (!blockMetaToRemove.containsKey(block))
            {
                list = blockMetaToRemove.get(block);
            }
            if (list == null)
            {
                list = new ArrayList();
            }
            if (!list.contains(meta))
            {
                list.add(meta);
            }
            else
            {
                logger.error("Meta value[" + meta + "] for block " + block + " is already contained.");
            }
        }
        else
        {
            logger.error("Meta value[" + meta + "] for block " + block + " is invalid, it must be from 0 to 15.");
        }
        return block;
    }

    private Block addBlockToRemove(String name)
    {
        Object object = Block.blockRegistry.getObject(name);
        if (object instanceof Block && object != Blocks.air) //air is the same as null
        {
            if(!blocksToRemove.contains(object))
            {
                blocksToRemove.add((Block) object);
                logger.info("\tAdded: " + object);
            }
            return (Block) object;
        }
        else
        {
            logger.error("\tError: " + name + " was not found in the block list");
        }
        return null;
    }

    private Block getBlock(String name)
    {
        Object object = Block.blockRegistry.getObject(name);
        if (object instanceof Block && object != Blocks.air) //air is the same as null
        {
            return (Block) object;
        }
        return null;
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

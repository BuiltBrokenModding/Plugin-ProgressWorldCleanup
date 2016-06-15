package com.builtbroken.worldcleanup;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/**
 * Simple plugin to process chunks over time to remove junk
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/14/2016.
 */
@Mod(modid = "progressworldcleanup", name = "Progress World Cleanup", version = "@MAJOR@.@MINOR@.@REVIS@.@BUILD@", acceptableRemoteVersions = "*")
public class Plugin
{
    public static TickHandler handler;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        handler = new TickHandler();
        FMLCommonHandler.instance().bus().register(handler);
    }
}

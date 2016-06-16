package com.builtbroken.worldcleanup.command;

import com.builtbroken.worldcleanup.Plugin;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/16/2016.
 */
public class CommandPWC extends CommandBase
{
    @Override
    public String getCommandName()
    {
        return "pwc";
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_)
    {
        return "/" + getCommandName() + " help";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args)
    {
        if(args == null || args.length == 0 || args[0].equalsIgnoreCase("help"))
        {

        }
        else if(args[0].equalsIgnoreCase("pause"))
        {
            if(Plugin.scanner.isAlive())
            {
                Plugin.scanner.stopScanner();
            }
            else
            {
                sender.addChatMessage(new ChatComponentText("Thread is already paused"));
            }
        }
        else if(args[0].equalsIgnoreCase("start"))
        {
            if(!Plugin.scanner.isAlive())
            {
                Plugin.scanner.startScanner();
            }
            else
            {
                sender.addChatMessage(new ChatComponentText("Thread is already started"));
            }
        }
        else if(args[0].equalsIgnoreCase("stop"))
        {
            if(!Plugin.scanner.isAlive())
            {
                Plugin.scanner.kill();
            }
            else
            {
                sender.addChatMessage(new ChatComponentText("Thread is already stopped"));
            }
        }
    }
}

package com.builtbroken.worldcleanup.command;

import com.builtbroken.worldcleanup.Plugin;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

import java.util.List;

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
            sender.addChatMessage(new ChatComponentText("/" + getCommandName() + " pause - turns scanner thread off"));
            sender.addChatMessage(new ChatComponentText("/" + getCommandName() + " stop - turns scanner thread off and clears caches"));
            sender.addChatMessage(new ChatComponentText("/" + getCommandName() + " start - turns scanner thread on"));
            sender.addChatMessage(new ChatComponentText("/" + getCommandName() + " alive - checks if scanner thread is running"));
            sender.addChatMessage(new ChatComponentText("/" + getCommandName() + " running - checks if scanner thread is running"));
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
        else if(args[0].equalsIgnoreCase("alive") || args[0].equalsIgnoreCase("running"))
        {
            if(Plugin.scanner.isAlive())
            {
                sender.addChatMessage(new ChatComponentText("Thread is running"));
            }
            else
            {
                sender.addChatMessage(new ChatComponentText("Thread is not running"));
            }
        }
    }

    //@Override
    public List addTabCompletionOptions(ICommandSender p_71516_1_, String[] p_71516_2_)
    {
        //TODO implement lazy mode :)
        return null;
    }
}

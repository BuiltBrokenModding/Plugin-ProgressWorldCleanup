package com.builtbroken.worldcleanup;

import net.minecraft.world.World;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/14/2016.
 */
public class RemoveBlock
{
    int x, y, z;

    public boolean isValid()
    {
        return true; //TODO check to ensure previous block matches expected value to avoid replacing blocks players have edited
    }

    public void remove(World world)
    {
        world.setBlockToAir(x, y, z); //TODO add field for block and meta to set
    }
}

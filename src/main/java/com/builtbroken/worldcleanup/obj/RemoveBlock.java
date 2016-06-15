package com.builtbroken.worldcleanup.obj;

import net.minecraft.world.World;

/**
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/14/2016.
 */
public class RemoveBlock
{
    public final int x, y, z;

    public RemoveBlock(int x, int y, int z)
    {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    /**
     * Is the block valid to be removed
     *
     * @return true if the previous block data is the same as the current
     */
    public boolean isValid()
    {
        return true; //TODO check to ensure previous block matches expected value to avoid replacing blocks players have edited
    }

    /**
     * Called to trigger the removal of the block
     *
     * @param world - world to use
     */
    public void remove(World world)
    {
        world.setBlockToAir(x, y, z); //TODO add field for block and meta to set
    }

    @Override
    public boolean equals(Object object)
    {
        if (object instanceof RemoveBlock)
        {
            return ((RemoveBlock) object).x == x && ((RemoveBlock) object).y == y && ((RemoveBlock) object).z == z;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + x;
        hash = hash * 31 + y;
        hash = hash * 31 + z;
        return hash;
    }

    @Override
    public String toString()
    {
        return "RemoveBlock[" + x + ", " + y + ", " + z + "]";
    }
}

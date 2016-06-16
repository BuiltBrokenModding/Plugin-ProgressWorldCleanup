package com.builtbroken.worldcleanup.obj;

import net.minecraft.block.Block;
import net.minecraft.world.World;

/**
 * Object that stores block placement calls
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/16/2016.
 */
public class PlaceBlock extends RemoveBlock
{
    public final Block newBlock;
    public final int newMeta;

    public PlaceBlock(World world, int x, int y, int z, Block block, int meta)
    {
        super(world, x, y, z);
        this.newBlock = block;
        this.newMeta = meta;
    }

    public PlaceBlock(World world, int x, int y, int z, BlockMeta blockMeta)
    {
        this(world, x, y, z, blockMeta.block, blockMeta.meta);
    }

    @Override
    public void doAction(World world)
    {
        world.setBlock(x, y, z, newBlock, newMeta, 3);
    }

    //Equals and hash code are the same, as location should be the only check
    @Override
    public String toString()
    {
        return "PlaceBlock[" + x + ", " + y + ", " + z + " | " + newBlock + ", " + newMeta + "]";
    }
}

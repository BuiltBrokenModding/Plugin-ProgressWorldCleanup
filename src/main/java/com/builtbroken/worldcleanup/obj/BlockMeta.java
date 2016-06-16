package com.builtbroken.worldcleanup.obj;

import net.minecraft.block.Block;

/**
 * Quick way to store block and meta pairs
 *
 * @see <a href="https://github.com/BuiltBrokenModding/VoltzEngine/blob/development/license.md">License</a> for what you can and can't do with the code.
 * Created by Dark(DarkGuardsman, Robert) on 6/16/2016.
 */
public class BlockMeta
{
    public final Block block;
    public final int meta;

    public BlockMeta(Block block, int meta)
    {
        this.block = block;
        this.meta = meta;
    }

    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = hash * 31 + Block.getIdFromBlock(block);
        hash = hash * 31 + meta;
        return hash;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj instanceof BlockMeta)
        {
            return ((BlockMeta) obj).block == block && ((BlockMeta) obj).meta == meta;
        }
        return false;
    }

    @Override
    public String toString()
    {
        return "BlockMeta[" + block + ", " + meta + "]";
    }
}

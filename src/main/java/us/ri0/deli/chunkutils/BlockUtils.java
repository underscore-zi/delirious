package us.ri0.deli.chunkutils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class BlockUtils {

    public static boolean isFluid(BlockPos pos) {
        return isFluid(mc.world.getBlockState(pos).getBlock());
    }

    public static boolean isFluid(Block block) {
        return block instanceof FluidBlock;
    }

    public static boolean isLoaded(BlockPos pos) {
        return !mc.world.getBlockState(pos).getBlock().equals(Blocks.VOID_AIR);
    }

    public static boolean isType(BlockPos pos, HashSet<Block> matchers) {
        return matchers.contains(mc.world.getBlockState(pos).getBlock());
    }



}

package us.ri0.deli.chunkutils;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.predicate.block.BlockPredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;

import java.util.HashSet;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class ChunkUtils {

    public static boolean hasAny(Chunk chunk, BlockPredicate predicate) {
        ChunkSection[] sections = chunk.getSectionArray();
        for(var i = 0; i < sections.length; i++) {
            ChunkSection section = sections[i];
            if(section.hasAny(predicate)) return true;
        }
        return false;
    }

    public static boolean hasAny(Chunk chunk, Block block) {
        return hasAny(chunk, BlockPredicate.make(block));
    }

    public static BlockPos positionFor(Chunk chunk, int sectionIndex, int x, int y, int z) {
        var chunkPos = chunk.getPos();
        return chunkPos.getBlockPos(x, (-64 + ((sectionIndex * 16) + y)), z);
    }

    public static int sectionIndexForY(int y) {
        return (y + 64) / 16;
    }

    public static HashSet<BlockPos> positionsOf(Chunk chunk, Block block) {
        ChunkSection[] sections = chunk.getSectionArray();
        HashSet<BlockPos> positions = new HashSet<>();
        for(var i = 0; i < sections.length; i++) {
            ChunkSection section = sections[i];
            if(!section.hasAny(BlockPredicate.make(block))) continue;

            for(int x = 0; x < 16; x++) {
                for(int y = 0; y < 16; y++) {
                    for(int z = 0; z < 16; z++) {
                        var blockState = section.getBlockState(x, y, z);
                        if(blockState.getBlock().equals(block)) {
                            positions.add(positionFor(chunk, i, x, y, z));
                        }
                    }
                }
            }

        }
        return positions;
    }

    public static HashSet<BlockPos> positionsOf(Chunk chunk, int sectionIndex, Block block) {
        ChunkSection[] sections = chunk.getSectionArray();
        if(sectionIndex < 0 || sectionIndex >= sections.length) return new HashSet<>();

        ChunkSection section = sections[sectionIndex];
        if(!section.hasAny(BlockPredicate.make(block))) return new HashSet<>();

        HashSet<BlockPos> positions = new HashSet<>();
        for(int x = 0; x < 16; x++) {
            for(int y = 0; y < 16; y++) {
                for(int z = 0; z < 16; z++) {
                    var blockState = section.getBlockState(x, y, z);
                    if(blockState.getBlock().equals(block)) {
                        positions.add(positionFor(chunk, sectionIndex, x, y, z));
                    }
                }
            }
        }
        return positions;
    }

    public static boolean areNeighborsLoaded(ChunkPos pos) {
        return mc.world.isChunkLoaded(pos.x - 1, pos.z) &&
            mc.world.isChunkLoaded(pos.x + 1, pos.z) &&
            mc.world.isChunkLoaded(pos.x, pos.z - 1) &&
            mc.world.isChunkLoaded(pos.x, pos.z + 1);
    }

    public static boolean areNeighborsLoaded(Chunk chunk) {
        return areNeighborsLoaded(chunk.getPos());
    }

    public static boolean areNeighborsLoaded(BlockPos pos) {
        return areNeighborsLoaded(new ChunkPos(pos.getX() >> 4, pos.getZ() >> 4));
    }
}

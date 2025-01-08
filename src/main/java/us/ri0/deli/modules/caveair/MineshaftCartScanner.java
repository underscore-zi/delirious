package us.ri0.deli.modules.caveair;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.predicate.block.BlockPredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import us.ri0.deli.chunkutils.ChunkUtils;
import us.ri0.deli.esp.Esp;

import java.util.HashSet;
import java.util.function.Consumer;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class MineshaftCartScanner {
    private Consumer<MinecartScanFinding> callback;

    public MineshaftCartScanner(Consumer<MinecartScanFinding> cb) {
        callback = cb;
    }

    /**
     * Scan the area around a chest minecart for missing cave_air
     * @param entity the chest minecart entity to scan around
     */
    public void scanEntity(ChestMinecartEntity entity) {
        var pos = entity.getBlockPos();
        var chunk = mc.world.getChunk(pos);
        var section = chunk.getSection(ChunkUtils.sectionIndexForY(pos.getY()));
        if(section == null || !section.hasAny(BlockPredicate.make(Blocks.CAVE_AIR))) return;

        var floorPos = pos.down();
        Direction[] directions = {Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH};

        HashSet<BlockPos> discoveredAir = new HashSet<>();
        HashSet<BlockPos> discoveredCaveAir = new HashSet<>();

        // The scan dimensions are based on the mineshft generation, a 3x3 tunnel. Unfortunately
        // there are a ton of false positives on the ceiling so I cap the height to 2.

        int width = 5;
        int depth = 3;
        int height = 2;

        for (Direction dir : directions) {
            if(!isWall(pos, dir)) continue;
            var opp = dir.getOpposite();

            Direction left = dir.rotateYCounterclockwise();
            Direction right = dir.rotateYClockwise();

            for (int i = 0; i < width; i++) {
                for (int j = 0; j < depth; j++) {
                    for(Direction perp : new Direction[]{left, right}) {
                        var scanPos = floorPos.offset(opp, j).offset(perp, i);
                        for (int y = 1; y <= height; y++) {
                            Block block = mc.world.getBlockState(scanPos.up(y)).getBlock();
                            if (block.equals(Blocks.CAVE_AIR)) discoveredCaveAir.add(scanPos.up(y));
                            else if (block.equals(Blocks.AIR)) discoveredAir.add(scanPos.up(y));
                        }
                    }
                }
            }
        }

        if (discoveredAir.isEmpty()) return;
        if (discoveredCaveAir.isEmpty()) return;

        callback.accept(new MinecartScanFinding(entity, discoveredAir, discoveredCaveAir));
    }

    /**
     * Determine if the block is part of an apparent wall that is atleast 3 blocks high including the start block
     * @param pos the position to begin the wall check from
     */
    private boolean isWall(BlockPos pos) {
        return mc.world.getBlockState(pos).isOpaqueFullCube() &&
            mc.world.getBlockState(pos.up()).isOpaqueFullCube() &&
            mc.world.getBlockState(pos.up(2)).isOpaqueFullCube();
    }

    /**
     * Determine if the blocks in the given direction represent a likely wall (3 blocks high in in the direction and to
     * the perpendicular sides
     * @param pos
     * @param dir
     * @return
     */
    private boolean isWall(BlockPos pos, Direction dir) {
        if (!isWall(pos.offset(dir))) return false;

        Direction left = dir.rotateYCounterclockwise();
        Direction right = dir.rotateYClockwise();

        return isWall(pos.offset(dir).offset(left)) && isWall(pos.offset(dir).offset(right));
    }

}

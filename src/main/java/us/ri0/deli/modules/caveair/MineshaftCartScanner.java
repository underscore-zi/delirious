package us.ri0.deli.modules.caveair;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.predicate.block.BlockPredicate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import us.ri0.deli.chunkutils.ChunkUtils;
import us.ri0.deli.esp.Esp;
import us.ri0.deli.esp.EspOptions;

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
        final int maxHeight = 2;


        HashSet<BlockPos> discoveredAir = new HashSet<>();
        HashSet<BlockPos> discoveredCaveAir = new HashSet<>();

        for (Direction dir : directions) {
            if (isWall(pos.offset(dir))) {
                var opp = dir.getOpposite();
                for (Direction offsetDir : directions) {
                    // Scan air up to maxHeight in a 4x3 area in each perpendicular direction
                    if (offsetDir == dir || offsetDir == opp) continue;
                    for (int i = 0; i <= 4; i++) {
                        for(int j = 0; j <= 3; j++) {
                            var scanPos = floorPos.offset(opp, j).offset(offsetDir, i);
                            for (int y = 1; y <= maxHeight; y++) {
                                Block block = mc.world.getBlockState(scanPos.up(y)).getBlock();
                                if (block.equals(Blocks.CAVE_AIR)) discoveredCaveAir.add(scanPos.up(y));
                                else if (block.equals(Blocks.AIR)) discoveredAir.add(scanPos.up(y));
                            }
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

}

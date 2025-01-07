package us.ri0.deli.modules.caveair;

import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.chunk.Chunk;
import us.ri0.deli.chunkutils.ChunkUtils;

import java.util.HashSet;
import java.util.function.Consumer;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class TouchScanner {
    public static void scan(Chunk chunk, Consumer<TouchScanFinding> callback) {
        if (!ChunkUtils.hasAny(chunk, Blocks.CAVE_AIR)) return;

        var caveAir = ChunkUtils.positionsOf(chunk, Blocks.CAVE_AIR);
        caveAir.stream().forEach(pos -> {
            for (var dir : Direction.values()) {
                try {
                    if (chunk.getBlockState(pos.offset(dir)).getBlock().equals(Blocks.AIR)) {
                        scanAirBlock(pos.offset(dir), callback);
                        break; // Only need to scan it once
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
    }

    public static void scanAirBlock(BlockPos pos, Consumer<TouchScanFinding> callback) {
        var touches = new HashSet<BlockPos>();
        for (var dir : Direction.values()) {
            try {
                if (mc.world.getBlockState(pos.offset(dir)).getBlock().equals(Blocks.CAVE_AIR)) {
                    touches.add(pos.offset(dir));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if(touches.isEmpty()) return;
        callback.accept(new TouchScanFinding(pos, touches));
    }
}

package us.ri0.deli.modules.caveair;

import meteordevelopment.meteorclient.utils.player.ChatUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.predicate.block.BlockPredicate;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import us.ri0.deli.chunkutils.BlockUtils;
import us.ri0.deli.chunkutils.ChunkUtils;
import us.ri0.deli.esp.Esp;
import us.ri0.deli.esp.EspOptions;

import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import static meteordevelopment.meteorclient.MeteorClient.mc;

public class DungeonScanner {
    private Esp esp;
    private EspOptions opts;

    private final ConcurrentHashMap<BlockPos, Boolean> delayed = new ConcurrentHashMap<BlockPos, Boolean>();

    public DungeonScanner(Esp newEsp, EspOptions newOpts) {
        esp = newEsp;
        opts = newOpts;
    }

    public void clear() {
        delayed.clear();
    }

    /**
     * Scans the area around a spawner to determine if all chunks have been loaded
     * @param spawnerPos the position of the spawner in the center of a dungeon
     */
    public boolean isScannable(BlockPos spawnerPos) {
        if (!BlockUtils.isLoaded(spawnerPos)) return false;
        if (!BlockUtils.isLoaded(spawnerPos.offset(Direction.EAST, 5))) return false;
        if (!BlockUtils.isLoaded(spawnerPos.offset(Direction.WEST, 5))) return false;
        if (!BlockUtils.isLoaded(spawnerPos.offset(Direction.NORTH, 5))) return false;
        if (!BlockUtils.isLoaded(spawnerPos.offset(Direction.SOUTH, 5))) return false;

        return true;
    }

    public void scanChunk(Chunk chunk) {
        ChunkSection[] sections = chunk.getSectionArray();

        for(var i = 0; i < sections.length; i++) {
            ChunkSection section = sections[i];

            // BUGFIX: In older versions spawners are generated without cave_air, but at some point something
            // generated that did put cave_air into the chunk, just not the dungeon.
            if (!section.hasAny(BlockPredicate.make(Blocks.CAVE_AIR))) {

                // In the unlikely event that a spawner generates right on the section boundary AND the `cave_air`
                // on the surrounding surface is missing check if there is any above or below
                var caveAirBelow = i <= 1 || sections[i - 1].hasAny(BlockPredicate.make(Blocks.CAVE_AIR));
                var caveAirAbove = i >= sections.length - 1 || sections[i + 1].hasAny(BlockPredicate.make(Blocks.CAVE_AIR));
                if (!caveAirBelow && !caveAirAbove) continue;
            }

            var spawners = ChunkUtils.positionsOf(chunk, i, Blocks.SPAWNER);

            delayed.forEach((pos, value) -> {
                if(isScannable(pos)) {
                    spawners.add(pos);
                    delayed.remove(pos);
                }
            });

            spawners.stream().filter(spawnerPos -> !isScannable(spawnerPos)).forEach(pos -> {
                delayed.put(pos, true);
            });

            spawners.stream().filter(this::isScannable).forEach(this::scanDungeon);
        }
    }

    /**
     * Scans a dungeon for the presence of air instead of cave_air
     * @param pos is the BlockPos of the spawner the dungeon is centered around
     */
    public void scanDungeon(BlockPos pos) {
        var entity = mc.world.getBlockEntity(pos);
        if(!(entity instanceof MobSpawnerBlockEntity spawner)) return;
        if(!isDungeonSpawner(spawner)) return;

        final int maxRadius = 5;
        final int maxHeight = 5;

        BlockPos floorPos = pos.down();

        // Even though we checked this before called, its possible the chunk has already unlocked
        if(!isScannable(floorPos)) {
            delayed.put(pos, true);
            return;
        }

        var bounds = calculateFloorBounds(floorPos, maxRadius);

        // Adjust the boundaries to not include the walls where normal air often encroaches.
        if(bounds.east > 0) bounds.east--;
        if(bounds.west > 0) bounds.west--;
        if(bounds.north > 0) bounds.north--;
        if(bounds.south > 0) bounds.south--;


        int[][] directions = {
            {1, 1, bounds.east, bounds.south},   // South-East Quadrant
            {-1, 1, bounds.west, bounds.south},  // South-West Quadrant
            {1, -1, bounds.east, bounds.north},  // North-East Quadrant
            {-1, -1, bounds.west, bounds.north}  // North-West Quadrant
        };


        HashSet<BlockPos> discoveredAir = new HashSet<BlockPos>();
        HashSet<BlockPos> discoveredCaveAir = new HashSet<BlockPos>();

        for (int[] dir : directions) {
            int xDir = dir[0];
            int zDir = dir[1];
            int xMax = dir[2];
            int zMax = dir[3];

            for (int x = 0; x <= xMax; x++) {
                for (int z = 0; z <= zMax; z++) {
                    BlockPos currentColumn = floorPos.add(x * xDir, 0, z * zDir);

                    for(int y = 1; y < maxHeight; y++) {
                        BlockPos cur = currentColumn.up(y);
                        Block curBlock = mc.world.getBlockState(cur).getBlock();
                        if(curBlock.equals(Blocks.AIR)) {
                            discoveredAir.add(cur);
                        } else if(curBlock.equals(Blocks.CAVE_AIR)) {
                            discoveredCaveAir.add(cur);
                        }
                    }
                }
            }
        }

        if(discoveredAir.isEmpty()) return;

        // Check we had atleast some cave air to avoid false positives on old/updated chunks.
        if(discoveredCaveAir.isEmpty()) return;

        discoveredAir.forEach(p -> esp.Block(p, opts));


        MutableText coords = ChatUtils.formatCoords(Vec3d.of(spawner.getPos()));
        Text msg = Text.literal("Found missing cave air around dungeon spawner at ").append(coords);

        ChatUtils.sendMsg("CaveAir",  msg);
    }


    /**
     * Determine if a spawner is one that generated as part of a dungeon
     * @param spawner the specific spawner entity to check
     */
    public boolean isDungeonSpawner(MobSpawnerBlockEntity spawner) {
        String monster = spawner.getLogic().spawnEntry.getNbt().get("id").toString();
        if(monster.contains("skeleton") || monster.contains("zombie")) {
            return true;
        } else if (monster.contains(":cave_spider")) {
            return false;
        } else if(monster.contains("spider")) {
            Block b = mc.world.getBlockState(spawner.getPos().down()).getBlock();
            return b.equals(Blocks.MOSSY_COBBLESTONE) || b.equals(Blocks.COBBLESTONE);
        }
        return false;
    }

    /**
     * Calculate the boundaries of a dungeon floor
     * @param floor the BlockPos of the floor of the dungeon
     * @param radius the maximum radius of the dungeon to scan
     */
    private Boundaries calculateFloorBounds(BlockPos floor, int radius) {
        // Dungeons typically have a solid cobblestone floor but are surrounded by non-cobble blocks
        // so we can use the presenence of cobblestone to calculate the boundaries of the dungeon that
        // should have cave_air present.

        var cobblestone = new HashSet<Block>();
        cobblestone.add(Blocks.COBBLESTONE);
        cobblestone.add(Blocks.MOSSY_COBBLESTONE);

        Boundaries result = new Boundaries();
        int i;

        for(i = 1;i < radius; i++) {
            if(!BlockUtils.isType(floor.east(i), cobblestone)) {
                break;
            };
        }
        result.east = i-1;

        for(i = 1;i < radius; i++) {
            if(!BlockUtils.isType(floor.west(i), cobblestone)) {
                break;
            };
        }
        result.west = i-1;

        for(i = 1;i < radius; i++) {
            if(!BlockUtils.isType(floor.south(i), cobblestone)) {
                break;
            };
        }
        result.south = i-1;

        for(i = 1;i < radius; i++) {
            if(!BlockUtils.isType(floor.north(i), cobblestone)) {
                break;
            };
        }
        result.north = i-1;
        return result;
    }


    private static class Boundaries {
        int east;
        int west;
        int north;
        int south;
    }
}

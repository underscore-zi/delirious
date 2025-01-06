package us.ri0.deli.modules;

import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.ChunkDataEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.Utils;
import meteordevelopment.meteorclient.utils.network.MeteorExecutor;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.predicate.block.BlockPredicate;
import net.minecraft.text.Text;
import net.minecraft.util.math.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.WorldChunk;
import us.ri0.deli.Addon;
import us.ri0.deli.chunkutils.ChunkUtils;
import us.ri0.deli.esp.Esp;
import us.ri0.deli.esp.EspOptions;
import us.ri0.deli.esp.MockSetting;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class MissingCaveAir extends Module {
    private final String chatPrefix = "CaveAir";
    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgDungeons = settings.createGroup("Dungeons Scans");
    private final SettingGroup sgMineshaft = settings.createGroup("Mineshaft Scans");
    private final SettingGroup sgDebug = settings.createGroup("Debugging");

    private final Setting<Integer> range = sgGeneral.add(new IntSetting.Builder()
        .name("Render Distance (chunks)")
        .description("Maximum distance to render the ESP box for any discovers")
        .defaultValue(15)
        .min(1)
        .sliderMax(64)
        .build()
    );

    private final Setting<Boolean> chatNotifications = sgGeneral.add(new BoolSetting.Builder()
        .name("chat-notifications")
        .description("Notifies you in chat with coords when a discovery is made")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> scanMinecarts = sgMineshaft.add(new BoolSetting.Builder()
        .name("scan-minecarts")
        .description("Scan area around minecarts for missing cave_air")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> minecartLineColor = sgMineshaft.add(new ColorSetting.Builder()
        .name("minecart-line-color")
        .description("Color of box around entity")
        .defaultValue(new SettingColor(255, 200, 200))
        .visible(scanMinecarts::get)
        .build()
    );

    private final Setting<SettingColor> minecartBoxColor = sgMineshaft.add(new ColorSetting.Builder()
        .name("minecart-box-color")
        .description("Color of entity's bounding box")
        .defaultValue(new SettingColor(255, 200, 200, 20))
        .visible(scanMinecarts::get)
        .build()
    );

    private final Setting<Boolean> minecartTracer = sgMineshaft.add(new BoolSetting.Builder()
        .name("minecart-tracer")
        .description("Draws a line to the entity")
        .defaultValue(false)
        .visible(scanMinecarts::get)
        .build()
    );

    private final Setting<SettingColor> minecartTracerColor = sgMineshaft.add(new ColorSetting.Builder()
        .name("minecart-tracer-color")
        .description("Color of the tracer line")
        .defaultValue(new SettingColor(255, 200, 200))
        .visible(scanMinecarts::get)
        .build()
    );



    private final Setting<Boolean> scanDungeons = sgDungeons.add(new BoolSetting.Builder()
        .name("scan-dungeons")
        .description("Scan area around dungeon spawners for missing cave_air")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> dungeonLineColor = sgDungeons.add(new ColorSetting.Builder()
        .name("dungeon-line-color")
        .description("Color of box around entity")
        .defaultValue(new SettingColor(0, 200, 150))
        .visible(scanDungeons::get)
        .build()
    );

    private final Setting<SettingColor> dungeonBoxColor = sgDungeons.add(new ColorSetting.Builder()
        .name("dungeon-box-color")
        .description("Color of entity's bounding box")
        .defaultValue(new SettingColor(0, 200, 150, 40))
        .visible(scanDungeons::get)
        .build()
    );

    private final Setting<Boolean> dungeonTracer = sgDungeons.add(new BoolSetting.Builder()
        .name("dungeon-tracer")
        .description("Draws a line to the entity")
        .defaultValue(false)
        .visible(scanDungeons::get)
        .build()
    );

    private final Setting<SettingColor> dungeonTracerColor = sgDungeons.add(new ColorSetting.Builder()
        .name("dungeon-tracer-color")
        .description("Color of the tracer line")
        .defaultValue(new SettingColor(0, 200, 150))
        .visible(scanDungeons::get)
        .build()
    );

    private final Setting<Boolean> debug = sgDebug.add(new BoolSetting.Builder()
        .name("debug")
        .description("Debugging information")
        .defaultValue(false)
        .build()
    );

    private final Esp esp = new Esp();
    private final ConcurrentHashMap<BlockPos, Boolean> delayedSpawners = new ConcurrentHashMap<BlockPos, Boolean>();

    public MissingCaveAir() {
        super(Addon.CATEGORY, "CaveAir", "Highlight air that should be cave_air");
    }

    private final ConcurrentHashMap<Integer, Boolean> seenCarts = new ConcurrentHashMap<Integer, Boolean>();
    @EventHandler
    public void onRender3d(Render3DEvent event) {
        esp.onRender3D(event);

        if(!scanMinecarts.get()) return;
        synchronized (seenCarts) {
            seenCarts.replaceAll((k, v) -> false);

            for (var entity : mc.world.getEntities()) {
                if (entity instanceof ChestMinecartEntity cart) {
                    if (seenCarts.containsKey(cart.getId())) {
                        seenCarts.put(cart.getId(), true);
                        continue;
                    }

                    seenCarts.put(cart.getId(), true);

                    // We don't want to hold up the syncronized stuff with the check
                    MeteorExecutor.execute(() -> {
                        try {
                            CheckAirNearMinecartChest(cart);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }

            seenCarts.values().removeIf((v) -> !v);
        }
    }

    @EventHandler
    public void onChunkData(ChunkDataEvent event) {
        if (debug.get()) {
            //MeteorExecutor.execute(() -> touchPoints(event.chunk()));
            MeteorExecutor.execute(() -> highlightCaveAir(event.chunk()));
        }

        if(!scanDungeons.get()) return;
        if(!ChunkUtils.hasAny(event.chunk(), Blocks.CAVE_AIR)) return;

        MeteorExecutor.execute(() -> {
            Chunk chunk = event.chunk();
            ChunkSection[] sections = chunk.getSectionArray();

            for(var i = 0; i < sections.length; i++) {
                ChunkSection section = sections[i];

                // BUGFIX: In older versions spawners are generated without cave_air, but at some point something
                // generated that did put cave_air into the chunk, just not the dungeon.
                if(!section.hasAny(BlockPredicate.make(Blocks.CAVE_AIR))) {

                    // In the unlikely event that a spawner generates right on the section boundary AND the `cave_air`
                    // on the surrounding surface is missing check if there is any above or below
                    var caveAirBelow = i <= 1 || sections[i - 1].hasAny(BlockPredicate.make(Blocks.CAVE_AIR));
                    var caveAirAbove = i >= sections.length - 1 || sections[i + 1].hasAny(BlockPredicate.make(Blocks.CAVE_AIR));
                    if(!caveAirBelow && !caveAirAbove) continue;

                }

                var spawners = ChunkUtils.positionsOf(chunk, i, Blocks.SPAWNER);
                spawners.forEach(this::CheckAirNearSpawner);
            }

            delayedSpawners.keySet().removeIf((spawnerPos) -> !isLoaded(spawnerPos));
            for(var spawner : delayedSpawners.keySet()) {
                if(isDungeonScannable(spawner.down(), 5)) {
                    delayedSpawners.remove(spawner);
                    CheckAirNearSpawner(spawner);
                }
            }
        });
    }

    /** Determines if a spawner entity is a dungeon spawner.
     * @param spawner The spawner block entity to check
     * @return true if the spawner is a dungeon spawner, false otherwise
     */
    private boolean isDungeonSpawner(MobSpawnerBlockEntity spawner) {
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


    public void CheckAirNearSpawner(BlockPos pos) {
        var opts = new EspOptions();
        opts.renderDistance = range;
        opts.sideColor = dungeonBoxColor;
        opts.lineColor = dungeonLineColor;
        opts.tracer = dungeonTracer;
        opts.tracerColor = dungeonTracerColor;


        var entity = mc.world.getBlockEntity(pos);
        if(!(entity instanceof MobSpawnerBlockEntity)) return;

        MobSpawnerBlockEntity spawner = (MobSpawnerBlockEntity) entity;
        if(!isDungeonSpawner(spawner)) return;

        /* The edges of dungeons generation often mix both air and cave_air in the locations where the cave "breaks in"
           to deal with these false positives I figure out where the dungeon walls begin and do not scan blocks that
           could belong to the walls (torches on the wall impact the block in front of the wall the torch reaches into).
           While this can lead to false negatives where only a wall block is touched, I feel like that is an unlikely
           scenario and worth the tradeoff.
         */

        final int maxRadius = 5;
        final int maxHeight = 5;

        var floorPos = pos.down();

        // I've run into a couple cases where it would briefly be scannable so it falls into the function
        // only for the player to immediately unload the relevant chunk so we repeat the check here
        if(!isDungeonScannable(floorPos, maxRadius)) {
            if(!delayedSpawners.contains(pos)) {
                delayedSpawners.put(pos, true);
                return;
            }
        };

        var bounds = scanCobblestone(floorPos, maxRadius);
        var eastMax = Math.max(0, bounds[0]-1);
        var westMax = Math.max(0, bounds[1]-1);
        var southMax = Math.max(0, bounds[2]-1);
        var northMax = Math.max(0, bounds[3]-1);

        int[][] directions = {
            {1, 1, eastMax, southMax},   // South-East Quadrant
            {-1, 1, westMax, southMax},  // South-West Quadrant
            {1, -1, eastMax, northMax},  // North-East Quadrant
            {-1, -1, westMax, northMax}  // North-West Quadrant
        };

        AtomicBoolean hadFinding = new AtomicBoolean(false);
        for (int[] dir : directions) {
            int xDir = dir[0];
            int zDir = dir[1];
            int xMax = dir[2];
            int zMax = dir[3];

            for (int x = 0; x <= xMax; x++) {
                for (int z = 0; z <= zMax; z++) {
                    BlockPos p = floorPos.add(x * xDir, 0, z * zDir);
                    scanDungeonAir(p, maxHeight, (match) -> {
                        if(esp.isNew(match)) {
                            esp.Block(match, opts);
                            hadFinding.set(true);
                        }
                    });
                }
            }
        }

        if (hadFinding.get() && chatNotifications.get()) {
            Text msg = Text.literal("Missing cave air around spawner at ").append(ChatUtils.formatCoords(Vec3d.of(pos)));
            ChatUtils.sendMsg(this.chatPrefix,  msg);
        }
    }

    /** Scan the cobblestone blocks in cardinal directions to determine where the generated cobblestone floor ends.
       @param floor The position of the cobblestone block under the spawner
       @param maxDistance The maximum distance to scan in each direction
       @return Integer array {east, west. south. north} containing the distance until the first non-cobblestone block
     */
    private int[] scanCobblestone(BlockPos floor, int maxDistance) {
        int i;
        int[] result = new int[4];

        for(i = 1;i < maxDistance; i++) {
            if(!isCobblestone(floor.east(i))) {
                break;
            };
        }
        result[0] = i-1;

        for(i = 1;i < maxDistance; i++) {
            if(!isCobblestone(floor.west(i))) {
                break;
            };
        }
        result[1] = i-1;

        for(i = 1;i < maxDistance; i++) {
            if(!isCobblestone(floor.south(i))) {
                break;
            };
        }
        result[2] = i-1;

        for(i = 1;i < maxDistance; i++) {
            if(!isCobblestone(floor.north(i))) {
                break;
            };
        }
        result[3] = i-1;

        return result;
    }

    /** Scans the blocks above a given floor position for any air blocks (where cave_air should exist).
     * @param pos is the floor position in the dungeon, must be a cobblestone block
     * @param maxHeight is the number of blocks above the floor to scan
     * @return the number of air blocks found in the column

     */
    private void scanDungeonAir(BlockPos pos, int maxHeight, Consumer<BlockPos> callback) {
        if(!isCobblestone(pos)) return;
        scanAirStack(pos, maxHeight, callback);
    }


    private void scanAirStack(BlockPos pos, int maxHeight, Consumer<BlockPos> callback) {
        scanAirStack(pos, maxHeight, false, callback);
    }
    private void scanAirStack(BlockPos pos, int maxHeight, boolean requireSolidFloor, Consumer<BlockPos> callback) {
        if(requireSolidFloor && !mc.world.getBlockState(pos).isOpaqueFullCube()) return;

        for(int i = 1; i <= maxHeight; i++) {
            BlockPos p = pos.up(i);
            if(mc.world.getBlockState(p).getBlock().equals(Blocks.AIR)) {
                callback.accept(p);
            }
        }
    }


    /** Determines if a block is one of the cobblestone variants we care about.
     * @param b The block to check
     * @return true if the block is one of the blocks of interest, false otherwise
     */
    private boolean isCobblestone(Block b) {
        return b.equals(Blocks.COBBLESTONE) || b.equals(Blocks.MOSSY_COBBLESTONE);
    }

    /** Determines if a block at a given position is one of the cobblestone variants we care about.
     * @param pos The position of the block to check
     * @return true if the block is one of the blocks of interest, false otherwise
     */
    private boolean isCobblestone(BlockPos pos) {
        return isCobblestone(mc.world.getBlockState(pos).getBlock());
    }


    /** Determines if a block at a given position is loaded by the client
     * @param pos The position of the block to check
     * @return true if the block is loaded, false otherwise
     */
    private boolean isLoaded(BlockPos pos) {
        return !mc.world.getBlockState(pos).getBlock().equals(Blocks.VOID_AIR);
    }

    /** Determines if the entire dungeon is loaded in by the client. When the dungeon is generated over a chunk border
     * the client may be missing part of the necessary information
     * @param pos The position of the block to check
     * @return true if the dungeon is loaded, false otherwise
     */
    private boolean isDungeonScannable(BlockPos pos, int maxRadius) {
        if(mc.world.getBlockState(pos.east(maxRadius)).getBlock().equals(Blocks.VOID_AIR)) return false;
        if(mc.world.getBlockState(pos.west(maxRadius)).getBlock().equals(Blocks.VOID_AIR)) return false;
        if(mc.world.getBlockState(pos.north(maxRadius)).getBlock().equals(Blocks.VOID_AIR)) return false;
        if(mc.world.getBlockState(pos.south(maxRadius)).getBlock().equals(Blocks.VOID_AIR)) return false;

        return true;
    }

        @Override
    public void onActivate() {
        for(var c : Utils.chunks(true)) {
            onChunkData(new ChunkDataEvent((WorldChunk) c));
        }
    }

    @Override
    public void onDeactivate() {
        esp.clear();
        seenCarts.clear();
    }


    /* Debug sutff */
    private void highlightCaveAir(Chunk chunk) {
        if (!ChunkUtils.hasAny(chunk, Blocks.CAVE_AIR)) return;
        var sections = chunk.getSectionArray();

        int r = (int) (Math.random() * 256);
        int g = (int) (Math.random() * 256);
        int b = (int) (Math.random() * 256);

        for (var i = 0; i < sections.length; i++) {
            var section = sections[i];
            if (!section.hasAny(BlockPredicate.make(Blocks.CAVE_AIR))) continue;

            var caveAirEsp = new EspOptions();
            caveAirEsp.tracer = new MockSetting<Boolean>(false);
            caveAirEsp.lineColor = new MockSetting<SettingColor>(new SettingColor(r,g,b));
            caveAirEsp.sideColor = new MockSetting<SettingColor>(new SettingColor(r,g,b,2));

            for (var pos : ChunkUtils.positionsOf(chunk, i, Blocks.CAVE_AIR)) {
                esp.Block(pos, caveAirEsp);
            }
        }
    }

    private void touchPoints(Chunk chunk) {
        if (!ChunkUtils.hasAny(chunk, Blocks.CAVE_AIR)) return;

        var touchingEsp = new EspOptions();
        touchingEsp.tracer = new MockSetting<Boolean>(false);
        touchingEsp.lineColor = new MockSetting<SettingColor>(new SettingColor(0,0,255));
        touchingEsp.sideColor = new MockSetting<SettingColor>(new SettingColor(0,0,255,30));

        var touchedEsp = new EspOptions();
        touchedEsp.tracer = new MockSetting<Boolean>(false);
        touchedEsp.lineColor = new MockSetting<SettingColor>(new SettingColor(0,255,0));
        touchedEsp.sideColor = new MockSetting<SettingColor>(new SettingColor(0,255,0,30));

        var sections = chunk.getSectionArray();
        for (var i = 0; i < sections.length; i++) {
            var section = sections[i];
            for (var x = 1; x < 15; x++) {
                for (var y = 1; y < 15; y++) {
                    for (var z = 1; z < 15; z++) {

                        var pos = ChunkUtils.positionFor(chunk, i, x, y, z);
                        if(section.getBlockState(x, y+1, z).getBlock().equals(Blocks.AIR)) continue;
                        if (section.getBlockState(x, y, z).getBlock().equals(Blocks.AIR)) {
                            int touchPoints = 0;
                            if (section.getBlockState(x+1, y, z).getBlock().equals(Blocks.CAVE_AIR)) touchPoints++;
                            if (section.getBlockState(x-1, y, z).getBlock().equals(Blocks.CAVE_AIR)) touchPoints++;
                            if (section.getBlockState(x, y+1, z).getBlock().equals(Blocks.CAVE_AIR)) touchPoints++;
                            if (section.getBlockState(x, y-1, z).getBlock().equals(Blocks.CAVE_AIR)) touchPoints++;
                            if (section.getBlockState(x, y, z+1).getBlock().equals(Blocks.CAVE_AIR)) touchPoints++;
                            if (section.getBlockState(x, y, z-1).getBlock().equals(Blocks.CAVE_AIR)) touchPoints++;

                            if (touchPoints == 5 || touchPoints == 4) {
                                esp.Block(pos, touchingEsp);

                                if (section.getBlockState(x+1, y, z).getBlock().equals(Blocks.CAVE_AIR)) esp.Block(ChunkUtils.positionFor(chunk, i, x+1, y, z), touchedEsp);
                                if (section.getBlockState(x-1, y, z).getBlock().equals(Blocks.CAVE_AIR)) esp.Block(ChunkUtils.positionFor(chunk, i, x-1, y, z), touchedEsp);
                                if (section.getBlockState(x, y+1, z).getBlock().equals(Blocks.CAVE_AIR)) esp.Block(ChunkUtils.positionFor(chunk, i, x, y+1, z), touchedEsp);
                                if (section.getBlockState(x, y-1, z).getBlock().equals(Blocks.CAVE_AIR)) esp.Block(ChunkUtils.positionFor(chunk, i, x, y-1, z), touchedEsp);
                                if (section.getBlockState(x, y, z+1).getBlock().equals(Blocks.CAVE_AIR)) esp.Block(ChunkUtils.positionFor(chunk, i, x, y, z+1), touchedEsp);
                                if (section.getBlockState(x, y, z-1).getBlock().equals(Blocks.CAVE_AIR)) esp.Block(ChunkUtils.positionFor(chunk, i, x, y, z-1), touchedEsp);

                            }
                        }

                    }
                }
            }
        }
    }


    private boolean isMineshaftWall(BlockPos pos) {
        return mc.world.getBlockState(pos).isOpaqueFullCube() &&
            mc.world.getBlockState(pos.up()).isOpaqueFullCube() &&
            mc.world.getBlockState(pos.up(2)).isOpaqueFullCube();
    }

    private void CheckAirNearMinecartChest(ChestMinecartEntity entity) {
        var opts = new EspOptions();
        opts.renderDistance = range;
        opts.sideColor = minecartBoxColor;
        opts.lineColor = minecartLineColor;
        opts.tracer = minecartTracer;
        opts.tracerColor = minecartTracerColor;

        // First make sure there is cave air at all in the section, this could be a player placed cart or just generated
        // before cave air was a thing
        var pos = entity.getBlockPos();
        var chunk = mc.world.getChunk(pos);
        var section = chunk.getSection(ChunkUtils.sectionIndexForY(pos.getY()));
        if(section == null || !section.hasAny(BlockPredicate.make(Blocks.CAVE_AIR))) return;

        var floorPos = pos.down();
        Direction[] directions = {Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH};

        AtomicBoolean hadFinding = new AtomicBoolean(false);
        for (Direction dir : directions) {
            if (isMineshaftWall(pos.offset(dir))) {
                var opp = dir.getOpposite();
                for (Direction offsetDir : directions) {
                    if(offsetDir == dir || offsetDir == opp) continue;

                    // Goal here is to scan a 3x4 area in each direction perpendicular to the wall
                    // Starting at 0 we scan the center row twice, but I'd rather not make it a special case
                    for(int i = 0; i <= 4; i++) {
                        scanAirStack(floorPos.offset(offsetDir, i), 2, true, (x) -> {
                            if(esp.isNew(x)) {
                                esp.Block(x, opts);
                                hadFinding.set(true);
                            }
                        });

                        scanAirStack(floorPos.offset(opp, 1).offset(offsetDir, i), 2, true, (x) -> {
                            if(esp.isNew(x)) {
                                esp.Block(x, opts);
                                hadFinding.set(true);
                            }
                        });

                        scanAirStack(floorPos.offset(opp, 2).offset(offsetDir, i), 2, true, (x) -> {
                            if(esp.isNew(x)) {
                                esp.Block(x, opts);
                                hadFinding.set(true);
                            }
                        });
                    }
                }
            }
        }

        if (hadFinding.get() && chatNotifications.get()) {
            Text msg = Text.literal("Found missing cave air around minecart at ").append(ChatUtils.formatCoords(entity.getPos()));
            ChatUtils.sendMsg(this.chatPrefix,  msg);
        }

    }
}

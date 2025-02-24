package us.ri0.deli.modules;

import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
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
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.MobSpawnerBlockEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import us.ri0.deli.Addon;
import us.ri0.deli.TextUtils;
import us.ri0.deli.chunkutils.ChunkUtils;
import us.ri0.deli.esp.Esp;
import us.ri0.deli.esp.EspOptions;
import us.ri0.deli.esp.MockSetting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class DisplacedStack extends Module {
    public DisplacedStack() {
        super(Addon.CATEGORY, "displaced-stack", "Detects stacks around dungeons/mines that appear displaced from the surrounding");
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final Setting<Boolean> doDungeonScan = sgGeneral.add(new BoolSetting.Builder()
        .name("scan-dungeons")
        .description("Scan the area around dungeons for displaced blocks")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> requireActivatedSpawner = sgGeneral.add(new BoolSetting.Builder()
        .name("require-activated-spawner")
        .description("Only scan dungeons with activated spawners")
        .defaultValue(false)
        .visible(() -> doDungeonScan.get())
        .build()
    );

    private final Setting<Boolean> doMinecartScan = sgGeneral.add(new BoolSetting.Builder()
        .name("scan-minecarts")
        .description("Scan the area around minecarts for displaced blocks")
        .defaultValue(true)
        .build()
    );

    private final Setting<Integer> maximumYDistance = sgGeneral.add(new IntSetting.Builder()
        .name("maximum-y-distance")
        .description("The maximum distance between the bottom of the stack and the source block. This helps filter out obvious false positives")
        .defaultValue(30)
        .min(1)
        .sliderMax(256)
        .build()
    );

    private final Setting<Integer> scanRadius = sgGeneral.add(new IntSetting.Builder()
        .name("scan-radius")
        .description("How far around the identified spawner/minecart to scan for stacks")
        .defaultValue(10)
        .min(1)
        .sliderMax(32)
        .build()
    );

    private final Setting<Integer> disturbanceThreshold = sgGeneral.add(new IntSetting.Builder()
        .name("disturbance-threshold")
        .description("The minimum number disturbed blocks before considering the area as a possible stack")
        .defaultValue(5)
        .min(1)
        .sliderMax(32)
        .build()
    );

    private final Setting<Boolean> espSource = sgGeneral.add(new BoolSetting.Builder()
        .name("esp-source")
        .description("Highlights the source block of the stack")
        .defaultValue(true)
        .build()
    );

    private final Setting<SettingColor> lineColor = sgGeneral.add(new ColorSetting.Builder()
        .name("line-color")
        .description("Color of lines around block that triggered the scan")
        .defaultValue(new SettingColor(255, 255, 255))
        .visible(espSource::get)
        .build()
    );

    private final Setting<SettingColor> boxColor = sgGeneral.add(new ColorSetting.Builder()
        .name("box-color")
        .description("Color of box around block that triggered the scan")
        .defaultValue(new SettingColor(255, 0, 255, 30, true))
        .visible(espSource::get)
        .build()
    );

    private final Setting<Boolean> tracer = sgGeneral.add(new BoolSetting.Builder()
        .name("tracer")
        .description("Draws a line to block that triggered the scan")
        .defaultValue(true)
        .visible(espSource::get)
        .build()
    );

    private final Setting<SettingColor> tracerColor = sgGeneral.add(new ColorSetting.Builder()
        .name("tracer-color")
        .description("Color of the tracer line")
        .defaultValue(new SettingColor(255, 255, 255, 255, true))
        .visible(espSource::get)
        .build()
    );

    private final Setting<Boolean> doChatNotif = sgGeneral.add(new BoolSetting.Builder()
        .name("do-chat-notif")
        .description("Notify in chat when there is a finding")
        .defaultValue(true)
        .build()
    );

    private final Setting<Boolean> includeCoords = sgGeneral.add(new BoolSetting.Builder()
        .name("include-coords")
        .description("Include coords in chat notification")
        .defaultValue(true)
        .build()
    );

    private final ReentrantLock lock = new ReentrantLock();
    private final ConcurrentHashMap<BlockPos, ScanResult> scanCache = new ConcurrentHashMap<>();

    // seenChunks should only be used while holding the `lock`
    private final HashMap<ChunkPos, Boolean> seenChunks = new HashMap<>();

    private final Esp esp = new Esp();

    @EventHandler
    public void onRender3d(Render3DEvent event) {
        esp.onRender3D(event);
    }

    @EventHandler
    public void onChunkData(ChunkDataEvent event) {
        if(doDungeonScan.get()) {
            MeteorExecutor.execute(this::processChunks);
        }
    }

    public void processChunks() {
        if(!lock.tryLock()) return;

        try {
            seenChunks.replaceAll((k, v) -> false);
            Utils.chunks(true).forEach((c) -> {
                if (!seenChunks.containsKey(c.getPos())) {
                    scanChunk(c);
                }
                seenChunks.put(c.getPos(), true);
            });
            seenChunks.entrySet().removeIf((e) -> !e.getValue());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    @EventHandler
    public void onEntityAdded(EntityAddedEvent event) {
        if(!doMinecartScan.get()) return;

        // I might want to move this onto its own executor like I do with chunks, but since this is already called
        // with just one entity at (apart from activation) a time I'm not sure its necessary
        if(event.entity instanceof net.minecraft.entity.vehicle.ChestMinecartEntity cart) {
            MeteorExecutor.execute(() -> {scanMinecart(cart);});
        }
    }

    public void scanMinecart(net.minecraft.entity.vehicle.ChestMinecartEntity cart) {
        if(cart.getVelocity().getX() > 0.5d || cart.getVelocity().getZ() > 0.5d) return;
        if(scanCache.containsKey(cart.getBlockPos())) return;

        int radius = scanRadius.get();
        int minDisturbance = disturbanceThreshold.get();

        var results = scanRegion(cart.getBlockPos(), radius, minDisturbance);
        if(results.stacks.isEmpty()) return;
        if(results.minimumYDistance() > maximumYDistance.get()) return;
        espResults(results);

        if (doChatNotif.get()) {
            MutableText msg = Text.literal("Minecart with disturbance");
            if(includeCoords.get()) {
                msg = msg.append(" at ").append(TextUtils.coords(results.pos.toCenterPos()));
            }
            msg = msg.append(TextUtils.circleCommandLink(results.pos.toCenterPos()));
            ChatUtils.sendMsg("DisplacedStack",  msg);
        }
    }

    public void espResults(ScanResult res) {
        for (var stack : res.stacks) {
            for (BlockPos p : stack) {
                esp.BlockWithMapColor(p);
            }
        }

        if (espSource.get()) {
            var opts = new EspOptions();
            opts.tracer = new MockSetting<>(tracer.get());
            opts.tracerColor = new MockSetting<>(tracerColor.get());
            opts.lineColor = new MockSetting<>(lineColor.get());
            opts.sideColor = new MockSetting<>(boxColor.get());

            esp.Block(res.pos, opts);
        }
    }


    public void scanChunk(Chunk chunk) {
        int radius = scanRadius.get();
        int minDisturbance = disturbanceThreshold.get();

        var spawners = ChunkUtils.positionsOf(chunk, Blocks.SPAWNER);
        spawners.forEach((pos) -> {
            if(scanCache.containsKey(pos)) return;
            if (!isInterestingSpawner(pos)) return;

            var results = scanRegion(pos, radius, minDisturbance);
            if(results.stacks.isEmpty()) return;

            if(requireActivatedSpawner.get()) {
                var entity = mc.world.getBlockEntity(pos, BlockEntityType.MOB_SPAWNER);
                if (entity.isEmpty()) return;

                if(entity.get() instanceof MobSpawnerBlockEntity spawner) {
                    if (spawner.getLogic().spawnDelay == 20) return;
                } else {
                    // Not a mob spawner?
                    return;
                }
            }

            if(results.minimumYDistance() > maximumYDistance.get()) return;

            espResults(results);
            if (doChatNotif.get()) {
                MutableText msg = Text.literal("Dungeon with disturbance");
                if(includeCoords.get()) {
                    msg = msg.append(" at ").append(ChatUtils.formatCoords(results.pos.toCenterPos()));
                }
                ChatUtils.sendMsg("DisplacedStack",  msg);
            }
        });
    }

    /**
     * Scans the region around a given position for any blocks that appear displaced
     *
     * @param pos center point of the scan
     * @param radius radius around the position to center
     * @param minDisturbance how many displaced blocks are needed before reporting
     */
    public ScanResult scanRegion(BlockPos pos, int radius, int minDisturbance) {
        ScanResult res = new ScanResult(pos);
        if (scanCache.containsKey(pos)) return scanCache.get(pos);


        BlockPos startPos = new BlockPos(pos.getX() - radius, pos.getY(), pos.getZ() - radius);
        BlockPos endPos = new BlockPos(pos.getX() + radius, pos.getY(), pos.getZ() + radius);

        for (BlockPos p : BlockPos.iterate(startPos, endPos)) {
            var stack = getDistinctStack(p);
            if (stack.size() < minDisturbance) continue;
            res.add(stack);
        }

        scanCache.put(pos, res);
        return res;
    }

    /**
     * An interesting spawner is basically just a dungeon spawner with a chest nearby.
     *
     * @param pos
     */
    public boolean isInterestingSpawner(BlockPos pos) {
        int radius = 4;

        BlockPos startPos = new BlockPos(pos.getX() - radius, pos.getY()-1, pos.getZ() - radius);
        BlockPos endPos = new BlockPos(pos.getX() + radius, pos.getY(), pos.getZ() + radius);

        boolean haveMossy = false;
        boolean haveChest = false;
        boolean haveCobble = false;

        for (BlockPos p : BlockPos.iterate(startPos, endPos)) {
            var block = mc.world.getBlockState(p).getBlock();
            if (block.equals(Blocks.MOSSY_COBBLESTONE)) haveMossy = true;
            if (block.equals(Blocks.CHEST)) haveChest = true;
            if (block.equals(Blocks.COBBLESTONE)) haveCobble = true;
        }

        return haveMossy && haveChest && haveCobble;
    }

    /**
     * Given a source block and position to check determine if the block at that position is distinct from the source.
     * The targeted block must be different AND solid to be considered distinct.
     * I had the idea for this sort of scan in the past, but wanted to focus more on detecting stone/native stacks by
     * looking for columns the intersect other generated block types. I still plan to do that implementation, but I saw
     * this implementation in [StashWalker](https://github.com/LukeStashWalker/stashwalker) and thought it was a good
     * performance compromise. Implemented it in my own scanner as that mod doesn't support my MC version.
     *
     * @param source
     * @param pos
     * @return
     */
    private boolean blocksAreDistinct(Block source, BlockPos pos) {
        var state = mc.world.getBlockState(pos);
        return !state.getBlock().equals(source) && state.isSolidBlock(mc.world, pos);
    }

    /**
     * Given a specific position scan the blocks above it for any apparent displacements. A displacement just means the
     * block is different from all surrounding blocks indicating a non-native placement. Though this can have some false
     * positives especially around blend areas a human can easily tell those apart.
     *
     * @param bottom the bottom block position to start scanning from
     */
    public List<BlockPos> getDistinctStack(BlockPos bottom) {
        BlockPos top = new BlockPos(bottom.getX(), mc.world.getTopY(Heightmap.Type.MOTION_BLOCKING, bottom.getX(), bottom.getZ()), bottom.getZ());
        List<BlockPos> stack = new ArrayList<>();

        for (BlockPos pos : BlockPos.iterate(bottom, top)) {
            var source = mc.world.getBlockState(pos).getBlock();

            boolean isDistinct = blocksAreDistinct(source, pos.north());
            isDistinct &= blocksAreDistinct(source, pos.south());
            isDistinct &= blocksAreDistinct(source, pos.east());
            isDistinct &= blocksAreDistinct(source, pos.west());
            isDistinct &= blocksAreDistinct(source, pos.north().east());
            isDistinct &= blocksAreDistinct(source, pos.north().west());
            isDistinct &= blocksAreDistinct(source, pos.south().east());
            isDistinct &= blocksAreDistinct(source, pos.south().west());

            if (isDistinct) {
                stack.add(new BlockPos(pos));
            }
        }

        return stack;
    }

    @Override
    public void onActivate() {
        scanCache.clear();

        for(var c : Utils.chunks(true)) {
            onChunkData(new ChunkDataEvent((WorldChunk) c));
        }

        for(var e : mc.world.getEntities()) {
            EntityAddedEvent ev = new EntityAddedEvent();
            ev.entity = e;
            onEntityAdded(ev);
        }
    }

    public void onDeactivate() {
        esp.clear();
        scanCache.clear();
        seenChunks.clear();
    }

    public class ScanResult {
        public final BlockPos pos;
        public final List<List<BlockPos>> stacks;

        public ScanResult(BlockPos pos) {
            this.pos = pos;
            this.stacks = new ArrayList<>();
        }

        public void add(List<BlockPos> stack) {
            stacks.add(stack);
        }

        public int minimumYDistance() {
            int min = Integer.MAX_VALUE;
            for (List<BlockPos> stack : stacks) {
                if(stack.isEmpty()) continue;
                var firstBLock = stack.getFirst();
                var dist = Math.abs(pos.getY() - firstBLock.getY() - pos.getY());
                if (dist < min) {
                    min = dist;
                }
            }
            return min;

        }
    }

}
